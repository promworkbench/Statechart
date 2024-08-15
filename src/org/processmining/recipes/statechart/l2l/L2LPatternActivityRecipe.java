package org.processmining.recipes.statechart.l2l;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.algorithms.statechart.l2l.L2LPatternActivity;
import org.processmining.algorithms.statechart.l2l.subtrace.L2LSubtracePatternActivity;

public class L2LPatternActivityRecipe extends AbstractL2LRecipe<L2LPatternActivity.Parameters> {

    public L2LPatternActivityRecipe() {
        super(new L2LPatternActivity.Parameters());
    }

    @Override
    protected XLog execute(XLog input) {
        L2LPatternActivity transform = new L2LSubtracePatternActivity(getParameters());
        return transform.apply(input);
    }

    @Override
    public XTrace apply(XTrace input) {
        L2LPatternActivity transform = new L2LSubtracePatternActivity(getParameters());
        return transform.apply(input);
    }
}
