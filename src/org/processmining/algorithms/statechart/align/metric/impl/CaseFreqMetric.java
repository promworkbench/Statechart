package org.processmining.algorithms.statechart.align.metric.impl;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Collections;
import java.util.Set;

import org.processmining.algorithms.statechart.align.metric.AbstractMetric;
import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.align.ExecIntervals;
import org.processmining.models.statechart.decorate.align.NodeNotDecoratedPredicate;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.utils.statechart.tree.impl.TreeIterator;
import org.processmining.xesalignmentextension.XAlignmentExtension.MoveType;

import com.google.common.base.Preconditions;

public class CaseFreqMetric extends AbstractMetric {

    public static final String Id = "CaseFreq";
    public static final String Name = "Case Frequency";
    public static final String ShortName = "Case Freq.";
    public static final String DescriptionShort = "In how many traces did the activity occur?";

    public CaseFreqMetric() {
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
        
        TIntSet cases = new TIntHashSet();
        for (IEPTreeNode target : to) {
            TreeIterator<IEPTreeNode> nodeIt = new TreeIterator<IEPTreeNode>(
                    target, true, new NodeNotDecoratedPredicate(decInt));
            for (IEPTreeNode node : nodeIt) {
                ExecIntervals dec = decInt.getDecoration(node);
                if (dec != null) {
                    for (int traceIndex : dec.getTraceIndices()) {
                        boolean containsSync = false;
                        for (ExecInterval interval : dec.getIntervalsForTrace(traceIndex)) {
                            containsSync = containsSync
                                    || ((interval.getStart().getType() != MoveType.MODEL)
                                        && (from == null 
                                            || moveInNodes(interval.getEnabled(), from)
                                            || moveInNodes(interval.getCompleteEnabled(), from)
                                        )
                            );
                        }
                        if (containsSync) {
                            cases.add(traceIndex);
                        }
                    }
                }
            }
        }
        
        if (from != null) {
            setMetric(decMetric, from, to, cases.size());
        } else {
            setMetric(decMetric, to.iterator().next(), cases.size());
        }
    }
    
}
