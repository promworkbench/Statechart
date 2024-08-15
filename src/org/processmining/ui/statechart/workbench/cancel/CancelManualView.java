package org.processmining.ui.statechart.workbench.cancel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.processmining.ui.statechart.gfx.GfxFigs;
import org.processmining.ui.statechart.workbench.common.TwoUpListSelectionUi;
import org.processmining.utils.statechart.generic.CompareComparator;
import org.processmining.utils.statechart.generic.IdentityFunction;

public class CancelManualView extends CancelManualController.View {

    private static final int ErrorListVisibleRows = 15;
    private static final Dimension PreferredListDim = new Dimension(
        80, 250
    );
    
    private CancelBaseView baseView;
    
    private TwoUpListSelectionUi<String> inputSelection;
    
    public CancelManualView() {
        baseView = new CancelBaseView();
        baseView.setHeaderInfo(
            "Cancellation Preprocessing -- Manually selected error oracle",
            getDescriptionPanel());
        
        baseView.setButtonRunSignal(SignalStartAction);

        _addParameterControls(baseView.getParamControlsPanel());
    }

    public static JPanel getDescriptionPanel() {
        return CancelBaseView.createDescriptionPanel(
                "Manually selected error oracle", 
                "Handpicked set of activites", 
                new String[] {
                    "Specific domain knowledge"
                }, GfxFigs.FigCancel_Manual.getImageLabel(""));
    }
    
    private void _addParameterControls(JPanel container) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BorderLayout());
        container.add(wrap);
        
        inputSelection = new TwoUpListSelectionUi<>(
            "Cancel triggers: ", false, ErrorListVisibleRows, PreferredListDim);
        wrap.add(inputSelection.getComponent(), BorderLayout.CENTER);
        
        inputSelection.SignalInputSelected.connect(SignalInputErrors);
    }

    @Override
    public JComponent getComponent() {
        return baseView.getRoot();
    }

    @Override
    public void setLogUi(XLog inputLog, Set<String> cancelOracle) {
        baseView.setLogUi(inputLog, cancelOracle);
    }

    @Override
    public void setLogUi(Set<String> cancelOracle) {
        baseView.setLogUi(cancelOracle);
    }

    @Override
    public void setInputOptions(Collection<String> values) {
        inputSelection.setOptions(values, new IdentityFunction<String>(), new CompareComparator<String>());
    }

    @Override
    public void setInputSelected(Collection<String> selected) {
        inputSelection.setSelected(selected, new IdentityFunction<String>());
    }

}
