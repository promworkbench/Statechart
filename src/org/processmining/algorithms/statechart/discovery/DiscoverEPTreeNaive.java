package org.processmining.algorithms.statechart.discovery;

public class DiscoverEPTreeNaive extends
        AbstractDiscoverEPTree<DiscoverEPTreeNaive.Parameters> {

    public static class Parameters extends AbstractDiscoverEPTree.Parameters {

    }

    public DiscoverEPTreeNaive() {
        this(new Parameters());
    }

    public DiscoverEPTreeNaive(Parameters parameters) {
        super(parameters);
    }

}
