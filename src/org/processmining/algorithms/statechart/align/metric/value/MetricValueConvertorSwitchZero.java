package org.processmining.algorithms.statechart.align.metric.value;

import org.processmining.algorithms.statechart.align.metric.MetricId;
import org.processmining.models.statechart.decorate.align.metric.MetricValue;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;

public class MetricValueConvertorSwitchZero implements IMetricValueConvertor {

    private MetricId firstMetricId;
    private IMetricValueConvertor metricValueConvertor;

    public MetricValueConvertorSwitchZero(MetricId firstMetricId,
            IMetricValueConvertor metricValueConvertor) {
        this.firstMetricId = firstMetricId;
        this.metricValueConvertor = metricValueConvertor;
    }

    @Override
    public void setStatMode(StatMode statMode) {
        metricValueConvertor.setStatMode(statMode);
    }

    @Override
    public double toValue(MetricValue value) {
        // First Metric is negative, Second Metric is positive
        double result = metricValueConvertor.toValue(value);
        assert (result >= 0 || Double.isNaN(result));
        if (Double.isNaN(result)) {
            return result;
        } else if (value.getMetricId() == firstMetricId) {
            return -1 * result;
        } else {
            return result;
        }
    }

    @Override
    public String toString(MetricValue value) {
        return metricValueConvertor.toString(value);
    }

    @Override
    public String toString(double value) {
        // Take into account the negative side for the First Metric
        return metricValueConvertor.toString(Math.abs(value));
    }

    @Override
    public String toStringShort(MetricValue value) {
        return metricValueConvertor.toStringShort(value);
    }

    @Override
    public String toStringShort(double value) {
        // Take into account the negative side for the First Metric
        return metricValueConvertor.toStringShort(Math.abs(value));
    }

    @Override
    public MetricValueScale getMetricValueScale() {
        return metricValueConvertor.getMetricValueScale();
    }

    @Override
    public void updateValueScale(MetricValue val) {
        // Take into account the negative side for the First Metric
        metricValueConvertor.getMetricValueScale().update(toValue(val));
    }

    @Override
    public void resetMetricValueScale() {
        metricValueConvertor.resetMetricValueScale();
    }

}
