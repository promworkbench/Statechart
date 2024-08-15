package org.processmining.models.statechart.decorate.align;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.align.ExecIntervals;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.utils.statechart.tree.impl.TreeIterator;

public class ChildIntervalIterator implements Iterable<ExecInterval>, Iterator<ExecInterval> {

    private final ExecIntervalTreeDecorator dec;

    private TreeIterator<IEPTreeNode> nodeIt;
    private Iterator<ExecInterval> itIntervals;

    public ChildIntervalIterator(
            ExecIntervalTreeDecorator dec,
            IEPTreeNode node, boolean includeSelf, boolean stopAtDecoratedChild) {
        this.dec = dec;
        
        NodeNotDecoratedPredicate itPred = null;
        if (stopAtDecoratedChild) {
            itPred = new NodeNotDecoratedPredicate(dec);
        }
        nodeIt = new TreeIterator<IEPTreeNode>(node, true, itPred);
        
        if (!includeSelf) {
            if (itPred != null) {
                itPred.forceAcceptNext();
            }
            nodeIt.next();
        }
        itIntervals = null;
    }

    @Override
    public Iterator<ExecInterval> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        while ((itIntervals == null || !itIntervals.hasNext()) && nodeIt.hasNext()) {
            IEPTreeNode node = nodeIt.next();
            ExecIntervals decs = dec.getDecoration(node);
            if (decs != null) {
                itIntervals = decs.iterator();
            } else {
                itIntervals = null;
            }
        }
        
        return itIntervals != null && itIntervals.hasNext();
    }

    @Override
    public ExecInterval next() {
        if (hasNext()) {
            return itIntervals.next();  
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
