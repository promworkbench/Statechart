package org.processmining.models.statechart.labeling;

import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.utils.statechart.software.JoinpointStructure;

public class AbbrPackageClassMethodNameActivityLabeler implements IActivityLabeler {

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
            for (String part : jpstruct.getJpPackage().split("\\.")) {
                if (!part.isEmpty()) {
                    bld.append(part.charAt(0));
                    bld.append(".");
                }
            }
            bld.append(jpstruct.getJpClass());
            bld.append(".");
            bld.append(jpstruct.getJpMethod());
            bld.append("()");
            return bld.toString();
        } catch (IllegalArgumentException e) {
            return label;
        }
    }
}
