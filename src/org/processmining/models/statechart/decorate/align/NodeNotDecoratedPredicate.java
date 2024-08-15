package org.processmining.models.statechart.decorate.align;

import org.processmining.models.statechart.align.ExecIntervals;
import org.processmining.models.statechart.eptree.IEPTreeNode;

import com.google.common.base.Predicate;

public class NodeNotDecoratedPredicate implements Predicate<IEPTreeNode> {

    private final ExecIntervalTreeDecorator dec;
    private boolean forceAcceptNext = false;
    
    public NodeNotDecoratedPredicate(ExecIntervalTreeDecorator dec) {
        this.dec = dec;
    }

    @Override
    public boolean apply(IEPTreeNode node) {
        if (forceAcceptNext) {
            forceAcceptNext = false;
            return true;
        }
        ExecIntervals decs = dec.getDecoration(node);
        return decs == null || decs.isEmpty();
    }

    /**
     * In some cases, you want to ignore this predicate on the next use.
     * Prime example is to skip the first (self) element and apply afterwards
     * This method accepts the next element, and reenable the predicate afterwards
     */
    public void forceAcceptNext() {
        forceAcceptNext  = true;
    }

}
