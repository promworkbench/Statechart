package org.processmining.utils.statechart.tree;

import org.junit.Assert;
import org.junit.Test;
import org.processmining.utils.statechart.tree.impl.ObjectTree;
import org.processmining.utils.statechart.tree.impl.ObjectTreeNode;
import org.processmining.utils.statechart.tree.impl.TreeIterator;

import com.google.common.base.Predicate;

public class TreeIterationTest {

    /**
     * See also http://en.wikipedia.org/wiki/Tree_traversal#Types
     * @return
     */
    public ObjectTree<String> constructTree() {
	ObjectTreeNode<String> F = new ObjectTreeNode<String>(null, "F");

	ObjectTreeNode<String> G = new ObjectTreeNode<String>(F, "G");
	ObjectTreeNode<String> B = new ObjectTreeNode<String>(F, "B");
	F.addChild(G);
	F.addChild(B);

	ObjectTreeNode<String> D = new ObjectTreeNode<String>(B, "D");
	ObjectTreeNode<String> A = new ObjectTreeNode<String>(B, "A");
	B.addChild(D);
	B.addChild(A);

	ObjectTreeNode<String> E = new ObjectTreeNode<String>(D, "E");
	ObjectTreeNode<String> C = new ObjectTreeNode<String>(D, "C");
	D.addChild(E);
	D.addChild(C);

	ObjectTreeNode<String> I = new ObjectTreeNode<String>(G, "I");
	ObjectTreeNode<String> H = new ObjectTreeNode<String>(I, "H");
	G.addChild(I);
	I.addChild(H);
	
	return new ObjectTree<String>(F);
    }
    
    @Test
    public void testPreOrder() {
	String[] expected = new String[] {"F", "G", "I", "H", "B", "D", "E", "C", "A"};
	String[] actual = new String[expected.length];
	
	ObjectTree<String> tree = constructTree();
	int i = 0;
	for (ObjectTreeNode<String> n : tree.iteratePreOrder()) {
	    actual[i++] = n.getObject();
	}
	
	Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testPostOrder() {
	String[] expected = new String[] {"H", "I", "G", "E", "C", "D", "A", "B", "F"};
	String[] actual = new String[expected.length];
	
	ObjectTree<String> tree = constructTree();
	int i = 0;
	for (ObjectTreeNode<String> n : tree.iteratePostOrder()) {
	    actual[i++] = n.getObject();
	}

	Assert.assertArrayEquals(expected, actual);
    }
    
    @Test
    public void testPreOrderPredicate() {
        String[] expected = new String[] {"F", "G", "I", "B", "D", "A"};
        String[] actual = new String[expected.length];
        
        ObjectTree<String> tree = constructTree();
        
        TreeIterator<ObjectTreeNode<String>> it = new TreeIterator<>(
            tree.getRoot(), true, new Predicate<ObjectTreeNode<String>>() {
                @Override
                public boolean apply(ObjectTreeNode<String> current) {
                    return !current.getObject().equals("I")
                            && !current.getObject().equals("D");
                }
        });
        
        int i = 0;
        for (ObjectTreeNode<String> n : it) {
            actual[i++] = n.getObject();
        }
        
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testPostOrderPredicate() {
        String[] expected = new String[] {"I", "G", "D", "A", "B", "F"};
        String[] actual = new String[expected.length];
        
        ObjectTree<String> tree = constructTree();

        TreeIterator<ObjectTreeNode<String>> it = new TreeIterator<>(
            tree.getRoot(), false, new Predicate<ObjectTreeNode<String>>() {
                @Override
                public boolean apply(ObjectTreeNode<String> current) {
                    return !current.getObject().equals("I")
                            && !current.getObject().equals("D");
                }
        });
        
        int i = 0;
        for (ObjectTreeNode<String> n : it) {
            actual[i++] = n.getObject();
        }

        Assert.assertArrayEquals(expected, actual);
    }
}
