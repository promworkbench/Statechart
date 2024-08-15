package org.processmining.ui.statechart.workbench.discovery.vis.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.recipes.statechart.discovery.DiscoverEPTreeRecipe.DiscoveryAlgorithm;
import org.processmining.ui.statechart.workbench.common.TwoUpListSelectionUi;
import org.processmining.ui.statechart.workbench.discovery.DiscoveryWorkbenchController;
import org.processmining.ui.statechart.workbench.util.UiFactory;
import org.processmining.utils.statechart.generic.CompareComparator;
import org.processmining.utils.statechart.signals.Action1;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.google.common.base.Function;

public class DiscoverySettingsPanel extends AbstractSettingsPanel {

    private static final int ErrorListVisibleRows = 5;
    private static final Dimension PreferredListDim = new Dimension(
        80, 150
    );
    
    private Map<DiscoveryAlgorithm, JToggleButton> mapAlg2Btn = new LinkedHashMap<>();
    private TwoUpListSelectionUi<String> inputSelectCancelation;
    private DiscoveryAlgorithm selectedAlg;
    private IActivityLabeler dataActivityLabeler;
    private JCheckBox chkEPTreeReduct;
    
    public DiscoverySettingsPanel(DiscoveryWorkbenchController.View baseView) {
        super(baseView);
        
        // Alg. button options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(_createAlgControls());
        panel.add(_createOptionsControls());
        root.add(panel, BorderLayout.NORTH);
        
        // Cancelation settings
        root.add(_createCancelControls(), BorderLayout.CENTER);
    }

    private Component _createOptionsControls() {
        chkEPTreeReduct = SlickerFactory.instance().createCheckBox(
                "Apply Process SCTree Reductions", false);
        chkEPTreeReduct.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                baseView.SignalInputUseEPTreeReduct.dispatch(chkEPTreeReduct
                        .isSelected());
            }
        });
        return UiFactory.leftJustify(chkEPTreeReduct);
    }

    private Component _createAlgControls() {
        Function<DiscoveryAlgorithm, String> names = new Function<DiscoveryAlgorithm, String>() {
            @Override
            public String apply(DiscoveryAlgorithm val) {
                return val.getName();
            }
        };
        Function<DiscoveryAlgorithm, String> tooltips = new Function<DiscoveryAlgorithm, String>() {
            @Override
            public String apply(DiscoveryAlgorithm val) {
                return val.getShortDescription();
            }
        };
        Action1<DiscoveryAlgorithm> onSelect = new Action1<DiscoveryAlgorithm>() {
            @Override
            public void call(DiscoveryAlgorithm key) {
                baseView.SignalInputUseAlgorithm.dispatch(key);
                selectedAlg = key;
                updateWidget();
            }
        };
        return PopoutSettingsPanelUiFactory.createTitleWrap(
            "Discovery Algorithm:", 
            PopoutSettingsPanelUiFactory.constructTogglePanel(
                DiscoveryAlgorithm.values(), 2, mapAlg2Btn,
                names, tooltips, onSelect
        ));
    }
    
    private Component _createCancelControls() {
        inputSelectCancelation = new TwoUpListSelectionUi<String>(
                "Cancel triggers:", true, ErrorListVisibleRows, PreferredListDim);
        
        inputSelectCancelation.SignalInputSelected.register(new Action1<Set<String>>() {
            @Override
            public void call(Set<String> t) {
                baseView.SignalInputErrors.dispatch(t);
                updateWidget();
            }
        });
        
        inputSelectCancelation.SignalInputUseSelection.register(new Action1<Boolean>() {
            
            @Override
            public void call(Boolean t) {
                baseView.SignalInputUseCancelation.dispatch(t);
                updateWidget();
            }
        });

        return PopoutSettingsPanelUiFactory.createTitleWrap("Cancelation:", 
                inputSelectCancelation.getComponent());
    }

    @Override
    protected void updateWidget() {
        String valAlg = "?";
        String valCancel = "?";
        if (selectedAlg != null) {
            valAlg = selectedAlg.getName();
        }
        if (inputSelectCancelation != null) {
            if (inputSelectCancelation.isUseSelection()) {
                valCancel = "Selected "
                        + inputSelectCancelation.getSelected().size()
                        + " triggers";
            } else {
                valCancel = "Disabled";
            }
        }
        
        widgetButton.setText(PopoutSettingsPanelUiFactory.constructWidgetText(
            "Discovery settings", 
            "Algorithm: " + valAlg,
            "Cancelation: " + valCancel
        ));
    }

    public void setDiscoveryAlgorithm(DiscoveryAlgorithm algorithm) {
        mapAlg2Btn.get(algorithm).setSelected(true);
        selectedAlg = algorithm;
        updateWidget();
    }

    public void setInputUseCancelation(boolean value) {
        inputSelectCancelation.setInputUseSelection(value);
        updateWidget();
    }

    public void setInputErrors(Set<String> values) {
        inputSelectCancelation.setSelected(values, new Function<String, String>() {
            @Override
            public String apply(String value) {
                return dataActivityLabeler.getLabel(value);
            }
        });
        updateWidget();
    }

    public void setErrorOptions(Set<String> values) {
        inputSelectCancelation.setOptions(values, new Function<String, String>() {
            @Override
            public String apply(String value) {
                return dataActivityLabeler.getLabel(value);
            }
        }, new CompareComparator<String>());
    }

    public void setDataActivityLabeler(IActivityLabeler labeler) {
        dataActivityLabeler = labeler;
    }

    public void setUseEPTreeReduct(boolean use) {
        chkEPTreeReduct.setSelected(use);
    }
}
