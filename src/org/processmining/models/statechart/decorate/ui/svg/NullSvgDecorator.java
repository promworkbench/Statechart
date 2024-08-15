package org.processmining.models.statechart.decorate.ui.svg;

import org.apache.commons.lang3.NotImplementedException;
import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.models.statechart.decorate.IDecorated;
import org.processmining.ui.statechart.color.IColorMap;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElementException;

public class NullSvgDecorator<T, E, M extends IDecorated<T>>
    implements ISvgDecorator<T, E, M> {

    @Override
    public void resetApplied() {
        // nop
    }

    @Override
    public boolean isApplied() {
        return false;
    }
    
    @Override
    public void visitModel(M model, SVGDiagram svg) throws SVGElementException {
        // nop
    }

    @Override
    public void visitNode(T node) {
        // nop
    }

    @Override
    public void visitEdge(E edge) {
        // nop
    }

    @Override
    public void finishVisit() {
        // nop
    }

    @Override
    public void decorateNode(T node, SvgLabelledElement svgNode) throws SVGElementException {
        // nop
    }

    @Override
    public void decorateEdge(E node, SvgLabelledElement svgNode) throws SVGElementException {
        // nop
    }

    @Override
    public void decorateRecursionBackArrow(T child, T entryNode, SvgLabelledElement e) {
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
