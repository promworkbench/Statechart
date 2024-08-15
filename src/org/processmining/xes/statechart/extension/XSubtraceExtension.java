package org.processmining.xes.statechart.extension;

import java.net.URI;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.info.XGlobalAttributeNameMap;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.xes.statechart.model.XAttributeSubtrace;
import org.processmining.xes.statechart.model.XAttributeSubtraceImpl;

public class XSubtraceExtension extends XExtension {

    private static final long serialVersionUID = -6544432256291732417L;

    public static final URI EXTENSION_URI = URI
            .create("http://www.processmining.org/subtrace.xesext");

    public static final String NAME = "Subtrace";
    public static final String PREFIX = "subtrace";

    public static final String KEY_SUBTRACE = PREFIX + ":subtrace";

    public static XAttributeSubtrace ATTR_SUBTRACE;

    private static transient XSubtraceExtension singleton = new XSubtraceExtension();

    public static XSubtraceExtension instance() {
        return singleton;
    }

    private XSubtraceExtension() {
        super(NAME, PREFIX, EXTENSION_URI);

        ATTR_SUBTRACE = new XAttributeSubtraceImpl(KEY_SUBTRACE, this);

        this.eventAttributes.add((XAttribute) ATTR_SUBTRACE.clone());

        XGlobalAttributeNameMap.instance().registerMapping("EN", KEY_SUBTRACE,
                "Subtrace");
    }

    public XTrace extractSubtrace(XEvent event) {
        XAttribute attribute = (XAttribute) event.getAttributes().get(KEY_SUBTRACE);
        if (attribute == null) {
            return null;
        }
        return ((XAttributeSubtrace) attribute).getSubtrace();
    }

    public void assignSubtrace(XEvent event, XTrace subtrace) {
        if (subtrace != null) {
            XAttributeSubtraceImpl attr = new XAttributeSubtraceImpl(KEY_SUBTRACE, this);
            attr.setSubtrace(subtrace);
            event.getAttributes().put(KEY_SUBTRACE, attr);
        }
    }

}
