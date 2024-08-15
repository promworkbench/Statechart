package org.processmining.models.statechart.decorate.align.metric;

import org.processmining.algorithms.statechart.align.metric.MetricId;


public class MetricLong extends MetricValue {

    private final long value;
    
    public MetricLong(MetricId metricId, long value) {
        super(metricId);
        this.value = value;
    }
    
    public MetricLong(long value) {
        this(null, value);
    }
    
    @Override
    public boolean isLong() {
        return true;
    }
    
    @Override
    public long getLong() {
        return value;
    }
    
    @Override
    public int hashCode() {
        return Long.valueOf(value).hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        return other != null 
            && other instanceof MetricLong
            && ((MetricLong) other).value == value;
    }
    
    @Override
    public String toString() {
        return Long.toString(value);
    }
    
}
