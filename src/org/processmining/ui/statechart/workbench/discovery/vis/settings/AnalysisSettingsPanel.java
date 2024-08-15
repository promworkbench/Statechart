package org.processmining.ui.statechart.workbench.discovery.vis.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.processmining.algorithms.statechart.align.metric.time.Event2TimeTimestamp;
import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;
import org.processmining.recipes.statechart.align.AnalysisAlgorithm;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlay;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlayManager;
import org.processmining.ui.statechart.workbench.discovery.DiscoveryWorkbenchController;
import org.processmining.ui.statechart.workbench.util.UiFactory;
import org.processmining.utils.statechart.signals.Action1;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.google.common.base.Function;

public class AnalysisSettingsPanel extends AbstractSettingsPanel {

    private Map<AnalysisAlgorithm, JToggleButton> mapAlg2Btn = new LinkedHashMap<>();
    private AnalysisAlgorithm selectedAlg;
    
    private Map<AnalysisAlignMetricOverlay, JToggleButton> mapAlignMetric2Btn = new LinkedHashMap<>();
    private AnalysisAlignMetricOverlay selectedAlignMetric;
    private JComboBox<IEvent2Time> inputEvent2Time;
    
    private JComboBox<StatMode> inputStatMode;
    private JComboBox<String> inputResourceAttribute;
    private JCheckBox chkShowLogMoves;
    
    public AnalysisSettingsPanel(DiscoveryWorkbenchController.View baseView,
            AnalysisAlignMetricOverlayManager overlayManager) {
        super(baseView);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(_createAlignControls());
        panel.add(_createAlignMetricsControls(overlayManager));
        root.add(panel, BorderLayout.NORTH);
        
        root.add(_createOptionControls(), BorderLayout.CENTER);
    }
    private Component _createAlignControls() {
        Function<AnalysisAlgorithm, String> names = new Function<AnalysisAlgorithm, String>() {
            @Override
            public String apply(AnalysisAlgorithm val) {
                return val.getName();
            }
        };
        Function<AnalysisAlgorithm, String> tooltips = new Function<AnalysisAlgorithm, String>() {
            @Override
            public String apply(AnalysisAlgorithm val) {
                return val.getDescriptionShort();
            }
        };
        Action1<AnalysisAlgorithm> onSelect = new Action1<AnalysisAlgorithm>() {
            @Override
            public void call(AnalysisAlgorithm key) {
                baseView.SignalInputUseAlignAlg.dispatch(key);
                selectedAlg = key;
                updateWidget();
            }
        };
        return PopoutSettingsPanelUiFactory.createTitleWrap(
            "Analysis based on:", 
            PopoutSettingsPanelUiFactory.constructTogglePanel(
                AnalysisAlgorithm.values(), 2, mapAlg2Btn,
                names, tooltips, onSelect
        ));
    }

    private Component _createAlignMetricsControls(AnalysisAlignMetricOverlayManager overlayManager) {
        Function<AnalysisAlignMetricOverlay, String> names = new Function<AnalysisAlignMetricOverlay, String>() {
            @Override
            public String apply(AnalysisAlignMetricOverlay val) {
                return "<html><p>" + val.getName() + "</p></html>";
            }
        };
        Function<AnalysisAlignMetricOverlay, Icon> icons = new Function<AnalysisAlignMetricOverlay, Icon>() {
            @Override
            public Icon apply(AnalysisAlignMetricOverlay val) {
                return val.getIcon().getImageIcon("");
            }
        };
        Function<AnalysisAlignMetricOverlay, String> tooltips = new Function<AnalysisAlignMetricOverlay, String>() {
            @Override
            public String apply(AnalysisAlignMetricOverlay val) {
                return val.getTooltip();
            }
        };
        Action1<AnalysisAlignMetricOverlay> onSelect = new Action1<AnalysisAlignMetricOverlay>() {
            @Override
            public void call(AnalysisAlignMetricOverlay key) {
                baseView.SignalInputUseAlignMetric.dispatch(key);
                selectedAlignMetric = key;
                updateWidget();
            }
        };
        
        Collection<AnalysisAlignMetricOverlay> overlays = overlayManager.getOverlays();
        return PopoutSettingsPanelUiFactory.createTitleWrap(
            "Metric overlay:", 
            PopoutSettingsPanelUiFactory.constructTogglePanel(
                overlays.toArray(new AnalysisAlignMetricOverlay[overlays.size()]), 2, mapAlignMetric2Btn,
                names, icons, tooltips, onSelect
        ));
    }


    @SuppressWarnings("unchecked")
    private Component _createOptionControls() {
        SlickerFactory f = SlickerFactory.instance();

        // Stat Mode
        inputStatMode = f.createComboBox(new StatMode[] {
            // Default option to avoid SlickerComboBoxUI paint() 
            // null pointer bug on box.getSelectedItem().toString()
            StatMode.Mean
        });
        inputStatMode.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                baseView.SignalInputAnalysisStatMode.dispatch((StatMode) e.getItem());
                updateWidget();
            }
        });

        // Time Attribute
        inputEvent2Time = (JComboBox<IEvent2Time>) f.createComboBox(new IEvent2Time[] {
            // Default option to avoid SlickerComboBoxUI paint() 
            // null pointer bug on box.getSelectedItem().toString()
            new Event2TimeTimestamp()
        });
        inputEvent2Time.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                baseView.SignalInputEvent2Time.dispatch((IEvent2Time) e.getItem());
                updateWidget();
            }
        });
        
        // Resource Attribute
        inputResourceAttribute = f.createComboBox(new String[] {
            // Default option to avoid SlickerComboBoxUI paint() 
            // null pointer bug on box.getSelectedItem().toString()
            XOrganizationalExtension.KEY_RESOURCE
        });
        inputResourceAttribute.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                baseView.SignalInputResourceAttribute.dispatch((String) e.getItem());
            }
        });

        // Show Log Moves
        chkShowLogMoves = f.createCheckBox(
                "Show Log Moves", false);
        chkShowLogMoves.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                baseView.SignalInputShowLogMoves.dispatch(chkShowLogMoves.isSelected());
            }
        });
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(UiFactory.createKeyValWrap(
            Pair.<String, Component>of(null, new JLabel(" ")),
            Pair.<String, Component>of("Statistics value:", inputStatMode),
            Pair.<String, Component>of("Time Attribute:", inputEvent2Time),
            Pair.<String, Component>of("Resource Attribute:", inputResourceAttribute),
            Pair.<String, Component>of("Deviations:", chkShowLogMoves)
        ), BorderLayout.NORTH);
        return panel;
    }

    @Override
    protected void updateWidget() {
        String valAlg = "?";
        if (selectedAlg != null) {
            valAlg = selectedAlg.getName();
        }
        String valOverlay = "?";
        if (selectedAlignMetric != null) {
            valOverlay = selectedAlignMetric.getName();
        }
        
        widgetButton.setText(PopoutSettingsPanelUiFactory.constructWidgetText(
                "Analysis settings", 
                "Based on: " + valAlg,
                "Overlay: " + valOverlay
            ));
    }

    public void setAlignmentAlgorithm(AnalysisAlgorithm algorithm) {
        mapAlg2Btn.get(algorithm).setSelected(true);
        selectedAlg = algorithm;
        updateWidget();
    }
    
    public void setAlignmentMetric(AnalysisAlignMetricOverlay metric) {
        mapAlignMetric2Btn.get(metric).setSelected(true);
        selectedAlignMetric = metric;
        updateWidget();
    }

    public void setEvent2TimeOptions(List<IEvent2Time> options) {
        DefaultComboBoxModel<IEvent2Time> model = 
            (DefaultComboBoxModel<IEvent2Time>) inputEvent2Time.getModel();
        model.removeAllElements();
        for (IEvent2Time item : options) {
            model.addElement(item);
        }
    }
    
    public void setEvent2Time(IEvent2Time event2time) {
        inputEvent2Time.setSelectedItem(event2time);
    }
    
    public void setAnalysisStatOptions(StatMode[] values) {
        DefaultComboBoxModel<StatMode> model = 
            (DefaultComboBoxModel<StatMode>) inputStatMode.getModel();
        model.removeAllElements();
        for (StatMode item : values) {
            model.addElement(item);
        }
    }
    
    public void setAnalysisStatMode(StatMode value) {
        inputStatMode.setSelectedItem(value);
    }
    
    public void setResourceAttributeOptions(List<String> attrOptions) {
        DefaultComboBoxModel<String> model = 
            (DefaultComboBoxModel<String>) inputResourceAttribute.getModel();
        model.removeAllElements();
        for (String item : attrOptions) {
            model.addElement(item);
        }
    }
    
    public void setResourceAttribute(String resourceAttribute) {
        inputResourceAttribute.setSelectedItem(resourceAttribute);
    }
    public void setShowLogMoves(boolean use) {
        chkShowLogMoves.setSelected(use);
    }

}
