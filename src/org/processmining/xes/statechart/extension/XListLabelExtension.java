package org.processmining.xes.statechart.extension;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.XGlobalAttributeNameMap;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeCollection;
import org.deckfour.xes.model.XAttributeList;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.xes.statechart.model.XAttributeMapRef;
import org.processmining.xes.statechart.model.XAttributeMapRefImpl;

import com.google.common.base.Optional;

/**
 * Extension for list-based labels (e.g., capturing hierarchy information)
 * @author mleemans
 *
 */
public class XListLabelExtension extends XExtension {

    private static final long serialVersionUID = 8181254953648284453L;

    public static final URI EXTENSION_URI = URI
            .create("http://www.processmining.org/listlabel.xesext");

    public static final String NAME = "List Label";
    public static final String PREFIX = "listlabel";

    public static final String KEY_NAME = PREFIX + ":name";
    public static final String KEY_NAME_ELM = PREFIX + ":name:element";

    public static final String KEY_INSTANCE = PREFIX + ":instance";
    public static final String KEY_INSTANCE_ELM = PREFIX + ":instance:element";

    public static final String KEY_DATA = PREFIX + ":data";
    public static final String KEY_DATA_ELM = PREFIX + ":data:element";
    
    public static XAttributeList ATTR_NAME;
    public static XAttributeLiteral ATTR_NAME_ELM;

    public static XAttributeList ATTR_INSTANCE;
    public static XAttributeLiteral ATTR_INSTANCE_ELM;

    public static XAttributeList ATTR_DATA;
    public static XAttributeMapRef ATTR_DATA_ELM;
    
    private XConceptExtension extConceptName = XConceptExtension.instance();

    private static transient XListLabelExtension singleton = new XListLabelExtension();

    public static XListLabelExtension instance() {
        return singleton;
    }

    private final XFactory factory;

    private XListLabelExtension() {
        super(NAME, PREFIX, EXTENSION_URI);

        factory = LogFactory.getFactory();

        ATTR_NAME = factory.createAttributeList(KEY_NAME, this);
        ATTR_NAME_ELM = factory.createAttributeLiteral(KEY_NAME_ELM, "__INVALID__", this);

        ATTR_INSTANCE = factory.createAttributeList(KEY_INSTANCE, this);
        ATTR_INSTANCE_ELM = factory.createAttributeLiteral(KEY_INSTANCE_ELM, "__INVALID__", this);

        ATTR_DATA = factory.createAttributeList(KEY_DATA, this);
        ATTR_DATA_ELM = new XAttributeMapRefImpl(KEY_DATA_ELM, this);
        
//        this.logAttributes.add((XAttribute) ATTR_NAME.clone());
//        this.traceAttributes.add((XAttribute) ATTR_NAME.clone());
        this.eventAttributes.add((XAttribute) ATTR_NAME.clone());
        this.eventAttributes.add((XAttribute) ATTR_INSTANCE.clone());
        this.eventAttributes.add((XAttribute) ATTR_DATA.clone());

        XGlobalAttributeNameMap.instance().registerMapping("EN", KEY_NAME,
                "List Label name");
        XGlobalAttributeNameMap.instance().registerMapping("EN", KEY_INSTANCE,
                "List Label instance");
        XGlobalAttributeNameMap.instance().registerMapping("EN", KEY_DATA,
                "List Data References");
    }
    
    private List<String> extractList(XAttributable element, String key) {
        XAttribute attribute = element.getAttributes().get(key);
        if (attribute == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        for (XAttribute attr : ((XAttributeCollection) attribute)
                .getCollection()) {
            result.add(((XAttributeLiteral) attr).getValue());
        }
        return result;
    }
    
    private String extractListString(XAttributable element, String key) {
        StringBuilder buf = new StringBuilder();
        String sep = "";
        buf.append("[");
        for (String elm : extractList(element, key)) {
            buf.append(sep);
            buf.append(elm);
            sep = ", ";
        }
        buf.append("]");
        return buf.toString();
    }

    public List<String> extractName(XAttributable element) {
        return extractList(element, KEY_NAME);
    }

    public List<String> extractInstance(XAttributable element) {
        return extractList(element, KEY_INSTANCE);
    }

    public List<XAttributeMap> extractData(XAttributable element) {
        XAttribute attribute = element.getAttributes().get(KEY_DATA);
        if (attribute == null || !(attribute instanceof XAttributeList)) {
            return Collections.emptyList();
        }
        
        List<XAttributeMap> result = new ArrayList<>();
        for (XAttribute attr : ((XAttributeCollection) attribute).getCollection()) {
            if (attr instanceof XAttributeMapRefImpl) {
                result.add(((XAttributeMapRefImpl) attr).getAttributeMap());
            }
        }
        
        return result;
    }
    
    public Optional<XAttributeMap> getDataMap(XAttributable element, int level) {
        XAttribute attr = element.getAttributes().get(KEY_DATA);
        if (attr == null || !(attr instanceof XAttributeList)) {
            return Optional.absent();
        }
        
        Collection<XAttribute> listPre = ((XAttributeList) attr).getCollection();
        if (level >= listPre.size() || !(listPre instanceof List)) {
            return Optional.absent();
        }
        
        attr = ((List<XAttribute>) listPre).get(level);
        if (attr == null || !(attr instanceof XAttributeMapRef)) {
            return Optional.absent();
        }
        
        return Optional.of(((XAttributeMapRef) attr).getAttributeMap());
    }
    
    public String extractNameFlat(XAttributable element) {
        return extractListString(element, KEY_NAME);
    }

    public String extractInstanceFlat(XAttributable element) {
        return extractListString(element, KEY_INSTANCE);
    }

    public void assignName(XAttributable element, Iterable<String> name) {
        assignName(element, name, true);
    }

    public void assignName(XAttributable element, Iterable<String> name,
            boolean setConceptName) {
        // Bug: collection not cloned
        // XAttributeList attr = (XAttributeList) ATTR_NAME.clone();
        XAttributeList attr = factory.createAttributeList(KEY_NAME, this);

        for (String part : name) {
            XAttributeLiteral elm = factory.createAttributeLiteral(
                    KEY_NAME_ELM, "__INVALID__", this);
            elm.setValue(part);
            attr.addToCollection(elm);
        }

        element.getAttributes().put(KEY_NAME, attr);

        if (setConceptName) {
            extConceptName.assignName(element, extractNameFlat(element));
        }
    }

    public void appendName(XAttributable element, String namePart) {
        XAttribute attribute = element.getAttributes().get(KEY_NAME);

        if (attribute == null) {
            assignName(element, Collections.singletonList(namePart));
        } else {
            XAttributeList col = (XAttributeList) attribute;

            XAttributeLiteral elm = (XAttributeLiteral) ATTR_NAME_ELM.clone();
            elm.setValue(namePart);
            col.addToCollection(elm);
        }
    }
    
    public void assignInstance(XAttributable element, Iterable<String> instance) {
        XAttributeList attr = factory.createAttributeList(KEY_INSTANCE, this);

        for (String part : instance) {
            XAttributeLiteral elm = factory.createAttributeLiteral(
                    KEY_INSTANCE_ELM, "__INVALID__", this);
            elm.setValue(part);
            attr.addToCollection(elm);
        }

        element.getAttributes().put(KEY_INSTANCE, attr);
    }
    
    public void appendInstance(XAttributable element, String instancePart) {
        XAttribute attribute = element.getAttributes().get(KEY_INSTANCE);

        if (attribute == null) {
            assignInstance(element, Collections.singletonList(instancePart));
        } else {
            XAttributeList col = (XAttributeList) attribute;

            XAttributeLiteral elm = (XAttributeLiteral) ATTR_INSTANCE_ELM.clone();
            elm.setValue(instancePart);
            col.addToCollection(elm);
        }
    }
    
    public void assignData(XAttributable element, Iterable<XAttributeMap> data) {
        XAttributeList attr = factory.createAttributeList(KEY_DATA, this);

        for (XAttributeMap part : data) {
            XAttributeMapRefImpl dataMap = new XAttributeMapRefImpl(KEY_DATA_ELM);
            dataMap.setAttributeMap(part);
            attr.addToCollection(dataMap);
        }

        element.getAttributes().put(KEY_DATA, attr);
    }

    public static boolean isListLabelLog(XLog log) {
        return log.getExtensions().contains(XListLabelExtension.instance())
                || containsListLabel(log);
    }

    public static boolean isListLabelTrace(XTrace trace) {
        for (XEvent event : trace) {
            return event.getAttributes().containsKey(XListLabelExtension.KEY_NAME);
        }
        return false;
    }

    public static boolean containsListLabel(XLog log) {
        for (XTrace trace : log) {
            for (XEvent event : trace) {
                return event.getAttributes().containsKey(XListLabelExtension.KEY_NAME);
            }
        }
        return false;
    }
    
}
