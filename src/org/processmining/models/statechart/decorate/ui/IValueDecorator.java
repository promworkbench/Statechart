package org.processmining.models.statechart.decorate.ui;

import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.ui.statechart.color.IColorMap;

public interface IValueDecorator {

    public void resetApplied();
    
    public boolean isApplied();
    
    public IColorMap getNodeColorMap();

    public IMetricValueConvertor getNodeValueConvertor();

    public IColorMap getEdgeColorMap();

    public IMetricValueConvertor getEdgeValueConvertor();
}
