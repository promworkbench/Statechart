package org.processmining.models.statechart.labeling;

import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.sc.ISCState;

public interface IActivityLabeler {

    public String getLabel(IEPTreeNode node);
    
    public String getLabel(ISCState state);
    
    public String getLabel(String rawLabel);
    
}
