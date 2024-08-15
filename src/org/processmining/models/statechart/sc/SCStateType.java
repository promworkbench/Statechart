package org.processmining.models.statechart.sc;

public enum SCStateType {
    Simple("S", "Simple (normal) state", false, false),
    SplitPseudo("-<", "Split pseudostate", true, false), // AND split
    JoinPseudo(">-", "Join pseudostate", true, false), // AND join
    ChoicePseudo("<>", "Choice pseudostate", true, false), // Choice diamond
    PointPseudo("()", "Point pseudostate", true, false), // Tau state
    OrStartPseudo("()S", "Or Start pseudostate", true, false), // OR start
    OrEndPseudo("()E", "Or End pseudostate", true, false), // OR end
    AndComposite("/\\", "And composite state", false, true),
    OrComposite("\\/", "Or composite state", false, true),
    Collapsed("C", "Collapsed composite state", false, false),
    Recurrent("R", "Recurrent (composite) state", false, false),
    SeqCancel("SC", "Sequence-cancellation control-flow group", false, true),
    LoopCancel("LC", "Loop-cancellation control-flow group", false, true),
    ErrorTrigger("Er", "Error Trigger state", false, false),
    LogSimple("Log", "Log-move simple (normal) state", false, false);

    private final String symbol;
    private final String name;
    private final boolean isPseudostate;
    private final boolean isCompositeType;

    private SCStateType(String symbol, String name, boolean isPseudostate,
            boolean isCompositeType) {
        this.symbol = symbol;
        this.name = name;
        this.isPseudostate = isPseudostate;
        this.isCompositeType = isCompositeType; 
    }

    public String getSymbol() {
        return symbol;
    }
    
    public String getName() {
        return name;
    }

    public boolean isPseudostate() {
        return isPseudostate;
    }

    public boolean isCompositeType() {
        return isCompositeType;
    }

    @Override
    public String toString() {
        return name;
    }
}
