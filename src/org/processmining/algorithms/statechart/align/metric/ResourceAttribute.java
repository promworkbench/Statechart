package org.processmining.algorithms.statechart.align.metric;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeID;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XLog;
import org.processmining.xes.statechart.xport.XApprunExtension;

public class ResourceAttribute {

    /**
     * Check if the given attribute is a valid time attribute
     * @param attr
     * @return
     */
    public static boolean acceptAttributeForResource(XAttribute attr) {
        return attr instanceof XAttributeDiscrete
            || attr instanceof XAttributeLiteral
            || attr instanceof XAttributeID;
    }
    
    /**
     * Extract list of valid resuorce attribute converters from XLog global event attributes
     * @param log
     * @param addStandard
     * @return
     */
    public static List<String> extractValidAttributes(XLog log, boolean addStandard) {
        List<String> attrs = new ArrayList<>();
        if (addStandard) {
            attrs.add(XOrganizationalExtension.KEY_RESOURCE);
            attrs.add(XApprunExtension.KEY_THREADID);
        }
        for (XAttribute attr : log.getGlobalEventAttributes()) {
            if (acceptAttributeForResource(attr)) {
                String attrKey = attr.getKey();
                if (!attrs.contains(attrKey)) {
                    attrs.add(attrKey);
                }
            }
        }
        return attrs;
    }

}
