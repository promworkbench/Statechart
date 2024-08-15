package org.processmining.ui.statechart.color;

import java.awt.Color;
import java.util.Arrays;

import org.processmining.utils.statechart.gfx.ColorUtil;

import com.google.common.base.Preconditions;

public class ColorRangeMap implements IColorMap {

    private Color[] colors;
    private double[] changepoints;
    
    private double minVal;
    private double maxVal;
    private double[] invValRange;

    public ColorRangeMap(Color... colors) {
        Preconditions.checkNotNull(colors);
        Preconditions.checkArgument(colors.length >= 2);
        this.colors = colors;
        this.changepoints = new double[colors.length - 1];

        this.minVal = 0;
        this.maxVal = 0;
        this.invValRange = new double[colors.length - 1];
    }

    @Override
    public void setValueChangepoints(double... changepoints) {
        Preconditions.checkNotNull(changepoints);
        Preconditions.checkArgument(changepoints.length == colors.length - 2
                                 || changepoints.length == colors.length - 1);
        // Add max as last changepoint, see getColor() implementation
        this.changepoints = Arrays.copyOf(changepoints, colors.length - 1);
    }
    
    @Override
    public void setValueRange(double minVal, double maxVal) {
        Preconditions.checkArgument(minVal <= maxVal);
        this.minVal = minVal;
        this.maxVal = maxVal;
        
        // setup changepoints
        if (changepoints == null) {
            changepoints = new double[colors.length - 1];
            for (int i = 0; i < changepoints.length; i++) {
                changepoints[i] = maxVal;
            }
        } else {
            changepoints[changepoints.length - 1] = maxVal;
        }
        
        // setup inverse multipliers
        if (invValRange == null || invValRange.length != changepoints.length) {
            invValRange = new double[changepoints.length];
        }
        
        double cpMin = minVal;
        for (int i = 0; i < changepoints.length; i++) {
            double cpMax = changepoints[i];
            double valRange = (cpMax - cpMin);
            if (valRange != 0) {
                this.invValRange[i] = 1.0 / valRange;
            } else {
                this.invValRange[i] = 0;
            }
            cpMin = cpMax;
        }
    }

    @Override
    public double getMin() {
        return minVal;
    }

    @Override
    public double getMax() {
        return maxVal;
    }

    @Override
    public Color getColor(double value) {
        double cpMin = minVal;
        for (int i = 0; i < changepoints.length; i++) {
            double cpMax = changepoints[i];
            if (value <= cpMax) {
                double alpha = 1.0;
                if (invValRange[i] != 0) {
                    alpha = (value - cpMin) * invValRange[i];  
                }
                return ColorUtil.lerp(colors[i], colors[i + 1], alpha);
            }
            cpMin = cpMax;
        }
        
        // fallback: use last max
        int i = changepoints.length - 1;
        double alpha = 1.0;
        if (invValRange[i] != 0) {
            alpha = (value - cpMin) * invValRange[i];  
        }
        return ColorUtil.lerp(colors[i], colors[i + 1], alpha);
    }

}
