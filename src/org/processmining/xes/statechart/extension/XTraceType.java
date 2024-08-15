package org.processmining.xes.statechart.extension;

import java.net.URI;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.XGlobalAttributeNameMap;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.statechart.log.LogFactory;

/**
 * 
 * @author mleemans
 *
 */
public class XTraceType extends XExtension {

    private static final long serialVersionUID = 486709392551399854L;

    /**
     * Valuations for software event type
     * 
     * @author mleemans
     *
     */
    public static enum TypeStandardModel {
        // basic types
        NORMAL("normal"), INCOMPLETE("incomplete"),
        // default fallback
        UNKNOWN("unknown");

        private final String encoding;

        private TypeStandardModel(String encoding) {
            this.encoding = encoding;
        }

        public String getEncoding() {
            return this.encoding;
        }

        public static TypeStandardModel decode(String encoding) {
            encoding = encoding.trim().toLowerCase();
            for (TypeStandardModel transition : values()) {
                if (transition.encoding.equals(encoding)) {
                    return transition;
                }
            }
            return UNKNOWN;
        }
    }

    public static final URI EXTENSION_URI = URI
            .create("http://www.processmining.org/swevent.xesext");

    public static final String NAME = "Software Event";
    public static final String PREFIX = "swevent";

    public static final String KEY_TYPE = PREFIX + ":type";

    public static XAttributeLiteral ATTR_TYPE;

    private static transient XTraceType singleton = new XTraceType();

    public static XTraceType instance() {
        return singleton;
    }

    private final XFactory factory;

    private XTraceType() {
        super(NAME, PREFIX, EXTENSION_URI);

        factory = LogFactory.getFactory();

        ATTR_TYPE = factory.createAttributeLiteral(KEY_TYPE, "__INVALID__",
                this);

        this.traceAttributes.add((XAttribute) ATTR_TYPE.clone());

        XGlobalAttributeNameMap.instance().registerMapping("EN", KEY_TYPE,
                "Software Event type");
    }

    public String extractTransition(XTrace trace) {
        XAttribute attribute = (XAttribute) trace.getAttributes().get(KEY_TYPE);
        if (attribute == null) {
            return null;
        }
        return ((XAttributeLiteral) attribute).getValue();
    }

    public TypeStandardModel extractStandardTransition(XTrace trace) {
        String transition = extractTransition(trace);
        if (transition != null) {
            return TypeStandardModel.decode(transition);
        }
        return null;
    }

    public void assignTransition(XTrace trace, String transition) {
        if ((transition != null) && (transition.trim().length() > 0)) {
            XAttributeLiteral transAttr = (XAttributeLiteral) ATTR_TYPE.clone();

            transAttr.setValue(transition.trim());
            trace.getAttributes().put(KEY_TYPE, transAttr);
        }
    }

    public void assignStandardTransition(XTrace trace,
            TypeStandardModel transition) {
        assignTransition(trace, transition.getEncoding());
    }
}
