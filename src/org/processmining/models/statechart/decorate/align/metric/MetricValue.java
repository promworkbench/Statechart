package org.processmining.models.statechart.decorate.align.metric;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.processmining.algorithms.statechart.align.metric.MetricId;

public class MetricValue {

    private MetricId metricId;

    public MetricValue(MetricId metricId) {
        this.metricId = metricId;
    }
    
    public MetricId getMetricId() {
        return metricId;
    }
    
    public boolean isDouble() {
        return false;
    }
    
    public double getDouble() {
        return Double.NaN;
    }

    public boolean isLong() {
        return false;
    }
    
    public long getLong() {
        return Long.MIN_VALUE;
    }

    public boolean isStat() {
        return false;
    }
    
    public StatisticalSummary getStat() {
        return null;
    }
}
