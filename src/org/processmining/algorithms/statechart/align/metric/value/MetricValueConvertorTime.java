package org.processmining.algorithms.statechart.align.metric.value;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTimeConstants;
import org.processmining.models.statechart.decorate.align.metric.MetricValue;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;

public class MetricValueConvertorTime extends MetricValueConvertorDefault {

    private static final int ShortNumEntries = 1;
    private static final int LongNumEntries = 3;
    
    private static final int WeeksPerYear = 52;
    
    private static final double[] FormatFactors = new double[]{
        (double) DateTimeConstants.MILLIS_PER_WEEK * (double) WeeksPerYear,
        DateTimeConstants.MILLIS_PER_WEEK,
        DateTimeConstants.MILLIS_PER_DAY,
        DateTimeConstants.MILLIS_PER_HOUR,
        DateTimeConstants.MILLIS_PER_MINUTE,
        DateTimeConstants.MILLIS_PER_SECOND
    };
    private static final double FactorNanos = 1.0 / 1000.0 / 1000.0;
    
    private static final String[] FormatStrings = new String[]{
        "%d yrs",
        "%d wks",
        "%d days",
        "%d hours",
        "%d mins",
        "%d secs"
    };
    private static final String StringMillis = "%,.4f millis";
    private static final String StringNanos = "%.0f nanos";
    
    public MetricValueConvertorTime(StringPostFix stringPostfix) {
        super(stringPostfix);
    }
    
    public MetricValueConvertorTime(StatMode defaultStatMode, StringPostFix stringPostfix) {
        super(defaultStatMode, stringPostfix);
    }

    @Override
    public String toString(MetricValue value) {
        return toString(scale.convert(value));
    }
    
    @Override
    public String toString(double value) {
        String str = stringifyTimeValue(value, LongNumEntries);
        
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
        return toStringShort(scale.convert(value));
    }

    @Override
    public String toStringShort(double value) {
        return stringifyTimeValue(value, ShortNumEntries);
    }
    
    public String stringifyTimeValue(double millis, int numEntries) {
        // in case of no duration
        if (millis == 0) {
            return "0 secs";
        }
        millis = Math.abs(millis);

        // figure out factors
        List<String> entries = new ArrayList<>();
        for (int i = 0; i < FormatFactors.length && entries.size() < numEntries; i++) {
            double factor = FormatFactors[i];
            double multiplier = millis / factor;
            if (multiplier >= 1) {
                entries.add(String.format(FormatStrings[i], (int)Math.floor(multiplier)));
                millis -= Math.floor(multiplier) * factor;
            }
        }
        
        // special cases for millis and micros
        if (millis != 0 && millis * 1000 >= 1 && entries.size() < numEntries) {
            // 1 microseconds or more, less than 1 second
            entries.add(String.format(StringMillis, millis));
        }
        // special cases for nanos
        if (millis > 0 && entries.isEmpty()) {
            entries.add(String.format(StringNanos, millis / FactorNanos));
        }
        
        // compile string
        StringBuilder bld = new StringBuilder();
        String sep = "";
        for (String str : entries) {
            bld.append(sep);
            bld.append(str);
            sep = " ";
        }
        return bld.toString();
        
        /*
        if (millis != 0 && Math.abs(millis) < 1000) {
            if (millis != 0 && Math.abs(millis) < 1) {
                double micros = millis * 1000;
                if (micros != 0 && Math.abs(micros) < 1) {
                    // less than 1 microseconds
                    double nanos = micros * 1000;
                    return String.format("%.2f nanoseconds", nanos);
                } else {
                    // 1 microseconds or more, less than 1 millisecond
                    //return String.format("%.2f microseconds", micros);
                    return String.format("%.4f milliseconds", millis);
                }
            } else {
                // 1 millisecond or more, less than 1 second
                return String.format("%.2f milliseconds", millis);
            }
        } else {
            // 1 second or more
            return PeriodFormat.getDefault().withParseType(PeriodType.standard()).print(new Period((long) millis));   
        }//*/
    }
}
