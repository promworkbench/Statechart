package org.processmining.models.statechart.decorate.ui.svg;

import java.awt.Color;

import org.processmining.models.statechart.decorate.IDecorated;
import org.processmining.utils.statechart.gfx.ColorUtil;
import org.processmining.utils.statechart.svg.SVGUtil;

import com.kitfox.svg.SVGElementException;

public abstract class AbstractSvgDecorator<T, E, M extends IDecorated<T>> 
    implements ISvgDecorator<T, E, M> {

    private boolean applied = false;
    
    private boolean extendLabel = true;

    @Override
    public void resetApplied() {
        applied = false;
    }

    @Override
    public boolean isApplied() {
        return applied;
    }
    
    protected void setApplied() {
        applied = true;
    }
    
    public void setExtendLabel(boolean extendLabel) {
        this.extendLabel = extendLabel;
    }
    
    protected void decorate(SvgLabelledElement node, Color color, Color fillColor, 
            Color textColor, String labelExtra) throws SVGElementException {
        
        SVGUtil.setAttr(node.element, "stroke", ColorUtil.rgbToHexString(color));
        SVGUtil.setAttr(node.element, "fill", ColorUtil.rgbToHexString(fillColor));
        if (node.label != null) {
            SVGUtil.setAttr(node.label, "fill", ColorUtil.rgbToHexString(textColor));
            if (extendLabel) {
                node.label.appendText(labelExtra);
            }
        }
//        node.setLabel("< <b>"
//                + node.getLabel() + "</b>\n" + labelExtra + " >");
    }

    protected void decorate(SvgLabelledElement edge, Color stroke, double penwidth,
            String label) throws SVGElementException {
        SVGUtil.setAttr(edge.element, "stroke", ColorUtil.rgbToHexString(stroke));
        SVGUtil.setAttr(edge.element, "stroke-width", Double.toString(penwidth));
        if (edge.label != null) {
            edge.label.appendText(" : " + label);
        }
//        edge.setLabel(label);
    }
}
