package org.processmining.algorithms.statechart.align.metric.time;

import java.util.Comparator;

import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public class MoveTimeCmp implements Comparator<XAlignmentMove> {

    private IEvent2Time event2time;
    
    public MoveTimeCmp(IEvent2Time event2time) {
        this.event2time = event2time;
    }

    public void setEvent2Time(IEvent2Time event2time) {
        this.event2time = event2time;
    }
    
    @Override
    public int compare(XAlignmentMove o1, XAlignmentMove o2) {
        double t1 = event2time.apply(o1);
        double t2 = event2time.apply(o2);
        return Double.compare(t1, t2);
    }

}
