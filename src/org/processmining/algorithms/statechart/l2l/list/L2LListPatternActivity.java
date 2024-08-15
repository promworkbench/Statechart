package org.processmining.algorithms.statechart.l2l.list;

import org.processmining.algorithms.statechart.l2l.L2LPatternActivity;

public class L2LListPatternActivity extends L2LPatternActivity {

    public L2LListPatternActivity(Parameters params) {
        super(params, new L2LListSingleEventDriver());
    }
}
