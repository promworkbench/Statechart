package org.processmining.algorithms.statechart.align.metric;

import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;

public interface ITimeMetric extends IMetric {

    public void setEvent2Time(IEvent2Time event2time);

    public IEvent2Time getEvent2Time();
    
}
