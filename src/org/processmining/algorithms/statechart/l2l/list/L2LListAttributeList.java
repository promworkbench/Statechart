package org.processmining.algorithms.statechart.l2l.list;

import org.processmining.algorithms.statechart.l2l.L2LAttributeList;

public class L2LListAttributeList extends L2LAttributeList {

    public L2LListAttributeList(Parameters params) {
        super(params, new L2LListSingleEventDriver());
    }

}
