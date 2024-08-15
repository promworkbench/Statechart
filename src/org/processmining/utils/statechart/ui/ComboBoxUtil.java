package org.processmining.utils.statechart.ui;

import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class ComboBoxUtil {

    @SuppressWarnings("unchecked")
    public static <T> void updateModel(JComboBox<T> comboBox, 
            List<T> options, T defaultValue) {
        DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>(
                (T[]) options.toArray());
        
        if (defaultValue != null) {
            model.setSelectedItem(defaultValue);
        }
        
        comboBox.setModel(model);
    }
}
