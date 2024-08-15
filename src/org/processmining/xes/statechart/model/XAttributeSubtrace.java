package org.processmining.xes.statechart.model;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XTrace;

public interface XAttributeSubtrace extends XAttribute {

    public XTrace getSubtrace();
}
