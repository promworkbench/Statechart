package org.processmining.algorithms.statechart.layout;

public enum Direction {
    TopDown(false), 
    LeftRight(true), 
    BottomTop(false), 
    RightLeft(true);
    
    private final boolean isHorizontal;

    private Direction(boolean isHorizontal) {
        this.isHorizontal = isHorizontal;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }
}
