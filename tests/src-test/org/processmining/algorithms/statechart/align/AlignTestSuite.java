package org.processmining.algorithms.statechart.align;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.processmining.algorithms.statechart.align.metric.AlignMetricTestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    AlignLog2TreeTest.class,
    ExecIntervalTest.class,
    AlignTreeEventIntervalsTest.class,
    LSubtrace2LAlignTest.class,
    AlignMetricTestSuite.class
})
public class AlignTestSuite {

}
