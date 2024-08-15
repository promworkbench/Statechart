package org.processmining.algorithms.statechart.layout;


public interface PGLayoutConfiguration<T> {
    
    public Direction getDirection();
    
    public double getGapBetweenNodes(T node1, T node2);
    
    public Padding getPaddingNode(T node);
    
    public boolean isLayoutOrtogonal(T node);
    
    public boolean isLayoutReverse(T node);
    
    public CenterAlignment getNodeCenterAlignment(T node);
    
    public ForwardAlignment getNodeForwardAlignment(T node);
    
}
