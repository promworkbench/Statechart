package org.processmining.algorithms.statechart.discovery.im.cancellation;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

public interface IQueryCancelError {

    public boolean containsCatchError(Set<XEventClass> activityClasses);
    
    public boolean isCatchError(XEventClass activityClass);
    
    public boolean isCatchError(String activity);
    
}
