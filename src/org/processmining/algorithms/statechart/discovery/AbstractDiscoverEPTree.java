package org.processmining.algorithms.statechart.discovery;

import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.discovery.im.MiningParametersSC;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.algorithms.statechart.m2m.ProcessTree2EPTree;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.models.statechart.im.log.IMLogHierarchyListImpl;
import org.processmining.models.statechart.im.log.IMLogHierarchySubtraceImpl;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.ProcessTreeImpl;
import org.processmining.xes.statechart.extension.XListLabelExtension;

import com.google.common.base.Preconditions;

public abstract class AbstractDiscoverEPTree<P extends AbstractDiscoverEPTree.Parameters> {

    protected static final Canceller mockupCanceller = new Canceller() {
        public boolean isCancelled() {
            return false;
        }
    };

    public static class Parameters {
        public double pathThreshold; // percentage of paths to keep (0.0 - 1.0)
                                     // (inverse of noise threshold)

        public boolean useCancelation;
        public IQueryCancelError queryCatchError;
        
        public Parameters() {
            pathThreshold = 0.8;
            useCancelation = false;
            queryCatchError = null;
        }
    }

    protected final P params;

    public AbstractDiscoverEPTree(P params) {
        Preconditions.checkNotNull(params, "No parameters set");
        this.params = params;
    }

    public IEPTree discover(XLog log) {
        IMLogHierarchy reclsLog;
        if (XListLabelExtension.isListLabelLog(log)) {
            reclsLog = new IMLogHierarchyListImpl(log, 0);
        } else {
            reclsLog = new IMLogHierarchySubtraceImpl(log);
        }
        return _discover(reclsLog);
    }

    public IEPTree discover(IMLog log) {
        if (log instanceof IMLogHierarchy) {
            return _discover((IMLogHierarchy) log);
        } else {
            // TODO use efficient inner datastructure copy for
            // IMLogImplReclassAbstract?

            return _discover(new IMLogHierarchyListImpl(log.toXLog(), 0));
        }
    }

    protected IEPTree _discover(IMLogHierarchy log) {
        MiningParametersSC imParams = _createMiningParameters(log instanceof IMLogHierarchySubtraceImpl);

        ProcessTree processTree = mine(log, imParams, mockupCanceller);

        ProcessTree2EPTree m2m = new ProcessTree2EPTree();
        IEPTree tree = m2m.transform(processTree);

        return tree;
    }

    protected MiningParametersSC _createMiningParameters(boolean useLifecycle) {
        MiningParametersSC imParams = new MiningParametersSC(
            useLifecycle, params.useCancelation, params.queryCatchError);
        imParams.setNoiseThreshold((float) (1.0 - params.pathThreshold));
        return imParams;
    }

    protected ProcessTree mine(IMLog log, MiningParametersSC parameters,
            Canceller canceller) {
        // repair life cycle if necessary
//        if (parameters.isRepairLifeCycle()) {
//            log = LifeCycles.preProcessLog(log);
//        }

        // create process tree
        ProcessTree tree = new ProcessTreeImpl();
        MinerState minerState = _createMinerState(parameters, canceller);
        Node root = _mineTree(log, tree, minerState);

        root.setProcessTree(tree);
        tree.setRoot(root);

        if (canceller.isCancelled()) {
            return null;
        }

        Miner.debug("discovered tree " + tree.getRoot(), minerState);

        // reduce the tree
        // tree = ReduceTree.reduceTree(tree);
        // Miner.debug("after reduction " + tree.getRoot(), minerState);

        minerState.shutdownThreadPools();

        if (canceller.isCancelled()) {
            return null;
        }

        return tree;
    }

    protected MinerState _createMinerState(MiningParametersSC parameters,
            Canceller canceller) {
        return new MinerState(parameters, canceller);
    }

    protected Node _mineTree(IMLog log, ProcessTree tree, MinerState minerState) {
        return Miner.mineNode(log, tree, minerState);
    }

}
