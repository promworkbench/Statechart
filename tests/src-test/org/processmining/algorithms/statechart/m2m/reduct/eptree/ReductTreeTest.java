package org.processmining.algorithms.statechart.m2m.reduct.eptree;

import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.discovery.EPTreeCompareSame;
import org.processmining.algorithms.statechart.m2m.reduct.eptree.ReductionEngine;
import org.processmining.algorithms.statechart.m2m.reduct.eptree.ReductionEngineDefault;
import org.processmining.models.statechart.eptree.EPTreeCreateUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public class ReductTreeTest {

    @Test
    public void testReduceAndOp2() {
        IEPTree subject = EPTreeCreateUtil.create("/\\(A, /\\(B, C))");
        IEPTree expected = EPTreeCreateUtil.create("/\\(A, B, C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceAndOp3() {
        IEPTree subject = EPTreeCreateUtil.create("/\\(A, /\\(/\\(B, C), D))");
        IEPTree expected = EPTreeCreateUtil.create("/\\(A, B, C, D)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceXorOp2() {
        IEPTree subject = EPTreeCreateUtil.create("x(A, x(B, C))");
        IEPTree expected = EPTreeCreateUtil.create("x(A, B, C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceXorOp3() {
        IEPTree subject = EPTreeCreateUtil.create("x(A, x(x(B, C), D))");
        IEPTree expected = EPTreeCreateUtil.create("x(A, B, C, D)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }


    @Test
    public void testReduceSeqOp2() {
        IEPTree subject = EPTreeCreateUtil.create("->(A, ->(B, C))");
        IEPTree expected = EPTreeCreateUtil.create("->(A, B, C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceSeqOp3() {
        IEPTree subject = EPTreeCreateUtil.create("->(A, ->(->(B, C), D))");
        IEPTree expected = EPTreeCreateUtil.create("->(A, B, C, D)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }
    
    @Test
    public void testReduceMixOp() {
        IEPTree subject = EPTreeCreateUtil.create("->(A, ->(B, x(C, x(D, /\\(E, /\\(F, G))))))");
        IEPTree expected = EPTreeCreateUtil.create("->(A, B, x(C, D, /\\(E, F, G)))");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }
    
    @Test
    public void testReduceSeqSingleChild1() {
        IEPTree subject = EPTreeCreateUtil.create("->(A)");
        IEPTree expected = EPTreeCreateUtil.create("A");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceSeqSingleChild1tau() {
        IEPTree subject = EPTreeCreateUtil.create("->(tau)");
        IEPTree expected = EPTreeCreateUtil.create("tau");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }
    
    @Test
    public void testReduceSeqSingleChild2() {
        IEPTree subject = EPTreeCreateUtil.create("x(A, ->(B), C)");
        IEPTree expected = EPTreeCreateUtil.create("x(A, B, C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }
    
    @Test
    public void testReduceXorSingleChild1() {
        IEPTree subject = EPTreeCreateUtil.create("x(A)");
        IEPTree expected = EPTreeCreateUtil.create("A");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }
    
    @Test
    public void testReduceXorSingleChild1tau() {
        IEPTree subject = EPTreeCreateUtil.create("x(tau)");
        IEPTree expected = EPTreeCreateUtil.create("tau");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceXorSingleChild2() {
        IEPTree subject = EPTreeCreateUtil.create("->(A, x(B), C)");
        IEPTree expected = EPTreeCreateUtil.create("->(A, B, C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }
    
    @Test
    public void testReduceAndSingleChild1() {
        IEPTree subject = EPTreeCreateUtil.create("/\\(A)");
        IEPTree expected = EPTreeCreateUtil.create("A");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }
    
    @Test
    public void testReduceAndSingleChild1tau() {
        IEPTree subject = EPTreeCreateUtil.create("/\\(tau)");
        IEPTree expected = EPTreeCreateUtil.create("tau");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceAndSingleChild2() {
        IEPTree subject = EPTreeCreateUtil.create("->(A, /\\(B), C)");
        IEPTree expected = EPTreeCreateUtil.create("->(A, B, C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceSeqTauChild1() {
        IEPTree subject = EPTreeCreateUtil.create("->(A, tau, C)");
        IEPTree expected = EPTreeCreateUtil.create("->(A, C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceSeqTauChild2() {
        IEPTree subject = EPTreeCreateUtil.create("->(tau, tau, A, tau, C, tau)");
        IEPTree expected = EPTreeCreateUtil.create("->(A, C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceAndTauChild1() {
        IEPTree subject = EPTreeCreateUtil.create("/\\(A, tau, C)");
        IEPTree expected = EPTreeCreateUtil.create("/\\(A, C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceAndTauChild2() {
        IEPTree subject = EPTreeCreateUtil.create("/\\(tau, tau, A, tau, C, tau)");
        IEPTree expected = EPTreeCreateUtil.create("/\\(A, C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceXorTauChild1() {
        IEPTree subject = EPTreeCreateUtil.create("x(A, tau, C)");
        IEPTree expected = EPTreeCreateUtil.create("x(A, tau, C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceXorTauChild2() {
        IEPTree subject = EPTreeCreateUtil.create("x(tau, A, tau, C, tau)");
        IEPTree expected = EPTreeCreateUtil.create("x(tau, A, C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceXorTauChild2deep() {
        IEPTree subject = EPTreeCreateUtil.create("x(A, x(tau, B), C, tau)");
        IEPTree expected = EPTreeCreateUtil.create("x(A, tau, B, C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceXorTauChild2loop() {
        IEPTree subject = EPTreeCreateUtil.create("x(A, <->(tau, B), C, tau)");
        IEPTree expected = EPTreeCreateUtil.create("x(A, <->(tau, B), C)");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }

    @Test
    public void testReduceLoopTauTau() {
        IEPTree subject = EPTreeCreateUtil.create("<->(tau, tau)");
        IEPTree expected = EPTreeCreateUtil.create("tau");
        
        ReductionEngine engine = new ReductionEngineDefault();
        engine.reduce(subject);

        Assert.assertTrue(
            "Expected: " + expected + "; Got: " + subject, 
            EPTreeCompareSame.same(expected, subject)
        );
        
        for (IEPTreeNode node : subject.iteratePreOrder()) {
            IEPTreeNode parent = node.getParent();
            if (parent != null) {
                Assert.assertTrue("Tree relation check", parent.getChildren()
                        .contains(node));
            }
        }
    }
}
