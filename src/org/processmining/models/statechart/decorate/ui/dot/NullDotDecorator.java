package org.processmining.models.statechart.decorate.ui.dot;

import org.apache.commons.lang3.NotImplementedException;
import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.models.statechart.decorate.IDecorated;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.ui.statechart.color.IColorMap;

public class NullDotDecorator<T, E, M extends IDecorated<T>>
    implements IDotDecorator<T, E, M> {

    @Override
    public void resetApplied() {
        // nop
    }

    @Override
    public boolean isApplied() {
        return false;
    }
    
    @Override
    public void visitModel(M model, Dot dot) {
        // nop
    }

    @Override
    public void visitNode(T node) {
        // nop
    }

    @Override
    public void visitEdge(E edge) {
        // null
    }

    @Override
    public void finishVisit() {
        // nop
    }

    @Override
    public void decorateNode(T node, DotNode dotNode) {
        // nop
    }

    @Override
    public void decorateEdge(E node, DotEdge dotNode) {
        // nop
    }

    @Override
    public void decorateRecursionBackArrow(T child, T entryNode, DotEdge e) {
        // nop
    }

    @Override
    public IColorMap getNodeColorMap() {
        throw new NotImplementedException("NullDecorator does not support getNodeColorMap()");
    }

    @Override
    public IMetricValueConvertor getNodeValueConvertor() {
        throw new NotImplementedException("NullDecorator does not support getNodeValueConvertor()");
    }

    @Override
    public IColorMap getEdgeColorMap() {
        throw new NotImplementedException("NullDecorator does not support getEdgeColorMap()");
    }

    @Override
    public IMetricValueConvertor getEdgeValueConvertor() {
        throw new NotImplementedException("NullDecorator does not support getEdgeValueConvertor()");
    }
    
}
