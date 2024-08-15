package org.processmining.models.statechart.im.log;

import java.util.BitSet;

import org.deckfour.xes.model.XEvent;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;

public class IMTraceReclass<C> extends IMTrace {

    private final C classifier;
    
    private final Decorations<XEvent> eventDecorations;

    public IMTraceReclass(int XTraceIndex, int IMTraceIndex, BitSet outEvents,
            C classifier, Decorations<XEvent> eventDecorations, IMLog log) {
        super(XTraceIndex, IMTraceIndex, outEvents, log);
        this.classifier = classifier;
        this.eventDecorations = eventDecorations;
    }
    
    public C getClassifier() {
        return classifier;
    }

    public Decorations<XEvent> getEventDecorations() {
        return eventDecorations;
    }
}
