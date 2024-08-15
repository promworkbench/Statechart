package org.processmining.algorithms.statechart.discovery.im.cancellation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

import com.google.common.base.Preconditions;

public class SetQueryCancelError implements IQueryCancelError {

    private Set<String> errorClasses;

    public SetQueryCancelError(String... errorClasses) {
        this(new HashSet<String>(Arrays.asList(errorClasses)));
    }
    
    public SetQueryCancelError(Set<String> errorClasses) {
        Preconditions.checkNotNull(errorClasses);
        this.errorClasses = errorClasses;
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
        return errorClasses.contains(activity);
    }

}
