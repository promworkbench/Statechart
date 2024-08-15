package org.processmining.models.statechart.decorate.align.metric;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.processmining.algorithms.statechart.align.metric.MetricId;

import com.google.common.base.Preconditions;

public class MetricStat extends MetricValue {

    private final StatisticalSummary value;
    
    /**
     * Create normal statistics metric by reference
     * @param value
     */
    public MetricStat(MetricId metricId, StatisticalSummary value) {
        super(metricId);
        Preconditions.checkNotNull(value);
        this.value = value;
    }
    
    public MetricStat(StatisticalSummary value) {
        this(null, value);
    }
    
    /**
     * Shorthand stats creation for test purposes
     * @param values
     */
    public MetricStat(MetricId metricId, double... values) {
        super(metricId);
        SummaryStatistics stats = new SummaryStatistics();
        for (double val : values) {
            stats.addValue(val);
        }
        this.value = stats;
    }
    
    public MetricStat(double... values) {
        this(null, values);
    }

    @Override
    public boolean isStat() {
        return true;
    }
    
    @Override
    public StatisticalSummary getStat() {
        return value;
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        return other != null 
            && other instanceof MetricStat
            && ((MetricStat) other).value.equals(value);
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}
