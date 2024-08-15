package org.processmining.algorithms.statechart.m2m.ui.layout;

import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.internal.util.java.util.ListUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

import com.google.common.collect.Lists;

public class EPTreeLayoutAdapter implements TreeForTreeLayout<IEPTreeNode> {

    private IEPTree tree;

    public EPTreeLayoutAdapter(IEPTree tree) {
        this.tree = tree;
    }

    @Override
    public IEPTreeNode getRoot() {
        return tree.getRoot();
    }

    @Override
    public boolean isLeaf(IEPTreeNode node) {
        return node.isLeaf();
    }

    @Override
    public boolean isChildOfParent(IEPTreeNode child, IEPTreeNode parent) {
        return child.getParent() == parent;
    }

    @Override
    public Iterable<IEPTreeNode> getChildren(IEPTreeNode node) {
        return node.getChildren();
    }

    @Override
    public Iterable<IEPTreeNode> getChildrenReverse(IEPTreeNode node) {
        return Lists.reverse(node.getChildren());
    }

    @Override
    public IEPTreeNode getFirstChild(IEPTreeNode node) {
        return node.getChildren().get(0);
    }

    @Override
    public IEPTreeNode getLastChild(IEPTreeNode node) {
        return ListUtil.getLast(node.getChildren());
    }

}
