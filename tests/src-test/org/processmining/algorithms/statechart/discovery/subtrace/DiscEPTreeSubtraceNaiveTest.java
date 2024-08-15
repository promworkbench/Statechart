package org.processmining.algorithms.statechart.discovery.subtrace;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.discovery.DiscoverEPTreeNaive;
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

public class DiscEPTreeSubtraceNaiveTest {

    protected static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();
    
    @Test
    public void testEmptyLog() {
        // BaseCaseFinderIM -> empty log
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
                
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("tau");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
        
        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric dec = decs.getDecoration(actual.getRoot());
        
        Assert.assertEquals("root - abs", 0, dec.getFreqAbsolute());
        Assert.assertEquals("root - case", 0, dec.getFreqCase());
    }

    @Test
    public void testEmptyString() {
        // BaseCaseFinderIM -> empty trace
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {} 
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("tau");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
        
        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric dec = decs.getDecoration(actual.getRoot());
        
        Assert.assertEquals("root - abs", 1, dec.getFreqAbsolute());
        Assert.assertEquals("root - case", 1, dec.getFreqCase());
    }

    @Test
    public void testSingleAct() {
        // BaseCaseFinderIM -> single activity
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            { "A_start", "A_complete"  } 
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("A");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
        
        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric dec = decs.getDecoration(actual.getRoot());
        
        Assert.assertEquals("root - abs", 1, dec.getFreqAbsolute());
        Assert.assertEquals("root - case", 1, dec.getFreqCase());
    }

    @Test
    public void testXorActTau() {
        // BaseCaseFinderIM -> xor(tau, ...)
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete"  } ,
            {} 
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("x(tau, A)");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
        
        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decXor = decs.getDecoration(actual.getRoot());
        FreqMetric decTau = decs.getDecoration(actual.getNodeByLabel("tau"));
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));

        Assert.assertEquals("xor - abs", 2, decXor.getFreqAbsolute());
        Assert.assertEquals("xor - case", 2, decXor.getFreqCase());
        
        Assert.assertEquals("tau - abs", 1, decTau.getFreqAbsolute());
        Assert.assertEquals("tau - case", 1, decTau.getFreqCase());
        
        Assert.assertEquals("A - abs", 1, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 1, decA.getFreqCase());
    }

    @Test
    public void testSequence() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete" }
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("->(A, B)");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
        
        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decSeq = decs.getDecoration(actual.getRoot());
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decB = decs.getDecoration(actual.getNodeByLabel("B"));

        Assert.assertEquals("seq - abs", 1, decSeq.getFreqAbsolute());
        Assert.assertEquals("seq - case", 1, decSeq.getFreqCase());
        
        Assert.assertEquals("A - abs", 1, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 1, decA.getFreqCase());
        
        Assert.assertEquals("B - abs", 1, decB.getFreqAbsolute());
        Assert.assertEquals("B - case", 1, decB.getFreqCase());
    }
    
    @Test
    public void testShortLoop() {
        // BaseCaseFinderIM -> single activity in semi-flower model
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "A_start", "A_complete", "A_start", "A_complete"  } ,
            { "A_start", "A_complete", "A_start", "A_complete", "A_start", "A_complete"  } ,
            { "A_start", "A_complete", "A_start", "A_complete", "A_start", "A_complete"  } ,
            { "A_start", "A_complete"  } ,
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("<->(A, tau)");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
        
        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decLoop = decs.getDecoration(actual.getRoot());
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decTau = decs.getDecoration(actual.getNodeByLabel("tau"));

        Assert.assertEquals("loop - abs", 4, decLoop.getFreqAbsolute());
        Assert.assertEquals("loop - case", 4, decLoop.getFreqCase());
        
        Assert.assertEquals("A - abs", 10, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 4, decA.getFreqCase());
        
        Assert.assertEquals("tau - abs", 6, decTau.getFreqAbsolute());
        Assert.assertEquals("tau - case", 3, decTau.getFreqCase());
        
    }

    @Test
    public void testShortLoopAB() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "A_start", "A_complete"  }
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
        IEPTree expected = EPTreeCreateUtil.create("<->(A, B)");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
        
        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decLoop = decs.getDecoration(actual.getRoot());
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decB = decs.getDecoration(actual.getNodeByLabel("B"));

        Assert.assertEquals("loop - abs", 1, decLoop.getFreqAbsolute());
        Assert.assertEquals("loop - case", 1, decLoop.getFreqCase());
        
        Assert.assertEquals("A - abs", 2, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 1, decA.getFreqCase());
        
        Assert.assertEquals("B - abs", 1, decB.getFreqAbsolute());
        Assert.assertEquals("B - case", 1, decB.getFreqCase());
        
    }

    @Test
    public void testShortLoopSkip() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete"  } ,
            { "C_start", "C_complete", "D_start", "D_complete"  } ,
            { "A_start", "A_complete", "D_start", "D_complete", "B_start", "B_complete"  } ,
            { "A_start", "A_complete", "D_start", "D_complete"  } ,
            { "C_start", "C_complete", "G_start", "G_complete"  } ,
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
        IEPTree expected = EPTreeCreateUtil.create("<->(x(D, C, B, A, G), tau)");

        DiscoverEPTreeNaive.Parameters params = new DiscoverEPTreeNaive.Parameters();
        params.pathThreshold = 1.0;
        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive(params);
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
        
        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decLoop = decs.getDecoration(actual.getRoot());
        FreqMetric decXor = decs.getDecoration(actual.getNodeByIndex(0));
        FreqMetric decTau = decs.getDecoration(actual.getNodeByLabel("tau"));
        FreqMetric decD = decs.getDecoration(actual.getNodeByLabel("D"));
        FreqMetric decC = decs.getDecoration(actual.getNodeByLabel("C"));
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));

        Assert.assertEquals("loop - abs", 5, decLoop.getFreqAbsolute());
        Assert.assertEquals("loop - case", 5, decLoop.getFreqCase());

        Assert.assertEquals("xor - abs", 11, decXor.getFreqAbsolute());
        Assert.assertEquals("xor - case", 5, decXor.getFreqCase());
        
        Assert.assertEquals("tau - abs", 6, decTau.getFreqAbsolute());
        Assert.assertEquals("tau - case", 5, decTau.getFreqCase());
        
        Assert.assertEquals("D - abs", 3, decD.getFreqAbsolute());
        Assert.assertEquals("D - case", 3, decD.getFreqCase());
        
        Assert.assertEquals("C - abs", 2, decC.getFreqAbsolute());
        Assert.assertEquals("C - case", 2, decC.getFreqCase());
        
        Assert.assertEquals("A - abs", 3, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 3, decA.getFreqCase());
        
    }

    @Test
    public void testLoop() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete",
                "A_start", "A_complete", "B_start", "B_complete", "D_start", "D_complete"  
                }
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil
                .create("->(<->(->(A, B), C), D)");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));

        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(
                EPTreeFreqMetricDecorator.class);
        FreqMetric decSeq1 = decs.getDecoration(actual.getRoot());
        FreqMetric decLoop = decs.getDecoration(actual.getNodeByIndex(0));
        FreqMetric decSeq2 = decs.getDecoration(actual.getNodeByIndex(0, 0));
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decB = decs.getDecoration(actual.getNodeByLabel("B"));
        FreqMetric decC = decs.getDecoration(actual.getNodeByLabel("C"));
        FreqMetric decD = decs.getDecoration(actual.getNodeByLabel("D"));

        Assert.assertEquals("seq 1 - abs", 1, decSeq1.getFreqAbsolute());
        Assert.assertEquals("seq 1 - case", 1, decSeq1.getFreqCase());

        Assert.assertEquals("loop - abs", 1, decLoop.getFreqAbsolute());
        Assert.assertEquals("loop - case", 1, decLoop.getFreqCase());

        Assert.assertEquals("seq 2 - abs", 2, decSeq2.getFreqAbsolute());
        Assert.assertEquals("seq 2 - case", 1, decSeq2.getFreqCase());

        Assert.assertEquals("A - abs", 2, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 1, decA.getFreqCase());

        Assert.assertEquals("B - abs", 2, decB.getFreqAbsolute());
        Assert.assertEquals("B - case", 1, decB.getFreqCase());

        Assert.assertEquals("C - abs", 1, decC.getFreqAbsolute());
        Assert.assertEquals("C - case", 1, decC.getFreqCase());

        Assert.assertEquals("D - abs", 1, decD.getFreqAbsolute());
        Assert.assertEquals("D - case", 1, decD.getFreqCase());
    }

    @Test
    public void testParallelOverlap() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "B_start", "A_complete", "B_complete" }
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("/\\(A, B)");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
        
        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decSeq = decs.getDecoration(actual.getRoot());
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decB = decs.getDecoration(actual.getNodeByLabel("B"));

        Assert.assertEquals("seq - abs", 1, decSeq.getFreqAbsolute());
        Assert.assertEquals("seq - case", 1, decSeq.getFreqCase());
        
        Assert.assertEquals("A - abs", 1, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 1, decA.getFreqCase());
        
        Assert.assertEquals("B - abs", 1, decB.getFreqAbsolute());
        Assert.assertEquals("B - case", 1, decB.getFreqCase());
    }
    
    @Test
    public void testInfrequent() {
        final int freqCount = 9;
        final int infreqCount = 1;
        final int freqTotal = freqCount + infreqCount;
        
        String[] traceFreq = new String[] {
            "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete",
        };
        String[] traceInfreq = new String[] {
            "A_start", "A_complete", "C_start", "C_complete",
        };
        
        String[][] log = new String[freqTotal][];
        
        for (int i = 0; i < freqCount; i++) {
            log[i] = traceFreq;
        }
        for (int i = freqCount; i < freqTotal; i++) {
            log[i] = traceInfreq;
        }
        
        XLog input = LogCreateTestUtil.createLogFlat(log);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, B, C)");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));

        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(
                EPTreeFreqMetricDecorator.class);
        FreqMetric decSeq1 = decs.getDecoration(actual.getRoot());
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decB = decs.getDecoration(actual.getNodeByLabel("B"));
        FreqMetric decC = decs.getDecoration(actual.getNodeByLabel("C"));

        Assert.assertEquals("seq 1 - abs", freqTotal, decSeq1.getFreqAbsolute());
        Assert.assertEquals("seq 1 - case", freqTotal, decSeq1.getFreqCase());

        Assert.assertEquals("A - abs", freqTotal, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", freqTotal, decA.getFreqCase());

        Assert.assertEquals("B - abs", freqCount, decB.getFreqAbsolute());
        Assert.assertEquals("B - case", freqCount, decB.getFreqCase());

        Assert.assertEquals("C - abs", freqTotal, decC.getFreqAbsolute());
        Assert.assertEquals("C - case", freqTotal, decC.getFreqCase());
    }

    @Test
    public void testInfrequent2() { // Xixi
        final int freqCount = 9;
        final int infreqCount = 1;
        final int freqTotal = freqCount + infreqCount;
        
        String[] traceFreq = new String[] {
            "A_start", "A_complete", "C_start", "C_complete",
        };
        String[] traceInfreq = new String[] {
            "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete",
        };
        
        String[][] log = new String[freqTotal][];
        
        for (int i = 0; i < freqCount; i++) {
            log[i] = traceFreq;
        }
        for (int i = freqCount; i < freqTotal; i++) {
            log[i] = traceInfreq;
        }
        
        XLog input = LogCreateTestUtil.createLogFlat(log);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, x(tau, B), C)");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));

        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(
                EPTreeFreqMetricDecorator.class);
        FreqMetric decSeq1 = decs.getDecoration(actual.getRoot());
        FreqMetric decSeq2 = decs.getDecoration(actual.getNodeByIndex(1));
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decB = decs.getDecoration(actual.getNodeByLabel("B"));
        FreqMetric decTau = decs.getDecoration(actual.getNodeByLabel("tau"));
        FreqMetric decC = decs.getDecoration(actual.getNodeByLabel("C"));

        Assert.assertEquals("seq 1 - abs", freqTotal, decSeq1.getFreqAbsolute());
        Assert.assertEquals("seq 1 - case", freqTotal, decSeq1.getFreqCase());

        Assert.assertEquals("seq 2 - abs", freqTotal, decSeq2.getFreqAbsolute());
        Assert.assertEquals("seq 2 - case", freqTotal, decSeq2.getFreqCase());

        Assert.assertEquals("A - abs", freqTotal, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", freqTotal, decA.getFreqCase());

        Assert.assertEquals("B - abs", infreqCount, decB.getFreqAbsolute());
        Assert.assertEquals("B - case", infreqCount, decB.getFreqCase());

        Assert.assertEquals("tau - abs", freqCount, decTau.getFreqAbsolute());
        Assert.assertEquals("tau - case", freqCount, decTau.getFreqCase());

        Assert.assertEquals("C - abs", freqTotal, decC.getFreqAbsolute());
        Assert.assertEquals("C - case", freqTotal, decC.getFreqCase());
    }
    

    @Test
    public void testInfrequent3() { // Felix
        final int freqCount = 9;
        final int infreqCount = 1;
        final int freqTotal = freqCount + infreqCount;
        
        String[] traceFreq = new String[] {
            "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete",
        };
        String[] traceInfreq = new String[] {
            "B_start", "B_complete", "A_start", "A_complete", "C_start", "C_complete",
        };
        
        String[][] log = new String[freqTotal][];
        
        for (int i = 0; i < freqCount; i++) {
            log[i] = traceFreq;
        }
        for (int i = freqCount; i < freqTotal; i++) {
            log[i] = traceInfreq;
        }
        
        XLog input = LogCreateTestUtil.createLogFlat(log);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil
                .create("->(I/\\(A, B), C)");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));

        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(
                EPTreeFreqMetricDecorator.class);
        FreqMetric decSeq1 = decs.getDecoration(actual.getRoot());
        FreqMetric decSeq2 = decs.getDecoration(actual.getNodeByIndex(1));
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decB = decs.getDecoration(actual.getNodeByLabel("B"));
        FreqMetric decC = decs.getDecoration(actual.getNodeByLabel("C"));

        Assert.assertEquals("seq 1 - abs", freqTotal, decSeq1.getFreqAbsolute());
        Assert.assertEquals("seq 1 - case", freqTotal, decSeq1.getFreqCase());

        Assert.assertEquals("seq 2 - abs", freqTotal, decSeq2.getFreqAbsolute());
        Assert.assertEquals("seq 2 - case", freqTotal, decSeq2.getFreqCase());

        Assert.assertEquals("A - abs", freqTotal, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", freqTotal, decA.getFreqCase());

        Assert.assertEquals("B - abs", freqTotal, decB.getFreqAbsolute());
        Assert.assertEquals("B - case", freqTotal, decB.getFreqCase());

        Assert.assertEquals("C - abs", freqTotal, decC.getFreqAbsolute());
        Assert.assertEquals("C - case", freqTotal, decC.getFreqCase());
    }
    
    @Test
    public void testPairLoop() {
        // Triggers FallThroughTauLoop
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete",
            "A_start", "A_complete", "B_start", "B_complete",
            "A_start", "A_complete", "B_start", "B_complete" }
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("<->(->(A, B), tau)");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );

        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decLoop = decs.getDecoration(actual.getRoot());
        FreqMetric decSeq = decs.getDecoration(actual.getNodeByIndex(0));
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decTau = decs.getDecoration(actual.getNodeByLabel("tau"));

        Assert.assertEquals("loop - abs", 1, decLoop.getFreqAbsolute());
        Assert.assertEquals("loop - case", 1, decLoop.getFreqCase());

        Assert.assertEquals("seq - abs", 3, decSeq.getFreqAbsolute());
        Assert.assertEquals("seq - case", 1, decSeq.getFreqCase());
        
        Assert.assertEquals("A - abs", 3, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 1, decA.getFreqCase());
        
        Assert.assertEquals("tau - abs", 2, decTau.getFreqAbsolute());
        Assert.assertEquals("tau - case", 1, decTau.getFreqCase());
    }
    
    @Test
    public void testActConcurrent() {
        // FallThroughActivityOncePerTraceConcurrent
        
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete" },
            { "B_start", "B_complete", "C_start", "C_complete", "A_start", "A_complete" }
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("I/\\(A, ->(B, C))");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );

        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decAnd1 = decs.getDecoration(actual.getRoot());
        FreqMetric decAnd2 = decs.getDecoration(actual.getNodeByIndex(1));
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decB = decs.getDecoration(actual.getNodeByLabel("B"));
        FreqMetric decC = decs.getDecoration(actual.getNodeByLabel("C"));

        Assert.assertEquals("and 1 - abs", 2, decAnd1.getFreqAbsolute());
        Assert.assertEquals("and 1 - case", 2, decAnd1.getFreqCase());
        
        Assert.assertEquals("and 2 - abs", 2, decAnd2.getFreqAbsolute());
        Assert.assertEquals("and 2 - case", 2, decAnd2.getFreqCase());
        
        Assert.assertEquals("A - abs", 2, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 2, decA.getFreqCase());

        Assert.assertEquals("B - abs", 2, decB.getFreqAbsolute());
        Assert.assertEquals("B - case", 2, decB.getFreqCase());

        Assert.assertEquals("C - abs", 2, decC.getFreqAbsolute());
        Assert.assertEquals("C - case", 2, decC.getFreqCase());
    }
    
    @Test
    public void testSimpleNesting() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete" },
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] {
            "X_start", "X_complete"
        });
        extSubtrace.assignSubtrace(input.get(0).get(2), sub1);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("->(A, \\/=B(X), C)");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );

        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decSeq1 = decs.getDecoration(actual.getRoot());
        FreqMetric decSeq2 = decs.getDecoration(actual.getNodeByIndex(1));
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decB = decs.getDecoration(actual.getNodeByLabel("B"));
        FreqMetric decX = decs.getDecoration(actual.getNodeByLabel("X"));
        FreqMetric decC = decs.getDecoration(actual.getNodeByLabel("C"));

        Assert.assertEquals("seq 1 - abs", 1, decSeq1.getFreqAbsolute());
        Assert.assertEquals("seq 1 - case", 1, decSeq1.getFreqCase());
        
        Assert.assertEquals("seq 2 - abs", 1, decSeq2.getFreqAbsolute());
        Assert.assertEquals("seq 2 - case", 1, decSeq2.getFreqCase());
        
        Assert.assertEquals("A - abs", 1, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 1, decA.getFreqCase());
        
        Assert.assertEquals("B - abs", 1, decB.getFreqAbsolute());
        Assert.assertEquals("B - case", 1, decB.getFreqCase());

        Assert.assertEquals("X - abs", 1, decX.getFreqAbsolute());
        Assert.assertEquals("X - case", 1, decX.getFreqCase());

        Assert.assertEquals("C - abs", 1, decC.getFreqAbsolute());
        Assert.assertEquals("C - case", 1, decC.getFreqCase());
    }

    @Test
    public void testSimpleNestingSeq() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete" },
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] {
            "X_start", "X_complete", "Y_start", "Y_complete"
        });
        extSubtrace.assignSubtrace(input.get(0).get(2), sub1);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("->(A, \\/=B(->(X, Y)), C)");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );

        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decSeq1 = decs.getDecoration(actual.getRoot());
        FreqMetric decSeq2 = decs.getDecoration(actual.getNodeByIndex(1));
        FreqMetric decSeq3 = decs.getDecoration(actual.getNodeByIndex(1, 0, 0));
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decB = decs.getDecoration(actual.getNodeByLabel("B"));
        FreqMetric decX = decs.getDecoration(actual.getNodeByLabel("X"));
        FreqMetric decY = decs.getDecoration(actual.getNodeByLabel("Y"));
        FreqMetric decC = decs.getDecoration(actual.getNodeByLabel("C"));

        Assert.assertEquals("seq 1 - abs", 1, decSeq1.getFreqAbsolute());
        Assert.assertEquals("seq 1 - case", 1, decSeq1.getFreqCase());
        
        Assert.assertEquals("seq 2 - abs", 1, decSeq2.getFreqAbsolute());
        Assert.assertEquals("seq 2 - case", 1, decSeq2.getFreqCase());

        Assert.assertEquals("seq 3 - abs", 1, decSeq3.getFreqAbsolute());
        Assert.assertEquals("seq 3 - case", 1, decSeq3.getFreqCase());
        
        Assert.assertEquals("A - abs", 1, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 1, decA.getFreqCase());
        
        Assert.assertEquals("B - abs", 1, decB.getFreqAbsolute());
        Assert.assertEquals("B - case", 1, decB.getFreqCase());

        Assert.assertEquals("X - abs", 1, decX.getFreqAbsolute());
        Assert.assertEquals("X - case", 1, decX.getFreqCase());

        Assert.assertEquals("Y - abs", 1, decY.getFreqAbsolute());
        Assert.assertEquals("Y - case", 1, decY.getFreqCase());
        
        Assert.assertEquals("C - abs", 1, decC.getFreqAbsolute());
        Assert.assertEquals("C - case", 1, decC.getFreqCase());
    }
    @Test
    public void testSimpleNestingRecurse() {
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

        IEPTree expected = EPTreeCreateUtil.create("->(A, \\/=B(->(X, \\/=B(Y))), C)");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );

        EPTreeFreqMetricDecorator decs = actual.getDecorations().getForType(EPTreeFreqMetricDecorator.class);
        FreqMetric decSeq1 = decs.getDecoration(actual.getRoot());
        FreqMetric decSeq2 = decs.getDecoration(actual.getNodeByIndex(1));
        FreqMetric decSeq3 = decs.getDecoration(actual.getNodeByIndex(1,0,0));
        FreqMetric decA = decs.getDecoration(actual.getNodeByLabel("A"));
        FreqMetric decB1 = decs.getDecoration(actual.getNodeByIndex(1,0));
        FreqMetric decB2 = decs.getDecoration(actual.getNodeByIndex(1,0,1));
        FreqMetric decX = decs.getDecoration(actual.getNodeByLabel("X"));
        FreqMetric decY = decs.getDecoration(actual.getNodeByLabel("Y"));
        FreqMetric decC = decs.getDecoration(actual.getNodeByLabel("C"));

        Assert.assertEquals("seq 1 - abs", 1, decSeq1.getFreqAbsolute());
        Assert.assertEquals("seq 1 - case", 1, decSeq1.getFreqCase());
        
        Assert.assertEquals("seq 2 - abs", 1, decSeq2.getFreqAbsolute());
        Assert.assertEquals("seq 2 - case", 1, decSeq2.getFreqCase());
        
        Assert.assertEquals("seq 3 - abs", 1, decSeq3.getFreqAbsolute());
        Assert.assertEquals("seq 3 - case", 1, decSeq3.getFreqCase());
        
        Assert.assertEquals("A - abs", 1, decA.getFreqAbsolute());
        Assert.assertEquals("A - case", 1, decA.getFreqCase());
        
        Assert.assertEquals("decB1 - abs", 1, decB1.getFreqAbsolute());
        Assert.assertEquals("decB1 - case", 1, decB1.getFreqCase());

        Assert.assertEquals("decB2 - abs", 1, decB2.getFreqAbsolute());
        Assert.assertEquals("decB2 - case", 1, decB2.getFreqCase());
        
        Assert.assertEquals("X - abs", 1, decX.getFreqAbsolute());
        Assert.assertEquals("X - case", 1, decX.getFreqCase());

        Assert.assertEquals("Y - abs", 1, decY.getFreqAbsolute());
        Assert.assertEquals("Y - case", 1, decY.getFreqCase());
        
        Assert.assertEquals("C - abs", 1, decC.getFreqAbsolute());
        Assert.assertEquals("C - case", 1, decC.getFreqCase());
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

        IEPTree expected = EPTreeCreateUtil.create("\\/=Main(->(\\/=S1(->(A, B)), \\/=S2(->(C, D))))");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
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
            "\\/=X(\\/=Y(\\/=X(<->(Z, tau))))"
        );
        
        L2LSubtraceNestedCalls l2l = new L2LSubtraceNestedCalls();
        XLog inputHierarchy = l2l.transform(inputLC);

        DiscoverEPTreeNaive.Parameters params = new DiscoverEPTreeNaive.Parameters();
        params.pathThreshold = 1.0;
        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive(params);
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
            "\\/=X(\\/=Y(<->(\\/=X(Z), tau)))"
        );
        
        L2LSubtraceNestedCalls l2l = new L2LSubtraceNestedCalls();
        XLog inputHierarchy = l2l.transform(inputLC);

        DiscoverEPTreeNaive.Parameters params = new DiscoverEPTreeNaive.Parameters();
        params.pathThreshold = 1.0;
        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive(params);
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
            "<->(\\/=X(\\/=Y(\\/=X(Z))), tau)"
        );
        
        L2LSubtraceNestedCalls l2l = new L2LSubtraceNestedCalls();
        XLog inputHierarchy = l2l.transform(inputLC);

        DiscoverEPTreeNaive.Parameters params = new DiscoverEPTreeNaive.Parameters();
        params.pathThreshold = 1.0;
        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive(params);
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

        DiscoverEPTreeNaive.Parameters params = new DiscoverEPTreeNaive.Parameters();
        params.pathThreshold = 1.0;
        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive(params);
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

        DiscoverEPTreeNaive.Parameters params = new DiscoverEPTreeNaive.Parameters();
        params.pathThreshold = 1.0;
        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive(params);
        IEPTree actual = disc.discover(inputHierarchy);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
    }
    
    @Test
    public void testInterleavedOff() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete",
              "X_start", "X_complete", "Y_start", "Y_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete",
              "Y_start", "Y_complete", "X_start", "X_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("->(A, B, C, I/\\(X, Y))");

        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive();
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
}
