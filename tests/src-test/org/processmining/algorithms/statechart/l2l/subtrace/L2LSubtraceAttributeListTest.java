package org.processmining.algorithms.statechart.l2l.subtrace;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.l2l.L2LAttributeList;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.xes.statechart.XesCompareSame;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

public class L2LSubtraceAttributeListTest {

    protected static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();

    @Test
    public void testFlatClassifier() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A", "B"} 
        });
        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"} 
        });
        
        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testFlatClassifierComplete() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_complete", "B_complete"} 
        });
        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"} 
        });
        
        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testFlatClassifierComplete2() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_COMPLETE", "B_COMPLETE"} 
        });
        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"} 
        });
        
        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testFlatClassifierLC() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"} 
        });
        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"} 
        });
        
        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testFlatClassifierPartial() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start",  "B_complete"} 
        });
        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "A_complete"} 
        });
        
        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testFlatClassifierLCNoOverlap() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"}
        });

        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testFlatClassifierLCHalfOverlap() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "A_complete", "B_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "A_complete", "B_complete"}
        });

        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testFlatClassifierLCContain() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "A_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "A_complete"}
        });
        
        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testFlatClassifierLCNoComplete() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "A_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "A_complete", "B_complete"}
        });
        
        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testFlatClassifierLCNoStart() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_complete", "A_complete"},
        });

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "A_complete"}
        });
        
        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testLevel1ClassifierLCHalfOverlap() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "A_complete", "B_complete"},
        });
        input.get(0).get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        input.get(0).get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        input.get(0).get(2).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        input.get(0).get(3).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        
        input.get(0).get(0).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 1));
        input.get(0).get(1).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 2));
        input.get(0).get(2).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 3));
        input.get(0).get(3).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 4));

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"P_start", "P_complete"}
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "A_start", "B_start", "A_complete", "B_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);
        expected.get(0).get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        expected.get(0).get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        sub1.get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        sub1.get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        sub1.get(2).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        sub1.get(3).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));

        expected.get(0).get(0).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 1));
        expected.get(0).get(1).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 4));
        sub1.get(0).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 1));
        sub1.get(1).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 2));
        sub1.get(2).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 3));
        sub1.get(3).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 4));

        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventAttributeClassifier("phase", "phase"));
        params.clsList.add(new XEventNameClassifier());
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testLevel0ClassifierLCHalfOverlap() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "A_complete", "B_complete"},
        });
        input.get(0).get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        input.get(0).get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "Q"));
        input.get(0).get(2).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        input.get(0).get(3).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "Q"));

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"P_start", "Q_start", "P_complete", "Q_complete"}
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "A_start", "A_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "B_start", "B_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);
        extSubtrace.assignSubtrace(expected.get(0).get(1), sub2);
        expected.get(0).get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        expected.get(0).get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "Q"));
        expected.get(0).get(2).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        expected.get(0).get(3).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "Q"));
        sub1.get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        sub1.get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "P"));
        sub2.get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "Q"));
        sub2.get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "Q"));

        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventAttributeClassifier("phase", "phase"));
        params.clsList.add(new XEventNameClassifier());
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testLoopLevel() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "A_start", "A_complete"},
        });
        input.get(0).get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        input.get(0).get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        input.get(0).get(2).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        input.get(0).get(3).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));

        input.get(0).get(0).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 1));
        input.get(0).get(1).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 2));
        input.get(0).get(2).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 3));
        input.get(0).get(3).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 4));
        
        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete"}
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "R_start", "R_complete", "R_start", "R_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);
        expected.get(0).get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        expected.get(0).get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        sub1.get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        sub1.get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        sub1.get(2).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        sub1.get(3).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));

        expected.get(0).get(0).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 1));
        expected.get(0).get(1).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 4));
        sub1.get(0).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 1));
        sub1.get(1).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 2));
        sub1.get(2).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 3));
        sub1.get(3).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 4));
        
        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        params.clsList.add(new XEventAttributeClassifier("phase", "phase"));
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }

    @Test
    public void testLoopLevelPartial() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_start", "A_complete"},
        });
        input.get(0).get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        input.get(0).get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        input.get(0).get(2).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));

        input.get(0).get(0).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 1));
        input.get(0).get(1).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 2));
        input.get(0).get(2).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 3));
        
        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete"}
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "R_start", "R_complete", "R_start", "R_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);
        expected.get(0).get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        expected.get(0).get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        sub1.get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        sub1.get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        sub1.get(2).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        sub1.get(3).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));

        expected.get(0).get(0).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 1));
        expected.get(0).get(1).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 3));
        sub1.get(0).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 1));
        sub1.get(1).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 1));
        sub1.get(2).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 2));
        sub1.get(3).getAttributes().put("inst", new XAttributeDiscreteImpl("inst", 3));
        
        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        params.clsList.add(new XEventAttributeClassifier("phase", "phase"));
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testParallelLevel() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "A_complete", "B_complete", 
                "A_start", "A_complete", "A_start", "A_complete"},
        });
        input.get(0).get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        input.get(0).get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "Q"));
        input.get(0).get(2).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        input.get(0).get(3).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "Q"));
        input.get(0).get(4).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "S"));
        input.get(0).get(5).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "S"));
        input.get(0).get(6).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        input.get(0).get(7).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));

        XLog expected = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "A_complete"}
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "R_start", "R_complete", "S_start", "S_complete", "R_start", "R_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] { 
            "Q_start", "Q_complete"
        });
        extSubtrace.assignSubtrace(expected.get(0).get(0), sub1);
        extSubtrace.assignSubtrace(expected.get(0).get(1), sub2);
        expected.get(0).get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        expected.get(0).get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "Q"));
        expected.get(0).get(2).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "Q"));
        expected.get(0).get(3).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        sub1.get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        sub1.get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        sub1.get(2).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "S"));
        sub1.get(3).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "S"));
        sub1.get(4).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        sub1.get(5).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "R"));
        sub2.get(0).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "Q"));
        sub2.get(1).getAttributes().put("phase", new XAttributeLiteralImpl("phase", "Q"));

        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        params.clsList.add(new XEventAttributeClassifier("phase", "phase"));
        L2LAttributeList transform = new L2LSubtraceAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
}
