package org.processmining.utils.statechart.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.google.common.base.Predicate;

/**
 * 
 * @author Maikel Leemans
 * 
 * @param <T>
 * 
 *            Usage example: <code>
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("JTree");
    FilteredTreeModel model = new FilteredTreeModel(new DefaultTreeModel(root));
    JTree tree = new JTree(model);
    JScrollPane scrollPane = new JScrollPane(tree);
 *            </code>
 * 
 * @see http://www.adrianwalker.org/2012/04/filtered-jtree.html
 */
public class FilteredTreeModel<T extends TreeNode> implements TreeModel {

    private TreeModel treeModel;
    private Predicate<T> filter;
    private DefaultMutableTreeNode nodeFiltered;

    private TreeCellRenderer defaultRenderer;

    private class FilteredTreeCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 6518856113057379488L;

        @SuppressWarnings("unchecked")
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {

            if (value == nodeFiltered) {
                Component c = super.getTreeCellRendererComponent(tree, value,
                        sel, expanded, leaf, row, hasFocus);
                c.setForeground(Color.gray);
                c.setFont(c.getFont().deriveFont(Font.ITALIC));
                return c;
            }
            
            if (filter != null && checkMatch((T) value)) {
                Component c = super.getTreeCellRendererComponent(tree, value,
                        sel, expanded, leaf, row, hasFocus);
                c.setForeground(Color.red);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                return c;
            }

            if (defaultRenderer != null) {
                Component c = defaultRenderer.getTreeCellRendererComponent(tree,
                        value, sel, expanded, leaf, row, hasFocus);
                if (filter != null) {
                    c.setForeground(Color.gray);
                }
                return c;
            }
            return null;
        }

    }

    private final FilteredTreeCellRenderer cellRenderer = new FilteredTreeCellRenderer();

    public FilteredTreeModel(final TreeModel treeModel) {
        this.treeModel = treeModel;
        this.filter = null;

        nodeFiltered = new DefaultMutableTreeNode("(Filtered)", false);
    }

    public TreeModel getTreeModel() {
        return treeModel;
    }

    public void installRenderer(JTree tree) {
        installRenderer(tree, tree.getCellRenderer());
    }
    
    public void installRenderer(JTree tree, TreeCellRenderer defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
        tree.setCellRenderer(cellRenderer);
    }

    public void setFilter(final Predicate<T> filter) {
        this.filter = filter;
    }

    protected boolean checkMatch(final T node) {
        return ((filter == null) || filter.apply(node));
    }
    
    @SuppressWarnings("unchecked")
    private boolean recursiveMatch(final T node) {
        boolean matches = checkMatch(node);

        int childCount = treeModel.getChildCount(node);
        for (int i = 0; i < childCount; i++) {
            T child = (T) treeModel.getChild(node, i);
            matches |= recursiveMatch(child);
        }

        return matches;
    }

    @Override
    public Object getRoot() {
        return treeModel.getRoot();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getChild(final Object parent, final int index) {
        int count = 0;
        int childCount = treeModel.getChildCount(parent);
        for (int i = 0; i < childCount; i++) {
            T child = (T) treeModel.getChild(parent, i);
            if (recursiveMatch(child)) {
                if (count == index) {
                    return child;
                }
                count++;
            }
        }
        if (childCount > 0 && index == 0) {
            return nodeFiltered;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getChildCount(final Object parent) {
        int count = 0;
        int childCount = treeModel.getChildCount(parent);
        for (int i = 0; i < childCount; i++) {
            T child = (T) treeModel.getChild(parent, i);
            if (recursiveMatch(child)) {
                count++;
            }
        }
        if (count == 0 && childCount > 0) {
            count = 1;
        }
        return count;
    }

    @Override
    public boolean isLeaf(final Object node) {
        return treeModel.isLeaf(node);
    }

    @Override
    public void valueForPathChanged(final TreePath path, final Object newValue) {
        treeModel.valueForPathChanged(path, newValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getIndexOfChild(final Object parent, final Object childToFind) {
        int childCount = treeModel.getChildCount(parent);
        for (int i = 0; i < childCount; i++) {
            T child = (T) treeModel.getChild(parent, i);
            if (recursiveMatch(child)) {
                if (childToFind.equals(child)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void addTreeModelListener(final TreeModelListener l) {
        treeModel.addTreeModelListener(l);
    }

    @Override
    public void removeTreeModelListener(final TreeModelListener l) {
        treeModel.removeTreeModelListener(l);
    }
}