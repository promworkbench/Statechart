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

public class CancelQualityView extends CancelQualityController.View {

    private static final int ErrorListVisibleRows = 15;
    private static final Dimension PreferredListDim = new Dimension(
        80, 250
    );
    
    private CancelBaseView baseView;
    
    private TwoUpListSelectionUi<String> inputSelection;
    
    public CancelQualityView() {
        baseView = new CancelBaseView();
        baseView.setHeaderInfo(
            "Cancellation Preprocessing -- Model Quality Optimization",
            getDescriptionPanel());
        
        baseView.setButtonRunSignal(SignalStartAction);

        _addParameterControls(baseView.getParamControlsPanel());
    }

    public static JPanel getDescriptionPanel() {
        return CancelBaseView.createDescriptionPanel(
                "Model Quality Optimization", 
                "Find the best error oracle, improving fitness & precision", 
                new String[] {
                    "Large set of activities & little domain knowledge",
                    "Less structured & possibly cancelling behavior"
                }, GfxFigs.FigCancel_Quality.getImageLabel(""));
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
    public void setInputOptions(Collection<String> values, Collection<String> defaultVals) {
        inputSelection.setOptions(values, new IdentityFunction<String>(), new CompareComparator<String>());
        inputSelection.setSelected(defaultVals, new IdentityFunction<String>());
    }

}
