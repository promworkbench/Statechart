package org.processmining.models.statechart.align;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.statechart.decorate.align.ExecIntervalTreeDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

import com.google.common.base.Function;

public class ExecStatistics {

    public static int NO_VALUE = -1;
    
    private ExecIntervals intervals;
    private IEPTreeNode treeNode;
    private Function<XEvent, Integer> fncEvent2Time;

    public ExecStatistics(IEPTree model, IEPTreeNode treeNode,
            Function<XEvent, Integer> fncEvent2Time) {
        this(model.getDecorations().getForType(ExecIntervalTreeDecorator.class), 
                treeNode, fncEvent2Time);
    }
    
    public ExecStatistics(ExecIntervalTreeDecorator dec, IEPTreeNode treeNode,
            Function<XEvent, Integer> fncEvent2Time) {
        this.treeNode = treeNode;
        intervals = dec.getDecoration(treeNode);
        if (intervals == null) {
            throw new IllegalArgumentException("Node has no decorations");
        }
        
        this.fncEvent2Time = fncEvent2Time;
    }

    public IEPTreeNode getNode() {
        return treeNode;
    }
    
    public int getAbsoluteFreq() {
        int freq = 0;
        for (int traceIndex : intervals.getTraceIndices()) {
            freq += intervals.getIntervalsForTrace(traceIndex).size();
        }
        return freq;
    }
    
    public int getCaseFreq() {
        return intervals.size();
    }
    
    public TIntList getDurationTimes() {
        TIntList list = new TIntArrayList();
        for (ExecInterval exec : intervals) {
            int dur = computeDuration(exec.getStart(), exec.getComplete());
            if (dur != NO_VALUE) {
                list.add(dur);
            }
        }
        return list;
    }
    
    public StatisticalSummary getDurationStats() {
        SummaryStatistics stats = new SummaryStatistics();
        TIntIterator it = getDurationTimes().iterator();
        while (it.hasNext()) {
            stats.addValue(it.next());
        }
        return stats;
    }

    public TIntList getWaitingTimes() {
        TIntList list = new TIntArrayList();
        for (ExecInterval exec : intervals) {
            int dur = computeDuration(exec.getEnabled(), exec.getStart());
            if (dur != NO_VALUE) {
                list.add(dur);
            }
        }
        return list;
    }
    
    public StatisticalSummary getWaitingStats() {
        SummaryStatistics stats = new SummaryStatistics();
        TIntIterator it = getWaitingTimes().iterator();
        while (it.hasNext()) {
            stats.addValue(it.next());
        }
        return stats;
    }
    
    private int computeDuration(XAlignmentMove begin, XAlignmentMove end) {
        if (begin != null && end != null) {
            XEvent eBegin = begin.getEvent();
            XEvent eEnd = end.getEvent();
            if (eBegin != null && eEnd != null) {
                return computeDuration(eBegin, eEnd);
            }
        }
        return NO_VALUE;
    }

    private int computeDuration(XEvent begin, XEvent end) {
        Integer tBegin = fncEvent2Time.apply(begin);
        Integer tEnd = fncEvent2Time.apply(end);
        if (tBegin != null && tEnd != null) {
            return tEnd - tBegin;
        } else {
            return NO_VALUE;
        }
    }
}

