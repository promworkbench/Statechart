package org.processmining.recipes.statechart.discovery;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.processmining.algorithms.statechart.discovery.AbstractDiscoverEPTree;
import org.processmining.algorithms.statechart.discovery.DiscoverEPTreeNaive;
import org.processmining.algorithms.statechart.discovery.DiscoverEPTreeRecursion;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.algorithms.statechart.discovery.im.cancellation.SetQueryCancelError;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.recipes.statechart.AbstractRecipe;

public class DiscoverEPTreeRecipe extends
        AbstractRecipe<IMLog, IEPTree, DiscoverEPTreeRecipe.Parameters> {

    public static enum DiscoveryAlgorithm {
        Naive("Naive", "Default Discovery algorithm, but does not fold recursive structures."), 
        Recursion("Recursion Aware", "Advanced Discovery algorithm that recognizes and folds recursive structures.");

        private String name, descriptionShort;

        private DiscoveryAlgorithm(String name, String descriptionShort) {
            this.name = name;
            this.descriptionShort = descriptionShort;
        }

        public String getName() {
            return name;
        }
        
        public String getShortDescription() {
            return descriptionShort;
        }
    }

    public static class Parameters {
        private DiscoveryAlgorithm algorithm;

        private double pathThreshold; // percentage of paths to keep (0.0 - 1.0)
                                     // (inverse of noise threshold)

        private boolean useCancelation;
        private IQueryCancelError queryCatchError;
        private Set<String> errorClasses;
        
        public Parameters() {
            algorithm = DiscoveryAlgorithm.Naive;
            pathThreshold = 0.8;
            useCancelation = false;
            errorClasses = new THashSet<>();
            queryCatchError = new SetQueryCancelError(errorClasses);
        }
        
        public DiscoveryAlgorithm getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(DiscoveryAlgorithm algorithm) {
            this.algorithm = algorithm;
        }

        public double getPathThreshold() {
            return pathThreshold;
        }

        public void setPathThreshold(double threshold) {
            this.pathThreshold = threshold;
        }

        public boolean getUseCancelation() {
            return useCancelation;
        }

        public void setUseCancelation(boolean input) {
            this.useCancelation = input;
        }

        public IQueryCancelError getQueryCatchError() {
            return queryCatchError;
        }

        public void setQueryCatchError(IQueryCancelError input) {
            this.queryCatchError = input;
            useCancelation = (input != null);
        }

        public Set<String> getErrorClasses() {
            return errorClasses;
        }

        public void setErrorClasses(Set<String> input) {
            this.errorClasses = input;
            useCancelation = (input != null && !input.isEmpty());
            if (useCancelation) {
                queryCatchError = new SetQueryCancelError(errorClasses);
            }
        }

        protected DiscoverEPTreeNaive.Parameters getParamsNaive() {
            DiscoverEPTreeNaive.Parameters params = new DiscoverEPTreeNaive.Parameters();
            _setAbstractProperties(params);
            return params;
        }

        protected DiscoverEPTreeRecursion.Parameters getParamsRecursion() {
            DiscoverEPTreeRecursion.Parameters params = new DiscoverEPTreeRecursion.Parameters();
            _setAbstractProperties(params);
            return params;
        }

        private void _setAbstractProperties(AbstractDiscoverEPTree.Parameters params) {
            params.pathThreshold = this.pathThreshold;
            params.useCancelation = this.useCancelation;
            params.queryCatchError = this.queryCatchError;
        }
    }

    public DiscoverEPTreeRecipe() {
        super(new Parameters());
    }

    @Override
    protected IEPTree execute(IMLog input) {
        AbstractDiscoverEPTree<?> disc = null;
        Parameters params = getParameters();
        switch (params.getAlgorithm()) {
        case Naive:
            disc = new DiscoverEPTreeNaive(params.getParamsNaive());
            break;
        case Recursion:
            disc = new DiscoverEPTreeRecursion(params.getParamsRecursion());
            break;
        }

        if (disc == null) {
            throw new IllegalStateException(
                    "DiscoverEPTreeNaiveRecipe - disc algorithm unkown");
        }

        return disc.discover(input);
    }
}
