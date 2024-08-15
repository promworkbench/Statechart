package org.processmining.models.statechart.eptree;

import java.util.Set;

import org.processmining.models.statechart.decorate.tracing.IEdgeSemanticTraced;
import org.processmining.utils.statechart.tree.ITreeNode;

public interface IEPTreeNode extends ITreeNode<IEPTreeNode>, IEdgeSemanticTraced<IEPTreeNode> {

    public void addNode(IEPTreeNode node);
    
    public void removeNode(IEPTreeNode node);
    
    public void replaceNode(int index, IEPTreeNode node);

    public void insertNode(int index, IEPTreeNode node);
    
    public EPNodeType getNodeType();
    
    boolean isLeaf();

    public String getId();
    
    public String getLabel();

    public void setParent(IEPTreeNode nodeComposite);
    
    public IEPTree getTree();

    public Set<IEPTreeNode> getStartSemantics();
    
    public Set<IEPTreeNode> getEndSemantics();
    
}
