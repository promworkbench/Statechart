package org.processmining.xes.statechart.extension;

import java.net.URI;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.XGlobalAttributeNameMap;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.statechart.log.LogFactory;

/**
 * Extension for software events
 * 
 * @author mleemans
 *
 */
public class XSoftwareEventExtension extends XExtension {

    private static final long serialVersionUID = 2694264735910181648L;

    /**
     * Valuations for software event type
     * 
     * @author mleemans
     *
     */
    public static enum TypeStandardModel {
        // basic event types for instrumented software
        CALL("call"), RETURN("return"), THROW("throw"),
        // optional extension for exception events
        CATCH("catch"),
        // other 'command types' based on Decompiling Java Part II: Control Flow
        // by David Lawrence Foster (2005)
        ASSIGN("assign"), EVAL("eval"), IF("if"), SWITCH("switch"), MONITOR_ENTER(
                "monitor-enter"), MONITOR_EXIT("monitor-exit"), INFINITE_LOOP(
                "infinite-loop"),
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

    private static transient XSoftwareEventExtension singleton = new XSoftwareEventExtension();

    public static XSoftwareEventExtension instance() {
        return singleton;
    }

    private final XFactory factory;

    private XSoftwareEventExtension() {
        super(NAME, PREFIX, EXTENSION_URI);

        factory = LogFactory.getFactory();

        ATTR_TYPE = factory.createAttributeLiteral(KEY_TYPE, "__INVALID__",
                this);

        this.eventAttributes.add((XAttribute) ATTR_TYPE.clone());

        XGlobalAttributeNameMap.instance().registerMapping("EN", KEY_TYPE,
                "Software Event type");
    }

    public String extractTransition(XEvent event) {
        XAttribute attribute = (XAttribute) event.getAttributes().get(KEY_TYPE);
        if (attribute == null) {
            return null;
        }
        return ((XAttributeLiteral) attribute).getValue();
    }

    public TypeStandardModel extractStandardTransition(XEvent event) {
        String transition = extractTransition(event);
        if (transition != null) {
            return TypeStandardModel.decode(transition);
        }
        return null;
    }

    public void assignTransition(XEvent event, String transition) {
        if ((transition != null) && (transition.trim().length() > 0)) {
            XAttributeLiteral transAttr = (XAttributeLiteral) ATTR_TYPE.clone();

            transAttr.setValue(transition.trim());
            event.getAttributes().put(KEY_TYPE, transAttr);
        }
    }

    public void assignStandardTransition(XEvent event,
            TypeStandardModel transition) {
        assignTransition(event, transition.getEncoding());
    }
}
