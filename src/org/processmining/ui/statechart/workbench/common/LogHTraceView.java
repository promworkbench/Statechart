package org.processmining.ui.statechart.workbench.common;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.ui.statechart.workbench.util.UiFactory;

import com.google.common.base.Function;

public class LogHTraceView {
    
    private JPanel panelRoot;
    
    private LogHTracePanel tracePanel;

    private JScrollPane scrollPane;

    public LogHTraceView(String titleStr) {
        this(titleStr, null);
    }
    
    public LogHTraceView(String titleStr, LogHTracePanel.Configuration config) {
        panelRoot = new JPanel();
        panelRoot.setLayout(new BorderLayout());
        
        UiFactory.forceMinSize(panelRoot, 100, 200);

        JLabel title = new JLabel("<html><h3>" + titleStr + "</h3></html>");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelRoot.add(title, BorderLayout.NORTH);
        
        tracePanel = new LogHTracePanel(config);
        scrollPane = new JScrollPane(tracePanel, 
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        panelRoot.add(scrollPane, BorderLayout.CENTER);
    }

    public JPanel getRoot() {
        return panelRoot;
    }

    public void viewNoTrace() {
        tracePanel.viewNoTrace();
        scrollPane.revalidate();
    }

    public void viewTrace(XTrace traceOriginal, Function<XEvent, String> labelFnc) {
        tracePanel.viewTrace(traceOriginal, labelFnc);
        scrollPane.revalidate();
    }

}
