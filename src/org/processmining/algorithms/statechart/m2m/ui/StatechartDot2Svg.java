package org.processmining.algorithms.statechart.m2m.ui;

import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.processmining.algorithms.statechart.m2m.TransformationException;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.Dot2Image;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;
import org.processmining.ui.statechart.svg.SvgIcons;
import org.processmining.utils.statechart.svg.ISVGReference;
import org.processmining.utils.statechart.svg.SVGCollection;
import org.processmining.utils.statechart.svg.SVGIterator;
import org.processmining.utils.statechart.svg.SVGUtil;
import org.processmining.utils.statechart.svg.SvgUseUtil;
import org.processmining.utils.statechart.svg.SvgUseUtil.Margin;
import org.processmining.utils.statechart.svg.UseFixed;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.kitfox.svg.Group;
import com.kitfox.svg.Line;
import com.kitfox.svg.Path;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.Text;
import com.kitfox.svg.pathcmd.Cubic;
import com.kitfox.svg.pathcmd.MoveTo;
import com.kitfox.svg.pathcmd.PathCommand;
import com.kitfox.svg.xml.StyleAttribute;

public class StatechartDot2Svg implements Function<Dot, SVGDiagram> {

    // DOT Font size is off, reduce it by 2 points to fit text
    private static final float DotFontsizeError = 2.0f;

    private static final String SVGNameDot = "dot";

    private final SVGCollection svgCollection;
    private final boolean layoutHorizontal;

    public StatechartDot2Svg(Dot.GraphDirection layoutDir) {
        this.layoutHorizontal = (layoutDir == GraphDirection.leftRight || layoutDir == GraphDirection.rightLeft);
        ;

        svgCollection = new SVGCollection();

        // load icons
        svgCollection.loadSVG(SvgIcons.IconStart);
        svgCollection.loadSVG(SvgIcons.IconEnd);
        svgCollection.loadSVG(SvgIcons.IconPlus);
        svgCollection.loadSVG(SvgIcons.IconMinus);
        svgCollection.loadSVG(SvgIcons.IconRecurrent);
        svgCollection.loadSVG(SvgIcons.IconError);
    }

    // allows us to access the package private fields
    private Statechart2Dot sc2dot;

    public void setDotTransformator(Statechart2Dot sc2dot) {
        this.sc2dot = sc2dot;
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
        } catch (SVGException e) {
            throw new TransformationException(
                    "SVG exception during correction: DOT font sizes", e);
        }

        // correcting svg: AND divisions
        try {
            for (String clusterId : sc2dot.trackAndClusters) {
                Group svgC = (Group) svg.getElement(clusterId);
                Rectangle2D bbC = svgC.getBoundingBox();

                // Use list of regions to track, sort by drawing order,
                // and then find adjacent pairs for drawing div-lines
                List<String> andDivs = new ArrayList<>();
                andDivs.addAll(sc2dot.trackAndDivs.get(clusterId));
                Collections.sort(andDivs, new Comparator<String>() {
                    @Override
                    public int compare(String paramT1, String paramT2) {
                        try {
                            Group svgDiv1 = (Group) svg.getElement(paramT1);
                            Group svgDiv2 = (Group) svg.getElement(paramT2);
                            Rectangle2D bb1 = svgDiv1.getBoundingBox();
                            Rectangle2D bb2 = svgDiv2.getBoundingBox();

                            // AND dividers are ortogonal to the layout
                            // direction
                            if (layoutHorizontal) {
                                return Double.compare(bb1.getY(), bb2.getY());
                            } else {
                                return Double.compare(bb1.getX(), bb2.getX());
                            }
                        } catch (SVGException e) {
                            throw new TransformationException(
                                    "SVG exception during correction: stretch AND divisions",
                                    e);
                        }
                    }
                });

                for (int i = 1; i < andDivs.size(); i++) {
                    Group svgDiv1 = (Group) svg.getElement(andDivs.get(i - 1));
                    Group svgDiv2 = (Group) svg.getElement(andDivs.get(i));

                    // correcting svg: remove AND region border
                    // (was needed for a bounding box to be computed)
                    // (no, dot 2 svg doesn't keep the hierarchy)
                    Path boxDiv1 = SVGUtil.getChild(svgDiv1, Path.class);
                    SVGUtil.unsetAttr(boxDiv1, "stroke");
                    Path boxDiv2 = SVGUtil.getChild(svgDiv2, Path.class);
                    SVGUtil.unsetAttr(boxDiv2, "stroke");

                    // correcting svg: add AND division line
                    Rectangle2D bbDiv1 = svgDiv1.getBoundingBox();
                    Rectangle2D bbDiv2 = svgDiv2.getBoundingBox();
                    _createDivLine(bbDiv1, bbDiv2, bbC, svgC, layoutHorizontal);
                }
            }
        } catch (SVGException e) {
            throw new TransformationException(
                    "SVG exception during correction: stretch AND divisions", e);
        }

        // correction svg: connector from cluster boundary for start nodes
        /* TODO: DOT Error: trouble in init_rank for JUnit 4.12, RAD, path 45%
        try {
            Group edgeContainer = new Group();
            svg.getRoot().getChild(0).loaderAddChild(null, edgeContainer);
            
            for (DotEdge edge : sc2dot.trackStartEdges.keySet()) {
                ISCTransition scEdge = sc2dot.trackStartEdges.get(edge);
                Group sourceEdge = (Group) svg.getElement(edge.getId());
                Group targetElement;//, targetCluster;
                
                if (!scEdge.isReverse()) {
                    // normal edge
                    targetElement = (Group) svg.getElement(edge.getTarget().getId());
//                    targetCluster = (Group) svg.getElement(edge.getOption("lhead"));
                } else {
                    // reversed edge
                    targetElement = (Group) svg.getElement(edge.getSource().getId());
//                    targetCluster = (Group) svg.getElement(edge.getOption("ltail"));
                }
                
                Rectangle2D bbDivSource = sourceEdge.getBoundingBox();
                Rectangle2D bbDivTarget = targetElement.getBoundingBox();
                _createStartConnectorLine(sourceEdge, bbDivSource, bbDivTarget, 
                        edgeContainer, scEdge.isReverse());
//                        targetCluster, scEdge.isReverse());
            }
        } catch (SVGException e) {
            throw new TransformationException(
                    "SVG exception during correction: start nodes", e);
        }
        //*/
        
        try {
            // add icons to special nodes
            for (String nodeId : sc2dot.trackStartNodes) {
                _addIcon(svg, nodeId, SvgIcons.IconStart,
                        SvgUseUtil.Anchor.Center, 0.65f, Margin.Zero);
            }
            for (String nodeId : sc2dot.trackEndNodes) {
                _addIcon(svg, nodeId, SvgIcons.IconEnd,
                        SvgUseUtil.Anchor.Center, 0.30f, Margin.Zero);
            }
            
            Margin iconMargin = new Margin(5.0f);

            for (String nodeId : sc2dot.dotid2sc.keySet()) {
                Group svgN = (Group) svg.getElement(nodeId);
                SVGUtil.setAttr(svgN, UiDataConstants.KeyDataNodeId, sc2dot.dotid2sc
                        .get(nodeId).getId());
            }
            
            for (String nodeId : sc2dot.trackRecurrentNodes) {
                Group svgN = (Group) svg.getElement(nodeId);
                UseFixed icon = _addIcon(svg, nodeId, SvgIcons.IconRecurrent,
                        SvgUseUtil.Anchor.MiddleLeft, 15.0f, 15.0f, iconMargin);
                
                Text tNode = SVGUtil.getChild(svgN, Text.class);
                String color = SVGUtil.getAttrStrDef(tNode, "fill", "black");
                SVGUtil.setAttr(icon, "fill", color);
            }
            
            for (String nodeId : sc2dot.trackErrorNodes) {
                Group svgN = (Group) svg.getElement(nodeId);
                UseFixed icon = _addIcon(svg, nodeId, SvgIcons.IconError,
                        SvgUseUtil.Anchor.MiddleLeft, 15.0f, 15.0f, iconMargin);
                
                Text tNode = SVGUtil.getChild(svgN, Text.class);
                String color = SVGUtil.getAttrStrDef(tNode, "fill", "black");
                SVGUtil.setAttr(icon, "fill", color);
            }
            
            for (String nodeId : sc2dot.trackCollapsedNodes) {
                Group svgN = (Group) svg.getElement(nodeId);
                UseFixed icon = _addIcon(svg, nodeId, SvgIcons.IconPlus,
                        SvgUseUtil.Anchor.MiddleLeft, 15.0f, 15.0f, iconMargin);
                
//                SVGUtil.setAttr(svgN, "data-nodeId", sc2dot.dotid2sc
//                        .get(nodeId).getId());
                SVGUtil.setAttr(svgN, UiDataConstants.KeyDataCollapsible, "true");
                SVGUtil.setAttr(icon, UiDataConstants.KeyDataCollapseUi, "true");

                Text tNode = SVGUtil.getChild(svgN, Text.class);
                String color = SVGUtil.getAttrStrDef(tNode, "fill", "black");
                SVGUtil.setAttr(icon, "fill", color);
            }
            for (String nodeId : sc2dot.trackExpandedNodes) {
                Group svgN = (Group) svg.getElement(nodeId);
                UseFixed icon = _addIcon(svg, nodeId, SvgIcons.IconMinus,
                        SvgUseUtil.Anchor.TopLeft, 15.0f, 15.0f, iconMargin);
                
//                SVGUtil.setAttr(svgN, "data-nodeId", sc2dot.dotid2sc
//                        .get(nodeId).getId());
                SVGUtil.setAttr(svgN, UiDataConstants.KeyDataCollapsible, "true");
                SVGUtil.setAttr(icon, UiDataConstants.KeyDataCollapseUi, "true");
                
                Text tNode = SVGUtil.getChild(svgN, Text.class);
                String color = SVGUtil.getAttrStrDef(tNode, "fill", "black");
                SVGUtil.setAttr(icon, "fill", color);
            }
        } catch (SVGException e) {
            throw new TransformationException(
                    "SVG exception during special nodes", e);
        }

        return svg;
    }

    private UseFixed _addIcon(SVGDiagram svg, String nodeId, ISVGReference icon,
            SvgUseUtil.Anchor anchor, float toWidth, float toHeight,
            Margin margin) {
        try {
            Group svgN = (Group) svg.getElement(nodeId);
            SVGDiagram ref = svgCollection.getDiagram(icon);

            // SVGUtil.addBBDebug(svgN);
            return SvgUseUtil.addSvg(svgN, ref, anchor, toWidth, toHeight, margin);

        } catch (SVGException e) {
            throw new TransformationException(
                    "SVG exception during adding icon", e);
        }
    }

    private UseFixed _addIcon(SVGDiagram svg, String nodeId, ISVGReference icon,
            SvgUseUtil.Anchor anchor, float ratio, Margin margin) {
        try {
            Group svgN = (Group) svg.getElement(nodeId);
            SVGDiagram ref = svgCollection.getDiagram(icon);

            // SVGUtil.addBBDebug(svgN);
            return SvgUseUtil.addSvg(svgN, ref, anchor, ratio, margin);

        } catch (SVGException e) {
            throw new TransformationException(
                    "SVG exception during adding icon", e);
        }
    }

    private void _createDivLine(Rectangle2D bbDiv1, Rectangle2D bbDiv2,
            Rectangle2D bbC, Group target, boolean horizontal) {
        try {
            float x1, y1, x2, y2;

            if (horizontal) {
                float baseY = _calcMean(bbDiv1.getMinY(), bbDiv1.getMaxY(),
                        bbDiv2.getMinY(), bbDiv2.getMaxY());
                y1 = baseY;
                y2 = baseY;
                x1 = (float) bbC.getMinX();
                x2 = (float) bbC.getMaxX();
            } else {
                float baseX = _calcMean(bbDiv1.getMinX(), bbDiv1.getMaxX(),
                        bbDiv2.getMinX(), bbDiv2.getMaxX());
                y1 = (float) bbC.getMinY();
                y2 = (float) bbC.getMaxY();
                x1 = baseX;
                x2 = baseX;
            }

            Line svgLine = new Line();
            SVGUtil.setAttr(svgLine, "x1", Float.toString(x1));
            SVGUtil.setAttr(svgLine, "x2", Float.toString(x2));
            SVGUtil.setAttr(svgLine, "y1", Float.toString(y1));
            SVGUtil.setAttr(svgLine, "y2", Float.toString(y2));
            SVGUtil.setAttr(svgLine, "stroke-dasharray", "10, 10");
            SVGUtil.setAttr(svgLine, "stroke", "black");

            target.loaderAddChild(null, svgLine);

        } catch (SVGException e) {
            throw new TransformationException(
                    "Error during stretch path: SVG Exception", e);
        }
    }

    private float _calcMean(double min1, double max1, double min2, double max2) {
        if (max1 < min2) {
            return (float) (max1 + min2) / 2.0f;
        } else {
            return (float) (min1 + max2) / 2.0f;
        }
    }

    private void _createStartConnectorLine(Group sourceEdge, Rectangle2D bbDivSource,
            Rectangle2D bbDivTarget, Group targetElement, boolean isEdgeReversed) {
        try {
            // get path
            Path edgePath = SVGUtil.getChild(sourceEdge, Path.class);
            String pathStr = SVGUtil.getAttr(edgePath, "d").getStringValue();
            PathCommand[] path = SVGElement.parsePathList(pathStr);
            
            // get edge end point for start
            float eeX = Float.NaN, eeY = Float.NaN;
            if (path.length > 0) {
                PathCommand pc;
                if (isEdgeReversed) {
                    // look at start
                    pc = path[0];
                } else {
                    // look at end
                    pc = path[path.length - 1];
                }
                if (pc instanceof Cubic) {
                    eeX = ((Cubic) pc).x;
                    eeY = ((Cubic) pc).y;
                } else if (pc instanceof MoveTo) {
                    eeX = ((MoveTo) pc).x;
                    eeY = ((MoveTo) pc).y;
                }
            }
            if (Float.isNaN(eeX) || Float.isNaN(eeY)) {
                // fallback, use bb
                eeX = _computeSourceConnector(
                    (float) bbDivSource.getMinX(), (float) bbDivSource.getMaxX(),
                    (float) bbDivTarget.getMinX(), (float) bbDivTarget.getMaxX()
                );
                eeY = _computeSourceConnector(
                    (float) bbDivSource.getMinY(), (float) bbDivSource.getMaxY(),
                    (float) bbDivTarget.getMinY(), (float) bbDivTarget.getMaxY()
                );
            }
            
            // correct for target element bb
//            Rectangle2D bbTElm = targetElement.getBoundingBox();
//            eeX = MathUtils.clamp(eeX, (float) bbTElm.getMinX(), (float) bbTElm.getMaxX());
//            eeY = MathUtils.clamp(eeY, (float) bbTElm.getMinY(), (float) bbTElm.getMaxY());
            
            // get connector point for end
            float cX = _computeTargetConnector(eeX, (float) bbDivTarget.getMinX(), (float) bbDivTarget.getMaxX());
            float cY = _computeTargetConnector(eeY, (float) bbDivTarget.getMinY(), (float) bbDivTarget.getMaxY());

//            cX = MathUtils.clamp(cX, (float) bbTElm.getMinX(), (float) bbTElm.getMaxX());
//            cY = MathUtils.clamp(cY, (float) bbTElm.getMinY(), (float) bbTElm.getMaxY());
                    
            Line svgLine = new Line();
            SVGUtil.setAttr(svgLine, "x1", Float.toString(eeX));
            SVGUtil.setAttr(svgLine, "x2", Float.toString(cX));
            SVGUtil.setAttr(svgLine, "y1", Float.toString(eeY));
            SVGUtil.setAttr(svgLine, "y2", Float.toString(cY));
            SVGUtil.setAttr(svgLine, "stroke-dasharray", "2, 2");
            SVGUtil.setAttr(svgLine, "stroke", "gray");

            targetElement.loaderAddChild(null, svgLine);
            
        } catch (SVGException e) {
            throw new TransformationException(
                "Error during create start connector line: SVG Exception", e);
        }
    }

    private float _computeSourceConnector(float minXS, float maxXS, float minXT,
            float maxXT) {
        if (maxXS < minXT) {
            // use right edge of S
            return maxXS;
        } else if (maxXT < minXS) {
            // use left edge of S
            return minXS;
        } else {
            // check for the shorter overlap interval, and use center
            if (maxXT - minXS < maxXS - minXT) {
                return minXS + 0.5f * (maxXT - minXS); 
            } else {
                return minXT + 0.5f * (maxXS - minXT);
            }
        }
    }

    private float _computeTargetConnector(float eeX, float minX, float maxX) {
        if (eeX < minX) {
            return minX;
        } else if (maxX < eeX) {
            return maxX;
        } else {
            float size = maxX - minX;
            if (size != 0) {
                float alpha = (eeX - minX) / size;
                return minX + alpha * size;
            } else {
                return minX;
            }
        }
    }
}
