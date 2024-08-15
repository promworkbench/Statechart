package org.processmining.algorithms.statechart.layout;

import com.google.common.base.Preconditions;

public class DefaultPGLayoutConfiguration<T> implements PGLayoutConfiguration<T> {

    private final Direction direction;
    private final double gapBetweenNodes;
    private final boolean isLayoutOrtogonal;
    private boolean isLayoutReverse;
    private final Padding paddingNode;
    private final CenterAlignment nodeCenterAlignment;
    private final ForwardAlignment nodeForwardAlignment;

    public DefaultPGLayoutConfiguration(Direction direction, 
            double gapBetweenNodes, boolean isLayoutOrtogonal, boolean isLayoutReverse,
            Padding paddingNode,
            CenterAlignment nodeCenterAlignment,
            ForwardAlignment nodeForwardAlignment) {
        Preconditions.checkArgument(gapBetweenNodes >= 0);
        Preconditions.checkNotNull(paddingNode);
        Preconditions.checkNotNull(nodeCenterAlignment);
        Preconditions.checkNotNull(nodeForwardAlignment);

        this.direction = direction;
        this.gapBetweenNodes = gapBetweenNodes;
        this.isLayoutReverse = isLayoutReverse;
        this.isLayoutOrtogonal = isLayoutOrtogonal;
        this.paddingNode = paddingNode;
        this.nodeCenterAlignment = nodeCenterAlignment;
        this.nodeForwardAlignment = nodeForwardAlignment;
    }
    
    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public double getGapBetweenNodes(T node1, T node2) {
        return gapBetweenNodes;
    }

    @Override
    public boolean isLayoutOrtogonal(T node) {
        return isLayoutOrtogonal;
    }

    @Override
    public boolean isLayoutReverse(T node) {
        return isLayoutReverse;
    }

    @Override
    public Padding getPaddingNode(T node) {
        return paddingNode;
    }

    @Override
    public CenterAlignment getNodeCenterAlignment(T node) {
        return nodeCenterAlignment;
    }

    @Override
    public ForwardAlignment getNodeForwardAlignment(T node) {
        return nodeForwardAlignment;
    }

}
