package org.processmining.algorithms.statechart.l2l.list;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.algorithms.statechart.l2l.list.L2LListNestedCalls;
import org.processmining.xes.statechart.XesCompareSame;

public class L2LListNestedCallsTest {

    @Test
    public void testNormal1() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "A_complete"} 
        });
        XTrace inTrace = input.get(0);
        
        XLog expected = LogCreateTestUtil.createLogList(new String[][][] {
            { {"A", "B"} }
        });
        XTrace trace = expected.get(0);
        
        LogCreateTestUtil.addMaps(trace.get(0), inTrace.get(0), inTrace.get(1));
        
        L2LListNestedCalls transform = new L2LListNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testNormal2() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"} 
        });
        XTrace inTrace = input.get(0);
        
        XLog expected = LogCreateTestUtil.createLogList(new String[][][] {
            { {"A"}, {"B"} }
        });
        XTrace trace = expected.get(0);
        
        LogCreateTestUtil.addMaps(trace.get(0), inTrace.get(0));
        LogCreateTestUtil.addMaps(trace.get(1), inTrace.get(2));
        
        L2LListNestedCalls transform = new L2LListNestedCalls();
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
        XTrace inTrace = input.get(0);
        
        XLog expected = LogCreateTestUtil.createLogList(new String[][][] {
                { {"A", "B", "C"}, {"A", "D"}, {"E"} }
        });
        XTrace trace = expected.get(0);
        
        LogCreateTestUtil.addMaps(trace.get(0), inTrace.get(0), inTrace.get(1), inTrace.get(2));
        LogCreateTestUtil.addMaps(trace.get(1), inTrace.get(0), inTrace.get(5));
        LogCreateTestUtil.addMaps(trace.get(2), inTrace.get(8));
        
        L2LListNestedCalls transform = new L2LListNestedCalls();
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
        XTrace inTrace = input.get(0);
        
        XLog expected = LogCreateTestUtil.createLogList(new String[][][] {
                { {"A", "B", "C"}, {"A", "B", "D"}, {"E"} }
        });
        XTrace trace = expected.get(0);
        
        LogCreateTestUtil.addMaps(trace.get(0), inTrace.get(0), inTrace.get(1), inTrace.get(2));
        LogCreateTestUtil.addMaps(trace.get(1), inTrace.get(0), inTrace.get(1), inTrace.get(4));
        LogCreateTestUtil.addMaps(trace.get(2), inTrace.get(8));
        
        L2LListNestedCalls transform = new L2LListNestedCalls();
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
        
        XAttributeMap mapB = trace.get(1).getAttributes();
        mapB.put("test", new XAttributeLiteralImpl("test", "B"));
        
        XAttributeMap mapC = trace.get(2).getAttributes();
        mapC.put("test", new XAttributeLiteralImpl("test", "C"));
        
        XAttributeMap mapD = trace.get(4).getAttributes();
        mapD.put("test", new XAttributeLiteralImpl("test", "D"));
        
        XAttributeMap mapE = trace.get(8).getAttributes();
        mapE.put("test", new XAttributeLiteralImpl("test", "E"));
        
        // expected
        XLog expected = LogCreateTestUtil.createLogList(new String[][][] {
            { {"A", "B", "C"}, {"A", "B", "D"}, {"E"} }
        });
        trace = expected.get(0);
        
        LogCreateTestUtil.addMaps(trace.get(0), mapA, mapB, mapC);
        LogCreateTestUtil.addMaps(trace.get(1), mapA, mapB, mapD);
        LogCreateTestUtil.addMaps(trace.get(2), mapE);
        
        // actual
        L2LListNestedCalls transform = new L2LListNestedCalls();
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
        XTrace inTrace = input.get(0);
        
        XLog expected = LogCreateTestUtil.createLogList(new String[][][] {
                { {"X", "Y", "X", "Z_1"}, {"X", "Y", "X", "Z_2"} }
        });
        XTrace trace = expected.get(0);
        
        LogCreateTestUtil.addMaps(trace.get(0), inTrace.get(0), inTrace.get(1), inTrace.get(2), inTrace.get(3));
        LogCreateTestUtil.addMaps(trace.get(1), inTrace.get(0), inTrace.get(1), inTrace.get(2), inTrace.get(5));
        
        L2LListNestedCalls transform = new L2LListNestedCalls();
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
        XTrace inTrace = input.get(0);
        
        XLog expected = LogCreateTestUtil.createLogList(new String[][][] {
                { {"X", "Y", "X_1", "Z_1"}, {"X", "Y", "X_2", "Z_2"} }
        });
        XTrace trace = expected.get(0);
        
        LogCreateTestUtil.addMaps(trace.get(0), inTrace.get(0), inTrace.get(1), inTrace.get(2), inTrace.get(3));
        LogCreateTestUtil.addMaps(trace.get(1), inTrace.get(0), inTrace.get(1), inTrace.get(6), inTrace.get(7));
        
        L2LListNestedCalls transform = new L2LListNestedCalls();
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
        XTrace inTrace = input.get(0);
        
        XLog expected = LogCreateTestUtil.createLogList(new String[][][] {
                { {"X", "Y", "X", "Z"}, {"X_2", "Y_2", "X_2", "Z_2"} }
        });
        XTrace trace = expected.get(0);
        
        LogCreateTestUtil.addMaps(trace.get(0), inTrace.get(0), inTrace.get(1), inTrace.get(2), inTrace.get(3));
        LogCreateTestUtil.addMaps(trace.get(1), inTrace.get(8), inTrace.get(9), inTrace.get(10), inTrace.get(11));
        
        L2LListNestedCalls transform = new L2LListNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testException1() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "C_start", "C_complete", "A_complete"},
            {"A_start", "B_start", "B_complete", "A_reassign", "E_start", "E_complete", "A_complete"} 
        });
        XTrace inTrace1 = input.get(0);
        XTrace inTrace2 = input.get(1);
        
        XLog expected = LogCreateTestUtil.createLogList(new String[][][] {
            { {"A", "B"}, {"A", "C"} },
            { {"A", "B"}, {"A", "A+handle"}, {"A", "E"} },
        });
        XTrace trace1 = expected.get(0);
        XTrace trace2 = expected.get(1);
        
        LogCreateTestUtil.addMaps(trace1.get(0), inTrace1.get(0), inTrace1.get(1));
        LogCreateTestUtil.addMaps(trace1.get(1), inTrace1.get(0), inTrace1.get(3));
        
        LogCreateTestUtil.addMaps(trace2.get(0), inTrace2.get(0), inTrace2.get(1));
        LogCreateTestUtil.addMaps(trace2.get(1), inTrace2.get(0), inTrace2.get(3));
        LogCreateTestUtil.addMaps(trace2.get(2), inTrace2.get(0), inTrace2.get(4));
        
        L2LListNestedCalls transform = new L2LListNestedCalls();
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

}
