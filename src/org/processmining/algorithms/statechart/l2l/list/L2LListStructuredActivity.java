package org.processmining.algorithms.statechart.l2l.list;

import org.processmining.algorithms.statechart.l2l.L2LStructuredActivity;

public class L2LListStructuredActivity extends L2LStructuredActivity {

    public L2LListStructuredActivity(Parameters params) {
        super(params, new L2LListSingleEventDriver());
    }
}
