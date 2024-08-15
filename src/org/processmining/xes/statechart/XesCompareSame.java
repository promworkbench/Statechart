package org.processmining.xes.statechart;

import java.util.Collection;
import java.util.Iterator;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeCollection;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.xes.statechart.model.XAttributeSubtrace;

import com.google.common.base.Preconditions;

public class XesCompareSame {

    public static boolean same(XLog expected, XLog actual) {
        Preconditions.checkNotNull(expected);
        Preconditions.checkNotNull(actual);
        
        if (!same(expected.getAttributes(), actual.getAttributes())) {
            return false;
        }

        if (expected.size() != actual.size()) {
            return false;
        }

        for (int i = 0; i < expected.size(); i++) {
            if (!same(expected.get(i), actual.get(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean same(XTrace expected, XTrace actual) {
        Preconditions.checkNotNull(expected);
        Preconditions.checkNotNull(actual);
        
        if (!same(expected.getAttributes(), actual.getAttributes())) {
            return false;
        }

        if (expected.size() != actual.size()) {
            return false;
        }

        for (int i = 0; i < expected.size(); i++) {
            if (!same(expected.get(i), actual.get(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean same(XEvent expected, XEvent actual) {
        Preconditions.checkNotNull(expected);
        Preconditions.checkNotNull(actual);
        
        return same(expected.getAttributes(), actual.getAttributes());
    }

    private static boolean same(XAttributeMap expected, XAttributeMap actual) {
        Preconditions.checkNotNull(expected);
        Preconditions.checkNotNull(actual);
        
        if (expected.size() != actual.size()) {
            return false;
        }
        
        for (String key : expected.keySet()) {
            if (!same(expected.get(key), actual.get(key))) {
                return false;
            }
        }
        
        return true;
    }

    private static boolean same(XAttribute expected, XAttribute actual) {
        Preconditions.checkNotNull(expected);
        if (actual == null) {
            return false;
        }
        
        // Known bug: mix of XES implementations may fail here
//        if (!expected.getClass().equals(actual.getClass())) {
//            return false;
//        }
        
        if (expected instanceof XAttributeCollection
            && actual instanceof XAttributeCollection) {
            // for collections, do a deep compare
            return same(
                ((XAttributeCollection) expected).getCollection(), 
                ((XAttributeCollection) actual).getCollection()
            );

        } else if (expected instanceof XAttributeSubtrace
                && actual instanceof XAttributeSubtrace) {
            // for subtraces, do a deep compare
            return same(
                ((XAttributeSubtrace) expected).getSubtrace(), 
                ((XAttributeSubtrace) actual).getSubtrace()
            );
        } else if (expected instanceof XAttributeTimestamp
                && actual instanceof XAttributeTimestamp) {
            // known bug: some implementations store as Date, others as long
            return ((XAttributeTimestamp) actual).getValueMillis()
                    == ((XAttributeTimestamp) expected).getValueMillis();
        } else {
            // else, trust the equals implementation
            return actual.equals(expected);
        }
    }

    public static boolean same(Collection<XAttribute> expected,
            Collection<XAttribute> actual) {
        if (expected.size() != actual.size()) {
            return false;
        }
        
        Iterator<XAttribute> expIt = expected.iterator();
        Iterator<XAttribute> actIt = actual.iterator();
        
        while(expIt.hasNext()) {
            if (!same(expIt.next(), actIt.next())) {
                return false;
            }
        }
        
        return true;
    }
}
