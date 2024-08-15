package org.processmining.models.statechart.msd;

import org.processmining.models.statechart.decorate.tracing.IEdgeSemanticTraced;

public interface IMSDMessage extends IMSDNode, IEdgeSemanticTraced<IActivation> {

    public IActivation getSource();
    
    public IActivation getTarget();
    
    public MessageType getMessageType();
    
    public String getNodeId();

    public boolean isStartActivation();
}
