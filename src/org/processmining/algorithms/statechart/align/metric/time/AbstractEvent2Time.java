package org.processmining.algorithms.statechart.align.metric.time;

import org.deckfour.xes.model.XEvent;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public abstract class AbstractEvent2Time implements IEvent2Time {

    @Override
    public double computeDiff(XAlignmentMove begin, XAlignmentMove end) {
        double tBegin = apply(begin);
        double tEnd = apply(end);
        if (!Double.isNaN(tBegin) && !Double.isNaN(tEnd)) {
            return tEnd - tBegin;
        } else {
            return Double.NaN;
        }
    }

    @Override
    public double computeDiff(XEvent begin, XEvent end) {
        double tBegin = apply(begin);
        double tEnd = apply(end);
        if (!Double.isNaN(tBegin) && !Double.isNaN(tEnd)) {
            return tEnd - tBegin;
        } else {
            return Double.NaN;
        }
    }

}
