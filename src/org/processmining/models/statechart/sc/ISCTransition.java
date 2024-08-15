package org.processmining.models.statechart.sc;

import org.processmining.models.statechart.decorate.tracing.IEdgeSemanticTraced;


public interface ISCTransition extends IEdgeSemanticTraced<ISCState> {

    public String getId();
    
    public void unregister();

    public ISCRegion getParentRegion();
    
    public ISCState getFrom();

    public ISCState getTo();

    public void setFromTo(ISCState from, ISCState to);

    public String getLabel();
    
    public boolean isReverse(); // for dot
    
    public boolean isInvolved(ISCState state);

}
