package org.processmining.recipes.statechart.m2m;

import org.processmining.algorithms.statechart.m2m.reduct.sc.ReductionEngineDefault;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.recipes.statechart.AbstractRecipe;

public class StatechartPostprocessRecipe extends
        AbstractRecipe<Statechart, Statechart, StatechartPostprocessRecipe.Parameters> {

    public static class Parameters {
        public boolean reduce;

        public Parameters() {
            reduce = true;
        }
    }

    public StatechartPostprocessRecipe() {
        super(new Parameters());
    }

    @Override
    protected Statechart execute(Statechart input) {
        Parameters params = getParameters();
        Statechart output = input.createCopy();

        if (params.reduce) {
            ReductionEngineDefault reduct = new ReductionEngineDefault();
            reduct.reduce(output);
        }

        return output;
    }
    
}
