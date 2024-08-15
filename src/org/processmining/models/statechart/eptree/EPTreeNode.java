package org.processmining.models.statechart.eptree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.processmining.utils.statechart.tree.impl.AbstractTreeNode;

import com.google.common.base.Preconditions;

public class EPTreeNode extends AbstractTreeNode<IEPTreeNode> 
    implements IEPTreeNode {

    protected static int cnt = 0;

    private final List<IEPTreeNode> children;
    private IEPTreeNode parent;
    private final String label;
    private final EPNodeType type;
    private final String id;

    private IEPTree tree;

    public EPTreeNode(IEPTree tree, IEPTreeNode parent, EPNodeType type) {
        this(tree, parent, type, "");
    }

    public EPTreeNode(IEPTree tree, IEPTreeNode parent, EPNodeType type, String label) {
        this(tree, parent, type, label, String.format("n%d", cnt++));
    }

    public EPTreeNode(IEPTree tree, IEPTreeNode parent, EPNodeType type, String label,
            String id) {
        Preconditions.checkNotNull(tree);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(label);
        Preconditions.checkNotNull(id);

        children = new ArrayList<IEPTreeNode>();
        this.tree = tree;
        this.parent = parent;
        this.label = label;
        this.type = type;
        this.id = id;
    }

    @Override
    public IEPTreeNode getParent() {
        return parent;
    }

    public void setParent(IEPTreeNode parent) {
        this.parent = parent;
    }

    @Override
    public List<IEPTreeNode> getChildren() {
        return children;
    }

    @Override
    public EPNodeType getNodeType() {
        return type;
    }

    @Override
    public boolean isLeaf() {
        return type.isLeafType();
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void addNode(IEPTreeNode node) {
        children.add(node);
    }

    @Override
    public void removeNode(IEPTreeNode node) {
        children.remove(node);
    }

    @Override
    public void replaceNode(int index, IEPTreeNode node) {
        Preconditions.checkElementIndex(index, children.size());
        children.set(index, node);
    }
    @Override
    public void insertNode(int index, IEPTreeNode node) {
        Preconditions.checkElementIndex(index, children.size());
        children.add(index, node);
    }
    

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(type.getSymbol());
        buf.append("'");
        buf.append(label);
        buf.append("'");

        if (!isLeaf()) {
            buf.append("(");
            String sep = "";
            for (IEPTreeNode child : getChildren()) {
                buf.append(sep);
                buf.append(child.toString());
                sep = ", ";
            }
            buf.append(")");
        }

        return buf.toString();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof EPTreeNode) {
            return ((EPTreeNode) other).id.equals(id);
        }
        return false;
    }

    @Override
    public Pair<Set<IEPTreeNode>, Set<IEPTreeNode>> getEdgeSemantics() {
        return Pair.of(getEdgeFromSemantics(), getEdgeToSemantics());
    }

    @Override
    public Set<IEPTreeNode> getEdgeFromSemantics() {
        return EPTreeSemantics.getNodePreset(this);
    }

    @Override
    public Set<IEPTreeNode> getEdgeToSemantics() {
        return EPTreeSemantics.getNodePostset(this);
    }

    @Override
    public Set<IEPTreeNode> getStartSemantics() {
        return EPTreeSemantics.getNodeOpeningSet(this);
    }

    @Override
    public Set<IEPTreeNode> getEndSemantics() {
        return EPTreeSemantics.getNodeClosingSet(this);
    }
    
    @Override
    public void setEdgeSemantics(Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        throw new NotImplementedException(
                "Process tree edge semantics are automatically derived "
              + "from the tree structure and semantics");
    }

    @Override
    public IEPTree getTree() {
        return tree;
    }

}
