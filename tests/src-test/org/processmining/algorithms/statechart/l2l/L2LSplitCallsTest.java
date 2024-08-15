package org.processmining.algorithms.statechart.l2l;

import java.util.regex.Pattern;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.xes.statechart.XesCompareSame;
import org.processmining.xes.statechart.extension.XTraceType;
import org.processmining.xes.statechart.extension.XTraceType.TypeStandardModel;

public class L2LSplitCallsTest {

    private static final XTraceType extTraceType = XTraceType.instance();
    private static final XConceptExtension extConcept = XConceptExtension.instance();
    
    @Test
    public void testNormal1() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
                {"A_start", "B_start", "B_complete", "A_complete"} 
            });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] {
            { "B_start", "B_complete" }
        });
        _setTraceData(expected.get(0), "A", TypeStandardModel.NORMAL);
        
        L2LSplitCalls.Parameters params = new L2LSplitCalls.Parameters();
        params.reTraceBaseName = Pattern.compile("A");
        L2LSplitCalls transform = new L2LSplitCalls(params);
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testNormal2() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
                {"A1_start", "B_start", "B_complete", "A1_complete",
                    "A2_start", "C_start", "G_complete", "C_complete", "A2_complete"} 
            });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] {
            { "B_start", "B_complete" },
            { "C_start", "G_complete", "C_complete" }
        });
        _setTraceData(expected.get(0), "A1", TypeStandardModel.NORMAL);
        _setTraceData(expected.get(1), "A2", TypeStandardModel.NORMAL);
        
        L2LSplitCalls.Parameters params = new L2LSplitCalls.Parameters();
        params.reTraceBaseName = Pattern.compile("A\\d");
        L2LSplitCalls transform = new L2LSplitCalls(params);
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testNormal3() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
                { "Y_start",  "X1_start", "X1_complete", 
                    "A1_start", "B_start", "B_complete", "A1_complete",
                  "X2_start", "X2_complete", 
                    "A2_start", "C_start", "G_complete", "C_complete", "A2_complete",
                  "X3_start", "X3_complete", 
                  "Y_complete",
                    } 
            });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] {
            { "B_start", "B_complete" },
            { "C_start", "G_complete", "C_complete" }
        });
        _setTraceData(expected.get(0), "A1", TypeStandardModel.NORMAL);
        _setTraceData(expected.get(1), "A2", TypeStandardModel.NORMAL);
        
        L2LSplitCalls.Parameters params = new L2LSplitCalls.Parameters();
        params.reTraceBaseName = Pattern.compile("A\\d");
        L2LSplitCalls transform = new L2LSplitCalls(params);
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testIncomplete1() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
                {"A_start", "B_start", "B_complete" } 
            });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] {
            { "B_start", "B_complete" }
        });
        _setTraceData(expected.get(0), "A", TypeStandardModel.INCOMPLETE);
        
        L2LSplitCalls.Parameters params = new L2LSplitCalls.Parameters();
        params.reTraceBaseName = Pattern.compile("A");
        L2LSplitCalls transform = new L2LSplitCalls(params);
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testIncomplete2() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
                {"A1_start", "B_start", "B_complete",
                    "A2_start", "C_start", "G_complete", "C_complete", "A2_complete"} 
            });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] {
            { "B_start", "B_complete" },
            { "C_start", "G_complete", "C_complete" }
        });
        _setTraceData(expected.get(0), "A1", TypeStandardModel.INCOMPLETE);
        _setTraceData(expected.get(1), "A2", TypeStandardModel.NORMAL);
        
        L2LSplitCalls.Parameters params = new L2LSplitCalls.Parameters();
        params.reTraceBaseName = Pattern.compile("A\\d");
        L2LSplitCalls transform = new L2LSplitCalls(params);
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testIncomplete3() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
                { "Y_start",  "X1_start", "X1_complete", 
                    "A1_start", "B_start", "B_complete",
                  "X2_start", "X2_complete", 
                    "A2_start", "C_start", "G_complete", "C_complete", "A2_complete",
                  "X3_start", "X3_complete", 
                  "Y_complete",
                    } 
            });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] {
            { "B_start", "B_complete", "X2_start", "X2_complete" },
            { "C_start", "G_complete", "C_complete" }
        });
        _setTraceData(expected.get(0), "A1", TypeStandardModel.INCOMPLETE);
        _setTraceData(expected.get(1), "A2", TypeStandardModel.NORMAL);
        
        L2LSplitCalls.Parameters params = new L2LSplitCalls.Parameters();
        params.reTraceBaseName = Pattern.compile("A\\d");
        L2LSplitCalls transform = new L2LSplitCalls(params);
        XLog actual = transform.transform(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    private void _setTraceData(XTrace trace, String name,
            TypeStandardModel type) {
        extConcept.assignName(trace, name);
        extTraceType.assignStandardTransition(trace, type);
    }
    
}
