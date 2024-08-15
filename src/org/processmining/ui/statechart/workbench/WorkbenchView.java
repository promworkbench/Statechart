package org.processmining.ui.statechart.workbench;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.models.statechart.doc.DocHelpButton;
import org.processmining.ui.statechart.workbench.WorkbenchController.ViewState;

import com.jidesoft.swing.JideTabbedPane;

public class WorkbenchView extends WorkbenchController.View {

    private JideTabbedPane tabbedPane;

    public WorkbenchView() {
        tabbedPane = new JideTabbedPane();
        Font font = tabbedPane.getFont().deriveFont(18.0f);
        tabbedPane.setFont(font);
        tabbedPane.setSelectedTabFont(font);
        tabbedPane.setBoldActiveTab(true);
        tabbedPane.setUI(new WorkbenchMainTabUi());

        final ViewState states[] = ViewState.values();
        for (final ViewState state : states) {
            tabbedPane.addTab(state.getLabel(), new JPanel());
        }
        
        tabbedPane.setTabTrailingComponent(new DocHelpButton());

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ViewState newState = states[tabbedPane.getSelectedIndex()];
                SignalViewStateChanged.dispatch(newState);
            }
        });
    }

    @Override
    public JComponent getComponent() {
        return tabbedPane;
    }

    @Override
    public void registerViewStatePane(ViewState state, JComponent component) {
        tabbedPane.setComponentAt(state.ordinal(), component);
    }

    @Override
    public void setViewStateEnabled(ViewState state, boolean isEnabled) {
        tabbedPane.setEnabledAt(state.ordinal(), isEnabled);
    }

    @Override
    public void showViewState(ViewState state) {
        tabbedPane.setSelectedIndex(state.ordinal());
    }

}
