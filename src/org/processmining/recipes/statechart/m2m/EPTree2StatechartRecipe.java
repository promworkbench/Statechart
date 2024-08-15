package org.processmining.recipes.statechart.m2m;

import org.processmining.algorithms.statechart.m2m.EPTree2StatechartStates;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.recipes.statechart.AbstractRecipe;

public class EPTree2StatechartRecipe extends
        AbstractRecipe<IEPTree, Statechart, Void> {

    public EPTree2StatechartRecipe() {
        super(null);
    }

    @Override
    protected Statechart execute(IEPTree input) {
        EPTree2StatechartStates m2m = new EPTree2StatechartStates();
        return m2m.transform(input);
    }

}
