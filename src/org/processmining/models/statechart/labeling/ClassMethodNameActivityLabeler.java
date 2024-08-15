package org.processmining.models.statechart.labeling;

import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.utils.statechart.software.JoinpointStructure;

public class ClassMethodNameActivityLabeler implements IActivityLabeler {

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
            StringBuilder bld = new StringBuilder();
            bld.append(jpstruct.getJpClass());
            bld.append(".");
            bld.append(jpstruct.getJpMethod());
            bld.append("()");
            bld.append(jpstruct.getPostfix());
            return bld.toString();
        } catch (IllegalArgumentException e) {
            return label;
        }
    }

}
