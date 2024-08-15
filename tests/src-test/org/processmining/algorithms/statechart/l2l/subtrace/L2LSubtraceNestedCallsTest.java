package org.processmining.algorithms.statechart.l2l.subtrace;

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
import org.processmining.xes.statechart.XesCompareSame;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

public class L2LSubtraceNestedCallsTest {

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
            {"A_start", "B_start", "B_complete", "A_complete"} 
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete"} 
        });
        XTrace trace = expected.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete"
        });
        extSubtrace.assignSubtrace(trace.get(0), sub1);
        
        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testNormal2() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"} 
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"} 
        });
        
        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testNormal3() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "C_start", "C_complete", "B_complete", 
                "D_start", "D_complete",
            "A_complete", "E_start", "E_complete"} 
        });
        
        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "E_start", "E_complete"} 
        });
        XTrace trace = expected.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete", "D_start", "D_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "C_start", "C_complete"
        });
        extSubtrace.assignSubtrace(trace.get(0), sub1);
        extSubtrace.assignSubtrace(sub1.get(0), sub2);
        
        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testNormal4() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "C_start", "C_complete", 
                "D_start", "D_complete", "B_complete",
            "A_complete", "E_start", "E_complete"} 
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "E_start", "E_complete"} 
        });
        XTrace trace = expected.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "C_start", "C_complete", "D_start", "D_complete"
        });
        extSubtrace.assignSubtrace(trace.get(0), sub1);
        extSubtrace.assignSubtrace(sub1.get(0), sub2);
        
        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testNormal4WithAttribs() {
        // input
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "C_start", "C_complete", 
                "D_start", "D_complete", "B_complete",
            "A_complete", "E_start", "E_complete"} 
        });
        XTrace trace = input.get(0);
        
        XAttributeMap mapA = trace.get(0).getAttributes();
        mapA.put("test", new XAttributeLiteralImpl("test", "A"));
        
        mapA = trace.get(7).getAttributes();
        mapA.put("test", new XAttributeLiteralImpl("test", "Acmp"));
        
        XAttributeMap mapB = trace.get(1).getAttributes();
        mapB.put("test", new XAttributeLiteralImpl("test", "B"));
        
        XAttributeMap mapC = trace.get(2).getAttributes();
        mapC.put("test", new XAttributeLiteralImpl("test", "C"));
        
        XAttributeMap mapD = trace.get(4).getAttributes();
        mapD.put("test", new XAttributeLiteralImpl("test", "D"));
        
        XAttributeMap mapE = trace.get(8).getAttributes();
        mapE.put("test", new XAttributeLiteralImpl("test", "E"));
        
        // expected
        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "E_start", "E_complete"} 
        });
        XTrace trace0 = expected.get(0);
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "C_start", "C_complete", "D_start", "D_complete"
        });
        extSubtrace.assignSubtrace(trace0.get(0), sub1);
        extSubtrace.assignSubtrace(sub1.get(0), sub2);
        
        LogCreateTestUtil.addDataFromMap(trace0.get(0), trace.get(0), ignoreKeys);
        LogCreateTestUtil.addDataFromMap(trace0.get(1), trace.get(7), ignoreKeys);
        LogCreateTestUtil.addDataFromMap(sub1.get(0), trace.get(1), ignoreKeys);
        LogCreateTestUtil.addDataFromMap(sub2.get(0), trace.get(2), ignoreKeys);
        LogCreateTestUtil.addDataFromMap(sub2.get(2), trace.get(4), ignoreKeys);
        LogCreateTestUtil.addDataFromMap(trace0.get(2), trace.get(8), ignoreKeys);
        
        // actual
        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        // check
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testNestedLifecycleLoopLeaf() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            { "X_start", "Y_start", 
                "X_start", 
                    "Z_start", "Z_complete", "Z_start", "Z_complete", 
                "X_complete", 
              "Y_complete", "X_complete"
            }
        });
        
        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "X_complete"} 
        });
        XTrace trace0 = expected.get(0);
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

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testNestedLifecycleLoopNode() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            { "X_start", "Y_start", 
                "X_start", "Z_start", "Z_complete", "X_complete", 
                "X_start", "Z_start", "Z_complete", "X_complete",
              "Y_complete", "X_complete"
            }
        });
        
        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "X_complete"} 
        });
        XTrace trace0 = expected.get(0);
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

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testNestedLifecycleLoopRoot() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "X_start", "Y_start", 
                "X_start", "Z_start", "Z_complete", "X_complete", 
              "Y_complete", "X_complete",
              "X_start", "Y_start", 
                "X_start", "Z_start", "Z_complete", "X_complete",
              "Y_complete", "X_complete"
            }
        });
        
        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "X_complete", "X_start", "X_complete"} 
        });
        XTrace trace0 = expected.get(0);
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

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testException1() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "C_start", "C_complete", "A_complete"},
            {"A_start", "B_start", "B_complete", "A_reassign", "E_start", "E_complete", "A_complete"} 
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete"},
            {"A_start", "A_complete"} 
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete", "C_start", "C_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete", "A+handle_start", "A+handle_complete", "E_start", "E_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);
        extSubtrace.assignSubtrace(expected.get(1).get(0), sub2);

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testLCNoOverlap() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"}
        });

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testLCHalfOverlap() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "A_complete", "B_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "A_complete", "B_complete"}
        });

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testLCHalfOverlapNested() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "A_start", "B_start", "A_complete", "B_complete", "X_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "X_complete"}
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "A_start", "B_start", "A_complete", "B_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testLCHalfOverlapNested2() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "A_start", "B_start", "A_complete", "C_start", "B_complete", "C_complete", "X_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "X_complete"}
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "A_start", "B_start", "A_complete", "C_start", "B_complete", "C_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        // TODO bug: C-start after B-start, but should be after A-complete
        // issue: parent(C) = B, parent(B) = A, parent(A) = X
        // Possible solution: use delayed list of open intervals on each node?/
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testLCHalfOverlapNested3a() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "A_start", "B_start", "A_complete", "C_start", "C_complete", "B_complete", "X_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "X_complete"}
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "A_start", "B_start", "A_complete", "B_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "C_start", "C_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);
        extSubtrace.assignSubtrace(sub1.get(1), sub2);

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testLCHalfOverlapNested3b() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "A_start", "B_start", "C_start", "A_complete", "C_complete", "B_complete", "X_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "X_complete"}
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "A_start", "B_start", "A_complete", "B_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "C_start", "C_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);
        extSubtrace.assignSubtrace(sub1.get(1), sub2);

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testLCHalfOverlapNested3c() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "A_start", "B_start", "C_start", "C_complete", "A_complete", "B_complete", "X_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"X_start", "X_complete"}
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "A_start", "B_start", "A_complete", "B_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "C_start", "C_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);
        extSubtrace.assignSubtrace(sub1.get(1), sub2);

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testLCContain() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "A_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete"}
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testLCNoComplete() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "A_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete"}
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testLCNoStart() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_complete", "A_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete"}
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);

        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
}
