package org.processmining.algorithms.statechart.layout;

public interface PGNodeExtendProvider<T> {

    public double getWidth(T node);
    
    public double getHeight(T node);
}
