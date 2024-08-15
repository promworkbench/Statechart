package org.processmining.algorithms.statechart.m2m;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.eptree.EPNodeType;
import org.processmining.models.statechart.eptree.EPTreeNode;

import com.google.common.base.Preconditions;

public class EPTreeFilter {

    private static final Logger logger = LogManager
            .getLogger(EPTreeFilter.class.getName());

    public static class Parameters {

        // percentage lower bound of levels to hide (0.0 - depthMax)
        private double depthMin;

        // percentage upper bound of levels before collapsing (depthMin - 1.0)
        private double depthMax;

        // set of node ids to collapse (overrides depthMax)
        private final Set<String> invertedNodes;

        public Parameters() {
            depthMin = 0.0;
            depthMax = 1.0;
            invertedNodes = new HashSet<>();
        }

        public void setDepthFilter(double min, double max) {
            this.depthMin = min;
            this.depthMax = max;
            invertedNodes.clear();
        }

        public void invertNodeState(String nodeId) {
            if (invertedNodes.contains(nodeId)) {
                invertedNodes.remove(nodeId);
            } else {
                invertedNodes.add(nodeId);
            }
        }

        public double getDepthMin() {
            return depthMin;
        }

        public double getDepthMax() {
            return depthMax;
        }
    }

    private final Parameters params;

    public EPTreeFilter(Parameters params) {
        Preconditions.checkNotNull(params, "No parameters set");
        this.params = params;
    }

    public void apply(IEPTree input) {
        // calculate depth absolute cutoffs
        int numLevels = _calculateNumLevels(input);
        int minDepth = (int) Math.round(params.depthMin * numLevels);
        int maxDepth = (int) Math.round(params.depthMax * numLevels);

        if (logger.isDebugEnabled()) {
            logger.debug(String
                    .format("# Levels: %d ; minDepth: %d (@ %.1f) ; maxDepth: %d (@ %.1f)",
                            numLevels, minDepth, params.depthMin, maxDepth,
                            params.depthMax));
        }

        // Traverse tree, and apply filter / collapse settings
        input.setRoot(_applyLogic(input.getRoot(), 0, null, minDepth, maxDepth));
    }

    private IEPTreeNode _applyLogic(IEPTreeNode current, int depth,
            IEPTreeNode parent, int minDepth, int maxDepth) {
        if (current.getNodeType() == EPNodeType.OrComposite) {
            depth++;
        }

        List<IEPTreeNode> children = current.getChildren();
        final int numChildren = children.size();

        if (numChildren > 0) {
            if (current.getNodeType() == EPNodeType.OrComposite) {
                if (numChildren != 1) {
                    throw new TransformationException(
                            "EPTree OrComposite doesn't have 1 child");
                }

                boolean nodeInverted = params.invertedNodes.contains(current
                        .getId());
                if (logger.isDebugEnabled()) {
                    logger.debug(String
                            .format("\tEval collapse OR \"%s\" - depth: %d ; nodeState inverted: %b ; minDepth: %d ; maxDepth: %d",
                                    current.getLabel(), depth, nodeInverted,
                                    minDepth, maxDepth));
                }

                // hide composite or if below minimum, by returning its contents
                // as new node
                if (depth <= minDepth) {
                    IEPTreeNode newChild = _applyLogic(children.get(0), depth,
                            current, minDepth, maxDepth);
                    newChild.setParent(parent);
                    return newChild;
                }

                // collapse node
                if ((depth > maxDepth && !nodeInverted)
                        || (depth <= maxDepth && nodeInverted)) {
                    return new EPTreeNode(current.getTree(), parent, EPNodeType.Collapsed,
                            current.getLabel(), current.getId());
                }
            }

            // normal case, revisit children
            for (int i = 0; i < numChildren; i++) {
                IEPTreeNode newChild = _applyLogic(children.get(i), depth,
                        current, minDepth, maxDepth);
                children.set(i, newChild);
            }
            
        } else {
            if (current.getNodeType().isLeafType()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String
                            .format("\tEval tau leaf \"%s\" - depth: %d ; minDepth: %d",
                                    current.getLabel(), depth, minDepth));
                }

                // rename to tau if below minimum, by returning its contents
                // as new node
                if (depth < minDepth) {
                    return new EPTreeNode(parent.getTree(), parent, EPNodeType.Silent, "",
                            current.getLabel());
                }
            }
        }

        return current;
    }

    private int _calculateNumLevels(IEPTree input) {
        return _calculateNumLevels(input.getRoot(), 0);
    }

    private int _calculateNumLevels(IEPTreeNode current, int depth) {
        if (current.getNodeType() == EPNodeType.OrComposite) {
            depth++;
        }
        int maxDepth = depth;

        final List<IEPTreeNode> children = current.getChildren();
        final int size = children.size();
        for (int i = 0; i < size; i++) {
            maxDepth = Math.max(maxDepth,
                    _calculateNumLevels(children.get(i), depth));
        }

        return maxDepth;
    }
}
