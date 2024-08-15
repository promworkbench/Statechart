package org.processmining.algorithms.statechart.align.metric.value;

import org.processmining.models.statechart.decorate.align.metric.MetricValue;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;

public interface IMetricValueConvertor {

    public void setStatMode(StatMode statMode);
    
    public double toValue(MetricValue value);
    
    public String toString(MetricValue value);
    
    public String toString(double value);

    public String toStringShort(MetricValue value);
    
    public String toStringShort(double value);

    public MetricValueScale getMetricValueScale();

    public void updateValueScale(MetricValue val);

    public void resetMetricValueScale();


}
