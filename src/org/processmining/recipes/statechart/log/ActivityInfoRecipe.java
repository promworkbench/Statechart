package org.processmining.recipes.statechart.log;

import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.log.cancel.Log2HierarchyActivityInfo;
import org.processmining.models.statechart.log.HierarchyActivityInfo;
import org.processmining.recipes.statechart.AbstractRecipe;

public class ActivityInfoRecipe extends
    AbstractRecipe<XLog, HierarchyActivityInfo, Void> {

    public ActivityInfoRecipe() {
        super(null);
    }

    @Override
    protected HierarchyActivityInfo execute(XLog input) {
        Log2HierarchyActivityInfo alg = new Log2HierarchyActivityInfo();
        return alg.apply(input);
    }

}
