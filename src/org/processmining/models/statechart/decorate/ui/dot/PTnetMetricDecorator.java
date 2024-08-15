package org.processmining.models.statechart.decorate.ui.dot;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.algorithms.statechart.m2m.ui.decorate.IUiDecorator;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.plugins.graphviz.dot.Dot;

import com.google.common.base.Function;

public class PTnetMetricDecorator extends DotMetricDecorator<Transition, Arc, PetrinetDecorated> {

    public static interface ModelAwareEdge2Node extends Function<Arc, Pair<Set<Transition>, Set<Transition>>> {
        public void visitModel(PetrinetDecorated model);
    }

    private final ModelAwareEdge2Node edge2node;
    
    public PTnetMetricDecorator(IUiDecorator<Transition, Arc> uiDecorator, ModelAwareEdge2Node edge2node) {
        super(uiDecorator);
        this.edge2node = edge2node;
    }

    @Override
    public void visitModel(PetrinetDecorated model, Dot dot) {
        super.visitModel(model, dot);
        edge2node.visitModel(model);
    }
}
