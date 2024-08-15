package org.processmining.algorithms.statechart.layout;


public interface ModelForPGLayout<T> {
    
    public T getRoot();
    
    public Iterable<T> getChildren(T node);

    public Iterable<T> getChildrenReverse(T node);

    public T getFirstChild(T node);
    
    public boolean isLeaf(T node);
}
