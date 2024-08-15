package org.processmining.algorithms.statechart.m2m.ui.decorate;

import java.awt.Color;

public class UiDecoration {

    private final String label;
    private final double strokeWidth;
    private final Color colorText;
    private final Color colorBackground;
    private final Color colorStroke;

    public UiDecoration(String label, double strokeWidth, 
            Color colorText, Color colorBackground, Color colorStroke) {
        this.label = label;
        this.strokeWidth  = strokeWidth;
        this.colorText = colorText;
        this.colorBackground = colorBackground;
        this.colorStroke = colorStroke;
    }

    public String getLabel() {
        return label;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public Color getColorText() {
        return colorText;
    }

    public Color getColorBackground() {
        return colorBackground;
    }

    public Color getColorStroke() {
        return colorStroke;
    }
}
