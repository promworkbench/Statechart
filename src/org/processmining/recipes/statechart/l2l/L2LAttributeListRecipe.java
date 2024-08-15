package org.processmining.recipes.statechart.l2l;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.algorithms.statechart.l2l.L2LAttributeList;
import org.processmining.algorithms.statechart.l2l.subtrace.L2LSubtraceAttributeList;

public class L2LAttributeListRecipe extends AbstractL2LRecipe<L2LAttributeList.Parameters> {

    public L2LAttributeListRecipe() {
        super(new L2LAttributeList.Parameters());
    }

    @Override
    protected XLog execute(XLog input) {
        L2LAttributeList transform = new L2LSubtraceAttributeList(getParameters());
        return transform.apply(input);
    }

    @Override
    public XTrace apply(XTrace input) {
        L2LAttributeList transform = new L2LSubtraceAttributeList(getParameters());
        return  transform.apply(input);
    }
}
