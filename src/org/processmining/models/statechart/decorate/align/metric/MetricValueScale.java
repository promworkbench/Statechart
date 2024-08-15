package org.processmining.models.statechart.decorate.align.metric;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.processmining.utils.statechart.gfx.MathUtils;

public class MetricValueScale {

    public static enum StatMode {
        Mean,
        Min,
        Max,
        Size,
        Sum,
        Variance,
        StandardDeviation;
    }

    private double min, max;
    private int count;
    private StatMode statMode;

    public MetricValueScale() {
        this(StatMode.Mean);
    }
    
    public MetricValueScale(StatMode statMode) {
        min = Float.MAX_VALUE;
        max = Float.MIN_VALUE;
        count = 0;
        this.statMode = statMode;
    }

    public double convert(MetricValue val) {
        if (val.isDouble()) {
            return val.getDouble();
        } else if (val.isLong()) {
            return val.getLong();
        } else if (val.isStat()) {
            StatisticalSummary stat = val.getStat();
            if (stat.getN() > 0) {
                switch (statMode) {
                case Min:
                    return stat.getMin();
                case Max:
                    return stat.getMax();
                case Size:
                    return stat.getN();
                case Sum:
                    return stat.getSum();
                case Variance:
                    return stat.getVariance();
                case StandardDeviation:
                    return stat.getStandardDeviation();
                case Mean:
                default:
                    return stat.getMean();
                }
            } else {
                return Double.NaN;
            }
        }
        throw new IllegalArgumentException();
    }
    
    public void update(MetricValue val) {
        update(convert(val));
    }

    public void update(double val) {
        if (!Double.isNaN(val)) {
            min = Math.min(min, val);
            max = Math.max(max, val);
            count++;
        }
    }

    public void resolveEmptyScale() {
        resolveEmptyScale(0);
    }
    
    public void resolveEmptyScale(int defaultValue) {
        if (isEmpty()) {
            min = defaultValue;
            max = defaultValue;
        }
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public int getCount() {
        return count;
    }
    
    public boolean isEmpty() {
        return count == 0;
    }
    
    public double getPercentOfMax(double val) {
        return MathUtils.clamp01(val / max);
    }
    
    public double getPercentOnScale(double val) {
        if (max != min) {
            return MathUtils.clamp01((val - min) / (max - min));
        } else {
            return 1.0;
        }
    }

    public StatMode getStatMode() {
        return statMode;
    }
}
