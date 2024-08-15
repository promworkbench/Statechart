package org.processmining.ui.statechart.workbench.log;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.ui.statechart.workbench.WorkbenchController;
import org.processmining.ui.statechart.workbench.util.UiFactory;
import org.processmining.utils.statechart.signals.Signal1;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.google.common.base.Function;

public class Log2LogView {

    private JPanel panelRoot;

    private JPanel panelHeaderInfo;

    private JPanel panelParamControls;

    private JButton btnRunDisc;
    private JButton btnRunCancellation;

    private LogTransformExplorerView logTransformExplorer;
    
    public static JPanel createDescriptionPanel(String name, String description, String[] useCases, JComponent image) {
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//        
//        panel.add(image);
//        
//        JPanel descrPanel = new JPanel();
//        descrPanel.setLayout(new SpringLayout());
//        panel.add(descrPanel);
//
//        descrPanel.add(UiFactory.createLabelTopLeft("<html><b>Name:</b></html>"));
//        descrPanel.add(UiFactory.createLabelTopLeft("<html><p>" + name + "</p></html>"));
//
//        descrPanel.add(UiFactory.createLabelTopLeft("<html><b>Heuristic:</b></html>"));
//        descrPanel.add(UiFactory.createLabelTopLeft("<html><p>" + description + "</p></html>"));
//        
//        StringBuilder usecaseStr = new StringBuilder();
//        usecaseStr.append("<html><ul style=\"margin-left: 10px; margin-top: 0px;\">");
//        for (String usecase : useCases) {
//            usecaseStr.append("<li>");
//            usecaseStr.append(usecase);
//            usecaseStr.append("</li>");
//        }
//        usecaseStr.append("</ul></html>");
//        descrPanel.add(UiFactory.createLabelTopLeft("<html><b>Use cases:</b></html>"));
//        descrPanel.add(UiFactory.createLabelTopLeft(usecaseStr.toString()));
//        
//        SpringUtilities.makeCompactGrid(descrPanel, 3, 2, 6, 6, 6, 6);
//        descrPanel.setMaximumSize(descrPanel.getPreferredSize());
//        
//        panel.add(Box.createGlue());
//        
//        return panel;
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(UiFactory.createLabelTopLeft("<html><h2>" + name + "</h2></html>"));
        panel.add(image);
        
        StringBuilder usecaseStr = new StringBuilder();
        usecaseStr.append("<html>");
        usecaseStr.append("<p>" + description + "</p>");
        usecaseStr.append("<ul style=\"margin-left: 10px; margin-top: 0px;\">");
        for (String usecase : useCases) {
            usecaseStr.append("<li>");
            usecaseStr.append(usecase);
            usecaseStr.append("</li>");
        }
        usecaseStr.append("</ul></html>");
        panel.add(UiFactory.createLabelTopLeft(usecaseStr.toString()));
        
        panel.setMaximumSize(panel.getPreferredSize());
        return panel;
    }

    public Log2LogView() {
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
        
        // start button
        JPanel buttonWrap = new JPanel();
        buttonWrap.setLayout(new BoxLayout(buttonWrap, BoxLayout.X_AXIS));
        
        btnRunDisc = SlickerFactory.instance().createButton("Accept, Start Discovery");
        buttonWrap.add(btnRunDisc);
        
        btnRunCancellation = SlickerFactory.instance().createButton("Accept, To Cancellation Setup");
        buttonWrap.add(btnRunCancellation);
        buttonWrap.add(Box.createHorizontalGlue());
        
        panelWrap.add(buttonWrap);

        panelWrap.add(Box.createVerticalStrut(3));
        
        // rest consists of example log data (before and after)
        logTransformExplorer = new LogTransformExplorerView();
        panelWrap.add(logTransformExplorer.getRoot());
        
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

    public void setHeaderInfo(String title, JPanel panel) {
        panelHeaderInfo.removeAll();
        
        panelHeaderInfo.add(UiFactory.leftJustify(UiFactory.createTitleLabel(title)));
        panelHeaderInfo.add(Box.createVerticalStrut(1));
        panelHeaderInfo.add(UiFactory.leftJustify(panel));
    }

    public JPanel getParamControlsPanel() {
        return panelParamControls;
    }

    public void setButtonRunSignal(final Signal1<WorkbenchController.ViewState> signal) {
        for (ActionListener l : btnRunDisc.getActionListeners()) {
            btnRunDisc.removeActionListener(l);
        }
        btnRunDisc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                signal.dispatch(WorkbenchController.ViewState.Discovery);
            }
        });
        
        for (ActionListener l : btnRunCancellation.getActionListeners()) {
            btnRunCancellation.removeActionListener(l);
        }
        btnRunCancellation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                signal.dispatch(WorkbenchController.ViewState.CancellationSetup);
            }
        });
    }
    
    public void setLogUi(XLog inputLog, Function<XEvent, String> labelNormal, Function<XTrace, XTrace> transform) {
        logTransformExplorer.setLogUi(inputLog, labelNormal, transform);
    }
    
    public void setLogUi(Function<XEvent, String> labelNormal, Function<XTrace, XTrace> transform) {
        logTransformExplorer.setLogUi(labelNormal, transform);
    }
    
    public JPanel getRoot() {
        return panelRoot;
    }
}
