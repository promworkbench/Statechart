package org.processmining.ui.statechart.color;

import java.awt.Color;

public class ColorSets {
    
public static final IColorMapProvider Greyscale = new ColorRangeMapProvider(
            new Color(229, 229, 229), Color.black);
    
    public static final IColorMapProvider DarkGreyscale = new ColorRangeMapProvider(
        new Color(137, 137, 137), Color.black);
    
    public static final IColorMapProvider Bluescale = new ColorRangeMapProvider(
        new Color(241, 238, 246), new Color(4, 90, 141));
    
    public static final IColorMapProvider Redscale = new ColorRangeMapProvider(
        new Color(254, 240, 217), new Color(179, 0, 0));
    
    public static final IColorMapProvider DarkRedscale = new ColorRangeMapProvider(
        new Color(252, 201, 118), new Color(179, 0, 0));

    public static final IColorMapProvider ModelMoveScale = new ColorRangeMapProvider(
        new Color(247, 255, 229), new Color(224, 116, 224));
    public static final IColorMapProvider LogMoveScale = new ColorRangeMapProvider(
        new Color(247, 255, 229), new Color(255, 233, 0));

    public static final IColorMapProvider LogModelMoveScale = new ColorRangeMapProvider(
        new Color(255, 233, 0),
        new Color(247, 255, 229),
        new Color(224, 116, 224)
    );
    
    public static final IColorMapProvider RedNeutralGreenscale = new ColorRangeMapProvider(
        new Color(179, 0, 0),
        //new Color(236, 249, 117),
        new Color(249, 234, 209),
        new Color(71, 196, 0)
    );
    
    public static final Color NeutralGrey = new Color(196, 196, 196);
    public static final IColorMapProvider RedOrangeGreenscale = new ColorRangeMapProvider(
        new Color(179, 0, 0),
        new Color(255, 198, 94),
        new Color(71, 196, 0)
    );

    
    
    private ColorSets() {
        
    }
}
