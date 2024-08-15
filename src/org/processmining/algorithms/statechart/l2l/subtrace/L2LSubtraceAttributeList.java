package org.processmining.algorithms.statechart.l2l.subtrace;

import org.processmining.algorithms.statechart.l2l.L2LAttributeList;

public class L2LSubtraceAttributeList extends L2LAttributeList {

    public L2LSubtraceAttributeList(Parameters params) {
        super(params, new L2LSubtraceSingleEventDriver());
    }

}
