package org.processmining.algorithms.statechart.align.metric.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.processmining.algorithms.statechart.align.metric.AbstractTimeMetric;
import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;
import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.decorate.align.IntervalClusters;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;

import com.google.common.base.Preconditions;

public class WaitDurationMetric extends AbstractTimeMetric {

    public static final String Id = "WaitDur";
    public static final String Name = "Wait Duration";
    public static final String ShortName = "Wait";
    
    public WaitDurationMetric() {
        super(Id, Name, ShortName);
    }
    
    public WaitDurationMetric(IEvent2Time event2time) {
        super(event2time, Id, Name, ShortName);
    }

    @Override
    public boolean computeForNode(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        compute(decMetric, null, Collections.singleton(target));
        return true;
    }

    @Override
    public boolean computeForEdge(IMetricsDecorator<IEPTreeNode> decMetric, 
            Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
//        if (to.size() == 1) {
            compute(decMetric, from, to);
            return true;
//        } else {
//            return false;
//        }
    }

    private void compute(IMetricsDecorator<IEPTreeNode> decMetric, 
            Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        Preconditions.checkArgument(!to.isEmpty());

        SummaryStatistics stat = new SummaryStatistics();
        for (IEPTreeNode target : to) {
            IntervalClusters clusters = decInt.getIntervalClusters(target);
            for (List<ExecInterval> cluster : clusters.getClusters()) {
                ExecInterval cval = new ExecInterval(cluster, predNoModelMove, moveCmp);
                if (from == null || moveInNodes(cval.getEnabled(), from)) {
                    double duration = event2time.computeDiff(cval.getEnabled(), cval.getStart());
                    if (!Double.isNaN(duration)) {
                        stat.addValue(duration);
                    }
                } else if (moveInNodes(cval.getCompleteEnabled(), from)) {
                    double duration = event2time.computeDiff(cval.getCompleteEnabled(), cval.getComplete());
                    if (!Double.isNaN(duration)) {
                        stat.addValue(duration);
                    }
                }
            }
        }
        
        if (from != null) {
            setMetric(decMetric, from, to, stat);
        } else {
            setMetric(decMetric, to.iterator().next(), stat);
        }
    }
    
}
