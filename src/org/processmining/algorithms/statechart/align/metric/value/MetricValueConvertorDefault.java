package org.processmining.algorithms.statechart.align.metric.value;

import org.processmining.models.statechart.decorate.align.metric.MetricLong;
import org.processmining.models.statechart.decorate.align.metric.MetricValue;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;

public class MetricValueConvertorDefault implements IMetricValueConvertor {

    public static enum StringPostFix {
        None, // No postfix
        Scale, // scale percent from min to max
        Max; // scale percent from 0 to max
    }
    
    protected MetricValueScale scale;
    protected StatMode statMode;
    protected StringPostFix stringPostfix;
    
    public String formatValue = "%,.2f";
    public String formatPercent = " (%.1f%%)";

    public MetricValueConvertorDefault(StringPostFix stringPostfix) {
        this(StatMode.Mean, stringPostfix);
    }
    
    public MetricValueConvertorDefault(StatMode defaultStatMode, StringPostFix stringPostfix) {
        this.statMode = defaultStatMode;
        this.stringPostfix = stringPostfix;
        resetMetricValueScale(); // sets this.scale
    }

    public MetricValueConvertorDefault(MetricValueScale scale, StringPostFix stringPostfix) {
        this.statMode = scale.getStatMode();
        this.scale = scale;
        this.stringPostfix = stringPostfix;
    }

    @Override
    public void setStatMode(StatMode statMode) {
        this.statMode = statMode;
    }
    
    @Override
    public double toValue(MetricValue value) {
        return scale.convert(value);
    }

    @Override
    public String toString(MetricValue value) {
        double val = scale.convert(value);
        String str;
        if (value instanceof MetricLong) {
            str = Long.toString((long) val);
        } else {
            str = String.format(formatValue, val);
        }
        
        if (stringPostfix == StringPostFix.Scale) {
            double percent = scale.getPercentOnScale(val);
            str = str + String.format(formatPercent, percent * 100.0);
        } else if(stringPostfix == StringPostFix.Max) {
            double percent = scale.getPercentOfMax(val);
            str = str + String.format(formatPercent, percent * 100.0);
        }
        
        return str;
    }
    
    @Override
    public String toString(double value) {
        String str = Double.toString(value);
        
        if (stringPostfix == StringPostFix.Scale) {
            double percent = scale.getPercentOnScale(value);
            str = String.format("%s (%.1f%%)", str, percent * 100.0);
        } else if(stringPostfix == StringPostFix.Max) {
            double percent = scale.getPercentOfMax(value);
            str = String.format("%s (%.1f%%)", str, percent * 100.0);
        }
        
        return str;
    }

    @Override
    public String toStringShort(MetricValue value) {
        double val = scale.convert(value);
        String str;
        if (value instanceof MetricLong) {
            str = Long.toString((long) val);
        } else {
            str = String.format(formatValue, val);
        }
        return str;
    }

    @Override
    public String toStringShort(double value) {
        return String.format(formatValue, value);
    }

    @Override
    public MetricValueScale getMetricValueScale() {
        return scale;
    }

    @Override
    public void updateValueScale(MetricValue val) {
        scale.update(val);
    }

    @Override
    public void resetMetricValueScale() {
        scale = new MetricValueScale(statMode);
    }

}
