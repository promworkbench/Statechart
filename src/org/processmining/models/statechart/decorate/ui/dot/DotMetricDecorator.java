package org.processmining.models.statechart.decorate.ui.dot;

import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.algorithms.statechart.m2m.ui.decorate.IUiDecorator;
import org.processmining.algorithms.statechart.m2m.ui.decorate.UiDecoration;
import org.processmining.models.statechart.decorate.IDecorated;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.ui.statechart.color.IColorMap;

public class DotMetricDecorator<T, E, M extends IDecorated<T>> 
    extends AbstractDotDecorator<T, E, M> {

    private IUiDecorator<T, E> uiDecorator;

    public DotMetricDecorator(IUiDecorator<T, E> uiDecorator) {
        this.uiDecorator = uiDecorator;
    }
    
    @Override
    public void visitModel(M model, Dot dot) {
        uiDecorator.visitModel(model);
    }

    @Override
    public void visitNode(T node) {
        uiDecorator.visitNode(node);
    }

    @Override
    public void visitEdge(E edge) {
        uiDecorator.visitEdge(edge);
    }


    @Override
    public void finishVisit() {
        uiDecorator.finishVisit();
        setApplied();
    }

    @Override
    public void decorateNode(T node, DotNode dotNode) {
        UiDecoration dec = uiDecorator.getDecorationNode(node);
        if (dec != null) {
            decorate(dotNode, 
                dec.getColorStroke(), 
                dec.getColorBackground(), 
                dec.getColorText(),
                dec.getLabel()
            );
            String style = dotNode.getOption("style");
            if (style == null || style.isEmpty()) {
                style = "filled";
            } else {
                style += ",filled";
            }
            dotNode.setOption("style", style);
        }
    }

    @Override
    public void decorateEdge(E edge, DotEdge dotEdge) {
        UiDecoration dec = uiDecorator.getDecorationEdge(edge);
        if (dec != null) {
            decorate(dotEdge, 
                dec.getColorBackground(), 
                dec.getStrokeWidth(),
                dec.getLabel()
            );
//            String style = dotNode.getOption("style");
//            if (style == null || style.isEmpty()) {
//                style = "filled";
//            } else {
//                style += ",filled";
//            }
//            dotNode.setOption("style", style);
        }
    }

    @Override
    public void decorateRecursionBackArrow(T child, T entryNode, DotEdge e) {
//        UiDecoration dec = uiDecorator.getDecorationEdge(entryNode);
//        if (dec != null) {
//            decorate(dotNode, 
//                dec.getColorStroke(), 
//                dec.getStrokeWidth(),
//                dec.getLabel()
//            );
////            String style = dotNode.getOption("style");
////            if (style == null || style.isEmpty()) {
////                style = "filled";
////            } else {
////                style += ",filled";
////            }
////            dotNode.setOption("style", style);
//        }
    }

    @Override
    public IColorMap getNodeColorMap() {
        return uiDecorator.getNodeColorMap();
    }

    @Override
    public IMetricValueConvertor getNodeValueConvertor() {
        return uiDecorator.getNodeValueConvertor();
    }

    @Override
    public IColorMap getEdgeColorMap() {
        return uiDecorator.getEdgeColorMap();
    }

    @Override
    public IMetricValueConvertor getEdgeValueConvertor() {
        return uiDecorator.getEdgeValueConvertor();
    }

}
