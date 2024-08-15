package org.processmining.algorithms.statechart.l2l.subtrace;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.l2l.L2LStructuredActivity;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.xes.statechart.XesCompareSame;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

public class L2LSubtraceStructuredActivityTest {

    protected static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();

    @Test
    public void testPackageNormal1() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
                {"org.apache.maven.plugin.descriptor.PluginDescriptor.setName"} 
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"org_start", "org_complete"} 
        });
        XTrace trace = expected.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "apache_start", "apache_complete"
        });
        extSubtrace.assignSubtrace(trace.get(0), sub1);
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "maven_start", "maven_complete"
        });
        extSubtrace.assignSubtrace(sub1.get(0), sub2);
        XTrace sub3 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "plugin_start", "plugin_complete"
        });
        extSubtrace.assignSubtrace(sub2.get(0), sub3);
        XTrace sub4 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "descriptor_start", "descriptor_complete"
        });
        extSubtrace.assignSubtrace(sub3.get(0), sub4);
        XTrace sub5 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "PluginDescriptor_start", "PluginDescriptor_complete"
        });
        extSubtrace.assignSubtrace(sub4.get(0), sub5);
        XTrace sub6 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "setName_start", "setName_complete"
        });
        extSubtrace.assignSubtrace(sub5.get(0), sub6);
        
        L2LSubtraceStructuredActivity transform = new L2LSubtraceStructuredActivity(new L2LStructuredActivity.Parameters());
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPackageMethodNormal1() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"org.apache.maven.plugin.descriptor.PluginDescriptor.setName(java.lang.String)"} 
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"org_start", "org_complete"} 
        });
        XTrace trace = expected.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "apache_start", "apache_complete"
        });
        extSubtrace.assignSubtrace(trace.get(0), sub1);
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "maven_start", "maven_complete"
        });
        extSubtrace.assignSubtrace(sub1.get(0), sub2);
        XTrace sub3 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "plugin_start", "plugin_complete"
        });
        extSubtrace.assignSubtrace(sub2.get(0), sub3);
        XTrace sub4 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "descriptor_start", "descriptor_complete"
        });
        extSubtrace.assignSubtrace(sub3.get(0), sub4);
        XTrace sub5 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "PluginDescriptor_start", "PluginDescriptor_complete"
        });
        extSubtrace.assignSubtrace(sub4.get(0), sub5);
        XTrace sub6 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "setName(java.lang.String)_start", "setName(java.lang.String)_complete"
        });
        extSubtrace.assignSubtrace(sub5.get(0), sub6);
        
        L2LSubtraceStructuredActivity transform = new L2LSubtraceStructuredActivity(new L2LStructuredActivity.Parameters());
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
}
