package org.processmining.ui.statechart.workbench.cancel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.ui.statechart.workbench.WorkbenchSideTabUi;

public class CancelWorkbenchView extends CancelWorkbenchController.View {

    private static final String CardMain = "CardMain";
    private static final String CardWait = "CardWait";

    public static final int WaitDisplayDelayDefault = 60;
    
    private JPanel rootPanel;
    private CardLayout cardManager;
    
    private JTabbedPane tabbedPane;

    Timer waitDisplayTimer;
    
    public CancelWorkbenchView() {
        rootPanel = new JPanel();
        cardManager = new CardLayout();
        rootPanel.setLayout(cardManager);

        tabbedPane = new JTabbedPane(SwingConstants.LEFT);
        tabbedPane.setUI(new WorkbenchSideTabUi());
        rootPanel.add(tabbedPane, CardMain);

        final CancelPreprocessors states[] = CancelPreprocessors.values();
        for (final CancelPreprocessors state : states) {
            tabbedPane.addTab(state.getLabel(), new JPanel());
        }

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                CancelPreprocessors newState = states[tabbedPane.getSelectedIndex()];
                SignalViewStateChanged.dispatch(newState);
            }
        });

        JPanel waitPane = new JPanel(new BorderLayout());
        waitPane.add(new JLabel("<html><h2>Computing, please wait...</h2></html>",
                SwingConstants.CENTER), BorderLayout.CENTER);
        rootPanel.add(waitPane, CardWait);

        waitDisplayTimer = new Timer(WaitDisplayDelayDefault, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent paramActionEvent) {
                cardManager.show(rootPanel, CardWait);
            }
        });
        waitDisplayTimer.setRepeats(false);
        
        cardManager.show(rootPanel, CardWait);
    }
    
    @Override
    public JComponent getComponent() {
        return rootPanel;
    }

    @Override
    public void registerViewStatePane(CancelPreprocessors state, JComponent component) {
        tabbedPane.setComponentAt(state.ordinal(), component);
    }

    @Override
    public void setViewStateEnabled(CancelPreprocessors state, boolean isEnabled) {
        tabbedPane.setEnabledAt(state.ordinal(), isEnabled);
    }

    @Override
    public void showViewState(CancelPreprocessors state) {
        tabbedPane.setSelectedIndex(state.ordinal());
    }

    @Override
    public void displayComputing() {
        waitDisplayTimer.start();
    }

    @Override
    public void displayReady() {
        waitDisplayTimer.stop();
        cardManager.show(rootPanel, CardMain);
    }

}
