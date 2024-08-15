package org.processmining.utils.statechart.tree;

import java.util.List;

import org.processmining.utils.statechart.signals.Action1;

/**
 * 
 * @author mleemans
 *
 * @param <Node> the subtype of the node
 * (i.e., class ConcreteNode implements ITreeNode<ConcreteNode> ) 
 */
public interface ITreeNode<Node> {

    /**
     * Get the parent of the current node
     * @return
     */
    Node getParent();
    
    /**
     * Get the children of the current node
     * @return
     */
    List<Node> getChildren();
    
    /**
     * Generic Tree traversal, depth-first order
     * @param preOrderOp	called before visiting the children of a tree node
     * @param postOrderOp	called after visiting the children of a tree node
     */
    void visitDepthFirstOrder(Action1<Node> preOrderOp, Action1<Node> postOrderOp);
    
    /**
     * Generic Tree traversal, depth-first pre-order
     * @return
     */
    Iterable<Node> iteratePreOrder();

    /**
     * Generic Tree traversal, depth-first post-order
     * @return
     */
    Iterable<Node> iteratePostOrder();
}
