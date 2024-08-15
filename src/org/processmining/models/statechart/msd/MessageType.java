package org.processmining.models.statechart.msd;

public enum MessageType {
    Call(true),
    Return(false),
    CallSelf(true),
    ReturnSelf(false),
    RecursiveCall(true),
    RecursiveReturn(false);
    
    private final boolean isStartActivation;
    
    private MessageType(boolean isStartActivation) {
        this.isStartActivation = isStartActivation;
    }

    public boolean isStartActivation() {
        return isStartActivation;
    }
}
