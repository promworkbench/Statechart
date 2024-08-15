package org.processmining.xes.statechart.xport;

import java.net.URI;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.XGlobalAttributeNameMap;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.processmining.models.statechart.log.LogFactory;


public class XAppcommExtension extends XExtension {

    private static final long serialVersionUID = -4162589859521509843L;

    public static final URI EXTENSION_URI = URI
            .create("http://www.processmining.org/appcomm.xesext");

    public static final String NAME = "Software Application Communication information";
    public static final String PREFIX = "appcomm";

    public static final String KEY_LOCALHOST = PREFIX + ":localhost";
    public static final String KEY_LOCALPORT = PREFIX + ":localport";

    public static final String KEY_REMOTEHOST = PREFIX + ":remotehost";
    public static final String KEY_REMOTEPORT = PREFIX + ":remoteport";
    
    public static XAttributeLiteral ATTR_LOCALHOST;
    public static XAttributeLiteral ATTR_LOCALPORT;

    public static XAttributeLiteral ATTR_REMOTEHOST;
    public static XAttributeLiteral ATTR_REMOTEPORT;

    private static transient XAppcommExtension singleton = new XAppcommExtension();

    public static XAppcommExtension instance() {
        return singleton;
    }

    private final XFactory factory;

    private XAppcommExtension() {
        super(NAME, PREFIX, EXTENSION_URI);
        
        factory = LogFactory.getFactory();

        ATTR_LOCALHOST = factory.createAttributeLiteral(KEY_LOCALHOST, "__INVALID__", this);
        ATTR_LOCALPORT = factory.createAttributeLiteral(KEY_LOCALPORT, "__INVALID__", this);

        ATTR_REMOTEHOST = factory.createAttributeLiteral(KEY_REMOTEHOST, "__INVALID__", this);
        ATTR_REMOTEPORT = factory.createAttributeLiteral(KEY_REMOTEPORT, "__INVALID__", this);
        
        this.eventAttributes.add((XAttribute) ATTR_LOCALHOST.clone());
        this.eventAttributes.add((XAttribute) ATTR_LOCALPORT.clone());
        
        this.eventAttributes.add((XAttribute) ATTR_REMOTEHOST.clone());
        this.eventAttributes.add((XAttribute) ATTR_REMOTEPORT.clone());

        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_LOCALHOST, "Local host address");
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_LOCALPORT, "Local host port");
        
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_REMOTEHOST, "Remote host address");
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_REMOTEPORT, "Remote host port");
    }

}
