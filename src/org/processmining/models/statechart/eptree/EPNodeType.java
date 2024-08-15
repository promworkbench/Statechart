package org.processmining.models.statechart.eptree;

public enum EPNodeType {
    // Leaf types
    Action("Act", "Action leaf node", true, false, false, true),
    Silent("Tau", "Silent/Tau leaf node", true, false, false, false),
    Collapsed("C\\/", "Collapsed composite leaf node", true, false, false, true),
    Recurrent("R\\/", "Recurrent (composite) leaf node", true, false, false, true),
    // Control flow and structure operators
    Seq("->", "Sequence control-flow node", false, true, false, false),
    Choice("x", "Choice (XOR) control-flow node", false, false, false, false),
    // Loop note: exactly two children: loop(0: do, 1: redo)
    Loop("<->", "Loop (Structured) control-flow node", false, true, false, false), 
    AndInterleaved("I/\\", "And Interleaved composite-state node", false, false, true, false),
    AndComposite("/\\", "And composite-state node", false, false, true, false),
    OrComposite("\\/", "Or composite-state node", false, true, true, true),
    // Cancellation note: exactly two children: cancel(0: do, 1: error)
    SeqCancel("SC", "Sequence-Cancellation control-flow node", false, true, true, false),
    LoopCancel("LC", "Loop-Cancellation control-flow node", false, true, true, false),
    ErrorTrigger("Er", "Error trigger leaf node", true, false, false, true),
    // Log moves / conformance
    Log("Log", "Log-move leaf node", true, false, false, false),
    ;
    
    private final String symbol;
    private final String name;
    private final boolean isLeafType;
    private final boolean isCompositeType;
    private final boolean isOrderAware;
    private final boolean isLabelled;

    private EPNodeType(String symbol, String name, boolean isLeafType,
            boolean isOrderAware, boolean isCompositeType, boolean isLabelled) {
        this.symbol = symbol;
        this.name = name;
        this.isLeafType = isLeafType;
        this.isOrderAware = isOrderAware;
        this.isCompositeType = isCompositeType;
        this.isLabelled = isLabelled;
    }

    public String getSymbol() {
        return symbol;
    }
    
    public String getName() {
        return name;
    }

    public boolean isLeafType() {
        return isLeafType;
    }

    public boolean isCompositeType() {
        return isCompositeType;
    }
    
    @Override
    public String toString() {
        return name;
    }

    public boolean isOrderAware() {
        return isOrderAware;
    }

    public boolean isLabelled() {
        return isLabelled;
    }
}
