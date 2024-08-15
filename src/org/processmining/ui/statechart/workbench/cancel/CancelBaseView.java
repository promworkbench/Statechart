package org.processmining.ui.statechart.workbench.cancel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.deckfour.xes.model.XLog;
import org.processmining.ui.statechart.workbench.util.UiFactory;
import org.processmining.utils.statechart.signals.Signal0;
import org.processmining.utils.statechart.ui.SpringUtilities;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class CancelBaseView {

    private JPanel panelRoot;

    private JPanel panelHeaderInfo;

    private JPanel panelParamControls;

    private JButton btnRunDisc;

    private LogCancelExplorerView logCancelExplorer;

    public static JPanel createDescriptionPanel(String name, String description, String[] useCases, JComponent image) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        panel.add(image);
        
        JPanel descrPanel = new JPanel();
        descrPanel.setLayout(new SpringLayout());
        panel.add(descrPanel);

        descrPanel.add(UiFactory.createLabelTopLeft("<html><b>Name:</b></html>"));
        descrPanel.add(UiFactory.createLabelTopLeft("<html><p>" + name + "</p></html>"));

        descrPanel.add(UiFactory.createLabelTopLeft("<html><b>Heuristic:</b></html>"));
        descrPanel.add(UiFactory.createLabelTopLeft("<html><p>" + description + "</p></html>"));
        
        StringBuilder usecaseStr = new StringBuilder();
        usecaseStr.append("<html><ul style=\"margin-left: 10px; margin-top: 0px;\">");
        for (String usecase : useCases) {
            usecaseStr.append("<li>");
            usecaseStr.append(usecase);
            usecaseStr.append("</li>");
        }
        usecaseStr.append("</ul></html>");
        descrPanel.add(UiFactory.createLabelTopLeft("<html><b>Use&nbsp;cases:</b></html>"));
        descrPanel.add(UiFactory.createLabelTopLeft(usecaseStr.toString()));
        
        SpringUtilities.makeCompactGrid(descrPanel, 3, 2, 6, 6, 6, 6);
        descrPanel.setMaximumSize(descrPanel.getPreferredSize());
        
        panel.add(Box.createGlue());
        
        return panel;
    }

    public CancelBaseView() {
        JPanel panelWrap = new JPanel();
        panelWrap.setLayout(new BoxLayout(panelWrap, BoxLayout.Y_AXIS));

        // Header info consists of title, image, description
        panelHeaderInfo = new JPanel();
        panelHeaderInfo.setLayout(new BoxLayout(panelHeaderInfo, BoxLayout.Y_AXIS));
        panelWrap.add(panelHeaderInfo);
        
        panelWrap.add(Box.createVerticalStrut(3));
        
        // parameter controls
        panelParamControls = new JPanel();
        panelParamControls.setLayout(new BoxLayout(panelParamControls, BoxLayout.Y_AXIS));
        panelWrap.add(panelParamControls);

        panelWrap.add(Box.createVerticalStrut(3));

        btnRunDisc = SlickerFactory.instance().createButton("Accept, Start Discovery");
        panelWrap.add(UiFactory.leftJustify(btnRunDisc));

        // rest consists of example log data (before and after)
        logCancelExplorer = new LogCancelExplorerView(); 
        panelWrap.add(logCancelExplorer.getRoot());
        
        // wrap up in scroll pane
        panelRoot = new JPanel();
        panelRoot.setLayout(new BorderLayout());
        panelRoot.setBorder(UiFactory.createPaddingBorder());
        
        JScrollPane scrollPane = new JScrollPane(panelWrap);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelRoot.add(scrollPane, BorderLayout.CENTER);
    }


    protected Component _createLogExplorer() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setHeaderInfo(String title, JPanel panel) {
        panelHeaderInfo.removeAll();
        
        panelHeaderInfo.add(UiFactory.leftJustify(UiFactory.createTitleLabel(title)));
        panelHeaderInfo.add(Box.createVerticalStrut(1));
        panelHeaderInfo.add(panel);
    }

    public JPanel getParamControlsPanel() {
        return panelParamControls;
    }

    public void setButtonRunSignal(final Signal0 signal) {
        for (ActionListener l : btnRunDisc.getActionListeners()) {
            btnRunDisc.removeActionListener(l);
        }
        btnRunDisc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                signal.dispatch();
            }
        });
    }
    
    public void setLogUi(XLog inputLog, Set<String> cancelOracle) {
        logCancelExplorer.setLogUi(inputLog, cancelOracle);
    }
    
    public void setLogUi(Set<String> cancelOracle) {
        logCancelExplorer.setLogUi(cancelOracle);
    }
    
    public JPanel getRoot() {
        return panelRoot;
    }
}
