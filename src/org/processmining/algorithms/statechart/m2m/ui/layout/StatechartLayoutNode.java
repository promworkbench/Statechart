package org.processmining.algorithms.statechart.m2m.ui.layout;

import java.util.ArrayList;
import java.util.List;

import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.utils.statechart.tree.impl.AbstractTreeNode;

public class StatechartLayoutNode extends AbstractTreeNode<StatechartLayoutNode> {

    private final StatechartLayoutNode parent;
    private final List<StatechartLayoutNode> children;
    
    private IEPTreeNode node;
    private ISCState state;
    
    public StatechartLayoutNode(StatechartLayoutNode parent,
            IEPTreeNode node, ISCState state) {
        children = new ArrayList<StatechartLayoutNode>();
        this.parent = parent;
        this.node = node;
        this.state = state;
    }

    public void addChild(StatechartLayoutNode o) {
        children.add(o);
    }
    
    @Override
    public List<StatechartLayoutNode> getChildren() {
        return children;
    }

    @Override
    public StatechartLayoutNode getParent() {
        return parent;
    }

    public void setNode(IEPTreeNode node) {
        this.node = node;
    }

    public void setState(ISCState state) {
        this.state = state;
    }
    
    public IEPTreeNode getNode() {
        return node;
    }

    public ISCState getState() {
        return state;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("<");
        if (node != null) {
            buf.append(node.getNodeType().getSymbol());
            buf.append("'");
            buf.append(node.getLabel());
            buf.append("'");
        } else {
            buf.append("null");
        }
        buf.append(", ");
        if (state != null) {
            buf.append(state.getStateType().getSymbol());
            buf.append("'");
            buf.append(state.getLabel());
            buf.append("'");
        } else {
            buf.append("null");
        }
        buf.append(">");

        if (!getChildren().isEmpty()) {
            buf.append("(");
            String sep = "";
            for (StatechartLayoutNode child : getChildren()) {
                buf.append(sep);
                buf.append(child.toString());
                sep = ", ";
            }
            buf.append(")");
        }

        return buf.toString();
    }

}
