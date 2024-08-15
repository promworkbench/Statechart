package org.processmining.utils.statechart.gfx;

public class MathUtils {

    public static double clamp(double alpha, double min, double max) {
        return Math.max(Math.min(alpha, max), min);
    }

    public static double clamp01(double alpha) {
        return clamp(alpha, 0.0, 1.0);
    }
    
    public static float clamp(float alpha, float min, float max) {
        return Math.max(Math.min(alpha, max), min);
    }
}
