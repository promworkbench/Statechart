package org.processmining.utils.statechart.tree;

import org.processmining.utils.statechart.signals.Action1;

public interface ITree<Node extends ITreeNode<Node>> {

    /**
     * Get root of tree
     * @return
     */
    Node getRoot();
    
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
