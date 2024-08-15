package org.processmining.models.statechart.decorate.ui.svg;

import org.processmining.models.statechart.decorate.IDecorated;
import org.processmining.models.statechart.decorate.ui.IValueDecorator;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElementException;

public interface ISvgDecorator<Node, Edge, Model extends IDecorated<Node>> extends IValueDecorator {

    public void visitModel(Model model, SVGDiagram svg) throws SVGElementException;
    
    public void visitNode(Node node);
    
    public void visitEdge(Edge edge);

    public void finishVisit();
    
    public void decorateNode(Node node, SvgLabelledElement svgNode) throws SVGElementException;
    
    public void decorateEdge(Edge edge, SvgLabelledElement svgEdge) throws SVGElementException;

    public void decorateRecursionBackArrow(Node child, Node entryNode, SvgLabelledElement e) throws SVGElementException;
    
}
