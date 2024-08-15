package org.processmining.recipes.statechart.l2l;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.algorithms.statechart.l2l.L2LStructuredActivity;
import org.processmining.algorithms.statechart.l2l.subtrace.L2LSubtraceStructuredActivity;

public class L2LStructuredActivityRecipe extends AbstractL2LRecipe<L2LStructuredActivity.Parameters> {

    public L2LStructuredActivityRecipe() {
        super(new L2LStructuredActivity.Parameters());
    }

    @Override
    protected XLog execute(XLog input) {
        L2LStructuredActivity transform = new L2LSubtraceStructuredActivity(getParameters());
        return transform.apply(input);
    }

    @Override
    public XTrace apply(XTrace input) {
        L2LStructuredActivity transform = new L2LSubtraceStructuredActivity(getParameters());
        return transform.apply(input);
    }
}
