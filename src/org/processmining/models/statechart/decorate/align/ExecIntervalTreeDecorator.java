package org.processmining.models.statechart.decorate.align;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.align.ExecIntervals;
import org.processmining.models.statechart.decorate.AbstractDecorator;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public class ExecIntervalTreeDecorator extends AbstractDecorator<IEPTreeNode, ExecIntervals> {

    protected final Map<IEPTreeNode, IntervalClusters> clusters = new THashMap<>();
    
    @Override
    public AbstractDecorator<IEPTreeNode, ExecIntervals> newInstance() {
        return new ExecIntervalTreeDecorator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T2> IDecorator<T2, ExecIntervals> deriveDecorationInstance(Class<T2> type) {
        if (type.equals(IEPTreeNode.class)) {
            return (IDecorator<T2, ExecIntervals>) new ExecIntervalTreeDecorator();
        }
        return null;
    }

    @Override
    public void copyDecoration(IEPTreeNode target, IEPTreeNode oldTarget,
            IDecorator<IEPTreeNode, ?> oldDecorator) {
        ExecIntervals old = (ExecIntervals) oldDecorator.getDecoration(oldTarget);
        if (old != null) {
            setDecoration(target, new ExecIntervals(old));
        }
    }

    @Override
    public void deriveDecoration(IEPTreeNode target, Object oldTarget,
            Decorations<?> oldDecorations) {
        throw new IllegalArgumentException("Cannot derive from "
                + oldTarget.getClass());
    }

    public Iterable<ExecInterval> aggregateDecorationsChildren(IEPTreeNode node,
            boolean includeSelf, boolean stopAtDecoratedChild) {
        return new ChildIntervalIterator(this, node, includeSelf, stopAtDecoratedChild);
    }
    
    public Iterable<ExecInterval> aggregatedDecorations(IEPTreeNode node) {
        return aggregateDecorationsChildren(node, true, true);
    }

    public IntervalClusters getIntervalClusters(IEPTreeNode node) {
        IntervalClusters clusters = this.clusters.get(node);
        if (clusters == null) {
            Iterable<ExecInterval> it = aggregatedDecorations(node);
            //clusters = new IntervalClusters(node, it);
            clusters = new IntervalClusters(it);
            this.clusters.put(node, clusters);
        }
        return clusters;
    }
    
    public IntervalClusters getIntervalClusters(IEPTreeNode node,
            boolean includeSelf, boolean stopAtDecoratedChild) {
        Iterable<ExecInterval> it = 
                aggregateDecorationsChildren(node, includeSelf, stopAtDecoratedChild);
        //return new IntervalClusters(node, it);
        return new IntervalClusters(it);
    }
}
