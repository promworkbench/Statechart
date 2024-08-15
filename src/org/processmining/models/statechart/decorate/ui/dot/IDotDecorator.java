package org.processmining.models.statechart.decorate.ui.dot;

import org.processmining.models.statechart.decorate.IDecorated;
import org.processmining.models.statechart.decorate.ui.IValueDecorator;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;

public interface IDotDecorator<T, E, M extends IDecorated<T>> extends IValueDecorator {

    public void visitModel(M model, Dot dot);
    
    public void visitNode(T node);
    
    public void visitEdge(E edge);

    public void finishVisit();
    
    public void decorateNode(T node, DotNode dotNode);
    
    public void decorateEdge(E edge, DotEdge dotEdge);

    public void decorateRecursionBackArrow(T child, T entryNode, DotEdge e);
    
}
