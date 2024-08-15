package org.processmining.algorithms.statechart.discovery.list;

import org.deckfour.xes.model.XLog;
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

import com.google.common.base.Predicate;

public class DiscEPTreeListNaiveCancellationTest {

    public AbstractDiscoverEPTree<?> disc;
    public PredicateQueryCancelError queryCatchError;
    
    public DiscEPTreeListNaiveCancellationTest() {
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
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {

        });

        IEPTree expected = EPTreeCreateUtil.create("tau");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testEmptyString() {
        // BaseCaseFinderIM -> empty trace
        XLog input = LogCreateTestUtil.createLogList(new String[][][] { 
            {} 
        });

        IEPTree expected = EPTreeCreateUtil.create("tau");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testSingleAct() {
        // BaseCaseFinderIM -> single activity
        XLog input = LogCreateTestUtil.createLogList(new String[][][] { 
                { { "A" } } 
        });

        IEPTree expected = EPTreeCreateUtil.create("A");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testXorActTau() {
        // BaseCaseFinderIM -> xor(tau, ...)
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" } }, 
            {} 
        });

        IEPTree expected = EPTreeCreateUtil.create("x(tau, A)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testXor() {
        // BaseCaseFinderIM -> xor(tau, ...)
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "D" } },
            { { "A" }, { "B2" }, { "C2" }, { "D" } }
        });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, x(->(B, C), ->(B2, C2)), D)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testSequence() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] { {
                { "A" }, { "B" } } });

        IEPTree expected = EPTreeCreateUtil.create("->(A, B)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testActConcurrent() {
        // FallThroughActivityOncePerTraceConcurrent

        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, }, { { "B" }, { "A" } } 
        });

        IEPTree expected = EPTreeCreateUtil.create("/\\(A, B)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testLoop() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] { {
            { "A" }, { "B" }, { "C" }, { "A" }, { "B" }, { "D" } } 
        });

        IEPTree expected = EPTreeCreateUtil
                .create("->(<->(->(A, B), C), D)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testSimpleTrycatch() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "D" } },
            { { "A" }, { "B" }, { "Error" }, { "E2" }, { "D" } } 
        });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(Er=B, C), ->(Error, E2)), D)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testSimpleTrycatch2() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "START" }, { "A" }, { "B" }, { "C" }, { "D" }, { "END" } },
            { { "START" }, { "A" }, { "Error" }, { "F" }, { "END" } },
            { { "START" }, { "A" }, { "B" }, { "C" }, { "Error" }, { "F" }, { "END" } }
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(START, SC(->(Er=A, B, Er=C, D), ->(Error, F)), END)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testSimpleTrycatch3b() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "D" }, {"F"} },
            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" }, { "B" }, { "Error" }, {"F"} },
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(<->(->(Er=B, C, D), E), Error), F)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testSimpleTrycatch3bc() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "D" }, {"F"} },
            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" }, { "B" }, { "Error" }, {"F"} },
            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" }, { "B" }, { "C" }, { "Error" }, {"F"} },
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(<->(->(Er=B, Er=C, D), E), Error), F)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testSimpleTrycatch3c() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "D" }, {"F"} },
            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" }, { "B" }, { "C" }, { "Error" }, {"F"} },
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(<->(->(B, Er=C, D), E), Error), F)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testSimpleTrycatch4() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "D" }, {"F"}, {"G"} },
            { { "A" }, { "B" }, { "D" }, {"Error"}, {"G"} },
            { { "A" }, { "B" }, { "Error" }, {"G"} },
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(Er=B, x(tau, C), Er=D, F), Error), G)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testSimpleTrycatch5() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "D" } },
            { { "A" }, {"Error"}, {"F"} },
            { { "A" }, { "B" }, { "C" }, { "Error" }, {"F"} },
         });

        IEPTree expected = EPTreeCreateUtil
                .create("SC(->(Er=A, B, Er=C, D), ->(Error, F))");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern1NestedError() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" } },
            { { "A" }, { "B" }, { "Error1" }, { "F" }, { "G" }, { "E" } },
            { { "A" }, { "B" }, { "C" }, { "D" }, { "Error1" }, { "F" }, { "Error2" }, { "H" }, { "G" }, { "E" } },
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(Er=B, C, Er=D), ->(Error1, SC(Er=F, ->(Error2, H)), G)), E)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern2MultiError() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "D" } },
            { { "A" }, { "B" }, { "Error1" }, { "E" }, { "D" } },
            { { "A" }, { "B" }, { "Error2" }, { "F" }, { "D" } }, 
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(Er=B, C), x(->(Error1, E), ->(Error2, F))), D)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern3NestedError() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" } },
            { { "A" }, { "B" }, { "Error2" }, { "G" }, { "E" } },
            { { "A" }, { "B" }, { "C" }, { "Error1" }, { "F" }, { "D" }, { "Error2" }, { "G" }, { "E" } },
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(Er=B, SC(Er=C, ->(Error1, F)), Er=D), ->(Error2, G)), E)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern4MultiError() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" } },
            { { "A" }, { "B" }, { "Error1" }, { "F" }, { "C" }, { "D" }, { "E" } },
            { { "A" }, { "B" }, { "Error2" }, { "G" }, { "D" }, { "E" } }, 
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(SC(Er=B, ->(Error1, F)), C), ->(Error2, G)), D, E)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern5ChoiceError() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "E" }, { "F" } },
            { { "A" }, { "B" }, { "D" }, { "E" }, { "F" } },
            { { "A" }, { "B" }, { "D" }, { "Error" }, { "G" }, { "F" } }, 
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, B, SC(->(x(Er=D, C), E), ->(Error, G)), F)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern6NestedError() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "D" }, { "E" } },
            { { "A" }, { "B" }, { "C" }, { "Error1" },  { "F" }, { "D" }, { "E" } },
            { { "A" }, { "B" }, { "Error2" }, { "G" }, { "E" } }, 
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(->(Er=B, SC(Er=C, ->(Error1, F)), D), ->(Error2, G)), E)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern7ParallelError() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "D" } },
            { { "A" }, { "C" }, { "B" }, { "D" } },
            { { "A" }, { "C" }, { "Error" },  { "E" }, { "D" } }, 
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(/\\(Er=C, B), ->(Error, E)), D)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern8LoopBody() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "B" }, { "D" } },
            { { "A" }, { "B" }, { "Error" },  { "E" }, { "D" } }, 
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(<->(Er=B, C), ->(Error, E)), D)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern9LoopRedo() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "C" }, { "B" }, { "D" } },
            { { "A" }, { "B" }, { "C" }, { "Error" },  { "E" }, { "D" } },
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(<->(B, Er=C), ->(Error, E)), D)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern10LoopTau() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "B_2" }, { "C" } },
            { { "A" }, { "B" }, { "Error" },  { "C" } }, 
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(<->(Er=B, tau), Error), C)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern11TauLoop() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "B_2" }, { "C" } },
            { { "A" }, { "C" } },
            { { "A" }, { "B" }, { "Error" },  { "C" } }, 
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, x(tau, SC(<->(Er=B, tau), Error)), C)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPattern12TauLoop() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B" }, { "B_2" }, { "C" } },
            { { "A" }, { "C" } },
            { { "A" }, { "B" }, { "Error" },  { "C" } }, 
            { { "A" }, { "Error" },  { "C" } }, 
         });

        IEPTree expected = EPTreeCreateUtil
                .create("->(SC(->(Er=A, x(tau, <->(Er=B, tau))), Error), C)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testSimpleNesting() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B", "X" }, { "C" } },
            { { "A" }, { "B", "X" }, { "Error" }, { "C" } }, 
        });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(\\/=B(Er=X), Error), C)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testSimpleNesting2() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B", "X" }, { "B", "Y" }, { "C" } },
            { { "A" }, { "B", "X" }, { "B", "Y" }, { "Error" }, { "C" } }, 
        });
        
        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(\\/=B(->(X, Er=Y)), Error), C)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }

    @Test
    public void testSimpleNesting3() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B", "X" }, { "B", "Y" }, { "C" } },
            { { "A" }, { "B", "X" }, { "Error" }, { "C" } }, 
        });
        
        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(\\/=B(->(Er=X, Y)), Error), C)");

        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
    
    @Test
    public void testRecursiveNesting() {
        XLog input = LogCreateTestUtil.createLogList(new String[][][] {
            { { "A" }, { "B", "X" }, { "B", "B", "Y" }, { "C" } },
            { { "A" }, { "B", "X" }, { "B", "B", "Y" }, { "Error" }, { "C" } }, 
        });

        IEPTree expected = EPTreeCreateUtil
                .create("->(A, SC(\\/=B(x(->(X, R\\/=B), Er=Y)), Error), C)");

        DiscoverEPTreeRecursion.Parameters parameters = new DiscoverEPTreeRecursion.Parameters();
        parameters.useCancelation = true;
        parameters.queryCatchError = this.queryCatchError;
        DiscoverEPTreeRecursion disc = new DiscoverEPTreeRecursion(parameters);
        IEPTree actual = disc.discover(input);

        Assert.assertTrue("Expected: " + expected + "; Got: " + actual,
                EPTreeCompareSame.same(expected, actual));
    }
}
