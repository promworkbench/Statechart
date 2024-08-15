package org.processmining.models.statechart.decorate.tracing;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Expresses the edge from-to semantics in a model
 * @author mleemans
 *
 */
public interface IEdgeSemanticTraced<T> {

    public Pair<Set<T>, Set<T>> getEdgeSemantics();
    
    public Set<T> getEdgeFromSemantics();
    
    public Set<T> getEdgeToSemantics();
    
    public void setEdgeSemantics(Set<T> from, Set<T> to);
    
}
