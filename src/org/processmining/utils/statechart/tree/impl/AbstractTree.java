package org.processmining.utils.statechart.tree.impl;

import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.tree.ITree;
import org.processmining.utils.statechart.tree.ITreeNode;

public abstract class AbstractTree<Node extends ITreeNode<Node>> implements ITree<Node> {

    @Override
    public void visitDepthFirstOrder(Action1<Node> preOrderOp, Action1<Node> postOrderOp) {
	getRoot().visitDepthFirstOrder(preOrderOp, postOrderOp);
    }

    @Override
    public Iterable<Node> iteratePreOrder() {
	return getRoot().iteratePreOrder();
    }

    @Override
    public Iterable<Node> iteratePostOrder() {
	return getRoot().iteratePostOrder();
    }

}
