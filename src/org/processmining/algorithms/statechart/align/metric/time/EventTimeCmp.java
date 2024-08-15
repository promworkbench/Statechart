package org.processmining.algorithms.statechart.align.metric.time;

import java.util.Comparator;

import org.deckfour.xes.model.XEvent;

public class EventTimeCmp implements Comparator<XEvent> {

    private IEvent2Time e2t;
    
    public EventTimeCmp(IEvent2Time e2t) {
        this.e2t = e2t;
    }
    
    @Override
    public int compare(XEvent o1, XEvent o2) {
        double t1 = e2t.apply(o1);
        double t2 = e2t.apply(o2);
        return Double.compare(t1, t2);
    }

}
