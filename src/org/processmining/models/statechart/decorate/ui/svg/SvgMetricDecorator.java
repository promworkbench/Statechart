package org.processmining.models.statechart.decorate.ui.svg;

import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.algorithms.statechart.m2m.ui.decorate.IUiDecorator;
import org.processmining.algorithms.statechart.m2m.ui.decorate.UiDecoration;
import org.processmining.models.statechart.decorate.IDecorated;
import org.processmining.ui.statechart.color.IColorMap;

import com.kitfox.svg.Line;
import com.kitfox.svg.Path;
import com.kitfox.svg.Polyline;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.Text;

public class SvgMetricDecorator<T, E, M extends IDecorated<T>> 
    extends AbstractSvgDecorator<T, E, M> {

    private IUiDecorator<T, E> uiDecorator;

    public SvgMetricDecorator(IUiDecorator<T, E> uiDecorator) {
        this.uiDecorator = uiDecorator;
    }
    
    @Override
    public void visitModel(M model, SVGDiagram svg) throws SVGElementException {
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
    public void decorateNode(T node, SvgLabelledElement svgNode) throws SVGElementException {
        decorate(svgNode, uiDecorator.getDecorationNode(node));
    }

    private void decorate(SvgLabelledElement svgElement, UiDecoration dec) throws SVGElementException {
        if (dec != null) {
            if (isNodeType(svgElement.element)) {
                // node type
                decorate(svgElement, 
                    dec.getColorStroke(), 
                    dec.getColorBackground(),
                    dec.getColorText(),
                    dec.getLabel()
                );
            } else {
                // edge type
                decorate(svgElement, 
                    dec.getColorBackground(), 
                    dec.getStrokeWidth(),
                    dec.getLabel()
                );
            }
            
//            String style = dotNode.getOption("style");
//            if (style == null || style.isEmpty()) {
//                style = "filled";
//            } else {
//                style += ",filled";
//            }
//            dotNode.setOption("style", style);
        }
    }

    private boolean isNodeType(SVGElement element) {
        return !(element instanceof Line
            || element instanceof Path
            || element instanceof Polyline
            || element instanceof Text);
    }

    @Override
    public void decorateEdge(E edge, SvgLabelledElement svgEdge) throws SVGElementException {
        decorate(svgEdge, uiDecorator.getDecorationEdge(edge));
    }

    @Override
    public void decorateRecursionBackArrow(T child, T entryNode, SvgLabelledElement e) throws SVGElementException {
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
