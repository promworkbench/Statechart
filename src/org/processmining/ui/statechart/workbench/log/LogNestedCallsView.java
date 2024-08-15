package org.processmining.ui.statechart.workbench.log;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JCheckBox;
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
import com.jidesoft.swing.AutoCompletionComboBox;

public class LogNestedCallsView extends LogNestedCallsController.View {

    private static final int PairRightWidth = 300;
    
    private Log2LogView log2log;
    
    private JComboBox<XEventClassifier> inputLabelClassifier;
    private JComboBox<XEventClassifier> inputStartEndClassifier;
    private AutoCompletionComboBox inputStartSymbol;
    private AutoCompletionComboBox inputEndSymbol;

    private JCheckBox inputUseSplit;

    private JComboBox<Pair<Pattern, String>> inputSplitPattern;

    private JLabel feedbackSplitPattern;

    private JCheckBox inputUseHandle;

    private AutoCompletionComboBox inputHandleSymbol;

    public LogNestedCallsView() {
        log2log = new Log2LogView();
        log2log.setHeaderInfo(
                "Log Transformation - Nested Calls",
                getDescriptionPanel());
        
        log2log.setButtonRunSignal(SignalStartAction);
        
        _addParameterControls(log2log.getParamControlsPanel());

    }
    
    public static JPanel getDescriptionPanel() {
        return Log2LogView.createDescriptionPanel(
                "Nested Calls", 
                "Contained lifecycle intervals", 
                new String[] {
                    "Callstack behavior in software logs",
                    "Subprocesses in hierarchical workflow processes"
                }, GfxFigs.FigLog2Log_NestedCalls.getImageLabel(""));
    }

    private void _addParameterControls(JPanel container) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new SpringLayout());
        container.add(wrap);

        wrap.add(new JLabel("Label classifier: "));
        inputLabelClassifier = new JComboBox<XEventClassifier>();
        //SlickerDecorator.instance().decorate(inputLabelClassifier);
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

        wrap.add(new JLabel("Start-End classifier: "));
        inputStartEndClassifier = new JComboBox<XEventClassifier>();
        //SlickerDecorator.instance().decorate(inputStartEndClassifier);
        wrap.add(inputStartEndClassifier);
        inputStartEndClassifier.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = inputStartEndClassifier.getSelectedIndex();
                XEventClassifier item = inputStartEndClassifier.getItemAt(index);
                SignalInputStartEndClassifier.dispatch(item);
            }
        });
        wrap.add(Box.createHorizontalGlue());
        wrap.add(Box.createHorizontalGlue());

        wrap.add(new JLabel("Start symbol: "));
        inputStartSymbol = new AutoCompletionComboBox();
        inputStartSymbol.setStrict(false);
        wrap.add(inputStartSymbol);
        inputStartSymbol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SignalInputStartSymbol.dispatch((String)inputStartSymbol.getSelectedItem());
            }
        });
        wrap.add(Box.createHorizontalGlue());
        wrap.add(Box.createHorizontalGlue());

        wrap.add(new JLabel("End symbol: "));
        inputEndSymbol = new AutoCompletionComboBox();
        inputEndSymbol.setStrict(false);
        wrap.add(inputEndSymbol);
        inputEndSymbol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SignalInputEndSymbol.dispatch((String)inputEndSymbol.getSelectedItem());
            }
        });
        wrap.add(Box.createHorizontalGlue());
        wrap.add(Box.createHorizontalGlue());

        inputUseHandle = new JCheckBox("Handle error symbol:");
        inputUseHandle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SignalInputUseHandle.dispatch(inputUseHandle.isSelected());
            }
        });
        wrap.add(inputUseHandle);
        inputHandleSymbol = new AutoCompletionComboBox();
        inputHandleSymbol.setStrict(false);
        wrap.add(inputHandleSymbol);
        inputHandleSymbol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SignalInputHandleSymbol.dispatch((String)inputHandleSymbol.getSelectedItem());
            }
        });
        wrap.add(Box.createHorizontalGlue());
        wrap.add(Box.createHorizontalGlue());
        
        inputUseSplit = new JCheckBox("Split traces on call:");
        inputUseSplit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SignalInputUseSplit.dispatch(inputUseSplit.isSelected());
            }
        });
        wrap.add(inputUseSplit);
        inputSplitPattern = new JComboBox<Pair<Pattern, String>>();
        inputSplitPattern.setFont(new Font("Courier New", Font.BOLD, 12));
        inputSplitPattern
                .setRenderer(new PairListCellRenderer<Pattern, String>(inputSplitPattern, PairRightWidth));
        inputSplitPattern.setEditable(true);
        inputSplitPattern.setEditor(new PairComboBoxEditor());
        wrap.add(inputSplitPattern);
        feedbackSplitPattern = new JLabel();
        wrap.add(feedbackSplitPattern);
        inputSplitPattern.addActionListener(new ComboBoxPatternInputProcessor(
                inputSplitPattern, feedbackSplitPattern, SignalInputSplitPattern));
        wrap.add(Box.createHorizontalGlue());
        
        final int rows = 6;
        final int cols = 4;
        SpringUtilities.makeCompactGrid(wrap, rows, cols, 6, 6, 6, 6);
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
    public void setStartEndClassifierOptions(List<XEventClassifier> options,
            XEventClassifier defaultValue) {
        ComboBoxUtil.updateModel(inputStartEndClassifier, options, defaultValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setStartSymbolOptions(List<String> options, String defaultValue) {
        inputStartSymbol.removeAllItems();
        for (String opt : options) {
            inputStartSymbol.addItem((Object) opt);
        }
        if (defaultValue != null) {
            inputStartSymbol.setSelectedItem(defaultValue);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setEndSymbolOptions(List<String> options, String defaultValue) {
        inputEndSymbol.removeAllItems();
        for (String opt : options) {
            inputEndSymbol.addItem((Object) opt);
        }
        if (defaultValue != null) {
            inputEndSymbol.setSelectedItem(defaultValue);
        }
    }

    @Override
    public void setUseSplit(boolean use) {
        inputUseSplit.setSelected(use);
    }

    @Override
    public void setSplitPatternOptions(List<Pair<Pattern, String>> options,
            Pair<Pattern, String> defaultValue) {
        ComboBoxUtil.updateModel(inputSplitPattern, options, defaultValue);
    }

    @Override
    public void setUseHandle(boolean use) {
        inputUseHandle.setSelected(use);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setHandleSymbolOptions(List<String> options, String defaultValue) {
        inputHandleSymbol.removeAllItems();
        for (String opt : options) {
            inputHandleSymbol.addItem((Object) opt);
        }
        if (defaultValue != null) {
            inputHandleSymbol.setSelectedItem(defaultValue);
        }
    }

}
