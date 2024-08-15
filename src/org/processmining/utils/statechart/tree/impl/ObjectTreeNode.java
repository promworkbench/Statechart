package org.processmining.utils.statechart.tree.impl;

import java.util.ArrayList;
import java.util.List;

import org.processmining.utils.statechart.tree.ITreeNode;

public class ObjectTreeNode<OType> extends AbstractTreeNode<ObjectTreeNode<OType>> implements ITreeNode<ObjectTreeNode<OType>> {

    private final List<ObjectTreeNode<OType>> children;
    private OType object;
    private final ObjectTreeNode<OType> parent;
    
    public ObjectTreeNode(ObjectTreeNode<OType> parent) {
	children = new ArrayList<ObjectTreeNode<OType>>();
	this.parent = parent;
    }
    
    public ObjectTreeNode(ObjectTreeNode<OType> parent, OType o) {
	this(parent);
	setObject(o);
    }
    
    public void setObject(OType o) {
	object = o;
    }
    
    public OType getObject()  {
	return object;
    }
    
    public void addChild(ObjectTreeNode<OType> o) {
	children.add(o);
    }
    
    @Override
    public List<ObjectTreeNode<OType>> getChildren() {
	return children;
    }

    @Override
    public ObjectTreeNode<OType> getParent() {
	return parent;
    }
}
