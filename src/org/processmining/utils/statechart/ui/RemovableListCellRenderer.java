package org.processmining.utils.statechart.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;

/**
 * Renders List Items with an X button to remove an item from the list 
 * @author mleemans
 *
 * @param <T>
 */
public class RemovableListCellRenderer<T> extends JPanel implements
        ListCellRenderer<T> {

    private static final long serialVersionUID = 8816947756436567555L;

    private static final int BtnRemoveWidth = 30;
    private static final int BtnRemoveHeight = 15;
    

    private final JLabel label = new JLabel();
    private final JButton btnRemove = new JButton("X");

    private MouseListener clickAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent event) {
            clickButtonAt(event.getComponent(), event.getPoint());
        }
    };

    public RemovableListCellRenderer() {
        super(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

        btnRemove.setMargin(new Insets(0, 0, 0, 0)); // avoid "..."
        btnRemove.setPreferredSize(new Dimension(BtnRemoveWidth, BtnRemoveHeight));
        
        this.add(label);
        this.add(btnRemove, BorderLayout.EAST);
    }

    protected void clickButtonAt(Component component, Point point) {
        int xOffset = 0;
        
        Container parent = component.getParent();
        if (parent instanceof JScrollPane) {
            parent = ((JScrollPane) parent).getViewport();
        }
        if (parent instanceof JViewport) {
            xOffset = ((JViewport) parent).getViewPosition().x;
        }
        
        int pointX = (int)point.getX() - xOffset;
        
        @SuppressWarnings("unchecked")
        JList<T> jlist = (JList<T>) component;
        int minX = jlist.getX() + jlist.getWidth() - btnRemove.getWidth();
        int maxX = jlist.getX() + jlist.getWidth();
        if (minX <= pointX && pointX <= maxX) {
            int index = jlist.locationToIndex(point);
            ((DefaultListModel<T>) jlist.getModel()).remove(index);
        }
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends T> list,
            T value, int index, boolean isSelected, boolean cellHasFocus) {
        // Note: JList will use this component as a "rubber stamps" to paint
        // the cells. Hence, any component listeners will not work, and we have 
        // to work with a click listener on the whole list. 
        list.removeMouseListener(clickAdapter);
        list.addMouseListener(clickAdapter);

        label.setText(value.toString());

        label.setFont(list.getFont());

        if (index < 0) {
            label.setForeground(list.getForeground());
            this.setOpaque(false);
        } else {
            label.setForeground(isSelected ? list.getSelectionForeground()
                    : list.getForeground());
            this.setBackground(isSelected ? list.getSelectionBackground()
                    : list.getBackground());
            this.setOpaque(true);
        }
        return this;
    }
}
