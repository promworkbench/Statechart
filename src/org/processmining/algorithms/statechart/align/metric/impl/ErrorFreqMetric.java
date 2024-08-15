package org.processmining.algorithms.statechart.align.metric.impl;

import gnu.trove.set.hash.THashSet;

import java.util.List;
import java.util.Set;

import org.processmining.algorithms.statechart.align.metric.AbstractMetric;
import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.decorate.align.IntervalClusters;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.eptree.EPNodeType;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public class ErrorFreqMetric extends AbstractMetric {

    public static final String Id = "ErrorFreq";
    public static final String Name = "Error / Cancel Frequency";
    public static final String ShortName = "Error Freq.";
    public static final String DescriptionShort = "How many times a cancel / exception trigger occured?";

    public ErrorFreqMetric() {
        super(Id, Name, ShortName);
    }

    @Override
    public boolean computeForNode(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        int value = 0;
        
        // Note: We make an exception for this metric:
        // We do investigate the actual tree operators
        if (target.getNodeType() == EPNodeType.ErrorTrigger) {
            Set<XAlignmentMove> causes = new THashSet<>();
            for (ExecInterval ival : decInt.getDecoration(target)) {
                causes.add(ival.getComplete());
            }
            
            IEPTreeNode parent = target.getParent();
            while (parent != null) {
                EPNodeType pt = parent.getNodeType();
                // check current parent for cancel node
                if (pt == EPNodeType.SeqCancel || pt == EPNodeType.LoopCancel) {
                    // check error paths for activations caused by target trigger
                    List<IEPTreeNode> children = parent.getChildren();
                    for (int i = 1; i < children.size(); i++) {
                        // count each activation (interval cluster) once
                        IntervalClusters clusters = decInt.getIntervalClusters(children.get(i));
                        for (List<ExecInterval> cluster : clusters.getClusters()) {
                            if (!cluster.isEmpty()) {
                                boolean foundCause = false;
                                for (ExecInterval ival : cluster) {
                                    foundCause = foundCause || causes.contains(ival.getCause());
                                }
                                if (foundCause) {
                                    value++;
                                }
                            }
                        }
                    }
                }
                
                // move to ancestor
                parent = parent.getParent();
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
