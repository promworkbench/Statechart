package org.processmining.algorithms.statechart.discovery;

import org.junit.Assert;
import org.junit.Test;
import org.processmining.models.statechart.eptree.EPTreeCreateUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public class AccessorTest {

    @Test
    public void testByActivityLabel() {
        IEPTree subject = EPTreeCreateUtil.create("->(A, ->(\\/=B(X), C))");
        IEPTreeNode expected;
        
        expected = subject.getRoot().getChildren().get(0);
        Assert.assertSame(expected, subject.getNodeByLabel("A"));

        expected = subject.getRoot().getChildren().get(1);
        expected = expected.getChildren().get(0);
        Assert.assertSame(expected, subject.getNodeByLabel("B"));

        expected = subject.getRoot().getChildren().get(1);
        expected = expected.getChildren().get(1);
        Assert.assertSame(expected, subject.getNodeByLabel("C"));

        expected = subject.getRoot().getChildren().get(1);
        expected = expected.getChildren().get(0);
        expected = expected.getChildren().get(0);
        Assert.assertSame(expected, subject.getNodeByLabel("X"));
    }
    
    @Test
    public void testByActivityLabelTau() {
        IEPTree subject = EPTreeCreateUtil.create("tau");
        Assert.assertSame(subject.getRoot(), subject.getNodeByLabel("tau"));
    }
    
    @Test
    public void testByActivityLabelNotFound() {
        IEPTree subject = EPTreeCreateUtil.create("->(A, ->(\\/=B(X), C))");
        
        Assert.assertNull(subject.getNodeByLabel("Z"));
        Assert.assertNull(subject.getNodeByLabel("BX"));
    }

    @Test
    public void testByIndices() {
        IEPTree subject = EPTreeCreateUtil.create("->(A, ->(\\/=B(X), C))");
        IEPTreeNode expected;

        expected = subject.getRoot();
        Assert.assertSame(expected, subject.getNodeByIndex());
        
        expected = subject.getRoot().getChildren().get(0);
        Assert.assertSame(expected, subject.getNodeByIndex(0));

        expected = subject.getRoot().getChildren().get(1);
        expected = expected.getChildren().get(0);
        Assert.assertSame(expected, subject.getNodeByIndex(1, 0));

        expected = subject.getRoot().getChildren().get(1);
        expected = expected.getChildren().get(1);
        Assert.assertSame(expected, subject.getNodeByIndex(1, 1));

        expected = subject.getRoot().getChildren().get(1);
        expected = expected.getChildren().get(0);
        expected = expected.getChildren().get(0);
        Assert.assertSame(expected, subject.getNodeByIndex(1, 0, 0));
    }
    
    @Test(expected = IndexOutOfBoundsException.class) 
    public void testByIndicesIllegalIndex1() {
        IEPTree subject = EPTreeCreateUtil.create("->(A, ->(\\/=B(X), C))");
        subject.getNodeByIndex(2);
    }
    
    @Test(expected = IndexOutOfBoundsException.class) 
    public void testByIndicesIllegalIndex2() {
        IEPTree subject = EPTreeCreateUtil.create("->(A, ->(\\/=B(X), C))");
        subject.getNodeByIndex(0, 2);
    }
    
    @Test(expected = IndexOutOfBoundsException.class) 
    public void testByIndicesIndexSingletonTree() {
        IEPTree subject = EPTreeCreateUtil.create("tau");
        subject.getNodeByIndex(0);
    }
}
