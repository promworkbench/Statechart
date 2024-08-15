package org.processmining.models.statechart.decorate.ui.dot;

import java.awt.Color;

import org.processmining.models.statechart.decorate.IDecorated;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.utils.statechart.gfx.ColorUtil;

public abstract class AbstractDotDecorator<T, E, M extends IDecorated<T>> 
    implements IDotDecorator<T, E, M> {

    private boolean applied = false;

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
    
    protected void decorate(DotNode node, Color color, Color fillColor, Color textColor,
            String labelExtra) {
        node.setOption("color", ColorUtil.rgbToHexString(color));
        node.setOption("fillcolor", ColorUtil.rgbToHexString(fillColor));
        node.setOption("fontcolor", ColorUtil.rgbToHexString(textColor));
        
        String label = node.getLabel();
        if (label.startsWith("<") && label.endsWith(">")) {
            StringBuilder labelBld = new StringBuilder();
            labelBld.append(label.substring(0, label.length() - 1));
            labelBld.append("<br/>");
            labelBld.append(labelExtra.replaceAll("\n", "<br/>"));
            labelBld.append(">");
            label = labelBld.toString();
        } else {
            StringBuilder labelBld = new StringBuilder();
            labelBld.append(label);
            labelBld.append("\n");
            labelBld.append(labelExtra);
            label = labelBld.toString();
        }
        node.setLabel(label);
//        node.setLabel("< <b>"
//                + node.getLabel() + "</b>\n" + labelExtra + " >");
    }

    protected void decorate(DotEdge edge, Color stroke, double penwidth,
            String label) {
        edge.setOption("color", ColorUtil.rgbToHexString(stroke));
        edge.setOption("penwidth", Double.toString(penwidth));
        edge.setLabel(label);
    }
}
