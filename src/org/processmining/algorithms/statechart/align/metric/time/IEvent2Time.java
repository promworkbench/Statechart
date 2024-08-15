package org.processmining.algorithms.statechart.align.metric.time;

import org.deckfour.xes.model.XEvent;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public interface IEvent2Time {

    public String getId();

    public String getName();
    
    /**
     * Returns the time associated with this event in milliseconds
     * @param event
     * @return
     */
    public double apply(XEvent event);

    /**
     * Returns the time associated with this move in milliseconds
     * @param event
     * @return
     */
    public double apply(XAlignmentMove move);
    
    /**
     * Returns the time difference in milliseconds between the two events
     * @param begin
     * @param end
     * @return
     */
    public double computeDiff(XEvent begin, XEvent end);
    
    /**
     * Returns the time difference in milliseconds between the two moves
     * @param begin
     * @param end
     * @return
     */
    public double computeDiff(XAlignmentMove begin, XAlignmentMove end);

}
