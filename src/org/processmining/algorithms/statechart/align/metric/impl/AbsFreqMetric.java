package org.processmining.algorithms.statechart.align.metric.impl;

import gnu.trove.set.hash.THashSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.processmining.algorithms.statechart.align.metric.AbstractMetric;
import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.decorate.align.IntervalClusters;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.xesalignmentextension.XAlignmentExtension.MoveType;

import com.google.common.base.Preconditions;

public class AbsFreqMetric extends AbstractMetric {

    public static final String Id = "AbsFreq";
    public static final String Name = "Absolute Frequency";
    public static final String ShortName = "Abs. Freq.";
    public static final String DescriptionShort = "How often did the activity occur?";

    public AbsFreqMetric() {
        super(Id, Name, ShortName);
    }

    @Override
    public boolean computeForNode(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        compute(decMetric, null, Collections.singleton(target));
        return true;
    }

    @Override
    public boolean computeForEdge(IMetricsDecorator<IEPTreeNode> decMetric, 
            Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        compute(decMetric, from, to);
        return true;
    }

    private void compute(IMetricsDecorator<IEPTreeNode> decMetric, Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        Preconditions.checkArgument(!to.isEmpty());
        
//        int value = 0;
        Set<Object> proof = new THashSet<>();
        for (IEPTreeNode target : to) {
            IntervalClusters clusters = decInt.getIntervalClusters(target);
            for (List<ExecInterval> cluster : clusters.getClusters()) {
//                boolean containsSync = false;
                Object proofIval = null;
                for (ExecInterval interval : cluster) {
                    if (proofIval == null && (
                           (interval.getStart().getType() != MoveType.MODEL)
                        && (from == null 
                            || moveInNodes(interval.getEnabled(), from)
                            || moveInNodes(interval.getCompleteEnabled(), from)
                        )
                    )) {
                        proofIval = interval.getCause();
                    }
//                    containsSync = containsSync
//                            || ((interval.getStart().getType() != MoveType.MODEL)
//                                && (from == null 
//                                    || moveInNodes(interval.getEnabled(), from)
//                                    || moveInNodes(interval.getCompleteEnabled(), from)
//                                )
//                    );
                }
//                if (containsSync) {
//                    value++;
//                }
                if (proofIval != null) {
                    proof.add(proofIval);
                }
            }
        }
        
        int value = proof.size();
        if (from != null) {
            setMetric(decMetric, from, to, value);
        } else {
            setMetric(decMetric, to.iterator().next(), value);
        }
    }

}
