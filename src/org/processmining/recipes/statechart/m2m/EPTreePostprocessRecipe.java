package org.processmining.recipes.statechart.m2m;

import org.processmining.algorithms.statechart.m2m.EPTreeFilter;
import org.processmining.algorithms.statechart.m2m.reduct.eptree.ReductionEngineDefault;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.recipes.statechart.AbstractRecipe;

public class EPTreePostprocessRecipe extends
        AbstractRecipe<IEPTree, IEPTree, EPTreePostprocessRecipe.Parameters> {

    public static class Parameters {
        public final EPTreeFilter.Parameters filter;

        // apply tree reduction rules
        public boolean reduce;

        public Parameters() {
            filter = new EPTreeFilter.Parameters();
            filter.setDepthFilter(0.0, 0.1);
            reduce = true;
        }
    }

    public EPTreePostprocessRecipe() {
        super(new Parameters());
    }

    @Override
    protected IEPTree execute(IEPTree input) {
        Parameters params = getParameters();
        IEPTree output = input.createCopy();

        EPTreeFilter filter = new EPTreeFilter(params.filter);
        filter.apply(output);

        if (params.reduce) {
            ReductionEngineDefault reduct = new ReductionEngineDefault();
            reduct.reduce(output);
        }

        return output;
    }

}
