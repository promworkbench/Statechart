package org.processmining.algorithms.statechart.m2m.ui;

import java.io.InputStream;

import org.processmining.algorithms.statechart.m2m.TransformationException;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot2Image;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;
import org.processmining.utils.statechart.svg.SVGCollection;
import org.processmining.utils.statechart.svg.SVGIterator;
import org.processmining.utils.statechart.svg.SVGUtil;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.kitfox.svg.Group;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.Text;
import com.kitfox.svg.xml.StyleAttribute;

public class PetrinetDot2Svg implements Function<Dot, SVGDiagram> {

    // DOT Font size is off, reduce it by 2 points to fit text
    private static final float DotFontsizeError = 2.0f;

    private static final String SVGNameDot = "dot";

    private final SVGCollection svgCollection;

    public PetrinetDot2Svg(Dot.GraphDirection layoutDir) {

        svgCollection = new SVGCollection();
    }
 
    // allows us to access the package private fields
    private Petrinet2Dot ptnet2dot;

    public void setDotTransformator(Petrinet2Dot ptnet2dot) {
        this.ptnet2dot = ptnet2dot;
    }
    
    @Override
    public SVGDiagram apply(Dot input) {
        return transform(input);
    }

    public SVGDiagram transform(Dot dot) {
        Preconditions.checkNotNull(dot);

        // transform dot to svg
        InputStream dotStream = Dot2Image.dot2imageInputStream(dot, Type.svg);
        final SVGDiagram svg = svgCollection.loadSVG(dotStream, SVGNameDot);

        // correcting svg: Font size is off, reduce it to fit text
        // in boxes
        try {
            for (SVGElement elm : new SVGIterator(svg)) {
                if (elm instanceof Text) {
                    StyleAttribute val = SVGUtil.getAttr(elm, "font-size");
                    if (val != null) {
                        float newVal = val.getFloatValueWithUnits()
                                - DotFontsizeError;
                        SVGUtil.setAttr(elm, "font-size",
                                Float.toString(newVal));
                    }
                }
            }

            if (ptnet2dot != null) {
                for (String nodeId : ptnet2dot.dotid2nodeid.keySet()) {
                    Group svgN = (Group) svg.getElement(nodeId);
                    SVGUtil.setAttr(svgN, UiDataConstants.KeyDataNodeId, 
                        ptnet2dot.dotid2nodeid.get(nodeId));
                }
            }
            
        } catch (SVGException e) {
            throw new TransformationException(
                    "SVG exception during correction: DOT font sizes", e);
        }
        return svg;
    }
}
