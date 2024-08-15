package org.processmining.ui.statechart.workbench.cancel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.ui.statechart.workbench.common.LogHTracePanel;
import org.processmining.ui.statechart.workbench.common.LogHTraceView;
import org.processmining.utils.statechart.ui.xes.XTraceListCellRenderer;

public class LogCancelExplorerView {

    private static final int Padding = 5;

    protected static final int MaxInstNameLength = 30;

    private JPanel panelRoot;
    private JList<XTrace> listTraces;

    private Set<String> cancelOracle;
    
    public class OracleAwareLogViewConfig extends LogHTracePanel.Configuration {
        public Color getChevronStrokeColor(String text, boolean active) {
            if (active || _isError(text)) {
                return chevronStrokeActiveColor;
            } else {
                return chevronStrokeColor;
            }
        }
        
        private boolean _isError(String text) {
            return cancelOracle != null && cancelOracle.contains(text);
        }
        
        public int getZLayer(String text, boolean active) {
            if (active || _isError(text)) {
                return chevronZActive;
            } else {
                return chevronZDefault;
            }
        }
    }
    private OracleAwareLogViewConfig traceViewConfig = new OracleAwareLogViewConfig();

    private LogHTraceView viewTrace;

    private XTrace currentTrace;


    public LogCancelExplorerView() {
        // Root consists of header (top) and example data (center)
        panelRoot = new JPanel();
        panelRoot.setLayout(new BorderLayout());
        
        panelRoot.add(_viewLogInstances(), BorderLayout.WEST);
        panelRoot.add(_viewTrace(), BorderLayout.CENTER);
    }

    private Component _viewLogInstances() {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BorderLayout());
        wrap.setBorder(BorderFactory.createEmptyBorder(Padding, Padding, Padding, Padding));
        
        JLabel title = new JLabel("<html><h3>Log traces:</h3></html>");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrap.add(title, BorderLayout.NORTH);
        
        listTraces = new JList<XTrace>(new DefaultListModel<XTrace>());
        listTraces.setLayoutOrientation(JList.VERTICAL);
        listTraces.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listTraces.setCellRenderer(new XTraceListCellRenderer());
        listTraces.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                _updateTraceSelection(listTraces.getSelectedValue());
            }
        });

        JScrollPane listScrollPane = new JScrollPane(listTraces);
        wrap.add(listScrollPane, BorderLayout.CENTER);
        
        return wrap;
    }
    
    private Component _viewTrace() {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BorderLayout(2, 1));
        wrap.setBorder(BorderFactory.createEmptyBorder(Padding, Padding, Padding, Padding));
        
        viewTrace = new LogHTraceView("Trace:", traceViewConfig);
        wrap.add(viewTrace.getRoot(), BorderLayout.CENTER);
        
        return wrap;
    }

    public JPanel getRoot() {
        return panelRoot;
    }

    
    public void setLogUi(XLog inputLog, Set<String> cancelOracle) {
        this.cancelOracle = cancelOracle;
        
        DefaultListModel<XTrace> modelTraces = new DefaultListModel<XTrace>();
        for (XTrace trace : inputLog) {
            modelTraces.addElement(trace);
        }
        listTraces.clearSelection();
        listTraces.setModel(modelTraces);
        
        _updateTraceSelection(null);
    }

    
    public void setLogUi(Set<String> cancelOracle) {
        this.cancelOracle = cancelOracle;
        _updateTraceSelection(currentTrace);
    }
    
    protected void _updateTraceSelection(XTrace traceOriginal) {
        currentTrace = traceOriginal;
        if (currentTrace == null) {
            viewTrace.viewNoTrace();
        } else {
            viewTrace.viewTrace(traceOriginal, null);
        }
    }
}
