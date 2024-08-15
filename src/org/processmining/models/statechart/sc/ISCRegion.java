package org.processmining.models.statechart.sc;

import java.util.Collection;
import java.util.Set;

import org.processmining.utils.statechart.tree.ITreeNode;

public interface ISCRegion extends ITreeNode<ISCRegion> {

    public String getId();

    public String getLabel();
    
    public ISCState getParentState();
    
    public void addState(ISCState state);
    
    public void removeState(ISCState state);
    
    public Set<ISCState> getStates();

    public ISCTransition addTransition(ISCTransition transition);
    
    public void removeTransition(ISCTransition transition);
    
    public ISCTransition addTransition(ISCState from, ISCState to, String label);

    public ISCTransition addTransition(ISCState from, ISCState to, String label,
            boolean isReverse);
    
    public Set<ISCTransition> getTransitions();
    
    public void setInitialState(ISCState state);
    
    public ISCState getInitialState();

    public void addEndState(ISCState state);

    public void removeEndState(ISCState state);
    
    public void addEndStates(Collection<ISCState> states);
    
    public Set<ISCState> getEndStates();

}
