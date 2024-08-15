package org.processmining.models.statechart.log;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeCollection;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeID;
import org.deckfour.xes.model.XAttributeList;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.xes.statechart.model.XAttributeMapRef;
import org.processmining.xes.statechart.model.XAttributeMapRefImpl;
import org.processmining.xes.statechart.model.XAttributeSubtrace;
import org.processmining.xes.statechart.model.XAttributeSubtraceImpl;
import org.xeslite.lite.factory.XFactoryLiteImpl;

public class LogFactory {

    private static XFactory factoryInst = null;
    
    public static XFactory getFactory() {
        //return (XFactory) XFactoryRegistry.instance().currentDefault();
        if (factoryInst == null) {
            factoryInst = new XFactoryLiteImpl();
        }
        return factoryInst;
    }

    private static XTrace clone(XTrace source, XFactory factory) {
        XTrace trace = factory.createTrace();
        for (XEvent srcEvent : source) {
            trace.add(clone(srcEvent, factory));
        }
        return trace;
    }
    
    public static XEvent clone(XEvent source, XFactory factory) {
        XEvent event = factory.createEvent();
        copy(source, event, factory);
        return event;
    }

    public static void copy(XAttributable source, XAttributable target, XFactory factory) {
        XAttributeMap sourceMap = source.getAttributes();
        XAttributeMap targetMap = target.getAttributes();
        
        if (sourceMap != null) {
            if (targetMap == null) {
                targetMap = factory.createAttributeMap();
                target.setAttributes(targetMap);
            }

            for (String key : sourceMap.keySet()) {
                targetMap.put(key, clone(sourceMap.get(key), factory));
            }
        }
        
    }

    public static XAttribute clone(XAttribute value, XFactory factory) {
        // TODO attributes are XAttributable
        if (value instanceof XAttributeLiteral) {
            return factory.createAttributeLiteral(value.getKey(), ((XAttributeLiteral) value).getValue(), value.getExtension());
        } else if (value instanceof XAttributeTimestamp) {
            return factory.createAttributeTimestamp(value.getKey(), ((XAttributeTimestamp) value).getValue(), value.getExtension());
        } else if (value instanceof XAttributeDiscrete) {
            return factory.createAttributeDiscrete(value.getKey(), ((XAttributeDiscrete) value).getValue(), value.getExtension());
        } else if (value instanceof XAttributeContinuous) {
            return factory.createAttributeContinuous(value.getKey(), ((XAttributeContinuous) value).getValue(), value.getExtension());
        } else if (value instanceof XAttributeBoolean) {
            return factory.createAttributeBoolean(value.getKey(), ((XAttributeBoolean) value).getValue(), value.getExtension());
        } else if (value instanceof XAttributeID) {
            return factory.createAttributeID(value.getKey(), ((XAttributeID) value).getValue(), value.getExtension());
        } else if (value instanceof XAttributeCollection) {
            XAttributeCollection newValue;
            if (value instanceof XAttributeList) {
                newValue = factory.createAttributeList(value.getKey(), value.getExtension());
            } else {
                newValue = factory.createAttributeContainer(value.getKey(), value.getExtension());
            }
            for(XAttribute item : ((XAttributeCollection) value).getCollection()) {
                newValue.addToCollection(clone(item, factory));
            }
            return newValue;
        } else if (value instanceof XAttributeMapRef) {
            XAttributeMapRefImpl newValue = new XAttributeMapRefImpl(value.getKey(), value.getExtension());
            newValue.setAttributeMap(((XAttributeMapRef) value).getAttributeMap());
            return newValue;
        } else if (value instanceof XAttributeSubtrace) {
            XAttributeSubtraceImpl newValue = new XAttributeSubtraceImpl(value.getKey(), value.getExtension());
            newValue.setSubtrace(clone(((XAttributeSubtrace) value).getSubtrace(), factory));
            return newValue;
        } else {
            throw new IllegalArgumentException("Attribute type unsupported by LogFactory " + value.getClass().getSimpleName());
        }
    }
}
