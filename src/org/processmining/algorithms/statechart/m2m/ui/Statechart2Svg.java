package org.processmining.algorithms.statechart.m2m.ui;

import gnu.trove.map.hash.THashMap;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;

import org.apache.commons.math3.util.Pair;
import org.processmining.algorithms.statechart.layout.ProcessGraphLayout;
import org.processmining.algorithms.statechart.m2m.ui.layout.StatechartLayoutNode;
import org.processmining.algorithms.statechart.m2m.ui.style.StatechartStyle;
import org.processmining.models.statechart.sc.ISCRegion;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.ISCTransition;
import org.processmining.models.statechart.sc.SCStateType;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.utils.statechart.svg.ISVGReference;
import org.processmining.utils.statechart.svg.SVGCollection;
import org.processmining.utils.statechart.svg.SVGUtil;
import org.processmining.utils.statechart.svg.UseFixed;

import com.google.common.base.Function;
import com.kitfox.svg.Circle;
import com.kitfox.svg.Defs;
import com.kitfox.svg.Group;
import com.kitfox.svg.Line;
import com.kitfox.svg.Marker;
import com.kitfox.svg.Path;
import com.kitfox.svg.Rect;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGLoaderHelper;
import com.kitfox.svg.SVGRoot;
import com.kitfox.svg.ShapeElement;
import com.kitfox.svg.Text;

public class Statechart2Svg implements Function<Pair<Statechart, ProcessGraphLayout<StatechartLayoutNode>>, SVGDiagram> {

    private static final String ID_MARKER_ARROW_MSG = "arrow";
    
    private StatechartStyle style;

    private SVGCollection collection;

    private SVGDiagram diagram;

    private SVGRoot root;

    private SVGLoaderHelper helper;

    private ProcessGraphLayout<StatechartLayoutNode> layout;

    private THashMap<ISCState, StatechartLayoutNode> layoutLookup;

    public Statechart2Svg(StatechartStyle style) {
        this.style = style;
    }
    
    @Override
    public SVGDiagram apply(Pair<Statechart, ProcessGraphLayout<StatechartLayoutNode>> input) {
        try {
            return transform(input.getFirst(), input.getSecond());
        } catch (SVGException e) {
            return null;
        }
    }

    public SVGDiagram transform(Statechart input, ProcessGraphLayout<StatechartLayoutNode> layout) throws SVGException {
        Rectangle2D rootBounds = layout.getBounds();
        final double offset = style.getCanvasPadding();

        this.layout = layout;
        layoutLookup = new THashMap<>();
        for (StatechartLayoutNode layoutNode : layout.getModel().getRoot().iteratePreOrder()) {
            if (layoutNode.getState() != null) {
                layoutLookup.put(layoutNode.getState(), layoutNode);
            }
        }
        
        // setup svg basis
        collection = new SVGCollection();
        
        diagram = collection.createDiagram("eptree");
        root = new SVGRoot();
        diagram.setRoot(root);
        
        helper = collection.createLoaderHelper(diagram);
        
        Rect background = new Rect();
        SVGUtil.setAttr(background, "fill", "white");
        root.loaderAddChild(helper, background);
        
        Group rootGroup = new Group();
        SVGUtil.setAttr(rootGroup, "transform", "translate(" + offset + " " + offset + ")");
        root.loaderAddChild(helper, rootGroup);
        
        // add content
        _defineMarkers(root, helper);
        _renderContent(input, rootGroup);

        // wrapup
        final double width = rootBounds.getWidth() + style.getCanvasPadding() * 2;
        final double height = rootBounds.getHeight() + style.getCanvasPadding() * 2;
        SVGUtil.setAttr(background, "width", width);
        SVGUtil.setAttr(background, "height", height);
        SVGUtil.setAttr(root, "width", width);
        SVGUtil.setAttr(root, "height", height);
        diagram.updateTime(0);
        
        return diagram;
    }

    private void _defineMarkers(Group group, SVGLoaderHelper helper) throws SVGElementException {
        Defs defs = new Defs();
        group.loaderAddChild(helper, defs);
        
        {
            Marker marker = new Marker();
            SVGUtil.setAttr(marker, "id", ID_MARKER_ARROW_MSG);
            SVGUtil.setAttr(marker, "markerWidth", "5");
            SVGUtil.setAttr(marker, "markerHeight", "5");
            SVGUtil.setAttr(marker, "refx", "0");
            SVGUtil.setAttr(marker, "refy", "1");
            SVGUtil.setAttr(marker, "orient", "auto");
            SVGUtil.setAttr(marker, "markerUnits", "userSpaceOnUse");
            defs.loaderAddChild(helper, marker);
            
            Path path = new Path();
            SVGUtil.setAttr(path, "d", "M-1,-0.5 V0.5 L0,0 Z");
            SVGUtil.setAttr(path, "fill", "black");
            marker.loaderAddChild(helper, path);
        }
    }
    
    private void _renderContent(Statechart input, Group rootGroup) throws SVGException {

        if (StatechartStyle.renderBoundingBoxes) {
            // Render bounding boxes
            for (StatechartLayoutNode key : layout.getNodeBounds().keySet()) {
                Double bound = layout.getNodeBounds().get(key);
                double centerOffset = layout.getCenterOffsets().get(key);
    
                Rect nodeBox = new Rect();
                SVGUtil.setAttr(nodeBox, "x", bound.getMinX());
                SVGUtil.setAttr(nodeBox, "y", bound.getMinY());
                SVGUtil.setAttr(nodeBox, "width", bound.getWidth());
                SVGUtil.setAttr(nodeBox, "height", bound.getHeight());
                if (layout.getConfiguration().isLayoutOrtogonal(key)) {
                    SVGUtil.setAttr(nodeBox, "stroke", "red");
                } else {
                    SVGUtil.setAttr(nodeBox, "stroke", "green");
                }
                SVGUtil.setAttr(nodeBox, "fill", "none");
                rootGroup.loaderAddChild(helper, nodeBox);
                
                Line line = new Line();
                if (style.getDirection().isHorizontal()) {
                    SVGUtil.setAttr(line, "x1", bound.getMinX());
                    SVGUtil.setAttr(line, "y1", bound.getMinY() + centerOffset);
                    SVGUtil.setAttr(line, "x2", bound.getMaxX());
                    SVGUtil.setAttr(line, "y2", bound.getMinY() + centerOffset);
                } else {
                    SVGUtil.setAttr(line, "x1", bound.getMinX() + centerOffset);
                    SVGUtil.setAttr(line, "y1", bound.getMinY());
                    SVGUtil.setAttr(line, "x2", bound.getMinX() + centerOffset);
                    SVGUtil.setAttr(line, "y2", bound.getMaxY());
                }
                
                if (layout.getConfiguration().isLayoutOrtogonal(key)) {
                    SVGUtil.setAttr(line, "stroke", "red");
                } else {
                    SVGUtil.setAttr(line, "stroke", "green");
                }
                SVGUtil.setAttr(line, "fill", "none");
                SVGUtil.setAttr(line, "stroke-dasharray", "5, 5");
                rootGroup.loaderAddChild(helper, line);
            }
        }
        
        // render states
        Iterator<ISCState> it = input.iteratePreOrder().iterator();
        it.next(); // skip base statechart in rendering
//        while (it.hasNext()) {
        for (ISCState state : input.iteratePreOrder()) {
            if (state == input) {
                // nop
            } else if (input.isInitialState(state)) {
                _renderInitialState(rootGroup, state);
            } else if (input.isEndState(state)) {
                _renderFinalState(rootGroup, state);
            } else {
                switch (state.getStateType()) {
                case PointPseudo:
                    _renderPoint(rootGroup, state);
                    break;
                case SplitPseudo:
                case JoinPseudo:
                    _renderSplitPseudo(rootGroup, state);
                    break;
                default:
                    _renderState(rootGroup, state);
                    break;
                        
                }
            }
        }
        
        // render transitions
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        
        DecimalFormat nformat = new DecimalFormat("0.0000", symbols);
        nformat.setGroupingUsed(false);
        
        for (ISCRegion region : input.getRegions().get(0).iteratePreOrder()) {
            for (ISCTransition tran : region.getTransitions()) {
                Rectangle2D.Double boundFrom = getBound(tran.getFrom());
                Rectangle2D.Double boundTo = getBound(tran.getTo());
                
                ShapeElement arrow = null;
                
                if (style.getDirection().isHorizontal()) {
                    // horizontal layout
                    if (tran.getFrom() == tran.getTo()) {
                        // self loop
                        arrow = new Path();
                        SVGUtil.setAttr(arrow, "d", 
                            "M" + nformat.format(boundFrom.getCenterX() + 0.25 * boundFrom.getWidth()) 
                                + " " + nformat.format(boundFrom.getMaxY())
                            + " Q " + nformat.format(boundFrom.getCenterX()) 
                                + " " + nformat.format(boundFrom.getMaxY() + style.getLoopOffset())
                            + " " + nformat.format(boundFrom.getCenterX() - 0.25 * boundFrom.getWidth()) 
                                + " " + nformat.format(boundFrom.getMaxY())
                        );
                        
                    } else if (boundFrom.getMaxX() <= boundTo.getMinX()) {
                        // forward arc
                        arrow = new Line();
                        SVGUtil.setAttr(arrow, "x1", boundFrom.getMaxX());
                        SVGUtil.setAttr(arrow, "y1", boundFrom.getCenterY());
                        SVGUtil.setAttr(arrow, "x2", boundTo.getMinX());
                        SVGUtil.setAttr(arrow, "y2", boundTo.getCenterY());
                    } else {
                        // backward arc
                        arrow = new Line();
                        SVGUtil.setAttr(arrow, "x1", boundFrom.getMinX());
                        SVGUtil.setAttr(arrow, "y1", boundFrom.getCenterY());
                        SVGUtil.setAttr(arrow, "x2", boundTo.getMaxX());
                        SVGUtil.setAttr(arrow, "y2", boundTo.getCenterY());
                    }
                } else {
                    // vertical layout
                    if (tran.getFrom() == tran.getTo()) {
                        // self loop
                        arrow = new Path();
                        SVGUtil.setAttr(arrow, "d", 
                            "M" + nformat.format(boundFrom.getMaxX())
                                + " " + nformat.format(boundFrom.getCenterY() + 0.25 * boundFrom.getHeight())
                            + " Q " + nformat.format(boundFrom.getMaxX() + style.getLoopOffset()) 
                                + " " + nformat.format(boundFrom.getCenterY())
                            + " " + nformat.format(boundFrom.getMaxX()) 
                                + " " + nformat.format(boundFrom.getCenterY() - 0.25 * boundFrom.getHeight())
                        );
                        
                    } else if (boundFrom.getMaxY() <= boundTo.getMinY()) {
                        // forward arc
                        arrow = new Line();
                        SVGUtil.setAttr(arrow, "x1", boundFrom.getCenterX());
                        SVGUtil.setAttr(arrow, "y1", boundFrom.getMaxY());
                        SVGUtil.setAttr(arrow, "x2", boundTo.getCenterX());
                        SVGUtil.setAttr(arrow, "y2", boundTo.getMinY());
                    } else {
                        // backward arc
                        arrow = new Line();
                        SVGUtil.setAttr(arrow, "x1", boundFrom.getCenterX());
                        SVGUtil.setAttr(arrow, "y1", boundFrom.getMinY());
                        SVGUtil.setAttr(arrow, "x2", boundTo.getCenterX());
                        SVGUtil.setAttr(arrow, "y2", boundTo.getMaxY());
                    }
                }
                
                SVGUtil.setAttr(arrow, "marker-end", SVGUtil.refUrlId(ID_MARKER_ARROW_MSG));
                SVGUtil.setAttrs(arrow, style.getStyleEdge());
                
                rootGroup.loaderAddChild(helper, arrow);
            }
        }
    }

    private void _renderState(Group rootGroup, ISCState state) throws SVGException {
        StatechartLayoutNode layoutNode = layoutLookup.get(state);
        Rectangle2D.Double bounds = layout.getNodeBounds().get(layoutNode);
        
        boolean hasSymbol = style.nodeHasSymbol(state);
        boolean hasLabel = style.nodeHasLabel(state);
//        Padding padding = style.getPaddingNode(state);
        
        double nodeOffset = 0;
        if (hasSymbol) {
            nodeOffset = style.getNodeSymbolWidth()
                    + style.getNodeSymbolPadding();
            ISVGReference icon = style.getNodeIcon(state);
            SVGDiagram ref = collection.loadSVG(icon);
            
            double x = bounds.getMinX() + style.getNodeWidthPadding(); 
            double y = bounds.getMinY() + style.getNodeHeightPadding();
            if (hasLabel) {
                // align with text
                y = bounds.getMinY() + style.getLabelHeightOffset(state) 
                        - style.getNodeSymbolHeight();
            }
            
            UseFixed use = new UseFixed();
            use.setRefSvg(ref);
            use.setPos((float) x, (float) y);
            use.setSize(
                (float) style.getNodeSymbolWidth(), 
                (float) style.getNodeSymbolHeight());
            rootGroup.loaderAddChild(helper, use);
        }
        if (hasLabel) {
            // create label text
            Text nodeName = new Text();
            SVGUtil.setAttr(nodeName, "x", bounds.getMinX() 
                    + style.getNodeWidthPadding() + nodeOffset);
//            SVGUtil.setAttr(nodeName, "y", bounds.getMaxY() 
//                    - style.getNodeHeightPadding() - style.getLabelHeightOffset(state));
            SVGUtil.setAttr(nodeName, "y", bounds.getMinY() + style.getLabelHeightOffset(state));
            SVGUtil.setAttrs(nodeName, style.getStyleNodeName());
            rootGroup.loaderAddChild(helper, nodeName);
            SVGUtil.setMultilineText(nodeName, style.getActivityLabeler().getLabel(state));
        }

        // special rendering bound for cancellation
        if ((state.getStateType() == SCStateType.SeqCancel
                || state.getStateType() == SCStateType.LoopCancel)
            && !layoutNode.getChildren().isEmpty()) {
            Double childBound = layout.getNodeBounds().get(layoutNode.getChildren().get(0));
            bounds = new Double(bounds.x, bounds.y, bounds.width, bounds.height);
            if (style.getDirection().isHorizontal()) {
                bounds.height = childBound.height + (childBound.y - bounds.y) * 2;
            } else {
                bounds.width = childBound.width + (childBound.x - bounds.x) * 2;
            }
        }
        
        // create box for symbol / label
        Rect nodeBox = new Rect();
        SVGUtil.setAttr(nodeBox, "x", bounds.getMinX());
        SVGUtil.setAttr(nodeBox, "y", bounds.getMinY());
        SVGUtil.setAttr(nodeBox, "width", bounds.getWidth());
        SVGUtil.setAttr(nodeBox, "height", bounds.getHeight());
        SVGUtil.setAttrs(nodeBox, style.getStyleNodeBox());
        
        rootGroup.loaderAddChild(helper, nodeBox);
    }

    private void _renderPoint(Group rootGroup, ISCState state) throws SVGElementException {
        Rectangle2D.Double bounds = getBound(state);
        
        // create point
        Circle nodeBox = new Circle();
        SVGUtil.setAttr(nodeBox, "cx", bounds.getCenterX());
        SVGUtil.setAttr(nodeBox, "cy", bounds.getCenterY());
        SVGUtil.setAttr(nodeBox, "r", Math.min(bounds.getWidth(), bounds.getHeight()) * 0.5);
        SVGUtil.setAttrs(nodeBox, style.getStyleNodePoint());
        
        rootGroup.loaderAddChild(helper, nodeBox);
    }

    private void _renderSplitPseudo(Group rootGroup, ISCState state) throws SVGElementException {
        Rectangle2D.Double bounds = getBound(state);
        
        // create box
        Rect nodeBox = new Rect();
        SVGUtil.setAttr(nodeBox, "x", bounds.getMinX());
        SVGUtil.setAttr(nodeBox, "y", bounds.getMinY());
        SVGUtil.setAttr(nodeBox, "width", bounds.getWidth());
        SVGUtil.setAttr(nodeBox, "height", bounds.getHeight());
        SVGUtil.setAttrs(nodeBox, style.getStyleNodeSplitPseudo());
        
        rootGroup.loaderAddChild(helper, nodeBox);
    }

    private void _renderInitialState(Group rootGroup, ISCState state) throws SVGElementException {
        Rectangle2D.Double bounds = getBound(state);
        
        ISVGReference icon = style.getNodeIcon(state);
        SVGDiagram ref = collection.loadSVG(icon);
        
        double x = bounds.getMinX() + style.getNodeWidthPadding(); 
        double y = bounds.getMinY() + style.getNodeHeightPadding();
        
        UseFixed use = new UseFixed();
        use.setRefSvg(ref);
        use.setPos((float) x, (float) y);
        use.setSize(
            (float) style.getNodeSymbolWidth(), 
            (float) style.getNodeSymbolHeight());
        rootGroup.loaderAddChild(helper, use);
        
        Circle nodeBox = new Circle();
        SVGUtil.setAttr(nodeBox, "cx", bounds.getCenterX());
        SVGUtil.setAttr(nodeBox, "cy", bounds.getCenterY());
        SVGUtil.setAttr(nodeBox, "r", Math.min(bounds.getWidth(), bounds.getHeight()) * 0.5);
        SVGUtil.setAttrs(nodeBox, style.getStyleNodeBox());
        
        rootGroup.loaderAddChild(helper, nodeBox);
    }

    private void _renderFinalState(Group rootGroup, ISCState state) throws SVGElementException {
        Rectangle2D.Double bounds = getBound(state);

        ISVGReference icon = style.getNodeIcon(state);
        SVGDiagram ref = collection.loadSVG(icon);
        
        double x = bounds.getMinX() + style.getNodeWidthPadding(); 
        double y = bounds.getMinY() + style.getNodeHeightPadding();
        
        UseFixed use = new UseFixed();
        use.setRefSvg(ref);
        use.setPos((float) x, (float) y);
        use.setSize(
            (float) style.getNodeSymbolWidth(), 
            (float) style.getNodeSymbolHeight());
        rootGroup.loaderAddChild(helper, use);
        
        Circle nodeBox = new Circle();
        SVGUtil.setAttr(nodeBox, "cx", bounds.getCenterX());
        SVGUtil.setAttr(nodeBox, "cy", bounds.getCenterY());
        SVGUtil.setAttr(nodeBox, "r", Math.min(bounds.getWidth(), bounds.getHeight()) * 0.5);
        SVGUtil.setAttrs(nodeBox, style.getStyleNodeBox());
        
        rootGroup.loaderAddChild(helper, nodeBox);
        
        Circle nodeBoxInner = new Circle();
        SVGUtil.setAttr(nodeBoxInner, "cx", bounds.getCenterX());
        SVGUtil.setAttr(nodeBoxInner, "cy", bounds.getCenterY());
        SVGUtil.setAttr(nodeBoxInner, "r", Math.min(bounds.getWidth(), bounds.getHeight()) * 0.5 - 2);
        SVGUtil.setAttrs(nodeBoxInner, style.getStyleNodeBox());
        
        rootGroup.loaderAddChild(helper, nodeBoxInner);
    }

    private Rectangle2D.Double getBound(ISCState state) {
        return layout.getNodeBounds().get(layoutLookup.get(state));
    }
}
