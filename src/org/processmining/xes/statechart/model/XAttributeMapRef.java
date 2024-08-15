package org.processmining.xes.statechart.model;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;

public interface XAttributeMapRef extends XAttribute {

    public XAttributeMap getAttributeMap();
}
