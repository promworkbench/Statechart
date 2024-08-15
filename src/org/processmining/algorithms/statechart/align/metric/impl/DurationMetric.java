package org.processmining.algorithms.statechart.align.metric.impl;

import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.processmining.algorithms.statechart.align.metric.AbstractTimeMetric;
import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;
import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.decorate.align.IntervalClusters;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public class DurationMetric extends AbstractTimeMetric {

    public static final String Id = "Dur";
    public static final String Name = "Duration";
    public static final String ShortName = "Dur.";
    public static final String DescriptionShort = "What was the total duration of an activity?";
    
    public DurationMetric() {
        super(Id, Name, ShortName);
    }
    
    public DurationMetric(IEvent2Time event2time) {
        super(event2time, Id, Name, ShortName);
    }
    
    @Override
    public boolean computeForNode(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        SummaryStatistics stat = new SummaryStatistics();

        IntervalClusters clusters = decInt.getIntervalClusters(target);
        for (List<ExecInterval> cluster : clusters.getClusters()) {
            ExecInterval cval = new ExecInterval(cluster, predNoModelMove, moveCmp);
            double duration = event2time.computeDiff(cval.getStart(), cval.getComplete());
            if (!Double.isNaN(duration)) {
                stat.addValue(duration);
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
