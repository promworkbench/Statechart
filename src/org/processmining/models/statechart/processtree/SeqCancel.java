package org.processmining.models.statechart.processtree;

import java.util.List;
import java.util.UUID;

import org.processmining.processtree.Edge;
import org.processmining.processtree.impl.AbstractBlock;

/**
 * Denotes an sequence-cancellation block
 * @author mleemans
 *
 */
public class SeqCancel extends AbstractBlock implements ISeqCancel {

    public SeqCancel(String name) {
            super(name);
    }
    
    public SeqCancel(UUID id, String name) {
            super(id, name);
    }

    public SeqCancel(String name, List<Edge> incoming, List<Edge> outgoing) {
            super(name, incoming, outgoing);
    }
    
    public SeqCancel(UUID id, String name, List<Edge> incoming, List<Edge> outgoing) {
            super(id, name, incoming, outgoing);
    }
    
    public SeqCancel(ISCCompositeOr b){
            super(b);
    }

    @Override
    public boolean orderingOfChildernMatters() {
            return true;
    }

    @Override
    public boolean expressionsOfOutgoingEdgesMatter() {
            return true;
    }

    @Override
    public String toStringShort() {
            return "SeqCancel";
    }
}
