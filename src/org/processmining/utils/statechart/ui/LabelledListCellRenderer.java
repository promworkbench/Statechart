package org.processmining.utils.statechart.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import com.google.common.base.Function;

public class LabelledListCellRenderer<T> extends DefaultListCellRenderer {
    private static final long serialVersionUID = -1432993062514103098L;

    private Function<T, String> labellingFnc;

    public LabelledListCellRenderer(Function<T, String> labellingFnc) {
        super();
        this.labellingFnc = labellingFnc;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        // DefaultListCellRenderer always returns a JLabel, super setups up all
        // the defaults
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                index, isSelected, cellHasFocus);

        label.setText(labellingFnc.apply((T) value));

        return label;
    }

}
