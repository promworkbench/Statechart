package org.processmining.utils.statechart.svg;

import java.awt.geom.AffineTransform;
import java.lang.reflect.Field;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.Use;
import com.kitfox.svg.animation.AnimationElement;

/**
 * Fixed implementation of SVG Use
 * 
 * @author mleemans
 *
 */
public class UseFixed extends Use {

    private static final long serialVersionUID = -133899355113182382L;
    
    private SVGDiagram svg;

    public void setRefSvg(SVGDiagram svg) throws SVGElementException {
        this.svg = svg;
        addAttribute("xlink:href", AnimationElement.AT_XML, svg.getXMLBase()
                .toString());
    }
    
    public void setPos(float x, float y) throws SVGElementException {
        addAttribute("x", AnimationElement.AT_XML, Float.toString(x));
        addAttribute("y", AnimationElement.AT_XML, Float.toString(y));
    }
    
    public void setSize(float wTo, float hTo) throws SVGElementException {
        addAttribute("width", AnimationElement.AT_XML, Float.toString(wTo));
        addAttribute("height", AnimationElement.AT_XML, Float.toString(hTo));
    }

    protected void build() throws SVGException {
        super.build();
        AffineTransform refXform = getFromParent("refXform");

        // transform absolute width height to scale/relative width height
        float width = getFloatFromParent("width");
        float height = getFloatFromParent("height");
        float scaleW = width / svg.getWidth();
        float scaleH = height / svg.getHeight();
        refXform.scale(scaleW, scaleH);
    }

    @SuppressWarnings("unchecked")
    private <T> T getFromParent(String fieldName) {
        try {
            Field field = Use.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(this);
        } catch (Exception e) {
            throw new SVGUtilException("Cannot access field in Use", e);
        }
    }

    private float getFloatFromParent(String fieldName) {
        try {
            Field field = Use.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getFloat(this);
        } catch (Exception e) {
            throw new SVGUtilException("Cannot access field in Use", e);
        }
    }
}
