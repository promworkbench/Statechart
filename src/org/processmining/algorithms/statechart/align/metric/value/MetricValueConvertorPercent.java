package org.processmining.algorithms.statechart.align.metric.value;

import org.processmining.models.statechart.decorate.align.metric.MetricValue;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;

public class MetricValueConvertorPercent extends MetricValueConvertorDefault {

    public MetricValueConvertorPercent() {
        super(StringPostFix.None);
    }
    
    public MetricValueConvertorPercent(StatMode defaultStatMode, StringPostFix stringPostfix) {
        super(defaultStatMode, stringPostfix);
    }

    @Override
    public String toString(MetricValue value) {
        return toString(scale.convert(value));
    }
    
    @Override
    public String toString(double value) {
        String str = stringifyPercentValue(value);
        
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
        return stringifyPercentValue(scale.convert(value));
    }

    @Override
    public String toStringShort(double value) {
        return stringifyPercentValue(value);
    }
    
    public String stringifyPercentValue(double percent) {
        return String.format("%.1f%%", percent * 100.0);
    }
}
