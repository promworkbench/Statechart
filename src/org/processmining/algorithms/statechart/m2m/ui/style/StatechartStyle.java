package org.processmining.algorithms.statechart.m2m.ui.style;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Map;

import org.processmining.algorithms.statechart.layout.CenterAlignment;
import org.processmining.algorithms.statechart.layout.Direction;
import org.processmining.algorithms.statechart.layout.ForwardAlignment;
import org.processmining.algorithms.statechart.layout.PGLayoutConfiguration;
import org.processmining.algorithms.statechart.layout.PGNodeExtendProvider;
import org.processmining.algorithms.statechart.layout.Padding;
import org.processmining.algorithms.statechart.m2m.ui.layout.StatechartLayoutNode;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.models.statechart.sc.ISCCompositeState;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.SCStateType;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.ui.statechart.svg.SvgIcons;
import org.processmining.utils.statechart.svg.ISVGReference;

import sun.swing.SwingUtilities2;

import com.google.common.collect.ImmutableMap;

public class StatechartStyle implements PGNodeExtendProvider<StatechartLayoutNode>, PGLayoutConfiguration<StatechartLayoutNode> {

    private static final double canvasPadding = 15;

    private static final double nodeWidthPadding = 8;
    private static final double nodeHeightPadding = 8;
    private static final double nodeCompositePadding = 8;

    private static final double nodeSymbolWidth = 12;
    private static final double nodeSymbolHeight = 12;
    private static final double nodeSymbolPadding = 4;

    private static final double nodePointWidth = 4;
    private static final double nodePointHeight = 4;
    
    private static final double nodeSplitPseudoWidth = 4;
    private static final double nodeSplitPseudoHeight = 16;

    private static final double nodeSepPadding = 16;
    
    private static final Font nodeFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

    private static final Map<String, String> styleNodeName = ImmutableMap.<String, String>builder()
            .put("font-family",  "Sans Serif")
            .put("font-size", "14")
            .put("font-style", "normal")
            .put("font-weight", "normal")
            .build();
    
    private static final Map<String, String> styleNodeBox = ImmutableMap.<String, String>builder()
            .put("stroke", "black")
            .put("fill", "none")
            .put("rx", "10")
            .put("ry", "10")
            .build();

    private static final Map<String, String> styleNodePoint = ImmutableMap.<String, String>builder()
            .put("stroke", "black")
            .put("fill", "black")
            .build();

    private static final Map<String, String> styleNodeSplitPseudo = ImmutableMap.<String, String>builder()
            .put("stroke", "black")
            .put("fill", "black")
            .build();
    
    private static final Map<String, String> styleEdge = ImmutableMap.<String, String>builder()
            .put("stroke", "black")
            .put("fill", "none")
            .build();

    public static final boolean renderBoundingBoxes = false;
    
    private Direction layoutDir;
    private IActivityLabeler activityLabeler;

    private Statechart model;

    
    public StatechartStyle(Statechart model, Direction layoutDir,
            IActivityLabeler activityLabeler) {
        this.model = model;
        this.layoutDir = layoutDir;
        this.activityLabeler = activityLabeler;
    }
    
    @Override
    public double getWidth(StatechartLayoutNode node) {
        if (node.getState() != null) {
            ISCState state = node.getState();
            if (state.getStateType() == SCStateType.PointPseudo) {
                return nodePointWidth;
            } else if (state.getStateType() == SCStateType.SplitPseudo
                    || state.getStateType() == SCStateType.JoinPseudo) {
                if (layoutDir.isHorizontal()) {
                    return nodeSplitPseudoWidth;
                } else {
                    return nodeSplitPseudoHeight;
                }
            }
            double width = nodeWidthPadding * 2;
            
            boolean hasSymbol = nodeHasSymbol(state);
            boolean hasLabel = nodeHasLabel(state);
            
            if (hasSymbol) {
                width += nodeSymbolWidth;
            }
            if (hasSymbol && hasLabel) {
                width += nodeSymbolPadding;
            }
            if (hasLabel) {
                FontMetrics fontMetrics = SwingUtilities2.getFontMetrics(null, nodeFont);
                String s = activityLabeler.getLabel(state);
                double textMaxWidth = 0;
                String[] lines = s.split("\n");
                for (String line : lines) {
                    textMaxWidth = Math.max(textMaxWidth, fontMetrics.stringWidth(line));
                }
                width += textMaxWidth;
            }
            
            return width;
        } else {
            return 0;
        }
    }

    @Override
    public double getHeight(StatechartLayoutNode node) {
        if (node.getState() != null) {
            ISCState state = node.getState();
            if (state.getStateType() == SCStateType.PointPseudo) {
                return nodePointHeight;
            } else if (state.getStateType() == SCStateType.SplitPseudo
                    || state.getStateType() == SCStateType.JoinPseudo) {
                if (layoutDir.isHorizontal()) {
                    return nodeSplitPseudoHeight;
                } else {
                    return nodeSplitPseudoWidth;
                }
            }
            double height = 0;

            boolean hasSymbol = nodeHasSymbol(state);
            boolean hasLabel = nodeHasLabel(state);
            
            if (hasSymbol) {
                height = nodeSymbolHeight;
            }
            if (hasLabel) {
                FontMetrics fontMetrics = SwingUtilities2.getFontMetrics(null, nodeFont);
                double h = fontMetrics.getHeight();
                String s = activityLabeler.getLabel(state);
                String[] lines = s.split("\n");
                height = Math.max(height, h * lines.length);
            }
            
            return height + nodeHeightPadding * 2;
        } else {
            return 0;
        }
    }

    public boolean nodeHasLabel(ISCState state) {
        if (model.isInitialState(state) || model.isEndState(state)
                || state instanceof Statechart) {
            return false;
        }
        switch (state.getStateType()) {
        case Simple:
        case Collapsed:
        case ErrorTrigger:
        case OrComposite:
        case Recurrent:
            return true;
        default:
            return false;
        }
    }

    public boolean nodeHasSymbol(ISCState state) {
        if (model.isInitialState(state) || model.isEndState(state)) {
            return true;
        }
        switch (state.getStateType()) {
        case OrComposite:
        case Collapsed:
        case Recurrent:
        case ErrorTrigger:
            return true;
        default:
            return false;
        }
    }

    public double getCanvasPadding() {
        return canvasPadding;
    }

    @Override
    public Direction getDirection() {
        return layoutDir;
    }

    @Override
    public double getGapBetweenNodes(StatechartLayoutNode node1,
            StatechartLayoutNode node2) {
//        FontMetrics fontMetrics = SwingUtilities2.getFontMetrics(null, nodeFont);
//        String s = "1000 (100%)";
//        return fontMetrics.stringWidth(s) + nodeWidthPadding * 2;
        return nodeSepPadding;
    }

    @Override
    public Padding getPaddingNode(StatechartLayoutNode node) {
        if (node.getState() != null
            && (node.getState() instanceof ISCCompositeState)) {
                boolean hasLabel = nodeHasLabel(node.getState());
                double top = nodeCompositePadding;
                if (hasLabel) {
                    FontMetrics fontMetrics = SwingUtilities2.getFontMetrics(null, nodeFont);
                    double h = fontMetrics.getHeight();
                    String s = activityLabeler.getLabel(node.getState());
                    String[] lines = s.split("\n");
                    top += h * lines.length;
                }
                return new Padding(top, nodeCompositePadding, nodeCompositePadding, nodeCompositePadding);
        } else {
            return new Padding(0);
        }
    }

    @Override
    public boolean isLayoutOrtogonal(StatechartLayoutNode node) {
        if (node.getNode() != null) {
            switch (node.getNode().getNodeType()) {
            case Choice:
            case AndComposite:
            case Loop:
            case LoopCancel:
            case SeqCancel:
//            case OrComposite:
                return true;
            default:
                return false;    
            }
        } else {
            return false;
        }
    }
    
    @Override
    public boolean isLayoutReverse(StatechartLayoutNode node) {
        boolean reverse = false;
        if (node != null && node.getParent() != null) {
            StatechartLayoutNode parent = node.getParent();
            reverse = isLayoutReverse(parent);
            
            if (parent.getNode() != null) {
                switch (parent.getNode().getNodeType()) {
                case Loop:
                case LoopCancel:
                    if (parent.getChildren().indexOf(node) > 0) {
                        reverse = !reverse;
                    }
                    break;
                default:
                    break;
                }
            }
        }
        return reverse;
    }

    @Override
    public CenterAlignment getNodeCenterAlignment(StatechartLayoutNode node) {
        if (node.getNode() != null) {
            switch (node.getNode().getNodeType()) {
            case Loop:
            case LoopCancel:
            case SeqCancel:
                return CenterAlignment.CenterOnFirstChild;
            default:
                return CenterAlignment.CenterWhole;    
            }
        } else {
            return CenterAlignment.CenterWhole;
        }
    }

    @Override
    public ForwardAlignment getNodeForwardAlignment(StatechartLayoutNode node) {
        return ForwardAlignment.Middle;
    }

    public double getNodeSymbolWidth() {
        return nodeSymbolWidth;
    }

    public double getNodeSymbolHeight() {
        return nodeSymbolHeight;
    }

    public double getNodeSymbolPadding() {
        return nodeSymbolPadding;
    }

    public double getNodeWidthPadding() {
        return nodeWidthPadding;
    }

    public double getNodeHeightPadding() {
        return nodeHeightPadding;
    }

    public ISVGReference getNodeIcon(ISCState state) {
        if (model.isInitialState(state)) {
            return SvgIcons.IconStart;
        }
        if (model.isEndState(state)) {
            return SvgIcons.IconEnd;
        }
        switch (state.getStateType()) {
        case Collapsed:
            return SvgIcons.IconPlus;
        case OrComposite:
            return SvgIcons.IconMinus;
        case ErrorTrigger:
            return SvgIcons.IconError;
        case Recurrent:
            return SvgIcons.IconRecurrent;
        default:
            return SvgIcons.Blank;
        }
    }

    public IActivityLabeler getActivityLabeler() {
        return activityLabeler;
    }

    public double getLabelHeightOffset(ISCState state) {
        if (nodeHasLabel(state)) {
            FontMetrics fontMetrics = SwingUtilities2.getFontMetrics(null, nodeFont);
            return fontMetrics.getHeight();// fontMetrics.getMaxDescent();
        } else {
            return 0;
        }
    }

    public Map<String, String> getStyleNodeName() {
        return styleNodeName;
    }

    public Map<String, String> getStyleNodeBox() {
        return styleNodeBox;
    }

    public Map<String, String> getStyleEdge() {
        return styleEdge;
    }
    
    public Map<String, String> getStyleNodePoint() {
        return styleNodePoint;
    }

    public Map<String, String> getStyleNodeSplitPseudo() {
        return styleNodeSplitPseudo;
    }
    
    public double getLoopOffset() {
        if (layoutDir.isHorizontal()) {
            return nodeHeightPadding;
        } else {
            return nodeWidthPadding;
        }
    }
}
