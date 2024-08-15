package org.processmining.models.statechart.msd;

import java.util.List;

import org.processmining.models.statechart.decorate.IDecorated;

public interface ISeqDiagram extends IDecorated<IActivation> {

    public List<ILifeline> getLifelines();
    
    public IMSDNode getRoot();
    
}
