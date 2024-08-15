package org.processmining.models.statechart.decorate.align.metric;

import org.processmining.algorithms.statechart.align.metric.MetricId;

public class MetricDouble extends MetricValue {

    private final double value;
    
    public MetricDouble(MetricId metricId, double value) {
        super(metricId);
        this.value = value;
    }
    
    public MetricDouble(double value) {
        this(null, value);
    }
    
    @Override
    public boolean isDouble() {
        return true;
    }
    
    @Override
    public double getDouble() {
        return value;
    }
    
    @Override
    public int hashCode() {
        return Double.valueOf(value).hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        return other != null 
            && other instanceof MetricDouble
            && ((MetricDouble) other).value == value;
    }
    
    @Override
    public String toString() {
        return Double.toString(value);
    }
}
