package org.processmining.algorithms.statechart.align.metric.impl;

import gnu.trove.map.hash.THashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.processmining.algorithms.statechart.align.metric.AbstractTimeMetric;
import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;
import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.decorate.align.IntervalClusters;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public class OwnDurationMetric extends AbstractTimeMetric {

    public static final String Id = "OwnDur";
    public static final String Name = "Own Duration";
    public static final String ShortName = "Own Dur.";
    public static final String DescriptionShort = "What was the duration of an activity, minus the time spent in lower activities?";
    
    public OwnDurationMetric() {
        super(Id, Name, ShortName);
    }
    
    public OwnDurationMetric(IEvent2Time event2time) {
        super(event2time, Id, Name, ShortName);
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
            for (ExecInterval parentCval : parentContainer.values()) {
                double ownDuration = event2time.computeDiff(parentCval.getStart(), parentCval.getComplete());
                if (!Double.isNaN(ownDuration)) {
                    stat.addValue(ownDuration);
                }
            }
        } else {
            for (List<ExecInterval> cluster : clusters.getClusters()) {
                ExecInterval cval = new ExecInterval(cluster, predNoModelMove, moveCmp);
                ExecInterval parentCval = parentContainer.get(cval.getCause());
                double busyDuration = 0;
                for (ExecInterval ex : ExecInterval
                        .condenseOverlappingIntervals(cluster, predNoModelMove, moveCmp)) {
                    double durPart = event2time.computeDiff(ex.getStart(), ex.getComplete());
                    if (!Double.isNaN(durPart)) {
                        busyDuration += durPart;
                    }
                }
                
                double ownDuration;
                if (parentCval != null) {
                    ownDuration = event2time.computeDiff(parentCval.getStart(), parentCval.getComplete()) 
                            - busyDuration;
                } else {
                    ownDuration = event2time.computeDiff(cval.getStart(), cval.getComplete()) 
                            - busyDuration;
                }
                if (!Double.isNaN(ownDuration)) {
                    stat.addValue(ownDuration);
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
