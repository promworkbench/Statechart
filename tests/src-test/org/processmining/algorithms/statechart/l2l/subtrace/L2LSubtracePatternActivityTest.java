package org.processmining.algorithms.statechart.l2l.subtrace;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.xes.statechart.XesCompareSame;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

public class L2LSubtracePatternActivityTest {

    protected static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();

    @Test
    public void testPlusNormal1() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A+B"} 
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete"} 
        });
        XTrace trace = expected.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete"
        });
        extSubtrace.assignSubtrace(trace.get(0), sub1);
        
        L2LSubtracePatternActivity transform = new L2LSubtracePatternActivity(new L2LSubtracePatternActivity.Parameters());
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPlusNormal2() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A+B+C"} 
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete"} 
        });
        XTrace trace = expected.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B+C_start", "B+C_complete"
        });
        extSubtrace.assignSubtrace(trace.get(0), sub1);
        
        L2LSubtracePatternActivity transform = new L2LSubtracePatternActivity(new L2LSubtracePatternActivity.Parameters());
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
}
