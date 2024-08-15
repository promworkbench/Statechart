package org.processmining.models.statechart.sc;

import java.util.Set;

import org.processmining.utils.statechart.tree.ITreeNode;

public interface ISCState extends ITreeNode<ISCState> {

    public String getId();

    public String getLabel();
    
    public boolean isPseudoState();

    public SCStateType getStateType();
    
    public ISCRegion getParentRegion();
    
    public void recordTransition(ISCTransition transition);
    
    public void unrecordTransition(ISCTransition transition);

    public Set<ISCTransition> getInvolvedTransitions();
    
    public Set<ISCState> getPostset();
    
    public Set<ISCState> getPreset();
    
    public boolean isInitialState();
    
    public boolean isEndState();
}
