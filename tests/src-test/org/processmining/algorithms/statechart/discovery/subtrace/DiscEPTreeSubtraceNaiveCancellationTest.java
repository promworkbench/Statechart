package org.processmining.algorithms.statechart.discovery.subtrace;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.discovery.AbstractDiscoverEPTree;
import org.processmining.algorithms.statechart.discovery.DiscoverEPTreeNaiveCancellation;
import org.processmining.algorithms.statechart.discovery.DiscoverEPTreeRecursion;
import org.processmining.algorithms.statechart.discovery.EPTreeCompareSame;
import org.processmining.algorithms.statechart.discovery.im.cancellation.PredicateQueryCancelError;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.models.statechart.eptree.EPTreeCreateUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.models.statechart.im.log.IMLogHierarchySubtraceImpl;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

import com.google.common.base.Predicate;

public class DiscEPTreeSubtraceNaiveCancellationTest {

    public AbstractDiscoverEPTree<?> disc;
    public PredicateQueryCancelError queryCatchError;
    
    public DiscEPTreeSubtraceNaiveCancellationTest() {
        queryCatchError = new PredicateQueryCancelError(new Predicate<String>() {
            @Override
            public boolean apply(String value) {
                return value.startsWith("Error");
            }
        });
        
        DiscoverEPTreeNaiveCancellation.Parameters params = new DiscoverEPTreeNaiveCancellation.Parameters();
        params.queryCatchError = queryCatchError;
        
        this.disc = new DiscoverEPTreeNaiveCancellation(params);
    }

    @Test
    public void testEmptyLog() {
        // BaseCaseFinderIM -> empty log
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
                
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("tau");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testEmptyString() {
        // BaseCaseFinderIM -> empty trace
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {} 
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("tau");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testSingleAct() {
        // BaseCaseFinderIM -> single activity
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            { "A_start", "A_complete"  } 
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("A");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
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

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testXor() {
        // BaseCaseFinderIM -> xor(tau, ...)
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete" },
            { "A_start", "A_complete", "B2_start", "B2_complete", "C2_start", "C2_complete", "D_start", "D_complete" }
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, x(->(B, C), ->(B2, C2)), D)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testSequence() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete" }
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("->(A, B)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testActConcurrent() {
        // FallThroughActivityOncePerTraceConcurrent
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete" },
            { "B_start", "B_complete", "A_start", "A_complete" }
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("I/\\(A, B)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
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

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testParallelOverlap() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "B_start", "A_complete", "B_complete" }
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("/\\(A, B)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + actual, 
            EPTreeCompareSame.same(expected, actual)
        );
    }
    
    @Test
    public void testSimpleTrycatch() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", 
                "Error_start", "Error_complete", "E2_start", "E2_complete", "D_start", "D_complete" }
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
            
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "D" } },
//            { { "A" }, { "B" }, { "Error" }, { "E2" }, { "D" } } 
//        });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(Er=B, C), ->(Error, E2)), D)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testSimpleTrycatch2() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "START_start", "START_complete",
                "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete",
                "END_start", "END_complete" },
            { "START_start", "START_complete",
                "A_start", "A_complete", "Error_start", 
                "Error_complete", "F_start", "F_complete",
                "END_start", "END_complete" },
            { "START_start", "START_complete",
                "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", 
                "Error_start", "Error_complete", "F_start", "F_complete",
                "END_start", "END_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
            
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "START" }, { "A" }, { "B" }, { "C" }, { "D" }, { "END" } },
//            { { "START" }, { "A" }, { "Error" }, { "F" }, { "END" } },
//            { { "START" }, { "A" }, { "B" }, { "C" }, { "Error" }, { "F" }, { "END" } }
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(START, SC(->(Er=A, B, Er=C, D), ->(Error, F)), END)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testSimpleTrycatch3b() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete", "F_start", "F_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete", 
                "E_start", "E_complete", "B_start", "B_complete", "Error_start", "Error_complete",
                "F_start", "F_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
            
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "D" }, {"F"} },
//            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" }, { "B" }, { "Error" }, {"F"} },
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(<->(->(Er=B, C, D), E), Error), F)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testSimpleTrycatch3bc() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete", "F_start", "F_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete", 
                "E_start", "E_complete", "B_start", "B_complete", "Error_start", "Error_complete",
                "F_start", "F_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete", 
                "E_start", "E_complete", "B_start", "B_complete", "C_start", "C_complete", "Error_start", "Error_complete",
                "F_start", "F_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "D" }, {"F"} },
//            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" }, { "B" }, { "Error" }, {"F"} },
//            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" }, { "B" }, { "C" }, { "Error" }, {"F"} },
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(<->(->(Er=B, Er=C, D), E), Error), F)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testSimpleTrycatch3c() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete", "F_start", "F_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete", 
                "E_start", "E_complete", "B_start", "B_complete", "C_start", "C_complete", "Error_start", "Error_complete",
                "F_start", "F_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "D" }, {"F"} },
//            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" }, { "B" }, { "C" }, { "Error" }, {"F"} },
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(<->(->(B, Er=C, D), E), Error), F)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testSimpleTrycatch4() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete", "F_start", "F_complete", "G_start", "G_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "D_start", "D_complete", "Error_start", "Error_complete", "G_start", "G_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error_start", "Error_complete", "G_start", "G_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
            
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "D" }, {"F"}, {"G"} },
//            { { "A" }, { "B" }, { "D" }, {"Error"}, {"G"} },
//            { { "A" }, { "B" }, { "Error" }, {"G"} },
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(Er=B, x(tau, C), Er=D, F), Error), G)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testSimpleTrycatch5() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete" },
            { "A_start", "A_complete", "Error_start", "Error_complete", "F_start", "F_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "Error_start", "Error_complete", "F_start", "F_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
            
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "D" } },
//            { { "A" }, {"Error"}, {"F"} },
//            { { "A" }, { "B" }, { "C" }, { "Error" }, {"F"} },
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("SC(->(Er=A, B, Er=C, D), ->(Error, F))");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern1NestedError() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete", "E_start", "E_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error1_start", "Error1_complete", "F_start", "F_complete", "G_start", "G_complete", "E_start", "E_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete",
                "Error1_start", "Error1_complete", "F_start", "F_complete",
                "Error2_start", "Error2_complete", "H_start", "H_complete", "G_start", 
                "G_complete", "E_start", "E_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
            
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" } },
//            { { "A" }, { "B" }, { "Error1" }, { "F" }, { "G" }, { "E" } },
//            { { "A" }, { "B" }, { "C" }, { "D" }, { "Error1" }, { "F" }, { "Error2" }, { "H" }, { "G" }, { "E" } },
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(Er=B, C, Er=D), ->(Error1, SC(Er=F, ->(Error2, H)), G)), E)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern2MultiError() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error1_start", "Error1_complete", "E_start", "E_complete", "D_start", "D_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error2_start", "Error2_complete", "F_start", "F_complete", "D_start", "D_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "D" } },
//            { { "A" }, { "B" }, { "Error1" }, { "E" }, { "D" } },
//            { { "A" }, { "B" }, { "Error2" }, { "F" }, { "D" } }, 
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(Er=B, C), x(->(Error1, E), ->(Error2, F))), D)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern3NestedError() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete", "E_start", "E_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error2_start", "Error2_complete", "G_start", "G_complete", "E_start", "E_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete",
                "Error1_start", "Error1_complete", "F_start", "F_complete", "D_start", "D_complete",
                "Error2_start", "Error2_complete", "G_start", "G_complete", "E_start", "E_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" } },
//            { { "A" }, { "B" }, { "Error2" }, { "G" }, { "E" } },
//            { { "A" }, { "B" }, { "C" }, { "Error1" }, { "F" }, { "D" }, { "Error2" }, { "G" }, { "E" } },
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(Er=B, SC(Er=C, ->(Error1, F)), Er=D), ->(Error2, G)), E)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern4MultiError() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete", "E_start", "E_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error1_start", "Error1_complete", "F_start", "F_complete",
                "C_start", "C_complete", "D_start", "D_complete", "E_start", "E_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error2_start", "Error2_complete", "G_start", "G_complete",
                "D_start", "D_complete", "E_start", "E_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" } },
//            { { "A" }, { "B" }, { "Error1" }, { "F" }, { "C" }, { "D" }, { "E" } },
//            { { "A" }, { "B" }, { "Error2" }, { "G" }, { "D" }, { "E" } }, 
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(SC(Er=B, ->(Error1, F)), C), ->(Error2, G)), D, E)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern5ChoiceError() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "E_start", "E_complete", "F_start", "F_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "D_start", "D_complete", "E_start", "E_complete", "F_start", "F_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "D_start", "D_complete", "Error_start", "Error_complete", "G_start", "G_complete", "F_start", "F_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
            
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "E" }, { "F" } },
//            { { "A" }, { "B" }, { "D" }, { "E" }, { "F" } },
//            { { "A" }, { "B" }, { "D" }, { "Error" }, { "G" }, { "F" } }, 
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, B, SC(->(x(Er=D, C), E), ->(Error, G)), F)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern6NestedError() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete", "E_start", "E_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", 
                "Error1_start", "Error1_complete", "F_start", "F_complete",
                "D_start", "D_complete", "E_start", "E_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", 
                "Error2_start", "Error2_complete", "G_start", "G_complete", "E_start", "E_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" } },
//            { { "A" }, { "B" }, { "C" }, { "Error1" },  { "F" }, { "D" }, { "E" } },
//            { { "A" }, { "B" }, { "Error2" }, { "G" }, { "E" } }, 
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(Er=B, SC(Er=C, ->(Error1, F)), D), ->(Error2, G)), E)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern7ParallelError() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete" },
            { "A_start", "A_complete", "C_start", "B_start", "C_complete", "B_complete", "D_start", "D_complete" },
            { "A_start", "A_complete", "C_start", "C_complete", "Error_start", "Error_complete", "E_start", "E_complete", "D_start", "D_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "D" } },
//            { { "A" }, { "C" }, { "B" }, { "D" } },
//            { { "A" }, { "C" }, { "Error" },  { "E" }, { "D" } }, 
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(/\\(Er=C, B), ->(Error, E)), D)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern7ParallelInterleavedError() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete" },
            { "A_start", "A_complete", "C_start", "C_complete", "B_start", "B_complete", "D_start", "D_complete" },
            { "A_start", "A_complete", "C_start", "C_complete", "Error_start", "Error_complete", "E_start", "E_complete", "D_start", "D_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "D" } },
//            { { "A" }, { "C" }, { "B" }, { "D" } },
//            { { "A" }, { "C" }, { "Error" },  { "E" }, { "D" } }, 
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(I/\\(Er=C, B), ->(Error, E)), D)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern8LoopBody() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "B_start", "B_complete", "D_start", "D_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error_start", "Error_complete", "E_start", "E_complete", "D_start", "D_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "B" }, { "D" } },
//            { { "A" }, { "B" }, { "Error" },  { "E" }, { "D" } }, 
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(<->(Er=B, C), ->(Error, E)), D)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern9LoopRedo() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "B_start", "B_complete", "D_start", "D_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "Error_start", "Error_complete", "E_start", "E_complete", "D_start", "D_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "C" }, { "B" }, { "D" } },
//            { { "A" }, { "B" }, { "C" }, { "Error" },  { "E" }, { "D" } },
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(<->(B, Er=C), ->(Error, E)), D)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern10LoopTau() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "B_start", "B_complete", "C_start", "C_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error_start", "Error_complete", "C_start", "C_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "B" }, { "C" } },
//            { { "A" }, { "B" }, { "Error" },  { "C" } }, 
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(<->(Er=B, tau), Error), C)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern11TauLoop() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "B_start", "B_complete", "C_start", "C_complete" },
            { "A_start", "A_complete", "C_start", "C_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error_start", "Error_complete", "C_start", "C_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "B" }, { "C" } },
//            { { "A" }, { "C" } },
//            { { "A" }, { "B" }, { "Error" },  { "C" } }, 
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, x(tau, SC(<->(Er=B, tau), Error)), C)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern12TauLoop() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "B_start", "B_complete", "C_start", "C_complete" },
            { "A_start", "A_complete", "C_start", "C_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error_start", "Error_complete", "C_start", "C_complete" },
            { "A_start", "A_complete", "Error_start", "Error_complete", "C_start", "C_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);
        
//        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
//            { { "A" }, { "B" }, { "B" }, { "C" } },
//            { { "A" }, { "C" } },
//            { { "A" }, { "B" }, { "Error" },  { "C" } }, 
//            { { "A" }, { "Error" },  { "C" } }, 
//         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(SC(->(Er=A, x(tau, <->(Er=B, tau))), Error), C)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    protected static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();

    @Test
    public void testSimpleNesting() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error_start", "Error_complete", "C_start", "C_complete" },
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] {
            "X_start", "X_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] {
            "X_start", "X_complete"
        });
        extSubtrace.assignSubtrace(input.get(0).get(2), sub1);
        extSubtrace.assignSubtrace(input.get(1).get(2), sub2);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(\\/=B(Er=X), Error), C)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testSimpleNesting2() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error_start", "Error_complete", "C_start", "C_complete" },
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] {
            "X_start", "X_complete", "Y_start", "Y_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] {
            "X_start", "X_complete", "Y_start", "Y_complete"
        });
        extSubtrace.assignSubtrace(input.get(0).get(2), sub1);
        extSubtrace.assignSubtrace(input.get(1).get(2), sub2);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(\\/=B(->(X, Er=Y)), Error), C)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testSimpleNesting3() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error_start", "Error_complete", "C_start", "C_complete" },
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] {
            "X_start", "X_complete", "Y_start", "Y_complete"
        });
        XTrace sub2 = LogCreateTestUtil.createTraceFlat(new String[] {
            "X_start", "X_complete"
        });
        extSubtrace.assignSubtrace(input.get(0).get(2), sub1);
        extSubtrace.assignSubtrace(input.get(1).get(2), sub2);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(\\/=B(->(Er=X, Y)), Error), C)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testRecursiveNesting() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "Error_start", "Error_complete", "C_start", "C_complete" },
        });
        XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] {
            "X_start", "X_complete", "B_start", "B_complete"
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
        extSubtrace.assignSubtrace(input.get(0).get(2), sub1);
        extSubtrace.assignSubtrace(sub1.get(2), sub11);
        extSubtrace.assignSubtrace(input.get(1).get(2), sub2);
        extSubtrace.assignSubtrace(sub2.get(2), sub21);
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(\\/=B(x(->(X, R\\/=B), Er=Y)), Error), C)");

        DiscoverEPTreeRecursion.Parameters parameters = new DiscoverEPTreeRecursion.Parameters();
        parameters.useCancelation = true;
        parameters.queryCatchError = this.queryCatchError;
        DiscoverEPTreeRecursion disc = new DiscoverEPTreeRecursion(parameters);
        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
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

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testInterleavedOn1() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete",
              "X_start", "X_complete", "Y_start", "Y_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete",
              "Y_start", "Y_complete", "X_start", "X_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete",
              "Error_start", "Error_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("SC(->(A, B, Er=C, I/\\(X, Y)), Error)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testInterleavedOn2() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] {
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete",
              "X_start", "X_complete", "Y_start", "Y_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete",
              "Y_start", "Y_complete", "X_start", "X_complete" },
            { "A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete",
              "Error_start", "Error_complete" },
            { "A_start", "A_complete", "B_start", "B_complete",
              "Error_start", "Error_complete" },
        });
        IMLogHierarchy inputSubtrace = new IMLogHierarchySubtraceImpl(input);

        IEPTree expected = EPTreeCreateUtil.create("SC(->(A, Er=B, Er=C, I/\\(X, Y)), Error)");

        IEPTree actual = disc.discover(inputSubtrace);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
}
