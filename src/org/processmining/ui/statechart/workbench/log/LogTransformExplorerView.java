package org.processmining.ui.statechart.workbench.log;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.ui.statechart.workbench.common.LogHTraceView;
import org.processmining.utils.statechart.ui.xes.XTraceListCellRenderer;

import com.google.common.base.Function;

public class LogTransformExplorerView {

    private static final int Padding = 5;

    protected static final int MaxInstNameLength = 30;

    private JPanel panelRoot;
    
    private Function<XTrace, XTrace> transform;
    private XTrace currentTrace;

    private JList<XTrace> listTraces;

    private LogHTraceView viewTraceOriginal;

    private LogHTraceView viewTraceTransformed;

    private Function<XEvent, String> labelNormal;

    public LogTransformExplorerView() {
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
        wrap.setLayout(new GridLayout(2, 1));
        wrap.setBorder(BorderFactory.createEmptyBorder(Padding, Padding, Padding, Padding));
        
        viewTraceOriginal = new LogHTraceView("Original trace:");
        viewTraceTransformed = new LogHTraceView("Transformed trace:");
        wrap.add(viewTraceOriginal.getRoot());
        wrap.add(viewTraceTransformed.getRoot());
        
        return wrap;
    }

    public JPanel getRoot() {
        return panelRoot;
    }

    public void setLogUi(XLog inputLog, Function<XEvent, String> labelNormal, Function<XTrace, XTrace> transform) {
        this.labelNormal = labelNormal;
        this.transform = transform;
        
        
        DefaultListModel<XTrace> modelTraces = new DefaultListModel<XTrace>();
        for (XTrace trace : inputLog) {
            modelTraces.addElement(trace);
        }
        listTraces.clearSelection();
        listTraces.setModel(modelTraces);
        
        _updateTraceSelection(null);
        
    }

    public void setLogUi(Function<XEvent, String> labelNormal, Function<XTrace, XTrace> transform) {
        this.transform = transform;
        _updateTraceSelection(currentTrace);
    }

    protected void _updateTraceSelection(XTrace traceOriginal) {
        currentTrace = traceOriginal;
        if (currentTrace == null) {
            viewTraceOriginal.viewNoTrace();
            viewTraceTransformed.viewNoTrace();
        } else {
            viewTraceOriginal.viewTrace(traceOriginal, labelNormal);
            viewTraceTransformed.viewTrace(transform.apply(traceOriginal), null);
        }
    }

}
