package org.processmining.algorithms.statechart.align.metric.time;

import org.processmining.xes.statechart.xport.XApprunExtension;

public class Event2TimeNanotime extends Event2TimeAttribute {

    private static final double NANO2MILLI = 0.000001;

    public Event2TimeNanotime() {
        this(XApprunExtension.KEY_NANOTIME);
    }
    
    public Event2TimeNanotime(String key) {
        super(key, NANO2MILLI);
    }
    
    @Override
    public String getName() {
        return "Nanotime (" + XApprunExtension.KEY_NANOTIME + ")";
    }
}
