package org.processmining.ui.statechart.workbench.cancel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.processmining.ui.statechart.gfx.GfxFigs;
import org.processmining.ui.statechart.workbench.util.UiFactory;

public class CancelNestedHandleView extends CancelNestedHandleController.View {

    private static final int ErrorListVisibleRows = 8;
    private static final Dimension PreferredListDim = new Dimension(
        80, 150
    );
    
    private CancelBaseView baseView;
    private JList<String> listOracle;
    
    public CancelNestedHandleView() {
        baseView = new CancelBaseView();
        baseView.setHeaderInfo(
            "Cancellation Preprocessing -- Nested Calls Handle Error/Exception",
            getDescriptionPanel());
        
        baseView.setButtonRunSignal(SignalStartAction);

        _addParameterControls(baseView.getParamControlsPanel());
    }

    public static JPanel getDescriptionPanel() {
        return CancelBaseView.createDescriptionPanel(
                "Nested Calls Handle Error/Exception", 
                "Use the handle error symbols, discovered in the Nested Calls hierarchy heuristics", 
                new String[] {
                    "Exception try-catch behavior in software logs"
                }, GfxFigs.FigCancel_NestedCalls.getImageLabel(""));
    }
    
    private void _addParameterControls(JPanel container) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BorderLayout());
        container.add(wrap);

        listOracle = new JList<>(new DefaultListModel<String>());
        listOracle.setLayoutOrientation(JList.VERTICAL);

        JLabel leftTitle = new JLabel("Cancel triggers: ");
        wrap.add(UiFactory.leftJustify(leftTitle), BorderLayout.NORTH);
        wrap.add(UiFactory.scrollVertical(listOracle, ErrorListVisibleRows), BorderLayout.CENTER);

        UiFactory.forceMinSize(wrap, PreferredListDim);
    }

    @Override
    public JComponent getComponent() {
        return baseView.getRoot();
    }

    @Override
    public void setLogUi(XLog inputLog, Set<String> cancelOracle) {
        baseView.setLogUi(inputLog, cancelOracle);
        setCancelOracle(cancelOracle);
    }

    @Override
    public void setLogUi(Set<String> cancelOracle) {
        baseView.setLogUi(cancelOracle);
        setCancelOracle(cancelOracle);
    }

    protected void setCancelOracle(Collection<String> values) {
        DefaultListModel<String> modelOracle = new DefaultListModel<String>();
        
        if (values != null && !values.isEmpty()) {
            List<String> sortedValues = new ArrayList<>(values);
            Collections.sort(sortedValues);
    
            for (String value : sortedValues) { 
                modelOracle.addElement(value);
            }
        }
        
        listOracle.setModel(modelOracle);
    }
}
