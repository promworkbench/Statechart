package org.processmining.xes.statechart;

import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;

public class XUtil {
    
    private XUtil() {
        
    }

    public static boolean hasAttribs(XAttributable event, String... keys) {
        return event.hasAttributes() && hasAttribs(event.getAttributes(), keys);
    }
    
    public static boolean hasAttribs(XAttributeMap map, String... keys) {
        for (String key : keys) {
            if (!map.containsKey(key)) {
                return false;
            }
        }
        return true;
    }
    
    public static String extractLiteral(XAttributeMap map, String key) {
        XAttribute attribute = (XAttribute) map.get(key);
        if (attribute == null || !(attribute instanceof XAttributeLiteral)) {
            return null;
        }
        return ((XAttributeLiteral) attribute).getValue();
    }

    public static long extractLong(XAttributeMap map, String key) {
        XAttribute attribute = (XAttribute) map.get(key);
        if (attribute == null) {
            return Long.MIN_VALUE;
        }
        if (attribute instanceof XAttributeDiscrete) {
            return ((XAttributeDiscrete) attribute).getValue();
        }
        if (attribute instanceof XAttributeLiteral) {
            try {
                return Long.parseLong(((XAttributeLiteral) attribute).getValue());
            } catch (NumberFormatException e) {
                return Long.MIN_VALUE;
            }
        }
        return Long.MIN_VALUE;
    }

    public static int extractInt(XAttributeMap map, String key) {
        return (int) extractLong(map, key);
    }

    public static String extractLiteral(XAttributable event, String key) {
        return extractLiteral(event.getAttributes(), key);
    }

    public static long extractLong(XAttributable event, String key) {
        return extractLong(event.getAttributes(), key);
    }

    public static int extractInt(XAttributable event, String key) {
        return extractInt(event.getAttributes(), key);
    }

    public static <T extends NamedEnum> T extractEnum(XAttributable event, String key, T[] values) {
        return extractEnum(event, key, values, null);
    }

    public static <T extends NamedEnum> T extractEnum(XAttributable event, String key, T[] values, T unkown) {
        return extractEnum(event.getAttributes(), key, values, unkown);
    }

    public static <T extends NamedEnum> T extractEnum(XAttributeMap map, String key, T[] values, T unkown) {
        String value = extractLiteral(map, key);
        return translateEnum(value, values, unkown);
    }
    
    public static <T extends NamedEnum> T translateEnum(String value, T[] values) {
        return translateEnum(value, values, null);
    }
    
    public static <T extends NamedEnum> T translateEnum(String value, T[] values, T unkown) {
        for (T entry : values) {
            if (entry.equalsName(value)) {
                return entry;
            }
        }
        
        return unkown;
    }
    
    public static void assignLiteral(XAttributable event, XAttributeLiteral template, String value) {
        assignLiteral(event.getAttributes(), template, value);
    }

    public static void assignInt(XAttributable event, XAttributeLiteral template, int value) {
        assignLiteral(event, template, Integer.toString(value));
    }

    public static void assignLong(XAttributable event, XAttributeLiteral template, long value) {
        assignLiteral(event, template, Long.toString(value));
    }
    
    public static void assignLiteral(XAttributeMap map, XAttributeLiteral template, String value) {
        if (value != null) {
            String valueTrim = value.trim();
            if (!valueTrim.isEmpty()) {
                XAttributeLiteral attr = (XAttributeLiteral) template.clone();

                attr.setValue(valueTrim);
                map.put(template.getKey(), attr);
            }
        }
    }

    public static void assignInt(XAttributeMap map, XAttributeLiteral template, int value) {
        assignLiteral(map, template, Integer.toString(value));
    }

    public static void assignLong(XAttributeMap map, XAttributeLiteral template, long value) {
        assignLiteral(map, template, Long.toString(value));
    }
    
    public static void assignEnum(XEvent event, XAttributeLiteral template, NamedEnum value) {
        if (value != null) {
            assignLiteral(event, template, value.getName());
        }
    }

}
