package org.processmining.utils.statechart.tree.impl;

import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.tree.ITreeNode;

/**
 * 
 * @author mleemans
 *
 * @param <Node> the subtype of the node
 * (i.e., class ConcreteNode implements AbstractTreeNode<ConcreteNode> ) 
 */
public abstract class AbstractTreeNode<Node extends ITreeNode<Node>> implements ITreeNode<Node> {

    public static <Node extends ITreeNode<Node>>
    void visitDepthFirstOrder(Node node, Action1<Node> preOrderOp, Action1<Node> postOrderOp) 
    {
	preOrderOp.call(node);
	for (Node child : node.getChildren()) {
	    visitDepthFirstOrder(child, preOrderOp, postOrderOp);
	}
	postOrderOp.call(node);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void visitDepthFirstOrder(Action1<Node> preOrderOp,
	    Action1<Node> postOrderOp) {
	visitDepthFirstOrder((Node) this, preOrderOp, postOrderOp);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Node> iteratePreOrder() {
	return new TreeIterator<Node>((Node) this, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Node> iteratePostOrder() {
	return new TreeIterator<Node>((Node) this, false);
    }

}
