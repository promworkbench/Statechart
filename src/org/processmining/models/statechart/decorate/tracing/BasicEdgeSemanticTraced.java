package org.processmining.models.statechart.decorate.tracing;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class BasicEdgeSemanticTraced<T> implements IEdgeSemanticTraced<T> {

    private Set<T> from;
    private Set<T> to;

    public BasicEdgeSemanticTraced() {
        
    }
    
    public BasicEdgeSemanticTraced(Set<T> from, Set<T> to) {
        setEdgeSemantics(from, to);
    }

    @Override
    public Pair<Set<T>, Set<T>> getEdgeSemantics() {
        return Pair.of(getEdgeFromSemantics(), getEdgeToSemantics());
    }

    @Override
    public Set<T> getEdgeFromSemantics() {
        return from;
    }

    @Override
    public Set<T> getEdgeToSemantics() {
        return to;
    }

    @Override
    public void setEdgeSemantics(Set<T> from, Set<T> to) {
        this.from = from;
        this.to = to;
    }

}
