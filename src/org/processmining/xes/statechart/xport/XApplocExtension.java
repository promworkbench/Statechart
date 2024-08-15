package org.processmining.xes.statechart.xport;

import java.net.URI;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.XGlobalAttributeNameMap;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.xes.statechart.NamedEnum;
import org.processmining.xes.statechart.XUtil;


public class XApplocExtension extends XExtension {

    private static final long serialVersionUID = -4162589859521509843L;

    public static final URI EXTENSION_URI = URI
            .create("http://www.processmining.org/apploc.xesext");

    public static final String NAME = "Software Application Locations";
    public static final String PREFIX = "apploc";

    public static enum EventTypeModel implements NamedEnum {
        CALL("call", XLifecycleExtension.StandardModel.START),
        RETURN("return", XLifecycleExtension.StandardModel.COMPLETE),
        CALL_NEW("call_new", XLifecycleExtension.StandardModel.START),
        RETURN_NEW("return_new", XLifecycleExtension.StandardModel.COMPLETE),
        THROW("throw", XLifecycleExtension.StandardModel.COMPLETE),
        HANDLE("handle", XLifecycleExtension.StandardModel.REASSIGN);

        private final String name;
        private final StandardModel mapping;

        private EventTypeModel(String name, 
                XLifecycleExtension.StandardModel mapping) {
            this.name = name;
            this.mapping = mapping;
        }
        
        public String getName() {
            return name;
        }

        public boolean equalsName(String name) {
            return this.name.equals(name);
        }
        
        public XLifecycleExtension.StandardModel getLifecycleMapping() {
            return mapping;
        }
    }
    
    public static final String KEY_APP = PREFIX + ":app";
    public static final String KEY_TIER = PREFIX + ":tier";
    public static final String KEY_NODE = PREFIX + ":node";

    public static final String KEY_ETYPE = PREFIX + ":etype";
    public static final String KEY_JOINPOINT = PREFIX + ":joinpoint";
    public static final String KEY_FILENAME = PREFIX + ":filename";
    public static final String KEY_LINENR = PREFIX + ":linenr";
    public static final String KEY_IDHASHCODE = PREFIX + ":idhashcode";

    public static final String KEY_CALLER_JOINPOINT = PREFIX + ":caller:joinpoint";
    public static final String KEY_CALLER_FILENAME = PREFIX + ":caller:filename";
    public static final String KEY_CALLER_LINENR = PREFIX + ":caller:linenr";
    public static final String KEY_CALLER_IDHASHCODE = PREFIX + ":caller:idhashcode";
    
    public static final String KEY_REGIONSTR = PREFIX + ":regionstr";

    public static XAttributeLiteral ATTR_APP;
    public static XAttributeLiteral ATTR_TIER;
    public static XAttributeLiteral ATTR_NODE;

    public static XAttributeLiteral ATTR_ETYPE;
    public static XAttributeLiteral ATTR_JOINPOINT;
    public static XAttributeLiteral ATTR_FILENAME;
    public static XAttributeLiteral ATTR_LINENR;
    public static XAttributeLiteral ATTR_IDHASHCODE;

    public static XAttributeLiteral ATTR_CALLER_JOINPOINT;
    public static XAttributeLiteral ATTR_CALLER_FILENAME;
    public static XAttributeLiteral ATTR_CALLER_LINENR;
    public static XAttributeLiteral ATTR_CALLER_IDHASHCODE;
    
    public static XAttributeLiteral ATTR_REGIONSTR;

    private static transient XApplocExtension singleton = new XApplocExtension();

    public static XApplocExtension instance() {
        return singleton;
    }

    private final XFactory factory;

    private XApplocExtension() {
        super(NAME, PREFIX, EXTENSION_URI);
        
        factory = LogFactory.getFactory();

        ATTR_APP = factory.createAttributeLiteral(KEY_APP, "__INVALID__", this);
        ATTR_TIER = factory.createAttributeLiteral(KEY_TIER, "__INVALID__", this);
        ATTR_NODE = factory.createAttributeLiteral(KEY_NODE, "__INVALID__", this);

        ATTR_ETYPE = factory.createAttributeLiteral(KEY_ETYPE, "__INVALID__", this);
        ATTR_JOINPOINT = factory.createAttributeLiteral(KEY_JOINPOINT, "__INVALID__", this);
        ATTR_FILENAME = factory.createAttributeLiteral(KEY_FILENAME, "__INVALID__", this);
        ATTR_LINENR = factory.createAttributeLiteral(KEY_LINENR, "__INVALID__", this);
        ATTR_IDHASHCODE = factory.createAttributeLiteral(KEY_IDHASHCODE, "__INVALID__", this);

        ATTR_CALLER_JOINPOINT = factory.createAttributeLiteral(KEY_CALLER_JOINPOINT, "__INVALID__", this);
        ATTR_CALLER_FILENAME = factory.createAttributeLiteral(KEY_CALLER_FILENAME, "__INVALID__", this);
        ATTR_CALLER_LINENR = factory.createAttributeLiteral(KEY_CALLER_LINENR, "__INVALID__", this);
        ATTR_CALLER_IDHASHCODE = factory.createAttributeLiteral(KEY_CALLER_IDHASHCODE, "__INVALID__", this);

        ATTR_REGIONSTR = factory.createAttributeLiteral(KEY_REGIONSTR, "__INVALID__", this);

        this.eventAttributes.add((XAttribute) ATTR_APP.clone());
        this.eventAttributes.add((XAttribute) ATTR_TIER.clone());
        this.eventAttributes.add((XAttribute) ATTR_NODE.clone());

        this.eventAttributes.add((XAttribute) ATTR_ETYPE.clone());
        this.eventAttributes.add((XAttribute) ATTR_JOINPOINT.clone());
        this.eventAttributes.add((XAttribute) ATTR_FILENAME.clone());
        this.eventAttributes.add((XAttribute) ATTR_LINENR.clone());
        this.eventAttributes.add((XAttribute) ATTR_IDHASHCODE.clone());

        this.eventAttributes.add((XAttribute) ATTR_CALLER_JOINPOINT.clone());
        this.eventAttributes.add((XAttribute) ATTR_CALLER_FILENAME.clone());
        this.eventAttributes.add((XAttribute) ATTR_CALLER_LINENR.clone());
        this.eventAttributes.add((XAttribute) ATTR_CALLER_IDHASHCODE.clone());

        this.eventAttributes.add((XAttribute) ATTR_REGIONSTR.clone());

        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_APP, "Software application name");
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_TIER, "Software tier name");
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_NODE, "Software node name");

        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_ETYPE, "Callee event type");
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_JOINPOINT, "Callee joinpoint name");
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_FILENAME, "Callee joinpoint sourcefile");
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_LINENR, "Callee joinpoint line number");
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_IDHASHCODE, "Callee object isntance id hashcode");

        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_CALLER_JOINPOINT, "Caller joinpoint name");
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_CALLER_FILENAME, "Caller joinpoint sourcefile");
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_CALLER_LINENR, "Caller joinpoint line number");
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_CALLER_IDHASHCODE, "Caller object isntance id hashcode");
        
        XGlobalAttributeNameMap.instance().registerMapping(
                "EN", KEY_REGIONSTR, "Software joinpoint region string");
    }

    public void assignEventType(XEvent event, EventTypeModel value) {
        XUtil.assignEnum(event, ATTR_ETYPE, value);
    }
    
    public EventTypeModel extractEventType(XEvent event) {
        return XUtil.extractEnum(event, KEY_ETYPE, EventTypeModel.values());
    }
    
}
