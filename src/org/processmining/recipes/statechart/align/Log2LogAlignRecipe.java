package org.processmining.recipes.statechart.align;

import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.align.LSubtrace2LAlign;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.recipes.statechart.AbstractRecipe;

public class Log2LogAlignRecipe extends AbstractRecipe<IMLog, XLog, Void> {

    public Log2LogAlignRecipe() {
        super(null);
    }

    @Override
    protected XLog execute(IMLog input) {
        LSubtrace2LAlign transform = new LSubtrace2LAlign();
        return transform.transform(input);
    }

}
