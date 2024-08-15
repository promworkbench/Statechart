package org.processmining.models.statechart.processtree;

import java.util.List;
import java.util.UUID;

import org.processmining.processtree.Edge;
import org.processmining.processtree.impl.AbstractBlock;

/**
 * Denotes an loop-cancellation block
 * @author mleemans
 *
 */
public class LoopCancel extends AbstractBlock implements ILoopCancel {

    public LoopCancel(String name) {
            super(name);
    }
    
    public LoopCancel(UUID id, String name) {
            super(id, name);
    }

    public LoopCancel(String name, List<Edge> incoming, List<Edge> outgoing) {
            super(name, incoming, outgoing);
    }
    
    public LoopCancel(UUID id, String name, List<Edge> incoming, List<Edge> outgoing) {
            super(id, name, incoming, outgoing);
    }
    
    public LoopCancel(ISCCompositeOr b){
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
            return "LoopCancel";
    }
}
