package org.processmining.algorithms.statechart.m2m.ui;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.awt.geom.Rectangle2D;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.processmining.models.statechart.decorate.tracing.EdgeSemanticTracedAdapter;
import org.processmining.models.statechart.decorate.ui.svg.ISvgDecorator;
import org.processmining.models.statechart.decorate.ui.svg.NullSvgDecorator;
import org.processmining.models.statechart.decorate.ui.svg.SvgLabelledElement;
import org.processmining.models.statechart.msd.FragmentType;
import org.processmining.models.statechart.msd.IActivation;
import org.processmining.models.statechart.msd.ILifeline;
import org.processmining.models.statechart.msd.IMSDFragment;
import org.processmining.models.statechart.msd.IMSDMessage;
import org.processmining.models.statechart.msd.IMSDNode;
import org.processmining.models.statechart.msd.ISeqDiagram;
import org.processmining.models.statechart.msd.LifelineType;
import org.processmining.models.statechart.msd.MessageType;
import org.processmining.utils.statechart.svg.SVGUniverseFixed;
import org.processmining.utils.statechart.svg.SVGUtil;

import com.google.common.base.Function;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
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

public class SeqDiagram2Svg implements Function<ISeqDiagram, SVGDiagram> {

    public static final double EdgeMinStroke = 1.0;
    public static final double EdgeMaxStroke = 2.0;

    public static final Function<IMSDMessage, Pair<Set<IActivation>, Set<IActivation>>> FncEdge2Node 
        = new EdgeSemanticTracedAdapter<IMSDMessage, IActivation>();
    
    private static final Logger logger = LogManager
            .getLogger(SeqDiagram2Svg.class.getName());

    private static final String ID_MARKER_ARROW_MSG = "arrow";

    public static class Configuration {
        // sizes
        public double diagramPadding = 15;
        
        public double lifelineBoxNamePadding = 10;
        public double lifelineBoxMarginLeftRight = 25;
        public double lifelineBoxMarginBottom = 10;
        
        public double lifelineBaseMarginBottom = 10;

        public double lifelineActivityWidth = 10;
        public double lifelineActivityOverlapOffset = 5;
        
        public String lifelineBaseDashPattern = "5, 5";

        public double fragmentBoxNamePadding = 5;
        public double fragmentPartPadding = 7;
        public double fragmentHorizontalPadding = 7;
        
        public String fragmentPartDashPattern = "10, 5";
        
        public double messageNamePaddingHorizontal = 15;
        public double messageNamePaddingVertical = 7;
        public double messageLinePadding = 7;
        public double messageNameBackdropPadding = 5;

        public double messageSelfHeight = 15;
        public double messageSelfWidth = 10;
        public double messageSelfCurveCPOffset = 35;
        
        // style maps
        public final Map<String, String> styleLifelineTitleName = ImmutableMap.<String, String>builder()
                .put("font-family", "Sans Serif")
                .put("font-size", "14")
                .put("font-style", "normal")
                .put("font-weight", "bold")
                .build();

        public Map<String, String> styleMessageText = ImmutableMap.<String, String>builder()
                .put("font-family", "Sans Serif")
                .put("font-size", "14")
                .put("font-style", "normal")
                .put("font-weight", "normal")
                .build();

        public Map<String, String> styleLifelineTitleBox = ImmutableMap.<String, String>builder()
                .put("stroke", "black")
                .put("fill", "none")
                .build();

        public Map<String, String> styleLifelineBase = ImmutableMap.<String, String>builder()
                .put("stroke", "black")
                .put("fill", "none")
                .build();

        public Map<String, String> styleLifelineActivity = ImmutableMap.<String, String>builder()
                .put("stroke", "black")
                .put("fill", "white")
                .build();

        public Map<String, String> styleFragmentText = ImmutableMap.<String, String>builder()
                .put("font-family", "Sans Serif")
                .put("font-size", "14")
                .put("font-style", "normal")
                .put("font-weight", "normal")
                .build();
        
        public Map<String, String> styleFragmentBox = ImmutableMap.<String, String>builder()
                .put("stroke", "black")
                .put("fill", "none")
                .build();
        
        public Map<String, String> styleFragmentPart = ImmutableMap.<String, String>builder()
                .put("stroke", "black")
                .put("fill", "none")
                .build();
        
        public Map<String, String> styleFragmentTextBackdrop = ImmutableMap.<String, String>builder()
                .put("stroke", "black")
                .put("fill", "white")
                .build();
        
        public Map<MessageType, Map<String, String>> styleMessage = ImmutableMap.<MessageType, Map<String, String>>builder()
                .put(MessageType.Call, ImmutableMap.<String, String>builder()
                    .put("stroke", "black")
                    .put("fill", "none")
                    .build()
                ).put(MessageType.Return, ImmutableMap.<String, String>builder()
                    .put("stroke", "black")
                    .put("fill", "none")
                    .put("stroke-dasharray", "5, 5")
                    .build()
                ).put(MessageType.CallSelf, ImmutableMap.<String, String>builder()
                    .put("stroke", "black")
                    .put("fill", "none")
                    .build()
                ).put(MessageType.ReturnSelf, ImmutableMap.<String, String>builder()
                    .put("stroke", "black")
                    .put("fill", "none")
                    .put("stroke-dasharray", "5, 5")
                    .build()
                ).build();

        public Map<String, String> styleMessageTextBackdrop = ImmutableMap.<String, String>builder()
                .put("stroke", "none")
                .put("fill", "white")
                .build();

    }
    private final Configuration config = new Configuration();

//    private Map<IMSDNode, Rect> messageBackdropInsts;
//    private Map<IMSDNode, Text> messageInsts;
    private TObjectDoubleMap<ILifeline> lifelineMaxNextMsgSize;
    private TObjectDoubleMap<IMSDNode> lifelineMsgVerticalOffset;
    
    private TObjectDoubleMap<ILifeline> lifelineActPosX;
    private Map<ILifeline, Rectangle2D> lifelineSize;
    private List<Line> lifelineBases;
    
    private double width, height;
    
    private double pntCurrentY;

    private Map<IActivation, Rect> lifelineActBoxes;
    private TObjectDoubleMap<IActivation> lifelineActBoxStart;
    private Multiset<ILifeline> lifelineActActive;

    private TObjectDoubleMap<Rect> fragmentBoxStart;

    private Set<String> selectedNodes;
    private ISvgDecorator<IActivation, IMSDMessage, ISeqDiagram> svgDecorator;

    private Map<IActivation, SvgLabelledElement> act2svg;
    private Map<IMSDMessage, SvgLabelledElement> mess2svg;
    
    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public SVGDiagram apply(ISeqDiagram input) {
        try {
            return transform(input, Collections.<String>emptySet(),
                    new NullSvgDecorator<IActivation, IMSDMessage, ISeqDiagram>());
        } catch (SVGException e) {
            return null;
        }
    }
    
    public SVGDiagram transform(ISeqDiagram input, Set<String> selectedNodes, 
            ISvgDecorator<IActivation, IMSDMessage, ISeqDiagram> svgDecorator) throws SVGException {
        this.selectedNodes = selectedNodes;
        this.svgDecorator = svgDecorator;
        
        act2svg = new THashMap<>();
        mess2svg = new THashMap<>();
        
//        messageBackdropInsts = new THashMap<>();
//        messageInsts = new THashMap<>();
        lifelineMaxNextMsgSize = new TObjectDoubleHashMap<>();
        lifelineMsgVerticalOffset = new TObjectDoubleHashMap<>();

        lifelineActPosX = new TObjectDoubleHashMap<>();
        lifelineSize = new THashMap<>();
        lifelineBases = new ArrayList<>();
        
        lifelineActBoxes = new THashMap<>();
        lifelineActBoxStart = new TObjectDoubleHashMap<>();
        lifelineActActive = HashMultiset.create(input.getLifelines().size());

        fragmentBoxStart = new TObjectDoubleHashMap<>();
        
        width = 0;
        height = 0;
        
        SVGUniverseFixed universe = new SVGUniverseFixed();
        URI uri = universe.getStreamBuiltURI("msd");
        
        SVGDiagram diagram = universe.createDiagram(uri);
        SVGRoot root = new SVGRoot();
        diagram.setRoot(root);
        
        SVGLoaderHelper helper = new SVGLoaderHelper(uri, universe, diagram);

        Group group = new Group();
        root.loaderAddChild(helper, group);
        
        Rect background = new Rect();
        SVGUtil.setAttr(background, "fill", "white");
        group.loaderAddChild(helper, background);

        // Apply transformations
        svgDecorator.visitModel(input, diagram);

        if (!input.getLifelines().isEmpty() && input.getRoot() != null) {
            _defineMarkers(group, helper);
            
            _calculateMessages(input.getRoot(), group, helper);
            
            _setupLifelines(input.getLifelines(), group, helper);
            
            _renderActivities(input.getRoot(), group, helper);
            
            _renderLifelineBases(input.getLifelines(), group, helper);
        }

        SVGUtil.setAttr(background, "width", width);
        SVGUtil.setAttr(background, "height", height);
        SVGUtil.setAttr(root, "width", width);
        SVGUtil.setAttr(root, "height", height);
        diagram.updateTime(0);

        svgDecorator.finishVisit();

        // Decorate
        for(IActivation activation : act2svg.keySet()) {
            SvgLabelledElement node = act2svg.get(activation);
            svgDecorator.decorateNode(activation, node);
        }
        for (IMSDMessage message : mess2svg.keySet()) {
            SvgLabelledElement edge = mess2svg.get(message);
            if (message.isStartActivation()) {
                // treat as arc into a node
                svgDecorator.decorateEdge(message, edge);
                _applySelectionStyle(message, edge);
            } else {
                // treat as summary for action complete
                svgDecorator.decorateNode(message.getSource(), edge);
            }
        }
        
        return diagram;
    }

    private void _defineMarkers(Group group, SVGLoaderHelper helper) throws SVGElementException {
        Defs defs = new Defs();
        group.loaderAddChild(helper, defs);
        
        {
            Marker marker = new Marker();
            SVGUtil.setAttr(marker, "id", ID_MARKER_ARROW_MSG);
            SVGUtil.setAttr(marker, "markerWidth", "10");
            SVGUtil.setAttr(marker, "markerHeight", "10");
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

    private void _updateDiagramWidth(double maxX) {
        width = Math.max(width, maxX + config.diagramPadding);
    }

    private void _updateDiagramHeight(double maxY) {
        height = Math.max(height, maxY + config.diagramPadding);
    }

    private void _calculateMessages(IMSDNode node, Group group,
            SVGLoaderHelper helper) throws SVGException {
        for (IMSDNode child : node.getChildren()) {
            _calculateMessages(child, group, helper);
        }
        
        if (node instanceof IMSDMessage) {
            IActivation act = ((IMSDMessage) node).getSource();
            ILifeline lifeline = act.getLifeline();

            // create text backdrop
//            Rect textBackdrop = new Rect();
//            SVGUtil.setAttrs(textBackdrop, config.styleMessageTextBackdrop);
//
//            group.loaderAddChild(helper, textBackdrop);
//            messageBackdropInsts.put(node, textBackdrop);
            
            // Message text
            Text messageName = new Text();
            messageName.appendText(node.getName());
            SVGUtil.setAttr(messageName, "x", 0);
            SVGUtil.setAttr(messageName, "y", 0);
            SVGUtil.setAttrs(messageName, config.styleMessageText);
            
            group.loaderAddChild(helper, messageName);
//            messageInsts.put(node, messageName);
            
            messageName.rebuild();
            Rectangle2D textBB = messageName.getBoundingBox();
            
            MessageType msgType = ((IMSDMessage) node).getMessageType();
            double messageWidth = textBB.getWidth();
            double messageHeight = textBB.getHeight();

            group.removeChild(messageName);
            
//            SVGUtil.setAttr(textBackdrop, "width", messageWidth);
//            SVGUtil.setAttr(textBackdrop, "height", messageHeight);
            
            if (msgType == MessageType.Call || msgType == MessageType.Return) {
                messageWidth += 2 * config.messageNamePaddingHorizontal;
                messageHeight += 2 * config.messageNamePaddingVertical;
            } else if (msgType == MessageType.CallSelf || msgType == MessageType.ReturnSelf) {
                messageWidth += 2 * config.messageNamePaddingHorizontal + config.messageSelfWidth;
                if (msgType == MessageType.CallSelf) {
                    messageHeight = config.messageNamePaddingVertical + config.messageSelfHeight;
                } else {
                    messageHeight = config.messageNamePaddingVertical;
                }
            }
            
            double size = 0;
            if (lifelineMaxNextMsgSize.containsKey(lifeline)) {
                size = lifelineMaxNextMsgSize.get(lifeline);
            }
            size = Math.max(size, messageWidth);
            lifelineMaxNextMsgSize.put(lifeline, size);
            lifelineMsgVerticalOffset.put(node, messageHeight);
            
        }
    }

    private void _setupLifelines(List<ILifeline> lifelines, Group group,
            SVGLoaderHelper helper) throws SVGException {

        Map<ILifeline, Text> labelText = new THashMap<>();
        Map<ILifeline, Rect> labelBox = new THashMap<>();
        
        // build lifeline objects, and calculate sizes
        double currentX = config.diagramPadding;
        double currentY = config.diagramPadding;
        for (ILifeline lifeline : lifelines) {
            Rectangle2D headBB = new Rectangle2D.Double(
                currentX, currentY, 
                config.lifelineBoxNamePadding * 2, 
                config.lifelineBoxNamePadding * 2
            );
            
            if (lifeline.getLifelineType() == LifelineType.Normal) {
                // create label text
                Text lifelineName = new Text();
                lifelineName.appendText(lifeline.getName());
                SVGUtil.setAttr(lifelineName, "x", currentX + config.lifelineBoxNamePadding);
                SVGUtil.setAttr(lifelineName, "y", currentY + config.lifelineBoxNamePadding);
                SVGUtil.setAttrs(lifelineName, config.styleLifelineTitleName);
                group.loaderAddChild(helper, lifelineName);
                labelText.put(lifeline, lifelineName);
                
                lifelineName.rebuild();
                Rectangle2D textBB = lifelineName.getBoundingBox();
                SVGUtil.setAttr(lifelineName, "y", currentY + config.lifelineBoxNamePadding + textBB.getHeight());
                
                // create box around label
                Rect lifelineBox = new Rect();
                SVGUtil.setAttr(lifelineBox, "x", currentX);
                SVGUtil.setAttr(lifelineBox, "y", currentY);
                SVGUtil.setAttr(lifelineBox, "width", textBB.getWidth() + 2 * config.lifelineBoxNamePadding);
                SVGUtil.setAttr(lifelineBox, "height", textBB.getHeight() + 2 * config.lifelineBoxNamePadding);
                SVGUtil.setAttrs(lifelineBox, config.styleLifelineTitleBox);
                
                group.loaderAddChild(helper, lifelineBox);
                labelBox.put(lifeline, lifelineBox);
                
                headBB = new Rectangle2D.Double(
                    currentX, currentY, 
                    textBB.getWidth() + config.lifelineBoxNamePadding * 2, 
                    textBB.getHeight() + config.lifelineBoxNamePadding * 2
                );
            }

            lifelineSize.put(lifeline, headBB);
            
            double latestMsgX = 0;
            if (lifelineMaxNextMsgSize.containsKey(lifeline)) {
                latestMsgX = headBB.getCenterX()
                    + config.lifelineActivityWidth
                    + lifelineMaxNextMsgSize.get(lifeline);
            }
            _updateDiagramWidth(Math.max(headBB.getMaxX(), latestMsgX));
            _updateDiagramHeight(headBB.getMaxY());
        }
        
        // position lifeline objects
        pntCurrentY = currentY;
        {
            ILifeline current = lifelines.get(0);
            Rectangle2D headBB = lifelineSize.get(current);
            lifelineActPosX.put(current, headBB.getCenterX());
            pntCurrentY = Math.max(pntCurrentY, headBB.getMaxY() + config.lifelineBoxMarginBottom);
        }
        
        for (int i = 1; i < lifelines.size(); i++) {
            ILifeline previous = lifelines.get(i - 1);
            ILifeline current = lifelines.get(i);
            Rectangle2D headBB = lifelineSize.get(current);
            
            Rectangle2D prevLoc = lifelineSize.get(previous);
            double newHeadX = prevLoc.getMaxX() + config.lifelineBoxMarginLeftRight;
            double newMsgX = 0;
            if (lifelineMaxNextMsgSize.containsKey(previous)) {
                newMsgX = prevLoc.getCenterX()
                    + config.lifelineActivityWidth
                    + lifelineMaxNextMsgSize.get(previous);
            }
            currentX = Math.max(newHeadX, newMsgX - headBB.getWidth() * 0.5);
            
            if (current.getLifelineType() == LifelineType.Normal) {
                Text lifelineName = labelText.get(current);
                Rectangle2D textBB = lifelineName.getBoundingBox();
                SVGUtil.setAttr(lifelineName, "x", currentX + config.lifelineBoxNamePadding);
                SVGUtil.setAttr(lifelineName, "y", currentY + config.lifelineBoxNamePadding + textBB.getHeight());
                
                Rect lifelineBox = labelBox.get(current);
                SVGUtil.setAttr(lifelineBox, "x", currentX);
                SVGUtil.setAttr(lifelineBox, "y", currentY);
            }
            
            headBB = new Rectangle2D.Double(
                currentX, currentY,
                headBB.getWidth(),
                headBB.getHeight()
            );
            lifelineSize.put(current, headBB);
            lifelineActPosX.put(current, headBB.getCenterX());
            pntCurrentY = Math.max(pntCurrentY, headBB.getMaxY() + config.lifelineBoxMarginBottom);

            double latestMsgX = 0;
            if (lifelineMaxNextMsgSize.containsKey(current)) {
                latestMsgX = headBB.getCenterX()
                    + config.lifelineActivityWidth
                    + lifelineMaxNextMsgSize.get(current);
            }
            _updateDiagramWidth(Math.max(headBB.getMaxX(), latestMsgX));
            _updateDiagramHeight(headBB.getMaxY());
        }

        // create lifeline base
        for (ILifeline lifeline : lifelines) {
            if (lifeline.getLifelineType() == LifelineType.Normal) {
                Rectangle2D headBB = lifelineSize.get(lifeline);
                
                // create lifeline base
                Line base = new Line();
                SVGUtil.setAttr(base, "x1", headBB.getCenterX());
                SVGUtil.setAttr(base, "y1", headBB.getMaxY());
                SVGUtil.setAttr(base, "x2", headBB.getCenterX());
                SVGUtil.setAttr(base, "y2", headBB.getMaxY() +  + config.lifelineBoxMarginBottom);
                SVGUtil.setAttr(base, "stroke-dasharray", config.lifelineBaseDashPattern);
                SVGUtil.setAttrs(base, config.styleLifelineBase);

                group.loaderAddChild(helper, base);
                lifelineBases.add(base);
            }
        }
    }

    private Rectangle2D _renderActivities(IMSDNode node, Group group,
            SVGLoaderHelper helper) throws SVGException {
        
        if (node instanceof IMSDFragment) {
            boolean isVis = ((IMSDFragment) node).getFragmentType() != FragmentType.Root;
            Rectangle2D bound = null;
            
            Triple<Rect, Text, Rect> fragment = null;
            if (isVis) {
                fragment = _renderStartFragment((IMSDFragment) node, group, helper);
            }

            List<IMSDNode> children = node.getChildren();
            List<Line> lines = new ArrayList<Line>(children.size());
            for (int i = 0; i < children.size(); i++) {
                IMSDNode part = children.get(i);
                for (IMSDNode child : part.getChildren()) {
                    bound = _addBound(bound, _renderActivities(child, group, helper));
                }
                if (isVis && i < children.size() - 1) {
                    lines.add(_renderSplitFragment(node, group, helper));
                }
            }

            if (isVis) {
                bound = _addBound(bound, _renderEndFragment(fragment, node, bound, lines, group, helper));
            }
            return bound;
        }
        
        if (node instanceof IMSDMessage) {
            return _renderMessage((IMSDMessage) node, group, helper);
        }
        
        return null;
    }

    private Rectangle2D _addBound(Rectangle2D bound, Rectangle2D newBound) {
        if (bound == null) {
            bound = newBound;
        } else if (newBound != null) {
            bound.add(newBound);
        }
        return bound;
    }

    private Triple<Rect, Text, Rect> _renderStartFragment(IMSDFragment node, Group group,
            SVGLoaderHelper helper) throws SVGException {
        // create text backdrop
        Rect textBackdrop = new Rect();
        SVGUtil.setAttr(textBackdrop, "y", pntCurrentY);
        SVGUtil.setAttrs(textBackdrop, config.styleFragmentTextBackdrop);
        
        group.loaderAddChild(helper, textBackdrop);
        
        // create label text
        Text fragmentName = new Text();
        fragmentName.appendText(node.getFragmentType().getLabel());
        SVGUtil.setAttr(fragmentName, "x", 0 + config.fragmentBoxNamePadding);
        SVGUtil.setAttr(fragmentName, "y", pntCurrentY + config.fragmentBoxNamePadding);
        SVGUtil.setAttrs(fragmentName, config.styleFragmentText);
        group.loaderAddChild(helper, fragmentName);
        
        fragmentName.rebuild();
        Rectangle2D textBB = fragmentName.getBoundingBox();
        SVGUtil.setAttr(fragmentName, "y", pntCurrentY + config.fragmentBoxNamePadding + textBB.getHeight());

        SVGUtil.setAttr(textBackdrop, "width", textBB.getWidth() + 2 * config.fragmentBoxNamePadding);
        SVGUtil.setAttr(textBackdrop, "height", textBB.getHeight() + 2 * config.fragmentBoxNamePadding);
        
        // create box around label
        Rect fragmentBox = new Rect();
        SVGUtil.setAttr(fragmentBox, "y", pntCurrentY);
        SVGUtil.setAttr(fragmentBox, "height", textBB.getHeight() + 2 * config.fragmentBoxNamePadding);
        SVGUtil.setAttrs(fragmentBox, config.styleFragmentBox);
        
        group.loaderAddChild(helper, fragmentBox);

        fragmentBoxStart.put(fragmentBox, pntCurrentY);
        pntCurrentY = pntCurrentY
            + textBB.getHeight() + 2 * config.fragmentBoxNamePadding;
        
        return Triple.of(textBackdrop, fragmentName, fragmentBox);
    }

    private Rectangle2D _renderEndFragment(Triple<Rect, Text, Rect> fragment, IMSDNode node, Rectangle2D bound, List<Line> lines, Group group,
            SVGLoaderHelper helper) throws SVGException {
        Rect textBackdrop = fragment.getLeft();
        Text fragmentName = fragment.getMiddle();
        Rect fragmentBox = fragment.getRight();
        
        double startX = bound.getMinX() - config.fragmentHorizontalPadding;
        double endX = bound.getMaxX() + config.fragmentHorizontalPadding;
        
        double endY = pntCurrentY + config.fragmentPartPadding * 0.5;
        double height = endY - fragmentBoxStart.get(fragmentBox);

        SVGUtil.setAttr(textBackdrop, "x", startX);
        SVGUtil.setAttr(fragmentName, "x", startX + config.fragmentBoxNamePadding);
        
        SVGUtil.setAttr(fragmentBox, "height", height);
        SVGUtil.setAttr(fragmentBox, "x", startX);
        SVGUtil.setAttr(fragmentBox, "width", endX - startX);
        
        for (Line splitLine : lines) {
            SVGUtil.setAttr(splitLine, "x1", startX);
            SVGUtil.setAttr(splitLine, "x2", endX);
        }

        pntCurrentY = pntCurrentY
                + config.fragmentPartPadding;
        
        return new Rectangle2D.Double(
            startX, SVGUtil.getAttrDouble(fragmentBox, "y"),
            endX - startX, height
        );
    }

    private Line _renderSplitFragment(IMSDNode node, Group group,
            SVGLoaderHelper helper) throws SVGElementException {
        double startX = 1;
        double endX = width - startX - 1;

        Line splitLine = new Line();
        SVGUtil.setAttr(splitLine, "x1", startX);
        SVGUtil.setAttr(splitLine, "y1", pntCurrentY + config.fragmentPartPadding);
        SVGUtil.setAttr(splitLine, "x2", endX);
        SVGUtil.setAttr(splitLine, "y2", pntCurrentY + config.fragmentPartPadding);
        SVGUtil.setAttr(splitLine, "stroke-dasharray", config.fragmentPartDashPattern);
        SVGUtil.setAttrs(splitLine, config.styleFragmentPart);

        group.loaderAddChild(helper, splitLine);

        pntCurrentY = pntCurrentY
                + config.fragmentPartPadding * 2;
        
        return splitLine;
    }

    private Rectangle2D _renderMessage(IMSDMessage message, Group group,
            SVGLoaderHelper helper) throws SVGException {
        IActivation source = message.getSource();
        ILifeline sourceLifeline = source.getLifeline();
        double sourceX = lifelineActPosX.get(sourceLifeline);

        IActivation target = message.getTarget();
        ILifeline targetLifeline = target.getLifeline();
        double targetX = lifelineActPosX.get(targetLifeline);
        
        MessageType msgType = message.getMessageType();
        
        // use ranking to figure out entry points of arrows
        boolean sourceLeft = sourceLifeline.getRank() > targetLifeline.getRank();
        boolean targetLeft = sourceLifeline.getRank() < targetLifeline.getRank();
        double sourceMod = sourceLeft ? -0.5 : 0.5;
        double targetMod = targetLeft ? -0.5 : 0.5;
        
        // handle activity block and message offsets
        double sourceOffset = config.lifelineActivityWidth * sourceMod
                + (_getActiveActivities(sourceLifeline) - 1) * config.lifelineActivityOverlapOffset;
        double sourceXbegin = sourceX + sourceOffset;
        
        if (msgType == MessageType.Call || msgType == MessageType.CallSelf) {
            _startActivityBlock(message, target, targetX, group, helper);
        } else if (msgType == MessageType.Return || msgType == MessageType.ReturnSelf) {
            _endActivityBlock(message, source);
        }
        
        double targetOffset = config.lifelineActivityWidth * targetMod
                + (_getActiveActivities(targetLifeline) - 1) * config.lifelineActivityOverlapOffset;
        double targetXend = targetX + targetOffset;
        
        // position message
        Rectangle2D msgBound = null;
        switch(message.getMessageType()) {
        case Call:
        case Return:
            msgBound = _renderMessageNormal(message, group, helper, sourceXbegin, targetXend);
            break;
            
        case CallSelf:
        case ReturnSelf:
            msgBound = _renderMessageSelf(message, group, helper, sourceXbegin, targetXend);
            break;
            
        default:
            logger.warn("SeqDiagram2Svg - Message type not supported");
            break;
        }
        _addActXBounds(msgBound, source, target);
        
        return msgBound;
    }

    private void _addActXBounds(Rectangle2D bound, IActivation source,
            IActivation target) throws SVGException {
        double y = bound.getMinY();
        _addActXBound(bound, source, y);
        _addActXBound(bound, target, y);
    }

    private void _addActXBound(Rectangle2D bound, IActivation act, double y) throws SVGException {
        Rect actRect = lifelineActBoxes.get(act);
        if (actRect != null) {
            double xStart = SVGUtil.getAttrDouble(actRect, "x");
            bound.add(xStart, y);
            bound.add(xStart + config.lifelineActivityWidth, y);
        }
    }

    private void _startActivityBlock(IMSDMessage message, IActivation target, double targetX, Group group, SVGLoaderHelper helper) throws SVGElementException {
        double startX = targetX 
            - config.lifelineActivityWidth * 0.5
            + _getActiveActivities(target.getLifeline()) * config.lifelineActivityOverlapOffset;
        double startY = pntCurrentY
            + lifelineMsgVerticalOffset.get(message);
        
        // create box for activity
        Rect lifelineActBox = new Rect();
        SVGUtil.setAttr(lifelineActBox, "x", startX);
        SVGUtil.setAttr(lifelineActBox, "y", startY);
        SVGUtil.setAttr(lifelineActBox, "width", config.lifelineActivityWidth);
        SVGUtil.setAttr(lifelineActBox, "height", config.lifelineActivityWidth);
        SVGUtil.setAttrs(lifelineActBox, config.styleLifelineActivity);
        
        SVGUtil.setAttr(lifelineActBox, UiDataConstants.KeyDataNodeId, message.getNodeId());
        
        group.loaderAddChild(helper, lifelineActBox);

        svgDecorator.visitNode(target);
        act2svg.put(target, new SvgLabelledElement(lifelineActBox, null));
        
        lifelineActBoxStart.put(target, startY);
        lifelineActBoxes.put(target, lifelineActBox);
        lifelineActActive.add(target.getLifeline());
    }

    private void _endActivityBlock(IMSDMessage message, IActivation source) throws SVGException {
        double endY = pntCurrentY
                + lifelineMsgVerticalOffset.get(message);
        
        Rect lifelineActBox = lifelineActBoxes.get(source);
        double height = endY - lifelineActBoxStart.get(source);
        SVGUtil.setAttr(lifelineActBox, "height", height);
        lifelineActActive.remove(source.getLifeline());
    }

    private double _getActiveActivities(ILifeline lifeline) {
        return lifelineActActive.count(lifeline);
    }

    private Rectangle2D _renderMessageNormal(IMSDMessage message, Group group,
            SVGLoaderHelper helper, double sourceX, double targetX) throws SVGException {

        // create text backdrop
        Rect textBackdrop = new Rect();
        SVGUtil.setAttrs(textBackdrop, config.styleMessageTextBackdrop);
        group.loaderAddChild(helper, textBackdrop);
        
        // Message text
        Text messageName = new Text();
        messageName.appendText(message.getName());
        SVGUtil.setAttr(messageName, "x", 0);
        SVGUtil.setAttr(messageName, "y", 0);
        SVGUtil.setAttrs(messageName, config.styleMessageText);
        group.loaderAddChild(helper, messageName);
        
        messageName.rebuild();
        Rectangle2D textBB = messageName.getBoundingBox();
        
        double messageWidth = textBB.getWidth();
        double messageHeight = textBB.getHeight();

        SVGUtil.setAttr(textBackdrop, "width", messageWidth + 2 * config.messageNameBackdropPadding);
        SVGUtil.setAttr(textBackdrop, "height", messageHeight + 2 * config.messageNameBackdropPadding);
        
        double startY = pntCurrentY;

        double yBackdrop = pntCurrentY
                + config.messageNamePaddingVertical;
        double x = sourceX + (targetX - sourceX) * 0.5 - messageWidth * 0.5;
        pntCurrentY = pntCurrentY
            + config.messageNamePaddingVertical
            + messageHeight;

        SVGUtil.setAttr(textBackdrop, "x", x - config.messageNameBackdropPadding);
        SVGUtil.setAttr(textBackdrop, "y", yBackdrop - config.messageNameBackdropPadding);
        SVGUtil.setAttr(messageName, "x", x);
        SVGUtil.setAttr(messageName, "y", pntCurrentY);

        pntCurrentY = pntCurrentY
            + config.messageNamePaddingVertical;
        
        // add arrow line
        Line arrow = new Line();
        SVGUtil.setAttr(arrow, "x1", sourceX);
        SVGUtil.setAttr(arrow, "y1", pntCurrentY);
        SVGUtil.setAttr(arrow, "x2", targetX);
        SVGUtil.setAttr(arrow, "y2", pntCurrentY);
        SVGUtil.setAttr(arrow, "marker-end", SVGUtil.refUrlId(ID_MARKER_ARROW_MSG));
        SVGUtil.setAttrs(arrow, config.styleMessage.get(message.getMessageType()));

        group.loaderAddChild(helper, arrow);

        pntCurrentY = pntCurrentY
            + config.messageLinePadding;

        svgDecorator.visitEdge(message);
        mess2svg.put(message, new SvgLabelledElement(arrow, messageName));

        SVGUtil.setAttr(textBackdrop, UiDataConstants.KeyDataNodeId, message.getNodeId());
        SVGUtil.setAttr(messageName, UiDataConstants.KeyDataNodeId, message.getNodeId());
        SVGUtil.setAttr(arrow, UiDataConstants.KeyDataNodeId, message.getNodeId());
        
        return new Rectangle2D.Double(
            sourceX, startY,
            targetX - sourceX, pntCurrentY - startY
        );
    }

    private Rectangle2D _renderMessageSelf(IMSDMessage message, Group group,
            SVGLoaderHelper helper, double sourceX, double targetX) throws SVGException {
        // create text backdrop
        Rect textBackdrop = new Rect();
        SVGUtil.setAttrs(textBackdrop, config.styleMessageTextBackdrop);
        group.loaderAddChild(helper, textBackdrop);
        
        // Message text
        Text messageName = new Text();
        messageName.appendText(message.getName());
        SVGUtil.setAttr(messageName, "x", 0);
        SVGUtil.setAttr(messageName, "y", 0);
        SVGUtil.setAttrs(messageName, config.styleMessageText);
        group.loaderAddChild(helper, messageName);
        
        messageName.rebuild();
        Rectangle2D textBB = messageName.getBoundingBox();
        
        double messageWidth = textBB.getWidth();
        double messageHeight = textBB.getHeight();

        SVGUtil.setAttr(textBackdrop, "width", messageWidth + 2 * config.messageNameBackdropPadding);
        SVGUtil.setAttr(textBackdrop, "height", messageHeight + 2 * config.messageNameBackdropPadding);
        
        double textHeight = textBB.getHeight();
        double startY = pntCurrentY;
        double minX = Math.min(sourceX, targetX);
        
        pntCurrentY = pntCurrentY
            + config.messageNamePaddingVertical;
        double yBackdrop = pntCurrentY;

        double x = minX
            + config.messageSelfWidth
            + config.messageNamePaddingHorizontal;
        double y = pntCurrentY
            + config.messageSelfHeight * 0.5
            + textHeight * 0.25;

        double textX2 = x + textBB.getWidth();
        
        SVGUtil.setAttr(textBackdrop, "x", x - config.messageNameBackdropPadding);
        SVGUtil.setAttr(textBackdrop, "y", yBackdrop - config.messageNameBackdropPadding);
        SVGUtil.setAttr(messageName, "x", x);
        SVGUtil.setAttr(messageName, "y", y);

        pntCurrentY = pntCurrentY
            + config.messageSelfHeight;
        
        // add arrow line
        Path arrow = new Path();
        SVGUtil.setAttr(arrow, "d", String.format(SVGUtil.FormatLocale,
            "M%f,%f Q%f,%f %f,%f",
            sourceX, pntCurrentY - config.messageSelfHeight,
            sourceX + config.messageSelfCurveCPOffset, pntCurrentY - 0.5 * config.messageSelfHeight,
            targetX, pntCurrentY
        ));
        SVGUtil.setAttr(arrow, "marker-end", SVGUtil.refUrlId(ID_MARKER_ARROW_MSG));
        SVGUtil.setAttrs(arrow, config.styleMessage.get(message.getMessageType()));

        group.loaderAddChild(helper, arrow);

        pntCurrentY = pntCurrentY
                + config.messageLinePadding;

        svgDecorator.visitEdge(message);
        mess2svg.put(message, new SvgLabelledElement(arrow, messageName));

        SVGUtil.setAttr(textBackdrop, UiDataConstants.KeyDataNodeId, message.getNodeId());
        SVGUtil.setAttr(messageName, UiDataConstants.KeyDataNodeId, message.getNodeId());
        SVGUtil.setAttr(arrow, UiDataConstants.KeyDataNodeId, message.getNodeId());
        
        return new Rectangle2D.Double(
            minX, startY,
            textX2 - minX, pntCurrentY - startY
        );
    }

    private void _renderLifelineBases(List<ILifeline> lifelines, Group group, SVGLoaderHelper helper) throws SVGException {

        double maxY = pntCurrentY + config.lifelineBaseMarginBottom;
        _updateDiagramHeight(maxY);
        
        for (Line base : lifelineBases) {
            SVGUtil.setAttr(base, "y2", maxY);
        }
    }
    
    private void _applySelectionStyle(IMSDMessage message, SvgLabelledElement element) throws SVGElementException {
        if (selectedNodes.contains(message.getNodeId())) {
            SVGUtil.setAttr(element.element, "stroke", "red");
            SVGUtil.setAttr(element.element, "stroke-width", "3");
        } else {
//            SVGUtil.setAttr(element.element, "stroke", "black");
//            SVGUtil.setAttr(element.element, "stroke-width", "1");
        }
    }
}
