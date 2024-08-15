package org.processmining.algorithms.statechart.m2m.ui.decorate;

import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.models.statechart.decorate.IDecorated;
import org.processmining.ui.statechart.color.IColorMap;

public interface IUiDecorator<T, E> {

    public void visitModel(IDecorated<T> model);
    
    public void visitNode(T node);
    
    public void visitEdge(E edge);

    public void finishVisit();
    
    public UiDecoration getDecorationNode(T node);

    public UiDecoration getDecorationEdge(E edge);
    
    public IColorMap getNodeColorMap();

    public IMetricValueConvertor getNodeValueConvertor();

    public IColorMap getEdgeColorMap();

    public IMetricValueConvertor getEdgeValueConvertor();
}
