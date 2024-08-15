package org.processmining.ui.statechart.workbench.log;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.ui.statechart.gfx.GfxFigs;
import org.processmining.utils.statechart.ui.ComboBoxPatternInputProcessor;
import org.processmining.utils.statechart.ui.ComboBoxUtil;
import org.processmining.utils.statechart.ui.PairComboBoxEditor;
import org.processmining.utils.statechart.ui.PairListCellRenderer;
import org.processmining.utils.statechart.ui.SpringUtilities;

import com.google.common.base.Function;

public class LogPatternNamesView extends LogPatternNamesController.View {

    private static final int PairRightWidth = 300;

    private Log2LogView log2log;
    
    private JComboBox<XEventClassifier> inputLabelClassifier;
    private JLabel feedbackSelectPattern;
    private JComboBox<Pair<Pattern, String>> inputPartPattern;


    public LogPatternNamesView() {
        log2log = new Log2LogView();
        log2log.setHeaderInfo(
                "Log Transformation - Pattern Names",
                getDescriptionPanel());
        
        log2log.setButtonRunSignal(SignalStartAction);
        
        _addParameterControls(log2log.getParamControlsPanel());
    }

    public static JPanel getDescriptionPanel() {
        return Log2LogView.createDescriptionPanel(
                "Pattern Names", 
                "Fixed pattern in activity names", 
                new String[] {
                    "Prefix_postfix activity names",
                    "Activity code patterns (e.g., 'aabbccc', 'NL40act')"
                }, GfxFigs.FigLog2Log_PatternNames.getImageLabel(""));
    }

    private void _addParameterControls(JPanel container) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new SpringLayout());
        container.add(wrap);

        wrap.add(new JLabel("Label classifier: "));
        inputLabelClassifier = new JComboBox<XEventClassifier>();
        // SlickerDecorator.instance().decorate(inputLabelClassifier);
        wrap.add(inputLabelClassifier);
        inputLabelClassifier.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = inputLabelClassifier.getSelectedIndex();
                XEventClassifier item = inputLabelClassifier.getItemAt(index);
                SignalInputLabelClassifier.dispatch(item);
            }
        });
        wrap.add(Box.createHorizontalGlue());
        wrap.add(Box.createHorizontalGlue());

        wrap.add(new JLabel("Part capture pattern: "));
        inputPartPattern = new JComboBox<Pair<Pattern, String>>();
        // SlickerDecorator.instance().decorate(inputSelectPattern);
        inputPartPattern.setFont(new Font("Courier New", Font.BOLD, 12));
        inputPartPattern
                .setRenderer(new PairListCellRenderer<Pattern, String>(inputPartPattern, PairRightWidth));
        inputPartPattern.setEditable(true);
        inputPartPattern.setEditor(new PairComboBoxEditor());
        wrap.add(inputPartPattern);
        feedbackSelectPattern = new JLabel();
        wrap.add(feedbackSelectPattern);
        inputPartPattern.addActionListener(new ComboBoxPatternInputProcessor(
                inputPartPattern, feedbackSelectPattern, SignalInputPartsPattern));
        wrap.add(Box.createHorizontalGlue());

        SpringUtilities.makeCompactGrid(wrap, 2, 4, 6, 6, 6, 6);
    }

    @Override
    public JComponent getComponent() {
        return log2log.getRoot();
    }

    @Override
    public void setLogUi(XLog inputLog, Function<XEvent, String> labelNormal, Function<XTrace, XTrace> transform) {
        log2log.setLogUi(inputLog, labelNormal, transform);
    }

    @Override
    public void setLogUi(Function<XEvent, String> labelNormal, Function<XTrace, XTrace> transform) {
        log2log.setLogUi(labelNormal, transform);
    }
    
    @Override
    public void setLabelClassifierOptions(List<XEventClassifier> options,
            XEventClassifier defaultValue) {
        ComboBoxUtil.updateModel(inputLabelClassifier, options, defaultValue);
    }

    @Override
    public void setPartsPatternOptions(List<Pair<Pattern, String>> options,
            Pair<Pattern, String> defaultValue) {
        ComboBoxUtil.updateModel(inputPartPattern, options, defaultValue);
    }
}
