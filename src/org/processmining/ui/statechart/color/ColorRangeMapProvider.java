package org.processmining.ui.statechart.color;

import java.awt.Color;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Preconditions;

public class ColorRangeMapProvider implements IColorMapProvider {

    private Color[] colors;
    private double[] changepoints;
    private Double min;

    public ColorRangeMapProvider(Color... colors) {
        Preconditions.checkNotNull(colors);
        Preconditions.checkArgument(colors.length >= 2);
        this.colors = colors;
        this.min = null;
        this.changepoints = null;
    }

    public ColorRangeMapProvider(Color[] colors, double[] changepoints) {
        Preconditions.checkNotNull(colors);
        Preconditions.checkNotNull(changepoints);
        Preconditions.checkArgument(colors.length >= 2);
        Preconditions.checkArgument(changepoints.length == colors.length - 2);
        this.colors = colors;
        this.min = null;
        this.changepoints = changepoints;
    }

    public ColorRangeMapProvider(Color[] colors, double min, double[] changepoints) {
        Preconditions.checkNotNull(colors);
        Preconditions.checkNotNull(changepoints);
        Preconditions.checkArgument(colors.length >= 2);
        Preconditions.checkArgument(changepoints.length == colors.length - 1);
        this.colors = colors;
        this.min = min;
        this.changepoints = changepoints;
    }

    @Override
    public IColorMap create() {
        ColorRangeMap colorMap = new ColorRangeMap(colors);
        if (changepoints != null) {
            colorMap.setValueChangepoints(changepoints);
        }
        if (min != null && changepoints.length == colors.length - 1) {
            colorMap.setValueRange(min, changepoints[changepoints.length - 1]);
        }
        return colorMap;
    }

    @Override
    public IColorMapProvider invert() {
        Color[] colorsCopy = Arrays.copyOf(colors, colors.length);
        ArrayUtils.reverse(colorsCopy);
        return new ColorRangeMapProvider(colorsCopy);
    }

    @Override
    public IColorMapProvider deriveWithValueChangepoints(double... changepoints) {
        Preconditions.checkNotNull(changepoints);
        Preconditions.checkArgument(changepoints.length == colors.length - 2);
        return new ColorRangeMapProvider(colors, changepoints);
    }
    
    @Override
    public IColorMapProvider deriveWithValueRanges(double min, double... changepoints) {
        Preconditions.checkNotNull(changepoints);
        Preconditions.checkArgument(changepoints.length == colors.length - 1);
        return new ColorRangeMapProvider(colors, min, changepoints);
    }

}
