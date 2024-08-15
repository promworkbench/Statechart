package org.processmining.algorithms.statechart.layout;

import com.google.common.base.Preconditions;

public class Padding {

    private final double top;
    private final double right;
    private final double bottom;
    private final double left;
    
    public Padding(double top, double right, double bottom, double left) {
        Preconditions.checkArgument(top >= 0);
        Preconditions.checkArgument(right >= 0);
        Preconditions.checkArgument(bottom >= 0);
        Preconditions.checkArgument(left >= 0);
        
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public Padding(double value) {
        this(value, value, value, value);
    }

    public double getTop() {
        return top;
    }

    public double getRight() {
        return right;
    }

    public double getBottom() {
        return bottom;
    }

    public double getLeft() {
        return left;
    }

    public double getHorizontal() {
        return left + right;
    }

    public double getVertical() {
        return top + bottom;
    }
}
