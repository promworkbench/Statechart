package org.processmining.ui.statechart.workbench.log;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.ui.statechart.gfx.GfxFigs;
import org.processmining.ui.statechart.workbench.util.UiFactory;
import org.processmining.utils.statechart.ui.ListItemCopyHandler;
import org.processmining.utils.statechart.ui.ListItemTransferHandler;
import org.processmining.utils.statechart.ui.RemovableListCellRenderer;

import com.google.common.base.Function;

public class LogMultiAttribView extends LogMultiAttribController.View {

    private static final int ListVisibleRows = 4;

    private Log2LogView log2log;
    
    private JList<Object> listSelected;
    private JList<XEventClassifier> listClassifiers;
    private JList<String> listAttributes;

    public LogMultiAttribView() {
        log2log = new Log2LogView();
        log2log.setHeaderInfo(
                "Log Transformation - Multi Attributes",
                getDescriptionPanel());
        
        log2log.setButtonRunSignal(SignalStartAction);
        
        _addParameterControls(log2log.getParamControlsPanel());

    }

    public static JPanel getDescriptionPanel() {
        return Log2LogView.createDescriptionPanel(
                "Multi Attributes", 
                "Combination of attributes and classifiers", 
                new String[] {
                    "Multi labelled event logs (e.g., multi-level labels)",
                    "Knowledge or states in attributes"
                }, GfxFigs.FigLog2Log_MultiAttribs.getImageLabel(""));
    }

    private void _addParameterControls(JPanel container) {
        JPanel wrapBase = new JPanel();
        wrapBase.setLayout(new GridLayout(1, 2));
        container.add(wrapBase);

        ListItemTransferHandler<String> arrayListHandler = new ListItemTransferHandler<String>();
        ListItemCopyHandler<String> arrayListCopyHandler = new ListItemCopyHandler<String>();

        DefaultListModel<Object> modelSelected = new DefaultListModel<Object>();
        listSelected = new JList<Object>(modelSelected);
        listSelected.setLayoutOrientation(JList.VERTICAL);
        listSelected
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listSelected.setDragEnabled(true);
        listSelected.setDropMode(DropMode.INSERT);
        listSelected.setTransferHandler(arrayListHandler);
        listSelected.setCellRenderer(new RemovableListCellRenderer<Object>());
        wrapBase.add(_addTitle(UiFactory.scrollVertical(listSelected, 2 * ListVisibleRows), "Selected attributes/classifiers:"));

        _registerListenerModel(modelSelected);

        JPanel wrapSide = new JPanel();
        wrapSide.setLayout(new GridLayout(2, 1));
        wrapBase.add(wrapSide);

        listClassifiers = new JList<XEventClassifier>(new DefaultListModel<XEventClassifier>());
        listClassifiers.setLayoutOrientation(JList.VERTICAL);
        listClassifiers
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listClassifiers.setDragEnabled(true);
        listClassifiers.setTransferHandler(arrayListCopyHandler);
        wrapSide.add(_addTitle(UiFactory.scrollVertical(listClassifiers, ListVisibleRows), "Available classifiers:"));

        listAttributes = new JList<String>(new DefaultListModel<String>());
        listAttributes.setLayoutOrientation(JList.VERTICAL);
        listAttributes
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listAttributes.setDragEnabled(true);
        listAttributes.setTransferHandler(arrayListCopyHandler);
        wrapSide.add(_addTitle(UiFactory.scrollVertical(listAttributes, ListVisibleRows), "Available attributes:"));
    }

    private void _registerListenerModel(DefaultListModel<Object> modelSelected) {
        modelSelected.addListDataListener(new ListDataListener() {
            @Override
            public void intervalRemoved(ListDataEvent e) {
                _updateSelected();
            }
            
            @Override
            public void intervalAdded(ListDataEvent e) {
                _updateSelected();
            }
            
            @Override
            public void contentsChanged(ListDataEvent e) {
                _updateSelected();
            }
        });
    }

    private Component _addTitle(Component content, String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        panel.setLayout(new BorderLayout());
        panel.add(UiFactory.leftJustify(new JLabel(title)), BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    protected void _updateSelected() {
        ListModel<Object> modelSelected = listSelected.getModel();
        List<Object> selection = new ArrayList<Object>();
        for (int i = 0; i < modelSelected.getSize(); i++) {
            selection.add(modelSelected.getElementAt(i));
        }
        SignalInputSelectedOptions.dispatch(selection);
    }

    @Override
    public JComponent getComponent() {
        return log2log.getRoot();
    }

    @Override
    public void setLogUi(XLog inputLog, Function<XEvent, String> labelNormal, Function<XTrace, XTrace> transform) {
        log2log.setLogUi(inputLog, labelNormal, transform);
    }

    @Override
    public void setLogUi(Function<XEvent, String> labelNormal, Function<XTrace, XTrace> transform) {
        log2log.setLogUi(labelNormal, transform);
    }
    
    @Override
    public void setClassifierOptions(Collection<XEventClassifier> options) {
//        for (int i = 0; i < modelClassifiers.size(); i++) {
//            modelSelected.removeElement(modelClassifiers.get(i));
//        }
        DefaultListModel<Object> modelSelected = new DefaultListModel<Object>();
        _registerListenerModel(modelSelected);
        

        DefaultListModel<XEventClassifier> modelClassifiers = new DefaultListModel<XEventClassifier>();
        for (XEventClassifier opt : options) {
            modelClassifiers.addElement(opt);
        }
        listClassifiers.setModel(modelClassifiers);
    }

    @Override
    public void setAttributeOptions(Collection<String> options) {
//        for (int i = 0; i < modelAttributes.size(); i++) {
//            modelSelected.removeElement(modelAttributes.get(i));
//        }
        DefaultListModel<Object> modelSelected = new DefaultListModel<Object>();
        _registerListenerModel(modelSelected);

        DefaultListModel<String> modelAttributes = new DefaultListModel<String>();
        for (String opt : options) {
            modelAttributes.addElement(opt);
        }
        listAttributes.setModel(modelAttributes);
    }
}
