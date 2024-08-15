package org.processmining.models.statechart.eptree;

import java.util.List;

import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.DecoratorFactory;
import org.processmining.models.statechart.decorate.IDecoratorFactory;
import org.processmining.utils.statechart.tree.impl.AbstractTree;

import com.google.common.base.Preconditions;

public class EPTree extends AbstractTree<IEPTreeNode> implements IEPTree {

    private IEPTreeNode root;
    private final String name;

    private final Decorations<IEPTreeNode> decorations;

    public EPTree(String name) {
        this(name, DecoratorFactory.getDefaultInst());
    }
    
    public EPTree(String name, IDecoratorFactory decFact) {
        this(name, decFact.createEPTreeDecorations());
    }

    public EPTree(String name,
            Decorations<IEPTreeNode> decorations) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(decorations);
        this.name = name;
//        this.root = root;
        this.decorations = decorations;
    }

    @Override
    public IEPTreeNode getRoot() {
        return root;
    }

    @Override
    public void setRoot(IEPTreeNode newRoot) {
        root = newRoot;
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
        buf.append("' : ");
        buf.append(root.toString());
        return buf.toString();
    }

    @Override
    public IEPTree createCopy() {
        Decorations<IEPTreeNode> newDecorations = decorations
                .deepNewInstance();

        IEPTree tree = new EPTree(this.getName(), newDecorations);
        tree.setRoot(_createCopy(tree, getRoot(), null, newDecorations));

        return tree;
    }

    private IEPTreeNode _createCopy(IEPTree tree, IEPTreeNode from, IEPTreeNode parent,
            Decorations<IEPTreeNode> newDecorations) {
        IEPTreeNode to = new EPTreeNode(tree, parent, from.getNodeType(),
                from.getLabel(), from.getId());

        newDecorations.copyDecorations(to, from, decorations);

        final List<IEPTreeNode> children = from.getChildren();
        final int size = children.size();
        for (int i = 0; i < size; i++) {
            to.addNode(_createCopy(tree, children.get(i), to, newDecorations));
        }

        return to;
    }

    @Override
    public Decorations<IEPTreeNode> getDecorations() {
        return decorations;
    }

    @Override
    public IEPTreeNode getNodeByLabel(String label) {
        Preconditions.checkNotNull(label);
        
        for (IEPTreeNode node : iteratePreOrder()) {
            if (label.equals(node.getLabel())) {
                return node;
            }
        }
        return null;
    }

    @Override
    public IEPTreeNode getNodeByIndex(int... indices) {
        Preconditions.checkNotNull(indices);
        IEPTreeNode current = getRoot();
        
        for (int i = 0; i < indices.length; i++) {
            current = current.getChildren().get(indices[i]);
        }
        
        return current;
    }

    @Override
    public IEPTreeNode getNodeById(String id) {
        Preconditions.checkNotNull(id);
        
        for (IEPTreeNode node : iteratePreOrder()) {
            if (id.equals(node.getId())) {
                return node;
            }
        }
        return null;
    }
    ;
}
