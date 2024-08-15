package org.processmining.algorithms.statechart.discovery;

import org.processmining.algorithms.statechart.discovery.im.MiningParametersSC;
import org.processmining.algorithms.statechart.discovery.im.MiningParametersSCCancellation;

public class DiscoverEPTreeNaiveCancellation extends
        AbstractDiscoverEPTree<DiscoverEPTreeNaiveCancellation.Parameters> {

    public static class Parameters extends AbstractDiscoverEPTree.Parameters {
//        public IQueryCancelError queryCatchError;
    }

    public DiscoverEPTreeNaiveCancellation() {
        this(new Parameters());
    }

    public DiscoverEPTreeNaiveCancellation(Parameters parameters) {
        super(parameters);
    }

    @Override
    protected MiningParametersSC _createMiningParameters(boolean useLifecycle) {
        MiningParametersSCCancellation imParams = new MiningParametersSCCancellation(useLifecycle, params.queryCatchError);
        imParams.setNoiseThreshold((float) (1.0 - params.pathThreshold));
        return imParams;
    }
}
