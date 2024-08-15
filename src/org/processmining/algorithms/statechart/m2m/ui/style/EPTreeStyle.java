package org.processmining.algorithms.statechart.m2m.ui.style;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Map;

import org.abego.treelayout.Configuration;
import org.abego.treelayout.NodeExtentProvider;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.ui.statechart.svg.SvgIcons;
import org.processmining.utils.statechart.svg.ISVGReference;

import sun.swing.SwingUtilities2;

import com.google.common.collect.ImmutableMap;

public class EPTreeStyle implements NodeExtentProvider<IEPTreeNode> {

    private static final double gapBetweenLevels = 15;
    private static final double gapBetweenNodes = 15;

    private static final double canvasPadding = 15;
    
    private static final double nodeWidthPadding = 4;
    private static final double nodeHeightPadding = 4;

    private static final double nodeSymbolWidth = 12;
    private static final double nodeSymbolHeight = 12;
    private static final double nodeSymbolPadding = 4;

    private static final double nodePlusMinWidth = 12;
    private static final double nodePlusMinHeight = 12;
    private static final double nodePlusMinPadding = 4;
    
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
            .put("rx", "5")
            .put("ry", "5")
            .build();
    
    private static final Map<String, String> styleEdge = ImmutableMap.<String, String>builder()
            .put("stroke", "black")
            .put("fill", "none")
            .build();
    
    private final IActivityLabeler activityLabeler;
    private final Configuration.Location layoutDir;

    public EPTreeStyle(Configuration.Location layoutDir,
            IActivityLabeler activityLabeler) {
        this.layoutDir = layoutDir;
        this.activityLabeler = activityLabeler;
    }

    public IActivityLabeler getActivityLabeler() {
        return activityLabeler;
    }
    
    public Configuration.Location getLayoutDir() {
        return layoutDir;
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

    public double getGapBetweenLevels() {
        return gapBetweenLevels;
    }

    public double getGapBetweenNodes() {
        return gapBetweenNodes;
    }

    public double getNodeWidthPadding() {
        return nodeWidthPadding;
    }

    public double getNodeHeightPadding() {
        return nodeHeightPadding;
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

    public double getNodePlusMinWidth() {
        return nodePlusMinWidth;
    }

    public double getNodePlusMinHeight() {
        return nodePlusMinHeight;
    }

    public double getNodePlusMinPadding() {
        return nodePlusMinPadding;
    }
    
    public double getCanvasPadding() {
        return canvasPadding;
    }
    
    @Override
    public double getWidth(IEPTreeNode node) {
        double width = nodeWidthPadding * 2;

        boolean hasPlusMin = nodeHasPlusMin(node);
        boolean hasSymbol = nodeHasSymbol(node);
        boolean hasLabel = nodeHasLabel(node);
        
        if (hasPlusMin) {
            width += nodePlusMinWidth;
        }
        if (hasSymbol) {
            if (hasPlusMin) {
                width += nodePlusMinPadding;
            }
            width += nodeSymbolWidth;
        }
        if (hasLabel) {
            if (hasSymbol) {
                width += nodeSymbolPadding;
            } else if (hasPlusMin) {
                width += nodePlusMinPadding;
            }
            FontMetrics fontMetrics = SwingUtilities2.getFontMetrics(null, nodeFont);
            String s = activityLabeler.getLabel(node);
            width += fontMetrics.stringWidth(s);
        }
        
        return width;
    }
    @Override
    public double getHeight(IEPTreeNode node) {
        double height = 0;

        boolean hasPlusMin = nodeHasPlusMin(node);
        boolean hasSymbol = nodeHasSymbol(node);
        boolean hasLabel = nodeHasLabel(node);

        if (hasPlusMin) {
            height = nodePlusMinHeight;
        }
        if (hasSymbol) {
            height = Math.max(height, nodeSymbolHeight);
        }
        if (hasLabel) {
            FontMetrics fontMetrics = SwingUtilities2.getFontMetrics(null, nodeFont);
            double h = fontMetrics.getHeight();
            String s = activityLabeler.getLabel(node);
            String[] lines = s.split("\n");
            height = Math.max(height, h * lines.length);
        }
       
        return height + nodeHeightPadding * 2;
    }

    public double getLabelHeightOffset(IEPTreeNode node) {
        if (nodeHasLabel(node)) {
            FontMetrics fontMetrics = SwingUtilities2.getFontMetrics(null, nodeFont);
            return fontMetrics.getMaxDescent();
        } else {
            return 0;
        }
    }

    public boolean nodeHasPlusMin(IEPTreeNode node) {
        switch (node.getNodeType()) {
        case OrComposite:
        case Collapsed:
            return true;
        default:
            return false;
        }
    }

    public boolean nodeHasSymbol(IEPTreeNode node) {
        switch (node.getNodeType()) {
        case Action:
            return false;
        default:
            return true;
        }
    }

    public boolean nodeHasLabel(IEPTreeNode node) {
        switch (node.getNodeType()) {
        case Action:
        case OrComposite:
        case Collapsed:
        case Recurrent:
        case ErrorTrigger:
            return true;
        default:
            return false;
        }
    }
    
    public ISVGReference getNodeIcon(IEPTreeNode node) {
        switch (node.getNodeType()) {
        case Silent:
            return SvgIcons.TreeOpTau;
        case Seq:
            return SvgIcons.TreeOpSeq;
        case AndComposite:
            return SvgIcons.TreeOpPar;
        case AndInterleaved:
            return SvgIcons.TreeOpInterleaved;
        case Choice:
            return SvgIcons.TreeOpChoice;
        case Collapsed:
        case OrComposite:
            return SvgIcons.TreeOpCompOr;
        case Loop:
            return SvgIcons.TreeOpLoop;
        case Recurrent:
            return SvgIcons.TreeOpRecurOr;
        case LoopCancel:
            return SvgIcons.TreeOpLoopCancel;
        case SeqCancel:
            return SvgIcons.TreeOpSeqCancel;
        case ErrorTrigger:
            return SvgIcons.TreeOpErrorTrigger;
        case Log:
            return SvgIcons.TreeOpLog;
        case Action:
        default:
            return SvgIcons.Blank;
        }
    }

    public ISVGReference getNodePlusMin(IEPTreeNode node) {
        switch (node.getNodeType()) {
        case OrComposite:
            return SvgIcons.IconMinus;
        case Collapsed:
            return SvgIcons.IconPlus;
        default:
            return SvgIcons.Blank;
        }
    }
}
