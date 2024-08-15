package org.processmining.algorithms.statechart.discovery;

import org.junit.Assert;
import org.junit.Test;
import org.processmining.models.statechart.eptree.EPTreeCreateUtil;

public class EPTreeCompareSameTest {

    @Test
    public void testAct() {
        Assert.assertTrue(EPTreeCompareSame.same(
            EPTreeCreateUtil.create("A"), 
            EPTreeCreateUtil.create("A")
        ));
        
        Assert.assertFalse(EPTreeCompareSame.same(
            EPTreeCreateUtil.create("A"), 
            EPTreeCreateUtil.create("B")
        ));
    }
    @Test
    public void testTau() {
        Assert.assertTrue(EPTreeCompareSame.same(
            EPTreeCreateUtil.create("tau"), 
            EPTreeCreateUtil.create("tau")
        ));
    }

    @Test
    public void testSeq() {
        Assert.assertTrue(EPTreeCompareSame.same(
            EPTreeCreateUtil.create("->(A, B)"), 
            EPTreeCreateUtil.create("->(A, B)")
        ));
        
        Assert.assertFalse(EPTreeCompareSame.same(
            EPTreeCreateUtil.create("->(A, B)"), 
            EPTreeCreateUtil.create("->(B, A)")
        ));
    }

    @Test
    public void testChoice() {
        Assert.assertTrue(EPTreeCompareSame.same(
            EPTreeCreateUtil.create("x(A, B)"), 
            EPTreeCreateUtil.create("x(A, B)")
        ));
        
        Assert.assertTrue(EPTreeCompareSame.same(
            EPTreeCreateUtil.create("x(A, B)"), 
            EPTreeCreateUtil.create("x(B, A)")
        ));
    }

    @Test
    public void testLoop() {
        Assert.assertTrue(EPTreeCompareSame.same(
            EPTreeCreateUtil.create("<->(A, B)"), 
            EPTreeCreateUtil.create("<->(A, B)")
        ));
        
        Assert.assertFalse(EPTreeCompareSame.same(
            EPTreeCreateUtil.create("<->(A, B)"), 
            EPTreeCreateUtil.create("<->(B, A)")
        ));
    }

    @Test
    public void testParallel() {
        Assert.assertTrue(EPTreeCompareSame.same(
            EPTreeCreateUtil.create("/\\(A, B)"), 
            EPTreeCreateUtil.create("/\\(A, B)")
        ));
        
        Assert.assertTrue(EPTreeCompareSame.same(
            EPTreeCreateUtil.create("/\\(A, B)"), 
            EPTreeCreateUtil.create("/\\(B, A)")
        ));
    }
}
