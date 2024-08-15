package org.processmining.models.statechart.labeling;

import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.sc.ISCState;

public class ClassifierActivityLabeler implements IActivityLabeler {

    @Override
    public String getLabel(IEPTreeNode node) {
        return node.getLabel();
    }

    @Override
    public String getLabel(ISCState state) {
        return state.getLabel();
    }
    
    @Override
    public String getLabel(String label) {
        return label;
    }

}
