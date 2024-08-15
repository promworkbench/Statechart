package org.processmining.models.statechart.msd;

import java.util.ArrayList;
import java.util.List;

import org.processmining.utils.statechart.tree.impl.AbstractTreeNode;

import com.google.common.base.Preconditions;

public class MSDNode extends AbstractTreeNode<IMSDNode> implements IMSDNode {

    protected static int cnt = 0;
    
    private final List<IMSDNode> children;
    private IMSDNode parent;
    private final String name;
    private final String id;

    public MSDNode(IMSDNode parent, String name) {
        this(parent, name, String.format("n%d", cnt++));
    }
    
    public MSDNode(IMSDNode parent, String name, String id) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(id);
        
        children = new ArrayList<IMSDNode>();
        this.parent = parent;
        this.name = name;
        this.id = id;
    }
    
    public void setParent(IMSDNode parent) {
        this.parent = parent;
    }
    
    @Override
    public IMSDNode getParent() {
        return parent;
    }
    
    public void addChild(IMSDNode child) {
        children.add(child);
    }

    @Override
    public List<IMSDNode> getChildren() {
        return children;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("'");
        buf.append(name);
        buf.append("'");

        if (!getChildren().isEmpty()) {
            buf.append("(");
            String sep = "";
            for (IMSDNode child : getChildren()) {
                buf.append(sep);
                buf.append(child.toString());
                sep = ", ";
            }
            buf.append(")");
        }

        return buf.toString();
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MSDNode) {
            return ((MSDNode) other).id.equals(id);
        }
        return false;
    }

}
