package org.processmining.ui.statechart.workbench.discovery.vis.settings;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;

import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.ui.statechart.color.IColorMap;

public class AnalysisSettingsWidget extends PopoutSettingsWidget {

    private static final long serialVersionUID = 453570460136045108L;
    
    public static final int LegendAreaHeight = 74;
    
    public static final int LegendOffset = 5;
    public static final int LegendTextHeight = 14;
    public static final int LegendBoxHeight = 15;
    public static final int LegendBoxWidth = WidgetWidth - 2 * LegendOffset;

    public static final boolean LegendTextAbove = true;
    public static final boolean LegendTextBelow = false;

    private static final int LegendFillSteps = 10;
    private static final int LegendFillWidth = LegendBoxWidth / LegendFillSteps;
    
    private IColorMap nodeColorMap = null;
    private IMetricValueConvertor nodeValueConvertor = null;
    private IColorMap edgeColorMap = null;
    private IMetricValueConvertor edgeValueConvertor = null;

    public AnalysisSettingsWidget(IPopoutSettingsPanel settingsPanel) {
        super(settingsPanel);
        setBorder(BorderFactory.createCompoundBorder(
            getBorder(),
            new EmptyBorder(0, 0, LegendAreaHeight + LegendOffset, 0)
        ));
    }
    
    @Override
    protected int getWidgetHeight() {
        return WidgetHeight + LegendAreaHeight + LegendOffset;
    }

    public void setOverlayColorLegend(IColorMap nodeColorMap, IMetricValueConvertor nodeValueConvertor,
            IColorMap edgeColorMap, IMetricValueConvertor edgeValueConvertor) {
        this.nodeColorMap = nodeColorMap;
        this.nodeValueConvertor = nodeValueConvertor;
        this.edgeColorMap = edgeColorMap;
        this.edgeValueConvertor = edgeValueConvertor;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Paint basic of button widget
        super.paintComponent(g);
        
        // Add legend
        Graphics g2 = g.create();
        try {
            if (nodeColorMap != null && nodeValueConvertor != null) {
                renderColorMap(nodeColorMap, nodeValueConvertor, LegendTextAbove, g2, 
                        LegendOffset, 
                        WidgetHeight + LegendOffset);
            } else {
                renderNoOverlay(g2, LegendTextAbove, 
                        LegendOffset, 
                        WidgetHeight + LegendOffset);
            }
            if (edgeColorMap != null && edgeValueConvertor != null) {
                renderColorMap(edgeColorMap, edgeValueConvertor, LegendTextBelow, g2, 
                        LegendOffset, 
                        WidgetHeight + LegendOffset * 2 + LegendTextHeight + LegendBoxHeight);
            } else {
                renderNoOverlay(g2, LegendTextBelow, 
                        LegendOffset, 
                        WidgetHeight + LegendOffset * 2 + LegendTextHeight + LegendBoxHeight);
            }
        } finally {
            g2.dispose();
        }
    }

    private void renderNoOverlay(Graphics g2, boolean textPos, int x, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int fontDecent = fm.getDescent();
        
        if (textPos == LegendTextAbove) {
            g2.drawString("No overlay", x, y + LegendTextHeight - fontDecent - 1);
            
            g2.setColor(Color.black);
            g2.drawRect(x, y + LegendTextHeight, LegendBoxWidth, LegendBoxHeight);
        } else {
            g2.setColor(Color.black);
            g2.drawRect(x, y, LegendBoxWidth, LegendBoxHeight);
            
            g2.drawString("No overlay", x, y + LegendBoxHeight + 1 + LegendTextHeight);
        }
    }

    private void renderColorMap(IColorMap colorMap, IMetricValueConvertor valueConvertor,
            boolean textPos, Graphics g2, int x, int y) {
        
        FontMetrics fm = g2.getFontMetrics();
        int fontDecent = fm.getDescent();
        
        // Avoid long 0.1234567E-45 like numbers in legend text
        double valMin = colorMap.getMin();
        double valMax = colorMap.getMax();
        if (Math.abs(valMin) > 0 && Math.abs(valMin) < 0.0000001) {
            valMin = 0;
        }
        if (Math.abs(valMax) > 0 && Math.abs(valMax) < 0.0000001) {
            valMax = 0;
        }
        String vMin = valueConvertor.toStringShort(valMin);
        String vMax = valueConvertor.toStringShort(valMax);
        
        int vMinX = x;
        int vMaxX = x + LegendBoxWidth - fm.stringWidth(vMax);
        
        // first text
        if (textPos == LegendTextAbove) {
            g2.drawString(vMin, vMinX, y + LegendTextHeight - fontDecent - 1);
            g2.drawString(vMax, vMaxX, y + LegendTextHeight - fontDecent - 1);
            y = y + LegendTextHeight;
        }
        
        // legend box fill
        double min = colorMap.getMin();
        double increment = (colorMap.getMax() - colorMap.getMin()) / (double) LegendFillSteps;
        for (int i = 0; i < LegendFillSteps; i++) {
            g2.setColor(colorMap.getColor(min + ((double) i) * increment));
            g2.fillRect(x + i * LegendFillWidth, y, LegendFillWidth, LegendBoxHeight);
        }
        
        // legend box border
        g2.setColor(Color.black);
        g2.drawRect(x, y, LegendBoxWidth, LegendBoxHeight);

        y = y + LegendBoxHeight;
        
        // last text
        if (textPos == LegendTextBelow) {
            g2.drawString(vMin, vMinX, y + LegendTextHeight + 1);
            g2.drawString(vMax, vMaxX, y + LegendTextHeight + 1);
        }
    }

}
