package org.processmining.algorithms.statechart.m2m.reduct.eptree;

import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public interface IReductionRule {

    public boolean reduce(IEPTree tree, IEPTreeNode node);
    
}
