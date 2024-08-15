package org.processmining.algorithms.statechart.align.metric.time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public class Event2TimeAttribute extends AbstractEvent2Time {

    private final String attrKey;
    private double correctionFactor;
    
    /**
     * Check if the given attribute is a valid time attribute
     * @param attr
     * @return
     */
    public static boolean acceptAttributeForTime(XAttribute attr) {
        return attr instanceof XAttributeTimestamp
            || attr instanceof XAttributeContinuous
            || attr instanceof XAttributeDiscrete
            || (attr instanceof XAttributeLiteral
                && NumberUtils.isNumber(((XAttributeLiteral) attr).getValue()));
    }
    
    /**
     * Extract list of valid Event2Time converters from XLog global event attributes
     * @param log
     * @param addStandard
     * @return
     */
    public static List<IEvent2Time> extractValidAttributes(XLog log, boolean addStandard) {
        List<Event2TimeAttribute> attrs = new ArrayList<>();
        if (addStandard) {
            attrs.add(new Event2TimeTimestamp());
            attrs.add(new Event2TimeNanotime());
        }
        for (XAttribute attr : log.getGlobalEventAttributes()) {
            if (acceptAttributeForTime(attr)) {
                boolean keyExists = false;
                for (Event2TimeAttribute e2tAttr : attrs) {
                    keyExists = keyExists || (
                        e2tAttr.getKey().equals(attr.getKey())
                    );
                }
                if (!keyExists) {
                    attrs.add(new Event2TimeAttribute(attr));
                }
            }
        }
        return new ArrayList<IEvent2Time>(attrs);
    }

    /**
     * 
     * @param attrKey           The key to extract the time value from
     * @param correctionFactor  The correction factor for Discrete and Literal
     *                      attributes such that the result is in milliseconds.
     */
    public Event2TimeAttribute(String attrKey, double correctionFactor) {
        this.attrKey = attrKey;
        this.correctionFactor = correctionFactor;
    }

    public Event2TimeAttribute(XAttribute attr) {
        this(attr.getKey(), 1.0);
    }
    
    public String getKey() {
        return attrKey;
    }

    @Override
    public String getId() {
        return "Event2Time-" + attrKey;
    }

    @Override
    public String getName() {
        return attrKey;
    }

    @Override
    public String toString() {
        return getName();
    }
    
    /**
     * Returns the time associated with this event in milliseconds
     * @param event
     * @return
     */
    @Override
    public double apply(XEvent event) {
        if (event != null) {
            XAttribute timeAttr = event.getAttributes().get(attrKey);
            if (timeAttr != null) {
                if (timeAttr instanceof XAttributeTimestamp) {
                    Date val = ((XAttributeTimestamp) timeAttr).getValue();
                    return val.getTime();
                } else if (timeAttr instanceof XAttributeContinuous) {
                    return ((XAttributeContinuous) timeAttr).getValue() * correctionFactor;
                } else if (timeAttr instanceof XAttributeDiscrete) {
                    return ((XAttributeDiscrete) timeAttr).getValue() * correctionFactor;
                } else if (timeAttr instanceof XAttributeLiteral) {
                    String val = ((XAttributeLiteral) timeAttr).getValue();
                    return Double.parseDouble(val) * correctionFactor;
                }
            }
        }
        return Double.NaN;
    }

    /**
     * Returns the time associated with this move in milliseconds
     * @param event
     * @return
     */
    @Override
    public double apply(XAlignmentMove move) {
        if (move != null) {
            return apply(move.getEvent());
        }
        return Double.NaN;
    }

}
