package org.processmining.algorithms.statechart.l2l.subtrace;

import org.processmining.algorithms.statechart.l2l.L2LPatternActivity;

public class L2LSubtracePatternActivity extends L2LPatternActivity {

    public L2LSubtracePatternActivity(Parameters params) {
        super(params, new L2LSubtraceSingleEventDriver());
    }
}
