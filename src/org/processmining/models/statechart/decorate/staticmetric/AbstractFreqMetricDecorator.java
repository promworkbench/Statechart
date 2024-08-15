package org.processmining.models.statechart.decorate.staticmetric;

import org.processmining.models.statechart.decorate.AbstractDecorator;

public abstract class AbstractFreqMetricDecorator<T> extends
        AbstractDecorator<T, FreqMetric> {

//    private StatisticalSummary statsAbsolute;
//    private StatisticalSummary statsCase;

    public void setDecoration(T target, FreqMetric decoration) {
        super.setDecoration(target, decoration);
//        _clearStats();
    }

//    private void _clearStats() {
//        statsAbsolute = null;
//        statsCase = null;
//    }
    
//    public StatisticalSummary getStatsAbsolute() {
//        if (statsAbsolute == null) {
//            SummaryStatistics stats = new SummaryStatistics();
//            for (FreqMetric metric : decorations.values()) {
//                stats.addValue(metric.getFreqAbsolute());
//            }
//            statsAbsolute = stats;
//        }
//        return statsAbsolute;
//    }
//    
//    public StatisticalSummary getStatsCase() {
//        if (this.statsCase == null) {
//            SummaryStatistics stats = new SummaryStatistics();
//            for (FreqMetric metric : decorations.values()) {
//                stats.addValue(metric.getFreqCase());
//            }
//            this.statsCase = stats;
//        }
//        return this.statsCase;
//    }
}
