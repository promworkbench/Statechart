package org.processmining.ui.statechart.workbench.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.Border;

import org.apache.commons.lang3.tuple.Pair;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class UiFactory {

    public static JLabel createTitleLabel(String title) {
        final JLabel label = new JLabel("<html><h2>" + title + "</h2></html>");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static JLabel createLabelTopLeft(String content) {
        JLabel label = new JLabel(content);
        label.setHorizontalAlignment(JLabel.LEFT);
        label.setVerticalAlignment(JLabel.TOP);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    public static JLabel createLabelWrapped(String content) {
        return new JLabel("<html>" + content + "</html>");
    }

    public static Border createPaddingBorder() {
        return createPaddingBorder(3);
    }

    public static Border createPaddingBorder(int padding) {
        return BorderFactory.createEmptyBorder(padding, padding, padding,
                padding);
    }

    public static Component createPaddedSeperator() {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BorderLayout());
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        wrap.add(sep, BorderLayout.CENTER);
        ;
        wrap.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return wrap;
    }

    public static Component leftJustify(Component content) {
        Box b = Box.createHorizontalBox();
        b.add(content);
        b.add(Box.createHorizontalGlue());
        return b;
    }

    public static Component centerJustify(Component content) {
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(content);
        b.add(Box.createHorizontalGlue());
        return b;
    }
    
    public static void forceSize(Component component, int width, int height) {
        forceSize(component, new Dimension(width, height));
    }

    public static void forceSize(Component component, Dimension dim) {
        component.setMinimumSize(dim);
        component.setMaximumSize(dim);
        component.setPreferredSize(dim);
    }

    public static void forceMinSize(Component component, int width, int height) {
        forceMinSize(component, new Dimension(width, height));
    }
    
    public static void forceMinSize(Component component, Dimension dim) {
        component.setMinimumSize(dim);
        component.setPreferredSize(dim);
    }

    public static <T> Component scrollVertical(JList<T> list, int visibleRowCount) {
//        JPanel panel = new JPanel();
        JScrollPane listScrollPane = new JScrollPane(list);
        
        list.setVisibleRowCount(visibleRowCount);
        return listScrollPane;
//        listScrollPane.setViewportView(list);
        
//        panel.setLayout(new BorderLayout());
//        panel.add(listScrollPane);
//        return panel;
    }

    @SafeVarargs
    public static JPanel createKeyValWrap(Pair<String, Component>... content) {
        SlickerFactory f = SlickerFactory.instance();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;
        c.insets = new Insets(1, 1, 1, 2);
        
        for (Pair<String, Component> keyval : content) {
            if (keyval.getKey() == null) {
                c.gridwidth = 2;
                c.gridx = 0;
                c.weightx = 1;
                panel.add(keyval.getValue(), c);
            } else {
                c.gridwidth = 1;
                c.gridx = 0;
                c.weightx = 0;
                panel.add(f.createLabel(keyval.getKey()), c);
                c.gridx = 1;
                c.weightx = 1;
                panel.add(keyval.getValue(), c);
            }
            c.gridy++;
        }

        return panel;
    }

    @SafeVarargs
    public static JPanel createLeftRightWrap(Pair<Component, Component>... content) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;
        c.insets = new Insets(1, 1, 1, 2);
        
        for (Pair<Component, Component> keyval : content) {
            if (keyval.getKey() == null) {
                c.gridwidth = 2;
                c.gridx = 0;
                c.weightx = 1;
                panel.add(keyval.getValue(), c);
            } else {
                c.gridwidth = 1;
                c.gridx = 0;
                c.weightx = 0;
                panel.add(keyval.getKey(), c);
                c.gridx = 1;
                c.weightx = 1;
                panel.add(keyval.getValue(), c);
            }
            c.gridy++;
        }

        return panel;
    }
    
}
