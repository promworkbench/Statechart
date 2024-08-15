package org.processmining.models.statechart.msd;

public enum FragmentType {
    Root(""),
    Alt("alt"),
    Parallel("par"),
    Loop("loop"),
    SeqCancel("seq. cancel"),
    LoopCancel("loop cancel");
    
    private final String label;

    private FragmentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
