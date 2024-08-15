package org.processmining.utils.statechart.gfx;

import java.awt.Color;

/**
 * 
 * @author mleemans
 *
 *         Switch contrasting due to:
 * @see http://www.nbdtech.com/Blog/archive/2008
 *      /04/27/Calculating-the-Perceived-Brightness-of-a-Color.aspx
 *
 */
public class ColorUtil {

    private static final int DefaultBrightnessCutoff = 130;

    public static Color lerp(Color start, Color end, double alpha) {
        alpha = MathUtils.clamp(alpha, 0, 1);
        int r = (int) Math.floor(start.getRed() * (1.0 - alpha) + end.getRed()
                * alpha);
        int g = (int) Math.floor(start.getGreen() * (1.0 - alpha)
                + end.getGreen() * alpha);
        int b = (int) Math.floor(start.getBlue() * (1.0 - alpha)
                + end.getBlue() * alpha);
        int a = (int) Math.floor(start.getAlpha() * (1.0 - alpha)
                + end.getAlpha() * alpha);
        return new Color(r, g, b, a);
    }

    public static String rgbToHexString(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        return String.format("#%02X%02X%02X", r, g, b);
    }

    public static int brightness(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        return (int) Math.sqrt(r * r * 0.241 + g * g * 0.691 + b * b * 0.068);
    }

    public static Color switchContrasting(Color backgroundColor, Color light,
            Color dark) {
        return switchContrasting(backgroundColor, light, dark,
                DefaultBrightnessCutoff);
    }

    public static Color switchContrasting(Color backgroundColor, Color light,
            Color dark, int threshold) {
        return brightness(backgroundColor) < threshold ? light : dark;
    }
}
