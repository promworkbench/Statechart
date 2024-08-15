package org.processmining.utils.statechart.ui;

import java.awt.BorderLayout;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DocumentFilter;
import javax.swing.text.NavigationFilter;
import javax.swing.text.NumberFormatter;
import javax.swing.text.Position.Bias;
import javax.swing.text.SimpleAttributeSet;

/**
 * 
 * @author mleemans
 *
 * @see https://stackoverflow.com/questions/7569929/jformattedtextfield-to-format-percent-numbers
 */
public class PercentInputField extends JComponent {

    private static final double MIN_VALUE = 0.0d;
    private static final double MAX_VALUE = 1.0d;
    private static final double STEP_SIZE = 0.01d;

    private static final long serialVersionUID = -779235114254706347L;

    private JSpinner spinner;

    public PercentInputField() {
        initComponents();
        initLayout();
        spinner.setValue(MIN_VALUE);
    }

    private void initComponents() {
        SpinnerNumberModel model = new SpinnerNumberModel(MIN_VALUE, MIN_VALUE, MAX_VALUE, STEP_SIZE);
        spinner = new JSpinner(model);
        initSpinnerTextField();
    }

    private void initSpinnerTextField() {
        DocumentFilter digitOnlyFilter = new PercentDocumentFilter(getMaximumDigits());
        NavigationFilter navigationFilter = new BlockLastCharacterNavigationFilter(getTextField());
        getTextField().setFormatterFactory(
                new DefaultFormatterFactory(new PercentNumberFormatter(createPercentFormat(), navigationFilter,
                        digitOnlyFilter)));
        getTextField().setColumns(6);
    }

    private int getMaximumDigits() {
        return Integer.toString((int) MAX_VALUE * 100).length();
    }

    public JFormattedTextField getTextField() {
        JSpinner.NumberEditor jsEditor = (JSpinner.NumberEditor) spinner.getEditor();
        JFormattedTextField textField = jsEditor.getTextField();
        return textField;
    }

    private NumberFormat createPercentFormat() {
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setGroupingUsed(false);
        format.setMaximumIntegerDigits(getMaximumDigits());
        format.setMaximumFractionDigits(0);
        return format;
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        add(spinner, BorderLayout.CENTER);
    }
    
    public JSpinner getSpinner() {
        return spinner;
    }
    
    public double getPercent() {
        return (Double) spinner.getValue();
    }

    public void setPercent(double percent) {
        spinner.setValue(percent);
    }

    private static class PercentNumberFormatter extends NumberFormatter {

        private static final long serialVersionUID = -1172071312046039349L;

        private final NavigationFilter navigationFilter;
        private final DocumentFilter digitOnlyFilter;

        private PercentNumberFormatter(NumberFormat format, NavigationFilter navigationFilter,
                DocumentFilter digitOnlyFilter) {
            super(format);
            this.navigationFilter = navigationFilter;
            this.digitOnlyFilter = digitOnlyFilter;
        }

        @Override
        protected NavigationFilter getNavigationFilter() {
            return navigationFilter;
        }

        @Override
        protected DocumentFilter getDocumentFilter() {
            return digitOnlyFilter;
        }

        @Override
        public Class<?> getValueClass() {
            return Double.class;
        }

        @Override
        public Object stringToValue(String text) throws ParseException {
            Double value = (Double) super.stringToValue(text);
            return Math.max(MIN_VALUE, Math.min(MAX_VALUE, value));
        }
    }

    /**
     * NavigationFilter that avoids navigating beyond the percent sign.
     */
    private static class BlockLastCharacterNavigationFilter extends NavigationFilter {

        private JFormattedTextField textField;

        private BlockLastCharacterNavigationFilter(JFormattedTextField textField) {
            this.textField = textField;
        }

        @Override
        public void setDot(FilterBypass fb, int dot, Bias bias) {
            super.setDot(fb, correctDot(fb, dot), bias);
        }

        @Override
        public void moveDot(FilterBypass fb, int dot, Bias bias) {
            super.moveDot(fb, correctDot(fb, dot), bias);
        }

        private int correctDot(FilterBypass fb, int dot) {
            // Avoid selecting the percent sign
            int lastDot = Math.max(0, textField.getText().length() - 1);
            return dot > lastDot ? lastDot : dot;
        }
    }

    private static class PercentDocumentFilter extends DocumentFilter {

        private int maxiumDigits;

        public PercentDocumentFilter(int maxiumDigits) {
            super();
            this.maxiumDigits = maxiumDigits;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs)
                throws BadLocationException {
            // Mapping an insert as a replace without removing
            replace(fb, offset, 0, text, attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            // Mapping a remove as a replace without inserting
            replace(fb, offset, length, "", SimpleAttributeSet.EMPTY);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            int replaceLength = correctReplaceLength(fb, offset, length);
            String cleanInput = truncateInputString(fb, filterDigits(text), replaceLength);
            super.replace(fb, offset, replaceLength, cleanInput, attrs);
        }

        /**
         * Removes all non-digit characters
         */
        private String filterDigits(String text) throws BadLocationException {
            StringBuilder sb = new StringBuilder(text);
            for (int i = 0, n = sb.length(); i < n; i++) {
                if (!Character.isDigit(text.charAt(i))) {
                    sb.deleteCharAt(i);
                }
            }
            return sb.toString();
        }

        /**
         * Removes all characters with which the resulting text would exceed the maximum number of digits
         */
        private String truncateInputString(FilterBypass fb, String filterDigits, int replaceLength) {
            StringBuilder sb = new StringBuilder(filterDigits);
            int currentTextLength = fb.getDocument().getLength() - replaceLength - 1;
            for (int i = 0; i < sb.length() && currentTextLength + sb.length() > maxiumDigits; i++) {
                sb.deleteCharAt(i);
            }
            return sb.toString();
        }

        private int correctReplaceLength(FilterBypass fb, int offset, int length) {
            if (offset + length >= fb.getDocument().getLength()) {
                // Don't delete the percent sign
                return offset + length - fb.getDocument().getLength();
            }
            return length;
        }
    }

}
