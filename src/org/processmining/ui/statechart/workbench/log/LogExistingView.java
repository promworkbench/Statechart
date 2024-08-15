package org.processmining.ui.statechart.workbench.log;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.ui.statechart.gfx.GfxFigs;

import com.google.common.base.Function;

public class LogExistingView extends LogExistingController.View {

    private Log2LogView log2log;

    public LogExistingView() {
        log2log = new Log2LogView();
        log2log.setHeaderInfo(
                "Log Transformation - Existing List Labels",
                getDescriptionPanel());
        
        log2log.setButtonRunSignal(SignalStartAction);
    }

    public static JPanel getDescriptionPanel() {
        return Log2LogView.createDescriptionPanel(
                "Existing List Labels", 
                "Use existing hierarchy", 
                new String[] {
                    "Import hierarchical log as-is"
                }, GfxFigs.FigLog2Log_ExistingListLabel.getImageLabel(""));
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

}
