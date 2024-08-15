package org.processmining.ui.statechart.workbench.common;

import gnu.trove.set.hash.THashSet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.processmining.ui.statechart.gfx.GfxIcons;
import org.processmining.ui.statechart.workbench.util.UiFactory;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Signal1;
import org.processmining.utils.statechart.ui.FilteredListModel;
import org.processmining.utils.statechart.ui.ListItemCopyHandler;
import org.processmining.utils.statechart.ui.ListItemTransferHandler;
import org.processmining.utils.statechart.ui.RemovableListCellRenderer;
import org.processmining.utils.statechart.ui.SearchFilterAdapter;
import org.processmining.utils.statechart.ui.ValueDisplayItem;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class TwoUpListSelectionUi<T> {
    
    public final Signal1<Boolean> SignalInputUseSelection = new Signal1<>();
    public final Signal1<Set<T>> SignalInputSelected = new Signal1<>();
    
    private JPanel contentPane;
    private JList<ValueDisplayItem<T>> listSelected;
    private JCheckBox chkUseSelection;
    private JList<ValueDisplayItem<T>> listOptions;
    private boolean lockModelSelected;

    public TwoUpListSelectionUi(String leftTitle,
            boolean useCheckboxForLeft,
            int listVisibleRows,
            Dimension preferredListDim) {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayout(1, 2));
        
        ListItemTransferHandler<ValueDisplayItem<T>> arrayListHandler = new ListItemTransferHandler<>();
        ListItemCopyHandler<ValueDisplayItem<T>> arrayListCopyHandler = new ListItemCopyHandler<>();

        DefaultListModel<ValueDisplayItem<T>> modelSelected = new DefaultListModel<ValueDisplayItem<T>>();
        listSelected = new JList<ValueDisplayItem<T>>(modelSelected);
        listSelected.setLayoutOrientation(JList.VERTICAL);
        listSelected
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listSelected.setDragEnabled(true);
        listSelected.setDropMode(DropMode.INSERT);
        listSelected.setTransferHandler(arrayListHandler);
        listSelected.setCellRenderer(new RemovableListCellRenderer<Object>());
        {
            Component leftComponent;
            if (useCheckboxForLeft) {
                chkUseSelection = SlickerFactory.instance().createCheckBox(leftTitle, false);
                chkUseSelection.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SignalInputUseSelection.dispatch(chkUseSelection.isSelected());
                    }
                });
                leftComponent = chkUseSelection;
            } else {
                leftComponent = new JLabel(leftTitle);
            }
            
            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            panel.setLayout(new BorderLayout());
            panel.add(UiFactory.leftJustify(leftComponent), BorderLayout.NORTH);
            panel.add(UiFactory.scrollVertical(listSelected, listVisibleRows), BorderLayout.CENTER);
            UiFactory.forceMinSize(panel, preferredListDim);
            contentPane.add(panel);
        }
        _registerListenerModel(modelSelected);

        listOptions = new JList<ValueDisplayItem<T>>(_createModelOptions());
        listOptions.setLayoutOrientation(JList.VERTICAL);
        listOptions
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listOptions.setDragEnabled(true);
        listOptions.setTransferHandler(arrayListCopyHandler);
        {
            final JTextField searchTextbox = new JTextField();
            Border outsideBorder = BorderFactory.createLineBorder(Color.black, 1);
            Border insideBorder = BorderFactory.createMatteBorder(0, 17, 0, 0,
                    GfxIcons.IconSearch.getImageIcon(""));
            Border border = BorderFactory.createCompoundBorder(outsideBorder,
                    insideBorder);
            searchTextbox.setBorder(border);

            // link searchbox input to list filter
            SearchFilterAdapter.installChangeFilter(searchTextbox, new Action1<Predicate<String>>() {
                @SuppressWarnings("unchecked")
                @Override
                public void call(final Predicate<String> filter) {
                    FilteredListModel<ValueDisplayItem<T>, DefaultListModel<ValueDisplayItem<T>>> modelOptions = 
                        (FilteredListModel<ValueDisplayItem<T>, DefaultListModel<ValueDisplayItem<T>>>) listOptions.getModel();
                    if (filter != null) {
                        modelOptions.setFilter(new Predicate<ValueDisplayItem<T>>() {
                            @Override
                            public boolean apply(ValueDisplayItem<T> item) {
                                return filter.apply(item.getDisplay());
                            }
                        });
                    } else {
                        modelOptions.setFilter(null);
                    }
                    modelOptions.reload();
                }
            });
            
            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            panel.setLayout(new BorderLayout());
            panel.add(searchTextbox, BorderLayout.NORTH);
            panel.add(UiFactory.scrollVertical(listOptions, listVisibleRows), BorderLayout.CENTER);
            UiFactory.forceMinSize(panel, preferredListDim);
            contentPane.add(panel);
        }
    }

    private void _registerListenerModel(DefaultListModel<ValueDisplayItem<T>> modelSelected) {
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

    private FilteredListModel<ValueDisplayItem<T>, DefaultListModel<ValueDisplayItem<T>>> _createModelOptions() {
        return new FilteredListModel<>(
            new DefaultListModel<ValueDisplayItem<T>>(),
            new ValueDisplayItem<T>(null, FilteredListModel.CaptionNodeFiltered));
    }

    protected void _updateSelected() {
        if (!lockModelSelected) {
            // set behavior enforced
            DefaultListModel<ValueDisplayItem<T>> modelSelected = 
                (DefaultListModel<ValueDisplayItem<T>>) listSelected.getModel();
            Set<T> selected = new THashSet<>();
            int size = modelSelected.getSize();
            for (int i = 0; i < size; i++) {
                T item = modelSelected.get(i).getValue();
                if (selected.contains(item)) {
                    modelSelected.removeElementAt(i);
                    i--;
                    size--;
                }
                selected.add(item);
            }
            
            SignalInputSelected.dispatch(selected);
        }
    }
    
    public JComponent getComponent() {
        return contentPane;
    }

    public Set<T> getSelected() {
        Set<T> selected = new THashSet<>();
        ListModel<ValueDisplayItem<T>> modelSelected = listSelected.getModel();
        final int size = modelSelected.getSize();
        for (int i = 0; i < size; i++) {
            selected.add((T) modelSelected.getElementAt(i).getValue());
        }
        return selected;
    }
    
    public void setOptions(Collection<T> values, Function<T, String> reLabeler, 
            Comparator<T> comparator) {
        lockModelSelected = true;
        DefaultListModel<ValueDisplayItem<T>> modelSelected = new DefaultListModel<ValueDisplayItem<T>>();
        ListModel<ValueDisplayItem<T>> oldModelSelected = listSelected.getModel();
        for (int i = 0; i < oldModelSelected.getSize(); i++) {
            T opt =  oldModelSelected.getElementAt(i).getValue();
            if (values.contains(opt)) {
                modelSelected.addElement(new ValueDisplayItem<>(opt, reLabeler.apply(opt)));
            }
        }
        listSelected.setModel(modelSelected);
        _registerListenerModel(modelSelected);
        lockModelSelected = false;
        
        List<T> sortedValues = new ArrayList<>(values);
        Collections.sort(sortedValues, comparator);
        
        FilteredListModel<ValueDisplayItem<T>, DefaultListModel<ValueDisplayItem<T>>> modelOptions = _createModelOptions();
        DefaultListModel<ValueDisplayItem<T>> model = modelOptions.getModel();
        for (T opt : sortedValues) {
            ValueDisplayItem<T> item  = new ValueDisplayItem<>(opt, reLabeler.apply(opt));  
            model.addElement(item);
        }
        listOptions.setModel(modelOptions);
    }
    
    public void setSelected(Collection<T> values, Function<T, String> reLabeler) {
        DefaultListModel<ValueDisplayItem<T>> modelSelected = new DefaultListModel<ValueDisplayItem<T>>();
        if (values != null) {
            for (T opt : values) {
                ValueDisplayItem<T> item = new ValueDisplayItem<>(opt, reLabeler.apply(opt));  
                modelSelected.addElement(item);
            }
        }
        listSelected.setModel(modelSelected);
        _registerListenerModel(modelSelected);
    }
    
    public void setInputUseSelection(boolean value) {
        if (chkUseSelection != null) {
            chkUseSelection.setSelected(value);
        }
    }
    
    public boolean isUseSelection() {
        return chkUseSelection.isSelected();
    }
}
