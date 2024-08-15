package org.processmining.models.statechart.decorate.tracing;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Function;

public class EdgeSemanticTracedAdapter<F extends IEdgeSemanticTraced<T>, T> implements Function<F, Pair<Set<T>, Set<T>>> {

    @Override
    public Pair<Set<T>, Set<T>> apply(F input) {
        return input.getEdgeSemantics();
    }

}
