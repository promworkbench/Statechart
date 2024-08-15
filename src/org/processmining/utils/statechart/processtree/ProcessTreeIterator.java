package org.processmining.utils.statechart.processtree;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ProcessTreeIterator implements Iterator<Node>, Iterable<Node> {

    private final Deque<Node> stack;
    private final Set<Node> processed;
    private final boolean preOrder;
    
    public ProcessTreeIterator(ProcessTree tree, boolean preOrder) {
        this(tree.getRoot(), preOrder);
    }
    
    public ProcessTreeIterator(Node node, boolean preOrder) {
        stack = new ArrayDeque<Node>();
        stack.push(node);
        this.preOrder = preOrder;

        if (!preOrder) {
            processed = new THashSet<Node>();
        } else {
            processed = null;
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
        if (current instanceof Block) {
            for (Node child : ((Block) current).getChildren()) {
                stack.push(child);
            }
        }
        return current;
    }

    private Node nextPostOrder() {
        Node current = stack.peek();
        if (current instanceof Block) {
            List<Node> children = ((Block) current).getChildren();
            while (!children.isEmpty() && !processed.contains(current)) {
                for (Node child : children) {
                    stack.push(child);
                }
                processed.add(current);

                current = stack.peek();
                if (current instanceof Block) {
                    children = ((Block) current).getChildren();
                }
            }
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
