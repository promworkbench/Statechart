package org.processmining.models.statechart.processtree;

import java.util.List;
import java.util.UUID;

import org.processmining.processtree.Edge;
import org.processmining.processtree.impl.AbstractBlock;

/**
 * Denotes an OR composite state in the Statechart semantics
 * @author mleemans
 *
 */
public class SCCompositeOr extends AbstractBlock implements ISCCompositeOr {

    public SCCompositeOr(String name) {
            super(name);
    }
    
    public SCCompositeOr(UUID id, String name) {
            super(id, name);
    }

    public SCCompositeOr(String name, List<Edge> incoming, List<Edge> outgoing) {
            super(name, incoming, outgoing);
    }
    
    public SCCompositeOr(UUID id, String name, List<Edge> incoming, List<Edge> outgoing) {
            super(id, name, incoming, outgoing);
    }
    
    public SCCompositeOr(ISCCompositeOr b){
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
            return "SCCompositeOr";
    }
}
