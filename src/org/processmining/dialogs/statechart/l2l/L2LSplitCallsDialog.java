package org.processmining.dialogs.statechart.l2l;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventClassifier;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.processmining.algorithms.statechart.l2l.L2LSplitCalls;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Signal1;
import org.processmining.utils.statechart.ui.ComboBoxPatternInputProcessor;
import org.processmining.utils.statechart.ui.ComboBoxUtil;
import org.processmining.utils.statechart.ui.PairComboBoxEditor;
import org.processmining.utils.statechart.ui.PairListCellRenderer;

public class L2LSplitCallsDialog extends JPanel {

    private static final long serialVersionUID = 5556671295064689248L;

    private static final int PairRightWidth = 300;
    
    private final ProMPropertiesPanel panel;
    private final L2LSplitCalls.Parameters params;

    public L2LSplitCallsDialog(
            List<XEventClassifier> clsLabelOptions, XEventClassifier clsLabelDefaultValue,
            List<XEventClassifier> clsSROptions, XEventClassifier clsSRDefaultValue,
            List<String> startSymbolOptions, String startSymbolDefaultValue,
            List<String> endSymbolOptions, String endSymbolDefaultValue,
            List<Pair<Pattern, String>> inputSplitOptions, Pair<Pattern, String> inputSplitDefaultValue
        ) {
        params = new L2LSplitCalls.Parameters();
        
        setBackground(WidgetColors.PROPERTIES_BACKGROUND);
        setLayout(new BorderLayout());
        
        panel = new ProMPropertiesPanel(null);
        panel.setPreferredSize(null);
        panel.setMaximumSize(null);
        panel.setMinimumSize(null);
        panel.setBackground(WidgetColors.PROPERTIES_BACKGROUND);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        add(panel, BorderLayout.CENTER);

        // Label classifier
        {
            final JComboBox<XEventClassifier> inputLabelClassifier = new JComboBox<XEventClassifier>();
            ComboBoxUtil.updateModel(inputLabelClassifier, clsLabelOptions, clsLabelDefaultValue);
            inputLabelClassifier.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = inputLabelClassifier.getSelectedIndex();
                    XEventClassifier item = inputLabelClassifier.getItemAt(index);
                    params.clsLabel = item;
                }
            });
            panel.addProperty("Label classifier: " , inputLabelClassifier);
        }

        // Start-End classifier classifier
        {
            final JComboBox<XEventClassifier> inputStartEndClassifier = new JComboBox<XEventClassifier>();
            ComboBoxUtil.updateModel(inputStartEndClassifier, clsSROptions, clsSRDefaultValue);
            inputStartEndClassifier.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = inputStartEndClassifier.getSelectedIndex();
                    XEventClassifier item = inputStartEndClassifier.getItemAt(index);
                    params.clsSR = item;
                }
            });
            panel.addProperty("Start-End classifier classifier: " , inputStartEndClassifier);
        }

        // Start symbol
        {
            final JTextField inputStartSymbol = new JTextField();
            if (startSymbolDefaultValue != null) {
                inputStartSymbol.setText(startSymbolDefaultValue);
            }
            AutoCompleteDecorator.decorate(inputStartSymbol, startSymbolOptions, false);
            inputStartSymbol.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    params.startSymbol = inputStartSymbol.getText();
                }
            });
            panel.addProperty("Start symbol: " , inputStartSymbol);
        }

        // End symbol
        {
            final JTextField inputEndSymbol = new JTextField();
            if (endSymbolDefaultValue != null) {
                inputEndSymbol.setText(endSymbolDefaultValue);
            }
            AutoCompleteDecorator.decorate(inputEndSymbol, endSymbolOptions, false);
            inputEndSymbol.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    params.returnSymbol = inputEndSymbol.getText();
                }
            });
            panel.addProperty("End symbol: " , inputEndSymbol);
        }

        // Split traces on call
        {
            JLabel feedbackSplitPattern = new JLabel();
            Signal1<Pattern> signalUdpate = new Signal1<>();
            signalUdpate.register(new Action1<Pattern>() {
                @Override
                public void call(Pattern t) {
                    params.reTraceBaseName = t;
                }
            });
            
            final JComboBox<Pair<Pattern, String>> inputSplitPattern = new JComboBox<Pair<Pattern, String>>();
            inputSplitPattern.setFont(new Font("Courier New", Font.BOLD, 12));
            inputSplitPattern.setRenderer(new PairListCellRenderer<Pattern, String>(inputSplitPattern, PairRightWidth));
            inputSplitPattern.setMinimumSize(new Dimension(PairRightWidth + 100, inputSplitPattern.getHeight()));
            inputSplitPattern.setPreferredSize(new Dimension(PairRightWidth + 100, inputSplitPattern.getHeight()));
            inputSplitPattern.setEditable(true);
            inputSplitPattern.setEditor(new PairComboBoxEditor());
            ComboBoxUtil.updateModel(inputSplitPattern, inputSplitOptions, inputSplitDefaultValue);
            inputSplitPattern.addActionListener(new ComboBoxPatternInputProcessor(
                    inputSplitPattern, feedbackSplitPattern, signalUdpate));
            panel.addProperty("Split traces on call: " , inputSplitPattern);
            panel.addProperty("", feedbackSplitPattern);
        }
    }
    
    public L2LSplitCalls.Parameters getParameters() {
        return params;
    }
}
