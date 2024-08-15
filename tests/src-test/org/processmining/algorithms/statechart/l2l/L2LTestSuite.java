package org.processmining.algorithms.statechart.l2l;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.processmining.algorithms.statechart.l2l.list.L2LListAttributeListTest;
import org.processmining.algorithms.statechart.l2l.list.L2LListNestedCallsTest;
import org.processmining.algorithms.statechart.l2l.list.L2LListStructuredActivityTest;
import org.processmining.algorithms.statechart.l2l.subtrace.L2LSubtraceAttributeListTest;
import org.processmining.algorithms.statechart.l2l.subtrace.L2LSubtraceNestedCallsTest;
import org.processmining.algorithms.statechart.l2l.subtrace.L2LSubtracePatternActivityTest;
import org.processmining.algorithms.statechart.l2l.subtrace.L2LSubtraceStructuredActivityTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    L2LSplitCallsTest.class,
    L2LListAttributeListTest.class,
    L2LListNestedCallsTest.class,
    L2LListStructuredActivityTest.class,
    L2LSubtraceAttributeListTest.class,
    L2LSubtraceNestedCallsTest.class,
    L2LSubtraceStructuredActivityTest.class,
    L2LSubtracePatternActivityTest.class
})
public class L2LTestSuite {

}
