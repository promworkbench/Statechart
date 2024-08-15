package org.processmining.xes.statechart.xport;

import java.net.URI;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.XGlobalAttributeNameMap;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.xes.statechart.XUtil;


public class XApprunExtension extends XExtension {

    private static final long serialVersionUID = -4162589859521509843L;

    public static final URI EXTENSION_URI = URI
            .create("http://www.processmining.org/apprun.xesext");

    public static final String NAME = "Software Application Runtime information";
    public static final String PREFIX = "apprun";

    public static final String KEY_THREADID = PREFIX + ":threadid";
    public static final String KEY_NANOTIME = PREFIX + ":nanotime";

    public static XAttributeLiteral ATTR_THREADID;
    public static XAttributeLiteral ATTR_NANOTIME;

    private static transient XApprunExtension singleton = new XApprunExtension();

    public static XApprunExtension instance() {
        return singleton;
    }

    private final XFactory factory;

    private XApprunExtension() {
        super(NAME, PREFIX, EXTENSION_URI);
        
        factory = LogFactory.getFactory();

        ATTR_THREADID = factory.createAttributeLiteral(KEY_THREADID, "__INVALID__", this);
        ATTR_NANOTIME = factory.createAttributeLiteral(KEY_NANOTIME, "__INVALID__", this);
        
        this.eventAttributes.add((XAttribute) ATTR_THREADID.clone());
        this.eventAttributes.add((XAttribute) ATTR_NANOTIME.clone());

        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_THREADID, "Executing thread id");
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_NANOTIME, "Current time in nanoseconds");
    }

    public void assignThreadId(XEvent event, int threadId) {
        XUtil.assignInt(event, ATTR_THREADID, threadId);
    }
    
    public int extractThreadId(XEvent event) {
        return XUtil.extractInt(event, KEY_THREADID);
    }
    
    public void assignNanotime(XEvent event, long nanotime) {
        XUtil.assignLong(event, ATTR_NANOTIME, nanotime);
    }
    
    public long extractNanotime(XEvent event) {
        return XUtil.extractLong(event, KEY_NANOTIME);
    }
}
