package org.processmining.ui.statechart.workbench.log;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.ui.statechart.gfx.GfxFigs;
import org.processmining.utils.statechart.ui.ComboBoxUtil;
import org.processmining.utils.statechart.ui.SpringUtilities;

import com.google.common.base.Function;

public class LogClassifierView extends LogClassifierController.View {

    private Log2LogView log2log;
    
    private JComboBox<XEventClassifier> inputLabelClassifier;

    public LogClassifierView() {
        log2log = new Log2LogView();
        log2log.setHeaderInfo(
                "Log Transformation - Single Classifier",
                getDescriptionPanel());
        
        log2log.setButtonRunSignal(SignalStartAction);
        
        _addParameterControls(log2log.getParamControlsPanel());

    }
    
    public static JPanel getDescriptionPanel() {
        return Log2LogView.createDescriptionPanel(
                "Single Classifier", 
                "No hierarchy, just one classifier", 
                new String[] {
                    "Flat event logs",
                    "Logs with cancelation patterns"
                }, GfxFigs.FigLog2Log_Classifier.getImageLabel(""));
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
        
        SpringUtilities.makeCompactGrid(wrap, 1, 2, 6, 6, 6, 6);
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

}
