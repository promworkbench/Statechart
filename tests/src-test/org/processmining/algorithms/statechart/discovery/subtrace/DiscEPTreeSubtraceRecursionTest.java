package org.processmining.algorithms.statechart.discovery.subtrace;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.discovery.DiscoverEPTreeRecursion;
import org.processmining.algorithms.statechart.discovery.EPTreeCompareSame;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.algorithms.statechart.l2l.subtrace.L2LSubtraceNestedCalls;
import org.processmining.models.statechart.decorate.staticmetric.EPTreeFreqMetricDecorator;
import org.processmining.models.statechart.decorate.staticmetric.FreqMetric;
import org.processmining.models.statechart.eptree.EPTreeCreateUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.models.statechart.im.log.IMLogHierarchySubtraceImpl;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

public class DiscEPTreeSubtraceRecursionTest {

    protected static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();
    
    @Test
    public void testSimpleNestingSeq() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete" },
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] {
            "X_start", "X_complete", "B_start", "B_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] {
            "Y_start", "Y_complete"
        });
        extSubtrace.assignSubtrace(input.get(0).get(2), sub1);
        extSubtrace.assignSubtrace(sub1.get(2), sub2);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
        IEPTree expected = EPTreeCreateUtil.create("->(A, \\/=B(x(->(X, R\\/=B), Y)), C)");

        DiscoverEPTreeRecursion disc = new DiscoverEPTreeRecursion();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );

        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decSeq1 = decs.getDecoration(actual.getRoot());
        FreqMetric decXor = decs.getDecoration(actual.getNodeByIndex(1, 0));
        FreqMetric decSeq3 = decs.getDecoration(actual.getNodeByIndex(1, 0, 0));
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decBdecl = decs.getDecoration(actual.getNodeByIndex(1, 0));
        FreqMetric decBrecurrent = decs.getDecoration(actual.getNodeByIndex(1, 0, 0, 1));
        FreqMetric decX = decs.getDecoration(actual.getNodeByLabel("X"));
        FreqMetric decY = decs.getDecoration(actual.getNodeByLabel("Y"));
        FreqMetric decC = decs.getDecoration(actual.getNodeByLabel("C"));

        Assert.assertEquals("seq 1 - abs", 1, decSeq1.getFreqAbsolute());
        Assert.assertEquals("seq 1 - case", 1, decSeq1.getFreqCase());
        
        Assert.assertEquals("xor - abs", 2, decXor.getFreqAbsolute());
        Assert.assertEquals("xor - case", 1, decXor.getFreqCase());
        
        Assert.assertEquals("seq 3 - abs", 1, decSeq3.getFreqAbsolute());
        Assert.assertEquals("seq 3 - case", 1, decSeq3.getFreqCase());
        
        Assert.assertEquals("A - abs", 1, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 1, decA.getFreqCase());
        
        Assert.assertEquals("B decl - abs", 2, decBdecl.getFreqAbsolute());
        //Assert.assertEquals("B decl - abs", 1, decBdecl.getFreqAbsolute());
        Assert.assertEquals("B decl - case", 1, decBdecl.getFreqCase());

        Assert.assertEquals("B recurrent - abs", 1, decBrecurrent.getFreqAbsolute());
        Assert.assertEquals("B recurrent - case", 1, decBrecurrent.getFreqCase());
        
        Assert.assertEquals("X - abs", 1, decX.getFreqAbsolute());
        Assert.assertEquals("X - case", 1, decX.getFreqCase());

        Assert.assertEquals("Y - abs", 1, decY.getFreqAbsolute());
        Assert.assertEquals("Y - case", 1, decY.getFreqCase());
        
        Assert.assertEquals("C - abs", 1, decC.getFreqAbsolute());
        Assert.assertEquals("C - case", 1, decC.getFreqCase());
    }

    @Test
    public void testLoopAndNesting() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete" },
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] {
            "X_start", "X_complete", "B_start", "B_complete", "Y_start", "Y_complete"
        });
        XTrace sub11 = LogCreateTestUtil.createTraceFlat(new String[] {
            "Y_start", "Y_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] {
            "X_start", "X_complete", "B_start", "B_complete"
        });
        XTrace sub21 = LogCreateTestUtil.createTraceFlat(new String[] {
            "Y_start", "Y_complete"
        });
        XTrace sub3 = LogCreateTestUtil.createTraceFlat(new String[] {
            "Y_start", "Y_complete"
        });
        extSubtrace.assignSubtrace(input.get(0).get(2), sub1);
        extSubtrace.assignSubtrace(sub1.get(2), sub11);
        extSubtrace.assignSubtrace(input.get(1).get(2), sub2);
        extSubtrace.assignSubtrace(sub2.get(2), sub21);
        extSubtrace.assignSubtrace(input.get(2).get(2), sub3);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
        // Loop and Nesting will be modeled as a nesting with additional seq / choice constructs
        // (that is, we cannot rediscover a looping group with to the single instance assumption
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B", "X" }, { "B", "B", "Y" }, { "B", "Y" }, { "C" } },
//            { { "A" }, { "B", "X" }, { "B", "B", "Y" }, { "C" } },
//            { { "A" }, { "B", "Y" }, { "C" } },
//        });

        IEPTree expected = EPTreeCreateUtil.create("->(A, \\/=B(->(x(tau, ->(X, R\\/=B)), x(tau, Y))), C)");

        DiscoverEPTreeRecursion disc = new DiscoverEPTreeRecursion();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );

        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decSeq1 = decs.getDecoration(actual.getRoot());
        FreqMetric decSeq3 = decs.getDecoration(actual.getNodeByIndex(1,0));
        FreqMetric decXor1 = decs.getDecoration(actual.getNodeByIndex(1,0,0));
        FreqMetric decXor2 = decs.getDecoration(actual.getNodeByIndex(1,0,1));
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decBdecl = decs.getDecoration(actual.getNodeByIndex(1));
        FreqMetric decBrecurrent = decs.getDecoration(actual.getNodeByIndex(1,0,0,1,1));
        FreqMetric decX = decs.getDecoration(actual.getNodeByLabel("X"));
        FreqMetric decY = decs.getDecoration(actual.getNodeByLabel("Y"));
        FreqMetric decC = decs.getDecoration(actual.getNodeByLabel("C"));
        FreqMetric decTau1 = decs.getDecoration(actual.getNodeByIndex(1,0,0,0));
        FreqMetric decTau2 = decs.getDecoration(actual.getNodeByIndex(1,0,1,0));

        Assert.assertEquals("seq 1 - abs", 3, decSeq1.getFreqAbsolute());
        Assert.assertEquals("seq 1 - case", 3, decSeq1.getFreqCase());

        Assert.assertEquals("seq 3 - abs", 5, decSeq3.getFreqAbsolute());
        Assert.assertEquals("seq 3 - case", 3, decSeq3.getFreqCase());

        Assert.assertEquals("xor 1 - abs", 5, decXor1.getFreqAbsolute());
        Assert.assertEquals("xor 1 - case", 3, decXor1.getFreqCase());

        Assert.assertEquals("xor 2 - abs", 5, decXor2.getFreqAbsolute());
        Assert.assertEquals("xor 2 - case", 3, decXor2.getFreqCase());

        Assert.assertEquals("A - abs", 3, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 3, decA.getFreqCase());
        
        //Assert.assertEquals("B decl - abs", 5, decBdecl.getFreqAbsolute());
        Assert.assertEquals("B decl - abs", 3, decBdecl.getFreqAbsolute());
        Assert.assertEquals("B decl - case", 3, decBdecl.getFreqCase());

        Assert.assertEquals("B recurrent - abs", 2, decBrecurrent.getFreqAbsolute());
        Assert.assertEquals("B recurrent - case", 2, decBrecurrent.getFreqCase());

        Assert.assertEquals("X - abs", 2, decX.getFreqAbsolute());
        Assert.assertEquals("X - case", 2, decX.getFreqCase());

        Assert.assertEquals("Y - abs", 4, decY.getFreqAbsolute());
        Assert.assertEquals("Y - case", 3, decY.getFreqCase());

        Assert.assertEquals("C - abs", 3, decC.getFreqAbsolute());
        Assert.assertEquals("C - case", 3, decC.getFreqCase());

        Assert.assertEquals("tau 1 - abs", 3, decTau1.getFreqAbsolute());
        Assert.assertEquals("tau 1 - case", 3, decTau1.getFreqCase());

        Assert.assertEquals("tau 2 - abs", 1, decTau2.getFreqAbsolute());
        Assert.assertEquals("tau 2 - case", 1, decTau2.getFreqCase());
        
    }
    
    @Test
    public void testDeepComposite() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "Main_start", "Main_complete" },
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] {
            "S1_start", "S1_complete", "S2_start", "S2_complete"
        });
        XTrace sub21 = LogCreateTestUtil.createTraceFlat(new String[] {
            "A_start", "A_complete", "B_start", "B_complete"
        });
        XTrace sub22 = LogCreateTestUtil.createTraceFlat(new String[] {
            "C_start", "C_complete", "D_start", "D_complete"
        });
        extSubtrace.assignSubtrace(input.get(0).get(0), sub1);
        extSubtrace.assignSubtrace(sub1.get(0), sub21);
        extSubtrace.assignSubtrace(sub1.get(2), sub22);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//                { { "Main", "S1", "A" },
//                  { "Main", "S1", "B" },
//                  { "Main", "S2", "C" },
//                  { "Main", "S2", "D" },
//                }
//            });

        IEPTree expected = EPTreeCreateUtil.create("\\/=Main(->(\\/=S1(->(A, B)), \\/=S2(->(C, D))))");

        DiscoverEPTreeRecursion disc = new DiscoverEPTreeRecursion();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );

        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decMain = decs.getDecoration(actual.getNodeByLabel("Main"));
        FreqMetric decS1 = decs.getDecoration(actual.getNodeByLabel("S1"));
        FreqMetric decS2 = decs.getDecoration(actual.getNodeByLabel("S2"));
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decB = decs.getDecoration(actual.getNodeByLabel("B"));
        FreqMetric decC = decs.getDecoration(actual.getNodeByLabel("C"));
        FreqMetric decD = decs.getDecoration(actual.getNodeByLabel("D"));
        FreqMetric decSeq1  = decs.getDecoration(actual.getNodeByIndex(0));
        FreqMetric decSeq2  = decs.getDecoration(actual.getNodeByIndex(0,0,0));
        FreqMetric decSeq3  = decs.getDecoration(actual.getNodeByIndex(0,1,0));
        
        Assert.assertEquals("decMain - abs", 1, decMain.getFreqAbsolute());
        Assert.assertEquals("decMain - case", 1, decMain.getFreqCase());
        
        Assert.assertEquals("decS1 - abs", 1, decS1.getFreqAbsolute());
        Assert.assertEquals("decS1 - case", 1, decS1.getFreqCase());
        
        Assert.assertEquals("decS2 - abs", 1, decS2.getFreqAbsolute());
        Assert.assertEquals("decS2 - case", 1, decS2.getFreqCase());
        
        Assert.assertEquals("decA - abs", 1, decA.getFreqAbsolute());
        Assert.assertEquals("decA - case", 1, decA.getFreqCase());
        
        Assert.assertEquals("decB - abs", 1, decB.getFreqAbsolute());
        Assert.assertEquals("decB - case", 1, decB.getFreqCase());
        
        Assert.assertEquals("decC - abs", 1, decC.getFreqAbsolute());
        Assert.assertEquals("decC - case", 1, decC.getFreqCase());
        
        Assert.assertEquals("decD - abs", 1, decD.getFreqAbsolute());
        Assert.assertEquals("decD - case", 1, decD.getFreqCase());
        
        Assert.assertEquals("decSeq1 - abs", 1, decSeq1.getFreqAbsolute());
        Assert.assertEquals("decSeq1 - case", 1, decSeq1.getFreqCase());
        
        Assert.assertEquals("decSeq2 - abs", 1, decSeq2.getFreqAbsolute());
        Assert.assertEquals("decSeq2 - case", 1, decSeq2.getFreqCase());
        
        Assert.assertEquals("decSeq3 - abs", 1, decSeq3.getFreqAbsolute());
        Assert.assertEquals("decSeq3 - case", 1, decSeq3.getFreqCase());
    }

    @Test
    public void testDualRecursionOnce() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "Pre_start", "Pre_complete", "F_start", "F_complete", "Post_start", "Post_complete"  },
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] {
            "S1_start", "S1_complete", "G_start", "G_complete", "S2_start", "S2_complete"
        });
        XTrace sub11 = LogCreateTestUtil.createTraceFlat(new String[] {
            "S1_start", "S1_complete", "F_start", "F_complete", "S2_start", "S2_complete"
        });
        XTrace sub111 = LogCreateTestUtil.createTraceFlat(new String[] {
            "B_start", "B_complete"
        });
        extSubtrace.assignSubtrace(input.get(0).get(2), sub1);
        extSubtrace.assignSubtrace(sub1.get(2), sub11);
        extSubtrace.assignSubtrace(sub11.get(2), sub111);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//                { { "Pre" }, 
//                  { "F", "S1" },
//                  { "F", "G", "S1" },
//                  { "F", "G", "F", "B" },
//                  { "F", "G", "S2" },
//                  { "F", "S2" },
//                  { "Post" } 
//                }
//            });
        
        IEPTree expected = EPTreeCreateUtil.create("->(Pre, \\/=F(x(->(S1, \\/=G(->(S1, R\\/=F, S2)), S2), B)), Post)");

        DiscoverEPTreeRecursion disc = new DiscoverEPTreeRecursion();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
        
        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decPre   = decs.getDecoration(actual.getNodeByLabel("Pre"));
        FreqMetric decPost  = decs.getDecoration(actual.getNodeByLabel("Post"));
        FreqMetric decB     = decs.getDecoration(actual.getNodeByLabel("B"));
        FreqMetric decSeq1  = decs.getDecoration(actual.getRoot());
        FreqMetric decFDecl = decs.getDecoration(actual.getNodeByIndex(1));
        FreqMetric decXor   = decs.getDecoration(actual.getNodeByIndex(1,0));
        FreqMetric decSeq3  = decs.getDecoration(actual.getNodeByIndex(1,0,0));
        FreqMetric decFS1   = decs.getDecoration(actual.getNodeByIndex(1,0,0,0));
        FreqMetric decGDecl = decs.getDecoration(actual.getNodeByIndex(1,0,0,1));
        FreqMetric decFS2   = decs.getDecoration(actual.getNodeByIndex(1,0,0,2));
        FreqMetric decGS1   = decs.getDecoration(actual.getNodeByIndex(1,0,0,1,0,0));
        FreqMetric decSeq6  = decs.getDecoration(actual.getNodeByIndex(1,0,0,1,0));
        FreqMetric decFRec  = decs.getDecoration(actual.getNodeByIndex(1,0,0,1,0,1));
        FreqMetric decGS2   = decs.getDecoration(actual.getNodeByIndex(1,0,0,1,0,2));

        Assert.assertEquals("decPre - abs", 1, decPre.getFreqAbsolute());
        Assert.assertEquals("decPre - case", 1, decPre.getFreqCase());

        Assert.assertEquals("decPost - abs", 1, decPost.getFreqAbsolute());
        Assert.assertEquals("decPost - case", 1, decPost.getFreqCase());

        Assert.assertEquals("decB - abs", 1, decB.getFreqAbsolute());
        Assert.assertEquals("decB - case", 1, decB.getFreqCase());

        Assert.assertEquals("decSeq1 - abs", 1, decSeq1.getFreqAbsolute());
        Assert.assertEquals("decSeq1 - case", 1, decSeq1.getFreqCase());

        //Assert.assertEquals("decFDecl - abs", 2, decFDecl.getFreqAbsolute());
        Assert.assertEquals("decFDecl - abs", 1, decFDecl.getFreqAbsolute());
        Assert.assertEquals("decFDecl - case", 1, decFDecl.getFreqCase());

        Assert.assertEquals("decXor - abs", 2, decXor.getFreqAbsolute());
        Assert.assertEquals("decXor - case", 1, decXor.getFreqCase());

        Assert.assertEquals("decSeq3 - abs", 1, decSeq3.getFreqAbsolute());
        Assert.assertEquals("decSeq3 - case", 1, decSeq3.getFreqCase());

        Assert.assertEquals("decFS1 - abs", 1, decFS1.getFreqAbsolute());
        Assert.assertEquals("decFS1 - case", 1, decFS1.getFreqCase());

        Assert.assertEquals("decGDecl - abs", 1, decGDecl.getFreqAbsolute()); // TODO issue
        Assert.assertEquals("decGDecl - case", 1, decGDecl.getFreqCase());

        Assert.assertEquals("decFS2 - abs", 1, decFS2.getFreqAbsolute());
        Assert.assertEquals("decFS2 - case", 1, decFS2.getFreqCase());

        Assert.assertEquals("decGS1 - abs", 1, decGS1.getFreqAbsolute());
        Assert.assertEquals("decGS1 - case", 1, decGS1.getFreqCase());

        Assert.assertEquals("decSeq6 - abs", 1, decSeq6.getFreqAbsolute());
        Assert.assertEquals("decSeq6 - case", 1, decSeq6.getFreqCase());

        Assert.assertEquals("decFRec - abs", 1, decFRec.getFreqAbsolute());
        Assert.assertEquals("decFRec - case", 1, decFRec.getFreqCase());

        Assert.assertEquals("decGS2 - abs", 1, decGS2.getFreqAbsolute());
        Assert.assertEquals("decGS2 - case", 1, decGS2.getFreqCase());
    }
    
    @Test
    public void testDualRecursionTwice() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "Pre_start", "Pre_complete", "F_start", "F_complete", "Post_start", "Post_complete"  },
            { "Pre_start", "Pre_complete", "F_start", "F_complete", "Post_start", "Post_complete"  },
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] {
            "S1_start", "S1_complete", "G_start", "G_complete", "S2_start", "S2_complete"
        });
        XTrace sub11 = LogCreateTestUtil.createTraceFlat(new String[] {
            "S1_start", "S1_complete", "F_start", "F_complete", "S2_start", "S2_complete"
        });
        XTrace sub111 = LogCreateTestUtil.createTraceFlat(new String[] {
            "B_start", "B_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] {
            "S1_start", "S1_complete", "G_start", "G_complete", "S2_start", "S2_complete"
        });
        XTrace sub21 = LogCreateTestUtil.createTraceFlat(new String[] {
            "S1_start", "S1_complete", "F_start", "F_complete", "S2_start", "S2_complete"
        });
        XTrace sub211 = LogCreateTestUtil.createTraceFlat(new String[] {
            "S1_start", "S1_complete", "G_start", "G_complete", "S2_start", "S2_complete"
        });
        XTrace sub2111 = LogCreateTestUtil.createTraceFlat(new String[] {
            "B_start", "B_complete"
        });
        extSubtrace.assignSubtrace(input.get(0).get(2), sub1);
        extSubtrace.assignSubtrace(sub1.get(2), sub11);
        extSubtrace.assignSubtrace(sub11.get(2), sub111);
        extSubtrace.assignSubtrace(input.get(1).get(2), sub2);
        extSubtrace.assignSubtrace(sub2.get(2), sub21);
        extSubtrace.assignSubtrace(sub21.get(2), sub211);
        extSubtrace.assignSubtrace(sub211.get(2), sub2111);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//                { { "Pre" }, 
//                  { "F", "S1" },
//                  { "F", "G", "S1" },
//                  { "F", "G", "F", "B" },
//                  { "F", "G", "S2" },
//                  { "F", "S2" },
//                  { "Post" } 
//                },
//                { { "Pre" }, 
//                  { "F", "S1" },
//                  { "F", "G", "S1" },
//                  { "F", "G", "F", "S1" },
//                  { "F", "G", "F", "G", "B" },
//                  { "F", "G", "F", "S2" },
//                  { "F", "G", "S2" },
//                  { "F", "S2" },
//                  { "Post" } 
//                }
//            });
        
        IEPTree expected = EPTreeCreateUtil.create("->(Pre, \\/=F(x(->(S1, \\/=G(x(->(S1, R\\/=F, S2), B)), S2), B)), Post)");

        DiscoverEPTreeRecursion disc = new DiscoverEPTreeRecursion();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
        
        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decSeq1  = decs.getDecoration(actual.getRoot());
        FreqMetric decPre   = decs.getDecoration(actual.getNodeByIndex(0));
        FreqMetric decFDecl = decs.getDecoration(actual.getNodeByIndex(1));
        FreqMetric decPost  = decs.getDecoration(actual.getNodeByIndex(2));
        FreqMetric decXor1  = decs.getDecoration(actual.getNodeByIndex(1,0));
        FreqMetric decSeq3  = decs.getDecoration(actual.getNodeByIndex(1,0,0));
        FreqMetric decFB    = decs.getDecoration(actual.getNodeByIndex(1,0,1));
        FreqMetric decFS1   = decs.getDecoration(actual.getNodeByIndex(1,0,0,0));
        FreqMetric decGDecl = decs.getDecoration(actual.getNodeByIndex(1,0,0,1));
        FreqMetric decFS2   = decs.getDecoration(actual.getNodeByIndex(1,0,0,2));
        FreqMetric decXor2  = decs.getDecoration(actual.getNodeByIndex(1,0,0,1,0));
        FreqMetric decSeq5  = decs.getDecoration(actual.getNodeByIndex(1,0,0,1,0,0));
        FreqMetric decGB    = decs.getDecoration(actual.getNodeByIndex(1,0,0,1,0,1));
        FreqMetric decGS1   = decs.getDecoration(actual.getNodeByIndex(1,0,0,1,0,0,0));
        FreqMetric decFRec  = decs.getDecoration(actual.getNodeByIndex(1,0,0,1,0,0,1));
        FreqMetric decGS2   = decs.getDecoration(actual.getNodeByIndex(1,0,0,1,0,0,2));

        Assert.assertEquals("decPre - abs", 2, decPre.getFreqAbsolute());
        Assert.assertEquals("decPre - case", 2, decPre.getFreqCase());

        Assert.assertEquals("decPost - abs", 2, decPost.getFreqAbsolute());
        Assert.assertEquals("decPost - case", 2, decPost.getFreqCase());

        Assert.assertEquals("decSeq1 - abs", 2, decSeq1.getFreqAbsolute());
        Assert.assertEquals("decSeq1 - case", 2, decSeq1.getFreqCase());

        //Assert.assertEquals("decFDecl - abs", 4, decFDecl.getFreqAbsolute());
        Assert.assertEquals("decFDecl - abs", 2, decFDecl.getFreqAbsolute());
        Assert.assertEquals("decFDecl - case", 2, decFDecl.getFreqCase());

        Assert.assertEquals("decXor1 - abs", 4, decXor1.getFreqAbsolute());
        Assert.assertEquals("decXor1 - case", 2, decXor1.getFreqCase());

        Assert.assertEquals("decSeq3 - abs", 3, decSeq3.getFreqAbsolute());
        Assert.assertEquals("decSeq3 - case", 2, decSeq3.getFreqCase());

        Assert.assertEquals("decFB - abs", 1, decFB.getFreqAbsolute());
        Assert.assertEquals("decFB - case", 1, decFB.getFreqCase());

        Assert.assertEquals("decFS1 - abs", 3, decFS1.getFreqAbsolute());
        Assert.assertEquals("decFS1 - case", 2, decFS1.getFreqCase());

        Assert.assertEquals("decGDecl - abs", 3, decGDecl.getFreqAbsolute());
        Assert.assertEquals("decGDecl - case", 2, decGDecl.getFreqCase());

        Assert.assertEquals("decFS2 - abs", 3, decFS2.getFreqAbsolute());
        Assert.assertEquals("decFS2 - case", 2, decFS2.getFreqCase());

        Assert.assertEquals("decXor2 - abs", 3, decXor2.getFreqAbsolute());
        Assert.assertEquals("decXor2 - case", 2, decXor2.getFreqCase());
        
        Assert.assertEquals("decSeq5 - abs", 2, decSeq5.getFreqAbsolute());
        Assert.assertEquals("decSeq5 - case", 2, decSeq5.getFreqCase());

        Assert.assertEquals("decGB - abs", 1, decGB.getFreqAbsolute());
        Assert.assertEquals("decGB - case", 1, decGB.getFreqCase());

        Assert.assertEquals("decGS1 - abs", 2, decGS1.getFreqAbsolute());
        Assert.assertEquals("decGS1 - case", 2, decGS1.getFreqCase());

        Assert.assertEquals("decFRec - abs", 2, decFRec.getFreqAbsolute());
        Assert.assertEquals("decFRec - case", 2, decFRec.getFreqCase());

        Assert.assertEquals("decGS2 - abs", 2, decGS2.getFreqAbsolute());
        Assert.assertEquals("decGS2 - case", 2, decGS2.getFreqCase());
    }
    
    @Test
    public void testNestedLifecycleLoopLeaf() {
        XLog inputLC = LogCreateTestUtil.createLogFlat(new String[][] {
                { "X_start", "Y_start", 
                    "X_start", 
                        "Z_start", "Z_complete", "Z_start", "Z_complete", 
                    "X_complete", 
                  "Y_complete", "X_complete"
            }
        });

        IEPTree expected = EPTreeCreateUtil.create(
            "\\/=X(x(\\/=Y(R\\/=X), <->(Z, tau)))"
        );

        L2LSubtraceNestedCalls l2l = new L2LSubtraceNestedCalls();
        XLog inputHierarchy = l2l.transform(inputLC);

        DiscoverEPTreeRecursion.Parameters params = new DiscoverEPTreeRecursion.Parameters();
        params.pathThreshold = 1.0;
        DiscoverEPTreeRecursion disc = new DiscoverEPTreeRecursion(params);
        IEPTree actual = disc.discover(inputHierarchy);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
    }
    
    @Test
    public void testNestedLifecycleLoopNode() {
        XLog inputLC = LogCreateTestUtil.createLogFlat(new String[][] {
            { "X_start", "Y_start", 
                "X_start", "Z_start", "Z_complete", "X_complete", 
                "X_start", "Z_start", "Z_complete", "X_complete",
              "Y_complete", "X_complete"
            }
        });

        IEPTree expected = EPTreeCreateUtil.create(
            "\\/=X(x(\\/=Y(<->(R\\/=X, tau)), Z))"
        );

        L2LSubtraceNestedCalls l2l = new L2LSubtraceNestedCalls();
        XLog inputHierarchy = l2l.transform(inputLC);

        DiscoverEPTreeRecursion.Parameters params = new DiscoverEPTreeRecursion.Parameters();
        params.pathThreshold = 1.0;
        DiscoverEPTreeRecursion disc = new DiscoverEPTreeRecursion(params);
        IEPTree actual = disc.discover(inputHierarchy);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
    }

    @Test
    public void testNestedLifecycleLoopRoot() {
        XLog inputLC = LogCreateTestUtil.createLogFlat(new String[][] {
            { "X_start", "Y_start", 
                "X_start", "Z_start", "Z_complete", "X_complete", 
              "Y_complete", "X_complete",
              "X_start", "Y_start", 
                "X_start", "Z_start", "Z_complete", "X_complete",
              "Y_complete", "X_complete"
            }
        });

        IEPTree expected = EPTreeCreateUtil.create(
            "<->(\\/=X(x(\\/=Y(R\\/=X), Z)), tau)"
        );

        L2LSubtraceNestedCalls l2l = new L2LSubtraceNestedCalls();
        XLog inputHierarchy = l2l.transform(inputLC);

        DiscoverEPTreeRecursion.Parameters params = new DiscoverEPTreeRecursion.Parameters();
        params.pathThreshold = 1.0;
        DiscoverEPTreeRecursion disc = new DiscoverEPTreeRecursion(params);
        IEPTree actual = disc.discover(inputHierarchy);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
    }
    
    @Test
    public void testNestedHalf() {
        XLog inputLC = LogCreateTestUtil.createLogFlat(new String[][] {
            { "X_start", "X_complete", "Y_start", "Y_complete", 
              "X_start", "Z_start", "Z_complete", "X_complete", "Y_start", "Y_complete",
              "X_start", "X_complete"
            }
        });
        
        IEPTree expected = EPTreeCreateUtil.create(
            "<->(\\/=X(x(Z, tau)), Y)"
        );

        L2LSubtraceNestedCalls l2l = new L2LSubtraceNestedCalls();
        XLog inputHierarchy = l2l.transform(inputLC);

        DiscoverEPTreeRecursion.Parameters params = new DiscoverEPTreeRecursion.Parameters();
        params.pathThreshold = 1.0;
        DiscoverEPTreeRecursion disc = new DiscoverEPTreeRecursion(params);
        IEPTree actual = disc.discover(inputHierarchy);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
    }
    
    @Test
    public void testNestedHalf2() {
        XLog inputLC = LogCreateTestUtil.createLogFlat(new String[][] {
            { "X_start", "X_complete", "Y_start", "Y_complete", 
              "X_start", "Z_start", "Z2_start", "Z2_complete", "Z_complete", "X_complete", "Y_start", "Y_complete",
              "X_start", "X_complete"
            }
        });
        
        IEPTree expected = EPTreeCreateUtil.create(
            "<->(\\/=X(x(\\/=Z(Z2), tau)), Y)"
        );

        L2LSubtraceNestedCalls l2l = new L2LSubtraceNestedCalls();
        XLog inputHierarchy = l2l.transform(inputLC);

        DiscoverEPTreeRecursion.Parameters params = new DiscoverEPTreeRecursion.Parameters();
        params.pathThreshold = 1.0;
        DiscoverEPTreeRecursion disc = new DiscoverEPTreeRecursion(params);
        IEPTree actual = disc.discover(inputHierarchy);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
    }
    
    @Test
    public void testNestedHalf3() {
        XLog inputLC = LogCreateTestUtil.createLogFlat(new String[][] {
            { "X_start", "Z_start", "Z2_start", "X_start", "X_complete", "Z2_complete", "Z_complete", "X_complete"
            }
        });
        
        IEPTree expected = EPTreeCreateUtil.create(
            "\\/=X(x(\\/=Z(\\/=Z2(R\\/=X)), tau))"
        );

        L2LSubtraceNestedCalls l2l = new L2LSubtraceNestedCalls();
        XLog inputHierarchy = l2l.transform(inputLC);

        DiscoverEPTreeRecursion.Parameters params = new DiscoverEPTreeRecursion.Parameters();
        params.pathThreshold = 1.0;
        DiscoverEPTreeRecursion disc = new DiscoverEPTreeRecursion(params);
        IEPTree actual = disc.discover(inputHierarchy);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
    }
}
