package org.processmining.utils.statechart.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import com.google.common.base.Predicate;

public class FilteredListModel<T, M extends ListModel<T>> implements ListModel<T> {

    public static final String CaptionNodeFiltered = "(Filtered)";
    
    private M model;
    private Predicate<T> filter;
    private T nodeFiltered;

    private ListCellRenderer<? super T> defaultRenderer;

    private class FilteredListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 3534983188008895442L;

        @SuppressWarnings("unchecked")
        @Override
        public Component getListCellRendererComponent(JList<?> list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            if (value == nodeFiltered) {
                Component c = super.getListCellRendererComponent(list, value,
                        index, isSelected, cellHasFocus);
                c.setForeground(Color.gray);
                c.setFont(c.getFont().deriveFont(Font.ITALIC));
                return c;
            }
            
            if (filter != null && checkMatch((T) value)) {
                Component c = super.getListCellRendererComponent(list, value,
                        index, isSelected, cellHasFocus);
                c.setForeground(Color.red);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                return c;
            }

            if (defaultRenderer != null) {
                Component c = super.getListCellRendererComponent(list, value,
                        index, isSelected, cellHasFocus);
                if (filter != null) {
                    c.setForeground(Color.gray);
                }
                return c;
            }
            return null;
        }

    }

    private final FilteredListCellRenderer cellRenderer = new FilteredListCellRenderer();

    private Method fireContentsChanged;

    public FilteredListModel(M model, T nodeFiltered) {
        this.model = model;
        this.filter = null;
        
        this.nodeFiltered = nodeFiltered;
        
        if (model instanceof AbstractListModel<?>) {
            for (Method method : AbstractListModel.class.getDeclaredMethods()) {
                if (method.getName().equals("fireContentsChanged")) {
                    method.setAccessible(true);
                    fireContentsChanged = method;
                }
            }
        }
    }

    public M getModel() {
        return model;
    }

    public void installRenderer(JList<T> list) {
        installRenderer(list, list.getCellRenderer());
    }
    
    public void installRenderer(JList<T> list, ListCellRenderer<? super T> defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
        list.setCellRenderer(cellRenderer);
    }

    public void setFilter(final Predicate<T> filter) {
        this.filter = filter;
    }

    protected boolean checkMatch(final T node) {
        return ((filter == null) || filter.apply(node));
    }

    @Override
    public int getSize() {
        int count = 0;
        int childCount = model.getSize();
        for (int i = 0; i < childCount; i++) {
            T child = model.getElementAt(i);
            if (checkMatch(child)) {
                count++;
            }
        }
        if (count == 0 && childCount > 0) {
            count = 1;
        }
        return count;
    }

    @Override
    public T getElementAt(int index) {
        int count = 0;
        int childCount = model.getSize();
        for (int i = 0; i < childCount; i++) {
            T child = model.getElementAt(i);
            if (checkMatch(child)) {
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

    @Override
    public void addListDataListener(ListDataListener l) {
        model.addListDataListener(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        model.removeListDataListener(l);
    }

    public void reload() {
        if (model instanceof AbstractListModel<?> && fireContentsChanged != null) {
            try {
                fireContentsChanged.invoke(model, model, 0, model.getSize());
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
