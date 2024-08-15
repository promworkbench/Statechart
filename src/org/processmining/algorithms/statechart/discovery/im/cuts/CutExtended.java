package org.processmining.algorithms.statechart.discovery.im.cuts;

import java.util.Collection;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;

public class CutExtended extends Cut {

    public enum OperatorExtended {
        SeqCancel,
        LoopCancel;
    }

    private OperatorExtended operatorExtended;

    public CutExtended(OperatorExtended operatorExtended,
            Collection<Set<XEventClass>> partition) {
        super(null, partition);
        this.operatorExtended = operatorExtended;
    }

    public boolean isValid() {
            if (getOperatorExtended() == null || getPartition().size() <= 1) {
                    return false;
            }
            for (Set<XEventClass> part : getPartition()) {
                    if (part.size() == 0) {
                            return false;
                    }
            }
            return true;
    }
    
    public String toString() {
            StringBuilder result = new StringBuilder();
            result.append(operatorExtended);
            result.append(" ");
            result.append(getPartition());
            return result.toString();
    }

    public OperatorExtended getOperatorExtended() {
        return operatorExtended;
    }

    public void setOperatorExtended(OperatorExtended operatorExtended) {
        this.operatorExtended = operatorExtended;
    }
}
