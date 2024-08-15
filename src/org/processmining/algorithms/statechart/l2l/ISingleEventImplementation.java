package org.processmining.algorithms.statechart.l2l;

import org.deckfour.xes.model.XEvent;

public interface ISingleEventImplementation {

    public static enum Lifecycle {
        Start, Complete, Other;
    }
    
    public void checkInput();

    public String[] getEventLabelParts(XEvent event);
    
    public Lifecycle getEventStartComplete(XEvent event);
}
