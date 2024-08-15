package org.processmining.algorithms.statechart.align.metric;

import java.util.Set;

import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.models.statechart.decorate.align.AlignMappingTreeDecorator;
import org.processmining.models.statechart.decorate.align.ExecIntervalTreeDecorator;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricValue;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public interface IMetric {

    public MetricId getId();
    
    public String getName();
    
    public void setDecorators(ExecIntervalTreeDecorator decInt, 
            AlignMappingTreeDecorator decAlignMap);

    public IMetricValueConvertor getValueConvertor();
    
    public IMetricValueConvertor getValueConvertor(StatMode statMode);

    // Node interface

    public String getLabelName(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode context);
    
    public boolean computeForNode(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target);

    public MetricValue getMetric(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target);
    
    public double getMetricValue(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target);

    public String getMetricValueString(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target);

    // Edge interface

    public String getLabelName(IMetricsDecorator<IEPTreeNode> decMetric,
            Set<IEPTreeNode> from, Set<IEPTreeNode> to);
    
    public boolean computeForEdge(IMetricsDecorator<IEPTreeNode> decMetric, Set<IEPTreeNode> from, Set<IEPTreeNode> to);

    public MetricValue getMetric(IMetricsDecorator<IEPTreeNode> decMetric, Set<IEPTreeNode> from, Set<IEPTreeNode> to);

    public double getMetricValue(IMetricsDecorator<IEPTreeNode> decMetric, Set<IEPTreeNode> from, Set<IEPTreeNode> to);

    public String getMetricValueString(IMetricsDecorator<IEPTreeNode> decMetric, Set<IEPTreeNode> from, Set<IEPTreeNode> to);


}
