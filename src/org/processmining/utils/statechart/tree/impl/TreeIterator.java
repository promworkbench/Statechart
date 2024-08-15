package org.processmining.utils.statechart.tree.impl;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.utils.statechart.tree.ITreeNode;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.google.common.base.Predicate;

public class TreeIterator<Node extends ITreeNode<Node>> implements Iterator<Node>, Iterable<Node> {

    private final Deque<Node> stack;
    private final Set<Node> processed;
    private final boolean preOrder;
    private final Predicate<Node> iterateChildren;

    public TreeIterator(Node root, boolean preOrder) {
        this(root, preOrder, null);
    }
    
    public TreeIterator(Node root, boolean preOrder, Predicate<Node> iterateChildren) {
	stack = new ArrayDeque<Node>();
	stack.push(root);
	this.preOrder = preOrder;
	this.iterateChildren = iterateChildren;
	
	if (!preOrder) {
	    processed = new THashSet<Node>();
	} else {
	    processed  = null;
	}
    }
    
    @Override
    public boolean hasNext() {
	return !stack.isEmpty();
    }

    @Override
    public Node next() {
	if (preOrder) {
	    return nextPreOrder();
	} else {
	    return nextPostOrder();
	}
    }

    private Node nextPreOrder() {
        Node current = stack.pop();
        if (iterateChildren == null || iterateChildren.apply(current)) {
            List<Node> children = current.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
        }
        return current;
    }
    
    private Node nextPostOrder() {
	Node current = stack.peek();
	List<Node> children = current.getChildren();
	while (!children.isEmpty() && !processed.contains(current)
	        && (iterateChildren == null || iterateChildren.apply(current))) {
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
	    processed.add(current);
	    
	    current = stack.peek();
	    children = current.getChildren();
	}
	
	current = stack.pop();
	processed.remove(current);
	return current;
    }

    @Override
    public void remove() {
	throw new NotImplementedException();
    }

    @Override
    public Iterator<Node> iterator() {
	return this;
    }
    
}
