package org.processmining.recipes.statechart.discovery;

import org.deckfour.xes.model.XLog;
import org.processmining.models.statechart.im.log.IMLogHierarchySubtraceImpl;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.recipes.statechart.AbstractRecipe;

public class Log2IMLogRecipe extends AbstractRecipe<XLog, IMLog, Void>  {

    public Log2IMLogRecipe() {
        super(null);
    }

    @Override
    protected IMLog execute(XLog input) {
//        return new IMLogHierarchyListImpl(input, 0); // TODO use hierarchical log
        return new IMLogHierarchySubtraceImpl(input);
    }

}
