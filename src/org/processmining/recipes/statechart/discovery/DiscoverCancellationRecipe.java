package org.processmining.recipes.statechart.discovery;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.discovery.DiscoverEPTreeNaiveCancellation;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.algorithms.statechart.discovery.im.cancellation.PredicateQueryCancelError;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.recipes.statechart.AbstractRecipe;

import com.google.common.base.Predicate;

public class DiscoverCancellationRecipe extends
    AbstractRecipe<XLog, IEPTree, DiscoverCancellationRecipe.Parameters> {

    public static class Parameters {

        private double pathThreshold; // percentage of paths to keep (0.0 - 1.0)
                                     // (inverse of noise threshold)

        private Set<String> errorClasses;
        
        public IQueryCancelError queryCatchError;
        
        public Parameters() {
            pathThreshold = 0.8;
            errorClasses = new THashSet<>();
            queryCatchError = new PredicateQueryCancelError(new Predicate<String>() {
                @Override
                public boolean apply(String value) {
                    return errorClasses.contains(value);
                }
            });
        }

        public double getPathThreshold() {
            return pathThreshold;
        }

        public void setPathThreshold(double threshold) {
            this.pathThreshold = threshold;
        }

        public Set<String> getErrorClasses() {
            return errorClasses;
        }

        public void setErrorClasses(Set<String> input) {
            this.errorClasses = input;
        }

    }
    
    public DiscoverCancellationRecipe() {
        super(new Parameters());
    }
    
    @Override
    protected IEPTree execute(XLog input) { // Discover
        final Parameters params = getParameters();
        
        DiscoverEPTreeNaiveCancellation.Parameters paramsNormal = new DiscoverEPTreeNaiveCancellation.Parameters();
        paramsNormal.queryCatchError = params.queryCatchError;
        paramsNormal.pathThreshold = params.pathThreshold;
        DiscoverEPTreeNaiveCancellation disc = new DiscoverEPTreeNaiveCancellation(
                paramsNormal);
        return disc.discover(input);
    }

}
