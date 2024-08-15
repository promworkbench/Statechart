package org.processmining.utils.statechart.svg;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.util.Locale;
import java.util.Map;

import sun.swing.SwingUtilities2;

import com.kitfox.svg.Rect;
import com.kitfox.svg.RenderableElement;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.Text;
import com.kitfox.svg.Tspan;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.xml.StyleAttribute;

public class SVGUtil {

    public static final Locale FormatLocale = Locale.US;

    @SuppressWarnings("unchecked")
    public static <T> T getChild(SVGElement svg, Class<T> type) {
        T result = null;

        for (Object elm : svg.getChildren(null)) {
            if (type.isInstance(elm)) {
                result = (T) elm;
            }
        }

        return result;
    }
    
    public static String refUrlId(String id) {
        return "url(#" + id + ")";
    }

    public static StyleAttribute getAttr(SVGElement elm, String name)
            throws SVGException {
        StyleAttribute sty = new StyleAttribute();
        if (elm.getStyle(sty.setName(name))) {
            return sty;
        } else {
            return null;
        }
    }

    public static String getAttrStrDef(SVGElement elm, String name, String def)
            throws SVGException {
        StyleAttribute sty = new StyleAttribute();
        if (elm.getStyle(sty.setName(name))) {
            return sty.getStringValue();
        } else {
            return def;
        }
    }

    public static double getAttrDouble(SVGElement elm, String name) throws SVGException {
        return getAttrDouble(elm, name, Double.NaN);
    }
    
    public static double getAttrDouble(SVGElement elm, String name, double def) throws SVGException {
        StyleAttribute sty = getAttr(elm, name);
        if (sty != null) {
            return sty.getDoubleValue();
        } else {
            return def;
        }
    }

    public static void setAttr(SVGElement elm, String name, String value)
            throws SVGElementException {
        setAttr(elm, name, AnimationElement.AT_XML, value);
    }

    public static void setAttr(SVGElement elm, String name, double value) 
            throws SVGElementException {
        setAttr(elm, name, AnimationElement.AT_XML, Double.toString(value));
    }

    public static void setAttrs(SVGElement elm, Map<String, String> map) throws SVGElementException {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            setAttr(elm, entry.getKey(), entry.getValue());
        }
    }

    public static void setAttr(SVGElement elm, String name, int attribType,
            String value) throws SVGElementException {
        if (elm.hasAttribute(name, attribType)) {
            elm.setAttribute(name, attribType, value);
        } else {
            elm.addAttribute(name, attribType, value);
        }
    }

    public static void unsetAttr(SVGElement elm, String name)
            throws SVGElementException {
        unsetAttr(elm, name, AnimationElement.AT_XML);
    }

    public static void unsetAttr(SVGElement elm, String name, int attribType)
            throws SVGElementException {
        if (elm.hasAttribute(name, attribType)) {
            elm.removeAttribute(name, attribType);
        }
    }

    public static void addBBDebug(RenderableElement element)
            throws SVGException {
        addBBDebug(element, "red");
    }

    public static void addBBDebug(RenderableElement element, String color)
            throws SVGException {
        element.updateTime(0.0); // force BB calculations
        Rectangle2D bb = element.getBoundingBox();
        Rect bbsvg = new Rect();
        SVGUtil.setAttr(bbsvg, "x", Double.toString(bb.getMinX()));
        SVGUtil.setAttr(bbsvg, "y", Double.toString(bb.getMinY()));
        SVGUtil.setAttr(bbsvg, "width", Double.toString(bb.getWidth()));
        SVGUtil.setAttr(bbsvg, "height", Double.toString(bb.getHeight()));
        SVGUtil.setAttr(bbsvg, "stroke", "red");
        SVGUtil.setAttr(bbsvg, "fill-opacity", "0.0");
        element.loaderAddChild(null, bbsvg);
    }

    public static void setMultilineText(Text node, String label) throws SVGException {
        boolean first = true;
        Font font = buildFont(node);
        FontMetrics fontMetrics = SwingUtilities2.getFontMetrics(null, font);
        double h = fontMetrics.getHeight();
        
        for (String line : label.split("\n")) {
            Tspan tspan = new Tspan();
            tspan.setText(line);
            if (first) {
                first = false;
            } else {
                SVGUtil.setAttr(tspan, "dy", h);
            }
            node.appendTspan(tspan);
        }
    }

    public static Font buildFont(Text node) throws SVGException {
        int style = Font.PLAIN;
        if ("italic".equals(getAttr(node, "font-style"))) {
            style = Font.ITALIC;
        }
        int weight = Font.PLAIN;
        if ("bold".equals(getAttr(node, "font-weight"))) {
            weight = Font.BOLD;
        }
        String fontFamily = getAttrStrDef(node, "font-family", "Sans Serif");
        int fontSize = (int) getAttrDouble(node, "font-size", 12);
        
        return new Font(fontFamily, style | weight, fontSize);
    }
}
