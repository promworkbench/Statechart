package org.processmining.algorithms.statechart.m2m.ui;

import gnu.trove.map.hash.THashMap;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.abego.treelayout.TreeLayout;
import org.apache.commons.lang3.tuple.Pair;
import org.processmining.algorithms.statechart.m2m.ui.style.EPTreeStyle;
import org.processmining.models.statechart.decorate.ui.svg.ISvgDecorator;
import org.processmining.models.statechart.decorate.ui.svg.NullSvgDecorator;
import org.processmining.models.statechart.decorate.ui.svg.SvgLabelledElement;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.utils.statechart.svg.ISVGReference;
import org.processmining.utils.statechart.svg.SVGCollection;
import org.processmining.utils.statechart.svg.SVGUtil;
import org.processmining.utils.statechart.svg.UseFixed;

import com.google.common.base.Function;
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
import com.kitfox.svg.Text;

public class EPTree2Svg implements Function<Pair<IEPTree, TreeLayout<IEPTreeNode>>, SVGDiagram> {

    public static final double EdgeMinStroke = 1.0;
    public static final double EdgeMaxStroke = 2.0;

    public static final Function<IEPTreeNode, Pair<Set<IEPTreeNode>, Set<IEPTreeNode>>> FncEdge2Node
//        = new EdgeSemanticTracedAdapter<IEPTreeNode, IEPTreeNode>();
        = new Function<IEPTreeNode, Pair<Set<IEPTreeNode>, Set<IEPTreeNode>>>() {
            @Override
            public Pair<Set<IEPTreeNode>, Set<IEPTreeNode>> apply(IEPTreeNode node) {
                return Pair.of(node.getEdgeFromSemantics(), Collections.singleton(node));
            }
    };
//          = new Function<IEPTreeNode, Pair<Set<IEPTreeNode>, Set<IEPTreeNode>>>() {
//        @Override
//        public Pair<List<IEPTreeNode>, List<IEPTreeNode>> apply(IEPTreeNode node) {
//            return Pair.of(Arrays.asList(node.getParent()), Arrays.asList(node));
//        }
//    };
    
    private static final String ID_MARKER_ARROW_MSG = "arrow";
    
    private final EPTreeStyle style;

    private Set<String> selectedNodes;
    private ISvgDecorator<IEPTreeNode, IEPTreeNode, IEPTree> svgDecorator;

    private Map<IEPTreeNode, SvgLabelledElement> node2svg;
    private Map<IEPTreeNode, SvgLabelledElement> edge2svg;
    
    public EPTree2Svg(EPTreeStyle style) {
        this.style = style;
    }

    @Override
    public SVGDiagram apply(Pair<IEPTree, TreeLayout<IEPTreeNode>> input) {
        try {
            return transform(input.getLeft(), input.getRight(), 
                    Collections.<String>emptySet(),
                    new NullSvgDecorator<IEPTreeNode, IEPTreeNode, IEPTree>());
        } catch (SVGException e) {
            return null;
        }
    }
    
    public SVGDiagram transform(IEPTree tree, TreeLayout<IEPTreeNode> layout, 
            Set<String> selectedNodes, 
            ISvgDecorator<IEPTreeNode, IEPTreeNode, IEPTree> svgDecorator) throws SVGException {
        this.selectedNodes = selectedNodes;
        this.svgDecorator = svgDecorator;
        
        node2svg = new THashMap<>();
        edge2svg = new THashMap<>();
        
        Rectangle2D rootBounds = layout.getBounds();
        Map<IEPTreeNode, Rectangle2D.Double> boundMap = layout.getNodeBounds();
        final double offset = style.getCanvasPadding();
        
        // setup svg basis
        SVGCollection collection = new SVGCollection();
        
        SVGDiagram diagram = collection.createDiagram("eptree");
        SVGRoot root = new SVGRoot();
        diagram.setRoot(root);
        
        SVGLoaderHelper helper = collection.createLoaderHelper(diagram);
        
        Rect background = new Rect();
        SVGUtil.setAttr(background, "fill", "white");
        root.loaderAddChild(helper, background);
        
        Group rootGroup = new Group();
        SVGUtil.setAttr(rootGroup, "transform", "translate(" + offset + " " + offset + ")");
        root.loaderAddChild(helper, rootGroup);

        // Apply transformations
        svgDecorator.visitModel(tree, diagram);
        
        // add content
        _defineMarkers(root, helper);
        _renderContent(tree, boundMap, collection, helper, rootGroup);
        
        // wrapup
        final double width = rootBounds.getWidth() + style.getCanvasPadding() * 2;
        final double height = rootBounds.getHeight() + style.getCanvasPadding() * 2;
        SVGUtil.setAttr(background, "width", width);
        SVGUtil.setAttr(background, "height", height);
        SVGUtil.setAttr(root, "width", width);
        SVGUtil.setAttr(root, "height", height);
        diagram.updateTime(0);

        svgDecorator.finishVisit();

        // Decorate
        for(IEPTreeNode treeNode : node2svg.keySet()) {
            SvgLabelledElement node = node2svg.get(treeNode);
            svgDecorator.decorateNode(treeNode, node);
            _applySelectionStyle(treeNode, node);
        }
        for(IEPTreeNode treeNode : edge2svg.keySet()) {
            SvgLabelledElement node = edge2svg.get(treeNode);
            svgDecorator.decorateEdge(treeNode, node);
        }
        
        return diagram;
        
    }

    private void _applySelectionStyle(IEPTreeNode treeNode,
            SvgLabelledElement element) throws SVGElementException {
        if (selectedNodes.contains(treeNode.getId())) {
            SVGUtil.setAttr(element.element, "stroke", "red");
            SVGUtil.setAttr(element.element, "stroke-width", "3");
        } else {
//            SVGUtil.setAttr(element.element, "stroke", "black");
            SVGUtil.setAttr(element.element, "stroke-width", "1");
        }
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
//            SVGUtil.setAttr(marker, "markerUnits", "userSpaceOnUse");
            defs.loaderAddChild(helper, marker);
            
            Path path = new Path();
            SVGUtil.setAttr(path, "d", "M-1,-0.5 V0.5 L0,0 Z");
            SVGUtil.setAttr(path, "fill", "black");
            marker.loaderAddChild(helper, path);
        }
    }

    private void _renderContent(IEPTree tree, Map<IEPTreeNode, Double> boundMap, 
            SVGCollection collection, SVGLoaderHelper helper, Group rootGroup) throws SVGElementException {
        for (IEPTreeNode node : tree.iteratePreOrder()) {
            UseFixed plusminUse = null, symbolUse = null;
            Text nodeName = null;
            
            Rectangle2D.Double bounds = boundMap.get(node);

            boolean hasPlusMin = style.nodeHasPlusMin(node);
            boolean hasSymbol = style.nodeHasSymbol(node);
            boolean hasLabel = style.nodeHasLabel(node);
    
            double nodeOffset = style.getNodeWidthPadding();
            if (hasPlusMin) {
                ISVGReference icon = style.getNodePlusMin(node);
                SVGDiagram ref = collection.loadSVG(icon);

                plusminUse = new UseFixed();
                plusminUse.setRefSvg(ref);
                plusminUse.setPos(
                    (float) (bounds.getMinX() + nodeOffset), 
                    //(float) (bounds.getMinY() + style.getNodeHeightPadding())
                    (float) (bounds.getCenterY() - 0.5 * style.getNodePlusMinHeight())
                );
                plusminUse.setSize(
                    (float) style.getNodePlusMinWidth(), 
                    (float) style.getNodePlusMinHeight());

                SVGUtil.setAttr(plusminUse, UiDataConstants.KeyDataCollapseUi, "true");
                
                nodeOffset += style.getNodePlusMinWidth()
                        + style.getNodePlusMinPadding();
            }
            if (hasSymbol) {
                ISVGReference icon = style.getNodeIcon(node);
                SVGDiagram ref = collection.loadSVG(icon);
                
                symbolUse = new UseFixed();
                symbolUse.setRefSvg(ref);
                symbolUse.setPos(
                    (float) (bounds.getMinX() + nodeOffset), 
                    //(float) (bounds.getMinY() + style.getNodeHeightPadding())
                    (float) (bounds.getCenterY() - 0.5 * style.getNodeSymbolHeight())
                );
                symbolUse.setSize(
                    (float) style.getNodeSymbolWidth(), 
                    (float) style.getNodeSymbolHeight());
                
                nodeOffset += style.getNodeSymbolWidth()
                        + style.getNodeSymbolPadding();
            }
            if (hasLabel) {
                // create label text
                nodeName = new Text();
                nodeName.appendText(style.getActivityLabeler().getLabel(node));
                SVGUtil.setAttr(nodeName, "x", 
                        bounds.getMinX() + nodeOffset);
                SVGUtil.setAttr(nodeName, "y", 
                        bounds.getMaxY() - style.getNodeHeightPadding() - style.getLabelHeightOffset(node));
                SVGUtil.setAttrs(nodeName, style.getStyleNodeName());
            }
            
            // create lines
            for (IEPTreeNode child : node.getChildren()) {
                Rectangle2D.Double childBounds = boundMap.get(child);
                
                Line arrow = new Line();
                switch (style.getLayoutDir()) {
                case Top:
                    SVGUtil.setAttr(arrow, "x1", bounds.getCenterX());
                    SVGUtil.setAttr(arrow, "y1", bounds.getMaxY());
                    SVGUtil.setAttr(arrow, "x2", childBounds.getCenterX());
                    SVGUtil.setAttr(arrow, "y2", childBounds.getMinY());
                    break;
                case Bottom:
                    SVGUtil.setAttr(arrow, "x1", bounds.getCenterX());
                    SVGUtil.setAttr(arrow, "y1", bounds.getMinY());
                    SVGUtil.setAttr(arrow, "x2", childBounds.getCenterX());
                    SVGUtil.setAttr(arrow, "y2", childBounds.getMaxY());
                    break;
                case Left:
                    SVGUtil.setAttr(arrow, "x1", bounds.getMaxX());
                    SVGUtil.setAttr(arrow, "y1", bounds.getCenterY());
                    SVGUtil.setAttr(arrow, "x2", childBounds.getMinX());
                    SVGUtil.setAttr(arrow, "y2", childBounds.getCenterY());
                    break;
                case Right:
                    SVGUtil.setAttr(arrow, "x1", bounds.getMinX());
                    SVGUtil.setAttr(arrow, "y1", bounds.getCenterY());
                    SVGUtil.setAttr(arrow, "x2", childBounds.getMaxX());
                    SVGUtil.setAttr(arrow, "y2", childBounds.getCenterY());
                    break;
                }
                SVGUtil.setAttr(arrow, "marker-end", SVGUtil.refUrlId(ID_MARKER_ARROW_MSG));
                SVGUtil.setAttrs(arrow, style.getStyleEdge());
                
                rootGroup.loaderAddChild(helper, arrow);

                edge2svg.put(child, new SvgLabelledElement(arrow, null));
                svgDecorator.visitEdge(node);
            }
            
            // create box for symbol / label
            Group nodeGroup = new Group();
            SVGUtil.setAttr(nodeGroup, "x", bounds.getMinX());
            SVGUtil.setAttr(nodeGroup, "y", bounds.getMinY());
            SVGUtil.setAttr(nodeGroup, "width", bounds.getWidth());
            SVGUtil.setAttr(nodeGroup, "height", bounds.getHeight());
            SVGUtil.setAttr(nodeGroup, UiDataConstants.KeyDataNodeId, node.getId());
            rootGroup.loaderAddChild(helper, nodeGroup);
            
            if (hasPlusMin) {
                SVGUtil.setAttr(nodeGroup, UiDataConstants.KeyDataCollapsible, "true");
            }
            
            Rect nodeBox = new Rect();
            SVGUtil.setAttr(nodeBox, "x", bounds.getMinX());
            SVGUtil.setAttr(nodeBox, "y", bounds.getMinY());
            SVGUtil.setAttr(nodeBox, "width", bounds.getWidth());
            SVGUtil.setAttr(nodeBox, "height", bounds.getHeight());
            SVGUtil.setAttrs(nodeBox, style.getStyleNodeBox());
            nodeGroup.loaderAddChild(helper, nodeBox);
//            SVGUtil.setAttr(nodeBox, "data-nodeId", node.getId());

            if (plusminUse != null) {
                nodeGroup.loaderAddChild(helper, plusminUse);
            }
            if (symbolUse != null) {
                nodeGroup.loaderAddChild(helper, symbolUse);
            }
            if (nodeName != null) {
                nodeGroup.loaderAddChild(helper, nodeName);
            }
            
            svgDecorator.visitNode(node);
            node2svg.put(node, new SvgLabelledElement(nodeBox, nodeName));
            
        }
    }
}
