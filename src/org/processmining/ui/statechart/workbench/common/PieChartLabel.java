package org.processmining.ui.statechart.workbench.common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.ui.statechart.color.IColorMap;
import org.processmining.ui.statechart.color.IColorMapProvider;
import org.processmining.utils.statechart.gfx.MathUtils;

public class PieChartLabel extends JPanel {

    private static final long serialVersionUID = -1383294960777109759L;

    public static final String DefaultValueFormat = "%.1f%%";
    
    public static final int ChartDefaultOffset = 5;
    public static final int ChartDefaultSize = 30;


    private double min;
    private double max;
    private double value;
    private Color colorChartBack;
    private IColorMap colormapChartFront;

    private int arcAngle;

    private int chartOffsetX = ChartDefaultOffset;
    private int chartOffsetY = ChartDefaultOffset;

    private int chartWidth = ChartDefaultSize;
    private int chartHeight = ChartDefaultSize;

    private JLabel valueLabel;

    private JLabel valueSubLabel;

    private String valueFormat;

    public PieChartLabel(String label, double min, double max, Color colorChartBack,
            IColorMapProvider colormapChartFront) {
        this(label, DefaultValueFormat, min, max, colorChartBack, colormapChartFront);
    }
    
    public PieChartLabel(String label, String valueFormat, double min, double max, Color colorChartBack,
            IColorMapProvider colormapChartFront) {
        this.min = min;
        this.max = max;
        this.colorChartBack = colorChartBack;
        this.colormapChartFront = colormapChartFront.create();
        
        this.valueFormat = valueFormat;
        this.valueLabel = new JLabel();
        valueLabel.setFont(valueLabel.getFont().deriveFont(16.0f));
        this.valueSubLabel = new JLabel(label);
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(valueLabel);
        this.add(valueSubLabel);
        
        this.setBorder(BorderFactory.createCompoundBorder(
            this.getBorder(), 
            BorderFactory.createEmptyBorder(0, chartOffsetX * 2 + chartWidth, 0, 0)
        ));
        
        setValue(Double.NaN);
    }

    public void setValue(double value) {
        if (Double.isNaN(value)) {
            valueLabel.setText("?");
            value = min;
        } else {
            valueLabel.setText(String.format(valueFormat, value));
        }
        
        this.value = value;
        this.arcAngle = -1 * (int) Math.round(MathUtils.clamp((value - min) / (max - min) * 360.0, 0.0, 360.0));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Paint basic of button widget
        super.paintComponent(g);
        
        // Add pie chart
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            g2.setColor(colorChartBack);
            g2.fillOval(chartOffsetX, chartOffsetY, chartWidth, chartHeight);
            
            g2.setColor(colormapChartFront.getColor(value));
            g2.fillArc(chartOffsetX, chartOffsetY, chartWidth, chartHeight, 90, arcAngle);
            
        } finally {
            g2.dispose();
        }
    }

}
