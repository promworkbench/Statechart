package org.processmining.recipes.statechart.metrics;

import org.processmining.models.statechart.decorate.staticmetric.SCComplexityMetric;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.recipes.statechart.AbstractRecipe;

public class SCMetricRecipe extends
        AbstractRecipe<Statechart, SCComplexityMetric, Void> {

    public SCMetricRecipe() {
        super(null);
    }

    @Override
    protected SCComplexityMetric execute(Statechart input) {
        return new SCComplexityMetric(input);
    }

}
