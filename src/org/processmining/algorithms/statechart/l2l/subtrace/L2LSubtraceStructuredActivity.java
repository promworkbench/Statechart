package org.processmining.algorithms.statechart.l2l.subtrace;

import org.processmining.algorithms.statechart.l2l.L2LStructuredActivity;

public class L2LSubtraceStructuredActivity extends L2LStructuredActivity {

    public L2LSubtraceStructuredActivity(Parameters params) {
        super(params, new L2LSubtraceSingleEventDriver());
    }
}
