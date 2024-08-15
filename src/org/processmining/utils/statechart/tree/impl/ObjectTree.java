package org.processmining.utils.statechart.tree.impl;


public class ObjectTree<OType> extends AbstractTree<ObjectTreeNode<OType>> {

    private final ObjectTreeNode<OType> root;
    
    public ObjectTree(ObjectTreeNode<OType> root) {
	this.root = root;
    }
    
    @Override
    public ObjectTreeNode<OType> getRoot() {
	return root;
    }

}
