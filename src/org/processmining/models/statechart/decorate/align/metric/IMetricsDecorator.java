package org.processmining.models.statechart.decorate.align.metric;

import java.util.Set;

import org.processmining.algorithms.statechart.align.metric.MetricId;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public interface IMetricsDecorator<T> {

    // Node interface
    public String getLabelName(T target, MetricId key);
    
    public MetricValue getMetric(T target, MetricId key);

    public double getMetricValue(T target, MetricId key);
    
    public String getMetricValueString(T target, MetricId key);
    
    public void setMetric(IEPTreeNode target, MetricId key, MetricValue value);

    public void resetMetric(MetricId id);
    
    // Edge interface
    public String getLabelName(Set<T> from, Set<T> to, MetricId key);
    
    public MetricValue getMetric(Set<T> from, Set<T> to, MetricId key);

    public double getMetricValue(Set<T> from, Set<T> to, MetricId key);
    
    public String getMetricValueString(Set<T> from, Set<T> to, MetricId key);
    
    public void setMetric(Set<IEPTreeNode> from, Set<IEPTreeNode> to, MetricId key, MetricValue value);

}
