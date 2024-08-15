package org.processmining.algorithms.statechart.align.metric.impl;

import gnu.trove.map.hash.THashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.processmining.algorithms.statechart.align.metric.AbstractTimeMetric;
import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;
import org.processmining.algorithms.statechart.align.metric.value.MetricValueConvertorPercent;
import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.decorate.align.IntervalClusters;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public class DurationEfficiencyMetric extends AbstractTimeMetric {

    public static final String Id = "DurEff";
    public static final String Name = "Duration Efficiency";
    public static final String ShortName = "Dur. Eff.";
    public static final String DescriptionShort = "How much work was performed divided how much time? "
            + "This can give an efficiency indication in case of concurrent executions.";
    
    public DurationEfficiencyMetric() {
        super(Id, Name, ShortName);
        valueConvertor = new MetricValueConvertorPercent();
    }
    
    public DurationEfficiencyMetric(IEvent2Time event2time) {
        super(event2time, Id, Name, ShortName);
        valueConvertor = new MetricValueConvertorPercent();
    }
    
    @Override
    public boolean computeForNode(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        SummaryStatistics stat = new SummaryStatistics();
        Map<XAlignmentMove, ExecInterval> parentContainer = new THashMap<>();
        
        // Step 1: compute containing intervals
        IntervalClusters clusters = decInt.getIntervalClusters(target);
        for (List<ExecInterval> cluster : clusters.getClusters()) {
            ExecInterval cval = new ExecInterval(cluster, predNoModelMove, moveCmp);
            if (cval.getStart() != null) {
                parentContainer.put(cval.getStart(), cval);
            }
        }
        
        // Step 2: subtract appropriate child durations
        clusters = decInt.getIntervalClusters(target, false, true);
        if (clusters.getClusters().isEmpty()) {
            for (@SuppressWarnings("unused") ExecInterval parentCval : parentContainer.values()) {
//                double ownDuration = event2time.computeDiff(parentCval.getStart(), parentCval.getComplete());
//                if (!Double.isNaN(ownDuration)) {
//                    stat.addValue(ownDuration);
//                }
                stat.addValue(1.0); // ownDuration / ownDuration
            }
        } else {
            for (List<ExecInterval> cluster : clusters.getClusters()) {
                ExecInterval cval = new ExecInterval(cluster, predNoModelMove, moveCmp);
                ExecInterval parentCval = parentContainer.get(cval.getCause());
                double totalDuration = 0;
                for (ExecInterval ex : cluster) {
                    double durPart = event2time.computeDiff(ex.getStart(), ex.getComplete());
                    if (!Double.isNaN(durPart)) {
                        totalDuration += durPart;
                    }
                }
                
                double durationSpan;
                if (parentCval != null) {
                    durationSpan = event2time.computeDiff(parentCval.getStart(), parentCval.getComplete());
                } else {
                    durationSpan = event2time.computeDiff(cval.getStart(), cval.getComplete());
                }
                if (!Double.isNaN(durationSpan) && durationSpan > 0) {
                    stat.addValue(totalDuration / durationSpan);
                }
            }
        }

        setMetric(decMetric, target, stat);
        return true;
    }

    @Override
    public boolean computeForEdge(IMetricsDecorator<IEPTreeNode> decMetric,
            Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        throw new UnsupportedOperationException(Name + " metric is not defined on edges");
    }
}
