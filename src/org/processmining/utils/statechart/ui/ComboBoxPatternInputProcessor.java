package org.processmining.utils.statechart.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.utils.statechart.signals.Signal1;

public class ComboBoxPatternInputProcessor implements ActionListener {

    private JComboBox<Pair<Pattern, String>> comboBox;
    private JLabel uiFeedbackLabel;
    private Signal1<Pattern> signalNewInput;

    public ComboBoxPatternInputProcessor(
            JComboBox<Pair<Pattern, String>> comboBox,
            JLabel uiFeedbackLabel,
            Signal1<Pattern> signalNewInput) {
        this.comboBox = comboBox;
        this.uiFeedbackLabel = uiFeedbackLabel;
        this.signalNewInput = signalNewInput;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object selectedItem = comboBox.getSelectedItem();
        if (selectedItem instanceof String) {
            // Handle custom string input
            String input = (String) selectedItem;
            try {
                Pattern pattern = Pattern.compile(input);
                setFeedbackError(uiFeedbackLabel, null);
                signalNewInput.dispatch(pattern);
            } catch (PatternSyntaxException ex) {
                setFeedbackError(uiFeedbackLabel,
                        "Pattern invalid: " + ex.getMessage());
            }
        } else if (selectedItem instanceof Pair<?, ?>) {
            // Handle Pair
            @SuppressWarnings("unchecked")
            Pair<Pattern, String> input = (Pair<Pattern, String>) selectedItem;
            signalNewInput.dispatch(input.getLeft());
        } else if (selectedItem instanceof Pattern) {
            // Handle Pattern (due to PairComboBoxEditor + select left in item)
            signalNewInput.dispatch((Pattern) selectedItem);
        } else {
            throw new IllegalStateException();
        }
    }

    protected void setFeedbackError(JLabel label, String message) {
        if (message != null) {
            label.setText(message);
        } else {
            label.setText("Ok");
        }
    }
}
