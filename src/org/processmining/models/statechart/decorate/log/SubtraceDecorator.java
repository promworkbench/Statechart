package org.processmining.models.statechart.decorate.log;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.statechart.decorate.IDecorator;

public interface SubtraceDecorator {

    public void deriveForSubtrace(XTrace target, XEvent[] oldTargets, IDecorator<XEvent, ?> oldDecorator);
    
}
