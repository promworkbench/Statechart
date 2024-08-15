package org.processmining.models.statechart.eptree;

import org.processmining.models.statechart.decorate.IDecorated;
import org.processmining.utils.statechart.tree.ITree;

/**
 * Extended Process Tree
 * @author mleemans
 * 
 */
public interface IEPTree extends ITree<IEPTreeNode>, IDecorated<IEPTreeNode> {

    String getName();

    void setRoot(IEPTreeNode newRoot);

    IEPTree createCopy();
    
    IEPTreeNode getNodeByLabel(String label);
    
    IEPTreeNode getNodeByIndex(int... indices);

    IEPTreeNode getNodeById(String t);
}
