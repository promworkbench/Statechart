package org.processmining.models.statechart.labeling;

import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.utils.statechart.software.JoinpointStructure;

public class ClassNameActivityLabeler implements IActivityLabeler {

    @Override
    public String getLabel(IEPTreeNode node) {
        return getLabel(node.getLabel());
    }

    @Override
    public String getLabel(ISCState state) {
        return getLabel(state.getLabel());
    }

    @Override
    public String getLabel(String label) {
        if (label.isEmpty()) {
            return label;
        }
        try {
            JoinpointStructure jpstruct = new JoinpointStructure(label);
            return jpstruct.getJpClass() + jpstruct.getPostfix();
        } catch (IllegalArgumentException e) {
            return label;
        }
    }
}
