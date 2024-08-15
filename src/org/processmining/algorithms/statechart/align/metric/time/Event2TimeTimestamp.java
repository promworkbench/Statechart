package org.processmining.algorithms.statechart.align.metric.time;

import org.deckfour.xes.extension.std.XTimeExtension;

public class Event2TimeTimestamp extends Event2TimeAttribute {

    public Event2TimeTimestamp() {
        super(XTimeExtension.KEY_TIMESTAMP, 1);
    }
    
    @Override
    public String getName() {
        return "Timestamp (" + XTimeExtension.KEY_TIMESTAMP + ")";
    }

}
