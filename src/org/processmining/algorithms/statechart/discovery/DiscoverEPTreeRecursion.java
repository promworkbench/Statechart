package org.processmining.algorithms.statechart.discovery;

import gnu.trove.map.hash.THashMap;

import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.processmining.algorithms.statechart.discovery.im.MiningParametersSC;
import org.processmining.algorithms.statechart.discovery.im.MiningParametersSCRecursion;
import org.processmining.algorithms.statechart.discovery.im.postprocessor.MetricsDecoratorPostprocessor;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.utils.statechart.processtree.ProcessTreeUtils;

public class DiscoverEPTreeRecursion extends
        AbstractDiscoverEPTree<DiscoverEPTreeRecursion.Parameters> {

    public static class Parameters extends AbstractDiscoverEPTree.Parameters {

    }

    private static final Logger logger = LogManager
            .getLogger(DiscoverEPTreeRecursion.class.getName());

    public DiscoverEPTreeRecursion() {
        this(new Parameters());
    }

    public DiscoverEPTreeRecursion(Parameters parameters) {
        super(parameters);
    }

    @Override
    protected MiningParametersSC _createMiningParameters(boolean useLifecycle) {
        MiningParametersSCRecursion imParams = new MiningParametersSCRecursion(
                useLifecycle, params.useCancelation, params.queryCatchError);
        imParams.setNoiseThreshold((float) (1.0 - params.pathThreshold));
        return imParams;
    }

    @Override
    protected Node _mineTree(IMLog log, ProcessTree tree, MinerState minerState) {
        // Start of with a normal discovery
        Node root = Miner.mineNode(log, tree, minerState);

        Map<ContextPath, Node> contextSubtrees = new THashMap<>();

        // Then investigate the discovery horizon for recursion mining
        MiningParametersSCRecursion params = (MiningParametersSCRecursion) minerState.parameters;
        {
            ContextPath contextPath = params.popContextFromHorizon();
            while (contextPath != null) {
                // Discover submodel
                IMLogHierarchy sublog = params.getContextSublog(contextPath);

                if (sublog != null) {
                    params.setCurrentContext(contextPath);
                    Node oldChild = contextSubtrees.get(contextPath);
                    if (oldChild != null) {
                        ProcessTreeUtils.cleanupNode(tree, oldChild);
                    }

                    Node child = Miner.mineNode(sublog, tree, minerState);
                    contextSubtrees.put(contextPath, child);
                } else {
                    logger.error("Empty sublog for context " + contextPath);
                }

                // check for next context to mine
                contextPath = params.popContextFromHorizon();
            }
        }

        // Map from node to subtree for that node
        Map<Node, Node> targetSubtree = new THashMap<>();
        Map<ContextPath, List<Block>> targetsMap = params.getAllTargets();
        for (ContextPath contextPath : targetsMap.keySet()) {
            for (Block target : targetsMap.get(contextPath)) {
                targetSubtree.put(target, contextSubtrees.get(contextPath));
            }
        }
        
        // Find metrics decorator instance
        MetricsDecoratorPostprocessor metricDecorator = null;
        for (PostProcessor pp : minerState.parameters.getPostProcessors()) {
            if (pp instanceof MetricsDecoratorPostprocessor) {
                metricDecorator = (MetricsDecoratorPostprocessor) pp;
            }
        }

        // Add subtree to all the right nodes
        _populateTree(tree, root, targetSubtree, new THashMap<Node, Node>(), metricDecorator);

        // Cleanup templates
        for (Node subtreeTemplate : targetSubtree.values()) {
            ProcessTreeUtils.cleanupNode(tree, subtreeTemplate);
        }

        return root;
    }

    private void _populateTree(ProcessTree tree, Node current,
            Map<Node, Node> targetSubtree, Map<Node, Node> mapNewToOld,
            MetricsDecoratorPostprocessor metricDecorator) {

        // find corresponding subtree template
        Node refNode = mapNewToOld.get(current);
        if (refNode == null) {
            refNode = current;
        }
        Node subtree = targetSubtree.get(refNode);

        // if node can accept subtree, and has an associated subtree to use
        if (current instanceof Block) {
            Block block = (Block) current;
            if (subtree != null) {
                // node should be empty
                if (!block.getChildren().isEmpty()) {
                    throw new IllegalStateException("Should be empty: " + block);
                }
                // add clone of subtree template
                block.addChild(ProcessTreeUtils.cloneNode(tree, subtree,
                        mapNewToOld));
            }
            
            // visit (possibly new) subtree of node to populate
            for (Node child : block.getChildren()) {
                _populateTree(tree, child, targetSubtree, mapNewToOld, metricDecorator);
            }
            
            // Revisit metrics decorations
            if (metricDecorator != null && subtree != null) {
                //metricDecorator.revisitBlock(block);
            }
        }
    }
}
