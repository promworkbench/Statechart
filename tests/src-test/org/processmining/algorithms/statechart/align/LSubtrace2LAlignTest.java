package org.processmining.algorithms.statechart.align;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.models.statechart.im.log.IMLogHierarchySubtraceImpl;
import org.processmining.xes.statechart.XesCompareSame;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

public class LSubtrace2LAlignTest {

    protected static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();

    private static Set<String> ignoreKeys;

    @BeforeClass
    public static void init() {
        ignoreKeys = new HashSet<>();
        ignoreKeys.add(XConceptExtension.KEY_NAME);
        ignoreKeys.add(XConceptExtension.KEY_INSTANCE);
        ignoreKeys.add(XLifecycleExtension.KEY_TRANSITION);
        ignoreKeys.add(XSubtraceExtension.KEY_SUBTRACE);
    }

    @Test
    public void testNormal1() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete"} 
        });
        XTrace trace = input.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete"
        });
        extSubtrace.assignSubtrace(trace.get(0), sub1);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "A_complete"} 
        });
        
        LSubtrace2LAlign transform = new LSubtrace2LAlign();
        XLog actual = transform.transform(inputSubtrace);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testNormal2() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"} 
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"} 
        });

        LSubtrace2LAlign transform = new LSubtrace2LAlign();
        XLog actual = transform.transform(inputSubtrace);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testNormal3() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "E_start", "E_complete"} 
        });
        XTrace trace = input.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete", "D_start", "D_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "C_start", "C_complete"
        });
        extSubtrace.assignSubtrace(trace.get(0), sub1);
        extSubtrace.assignSubtrace(sub1.get(0), sub2);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "C_start", "C_complete", "B_complete", 
                "D_start", "D_complete",
            "A_complete", "E_start", "E_complete"} 
        });

        LSubtrace2LAlign transform = new LSubtrace2LAlign();
        XLog actual = transform.transform(inputSubtrace);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testNormal4() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "E_start", "E_complete"} 
        });
        XTrace trace = input.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "C_start", "C_complete", "D_start", "D_complete"
        });
        extSubtrace.assignSubtrace(trace.get(0), sub1);
        extSubtrace.assignSubtrace(sub1.get(0), sub2);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "C_start", "C_complete", 
                "D_start", "D_complete", "B_complete",
            "A_complete", "E_start", "E_complete"} 
        });
        
        LSubtrace2LAlign transform = new LSubtrace2LAlign();
        XLog actual = transform.transform(inputSubtrace);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testNormal4WithAttribs() {
        // input
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "E_start", "E_complete"} 
        });
        XTrace trace0 = input.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "C_start", "C_complete", "D_start", "D_complete"
        });
        extSubtrace.assignSubtrace(trace0.get(0), sub1);
        extSubtrace.assignSubtrace(sub1.get(0), sub2);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
        XAttributeMap mapA = trace0.get(0).getAttributes();
        mapA.put("test", new XAttributeLiteralImpl("test", "A"));
        
        mapA = trace0.get(1).getAttributes();
        mapA.put("test", new XAttributeLiteralImpl("test", "Acmp"));
        
        XAttributeMap mapB = sub1.get(0).getAttributes();
        mapB.put("test", new XAttributeLiteralImpl("test", "B"));
        
        XAttributeMap mapC = sub2.get(0).getAttributes();
        mapC.put("test", new XAttributeLiteralImpl("test", "C"));
        
        XAttributeMap mapD = sub2.get(2).getAttributes();
        mapD.put("test", new XAttributeLiteralImpl("test", "D"));
        
        XAttributeMap mapE = trace0.get(2).getAttributes();
        mapE.put("test", new XAttributeLiteralImpl("test", "E"));
        
        // expected
        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "C_start", "C_complete", 
                "D_start", "D_complete", "B_complete",
            "A_complete", "E_start", "E_complete"} 
        });
        XTrace trace = expected.get(0);
        
        LogCreateTestUtil.addDataFromMap(trace.get(0), trace0.get(0), ignoreKeys);
        LogCreateTestUtil.addDataFromMap(trace.get(7), trace0.get(1), ignoreKeys);
        LogCreateTestUtil.addDataFromMap(trace.get(1), sub1.get(0), ignoreKeys);
        LogCreateTestUtil.addDataFromMap(trace.get(2), sub2.get(0), ignoreKeys);
        LogCreateTestUtil.addDataFromMap(trace.get(4), sub2.get(2), ignoreKeys);
        LogCreateTestUtil.addDataFromMap(trace.get(8), trace0.get(2), ignoreKeys);
        
        // actual
        LSubtrace2LAlign transform = new LSubtrace2LAlign();
        XLog actual = transform.transform(inputSubtrace);
        
        // check
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testNestedLifecycleLoopLeaf() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "X_complete"} 
        });
        XTrace trace0 = input.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "Y_start", "Y_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "X_start", "X_complete"
        });
        XTrace sub3 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "Z_start", "Z_complete", "Z_start", "Z_complete"
        });
        extSubtrace.assignSubtrace(trace0.get(0), sub1);
        extSubtrace.assignSubtrace(sub1.get(0), sub2);
        extSubtrace.assignSubtrace(sub2.get(0), sub3);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            { "X_start", "Y_start", 
                "X_start", 
                    "Z_start", "Z_complete", "Z_start", "Z_complete", 
                "X_complete", 
              "Y_complete", "X_complete"
            }
        });
        
        LSubtrace2LAlign transform = new LSubtrace2LAlign();
        XLog actual = transform.transform(inputSubtrace);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testNestedLifecycleLoopNode() {
        
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "X_complete"} 
        });
        XTrace trace0 = input.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "Y_start", "Y_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "X_start", "X_complete", "X_start", "X_complete"
        });
        XTrace sub31 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "Z_start", "Z_complete"
        });
        XTrace sub32 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "Z_start", "Z_complete"
        });
        extSubtrace.assignSubtrace(trace0.get(0), sub1);
        extSubtrace.assignSubtrace(sub1.get(0), sub2);
        extSubtrace.assignSubtrace(sub2.get(0), sub31);
        extSubtrace.assignSubtrace(sub2.get(2), sub32);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            { "X_start", "Y_start", 
                "X_start", "Z_start", "Z_complete", "X_complete", 
                "X_start", "Z_start", "Z_complete", "X_complete",
              "Y_complete", "X_complete"
            }
        });
        
        LSubtrace2LAlign transform = new LSubtrace2LAlign();
        XLog actual = transform.transform(inputSubtrace);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testNestedLifecycleLoopRoot() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "X_complete", "X_start", "X_complete"} 
        });
        XTrace trace0 = input.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "Y_start", "Y_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "X_start", "X_complete"
        });
        XTrace sub3 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "Z_start", "Z_complete"
        });
        extSubtrace.assignSubtrace(trace0.get(0), sub1);
        extSubtrace.assignSubtrace(trace0.get(2), sub1);
        extSubtrace.assignSubtrace(sub1.get(0), sub2);
        extSubtrace.assignSubtrace(sub2.get(0), sub3);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] {
            { "X_start", "Y_start", 
                "X_start", "Z_start", "Z_complete", "X_complete", 
              "Y_complete", "X_complete",
              "X_start", "Y_start", 
                "X_start", "Z_start", "Z_complete", "X_complete",
              "Y_complete", "X_complete"
            }
        });

        LSubtrace2LAlign transform = new LSubtrace2LAlign();
        XLog actual = transform.transform(inputSubtrace);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
}
