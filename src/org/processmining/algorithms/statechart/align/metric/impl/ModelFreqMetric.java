package org.processmining.algorithms.statechart.align.metric.impl;

import java.util.List;
import java.util.Set;

import org.processmining.algorithms.statechart.align.metric.AbstractMetric;
import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.decorate.align.IntervalClusters;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.xesalignmentextension.XAlignmentExtension.MoveType;

public class ModelFreqMetric extends AbstractMetric {

    public static final String Id = "MMFreq";
    public static final String Name = "Model-move Frequency";
    public static final String ShortName = "Model moves";

    public ModelFreqMetric() {
        super(Id, Name, ShortName);
    }

    @Override
    public boolean computeForNode(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        IntervalClusters clusters = decInt.getIntervalClusters(target);
        int value = 0;
        for (List<ExecInterval> cluster : clusters.getClusters()) {
            boolean isModel = !cluster.isEmpty();
            for (ExecInterval interval : cluster) {
                isModel = isModel && (
                    (interval.getStart() != null
                    && interval.getStart().getType() == MoveType.MODEL)
                || (interval.getComplete() != null
                    && interval.getComplete().getType() == MoveType.MODEL)
                );
            }
            if (isModel) {
                value++;
            }
        }
        setMetric(decMetric, target, value);
        return true;
    }
    
    @Override
    public boolean computeForEdge(IMetricsDecorator<IEPTreeNode> decMetric,
            Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        throw new UnsupportedOperationException(Name + " metric is not defined on edges");
    }
}