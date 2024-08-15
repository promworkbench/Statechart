package org.processmining.algorithms.statechart.discovery.im.cancellation;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

import com.google.common.base.Predicate;

public class PredicateQueryCancelError implements IQueryCancelError {

    private Predicate<String> matcher;

    public PredicateQueryCancelError(Predicate<String> matcher) {
        this.matcher = matcher;
    }
    
    @Override
    public boolean containsCatchError(Set<XEventClass> activityClasses) {
        boolean result = false;
        for (XEventClass activityClass : activityClasses) {
            result = result || isCatchError(activityClass);
        }
        return result;
    }

    @Override
    public boolean isCatchError(XEventClass activityClass) {
        return isCatchError(activityClass.getId());
    }

    @Override
    public boolean isCatchError(String activity) {
        return matcher.apply(activity);
    }

}
