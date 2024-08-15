package org.processmining.ui.statechart.workbench.discovery.vis.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.models.statechart.labeling.ActivityLabeler;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.ui.statechart.workbench.discovery.DiscoveryWorkbenchController;
import org.processmining.ui.statechart.workbench.discovery.ModelVisualization;
import org.processmining.ui.statechart.workbench.util.UiFactory;
import org.processmining.utils.statechart.signals.Action1;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.google.common.base.Function;

public class VisualizationSettingsPanel extends AbstractSettingsPanel {

    private Map<ModelVisualization, JToggleButton> mapVis2Btn = new LinkedHashMap<>();
    private ModelVisualization selectedVis;
    private Map<GraphDirection, JToggleButton> mapDir2Btn = new LinkedHashMap<>();
    private JComboBox<ActivityLabeler> inputActivityLabeler;
    private JCheckBox chkRecurseArrow;
    private JCheckBox chkSCReduct;
    
    public VisualizationSettingsPanel(DiscoveryWorkbenchController.View baseView) {
        super(baseView);
        
        // Vis. button options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(_createVisControls());
        panel.add(_createDirControls());
        root.add(panel, BorderLayout.NORTH);
        
        // Option settings
        root.add(_createOptionControls(), BorderLayout.CENTER);
    }

    private Component _createVisControls() {
        Function<ModelVisualization, String> names = new Function<ModelVisualization, String>() {
            @Override
            public String apply(ModelVisualization val) {
                return val.getName();
            }
        };
        Function<ModelVisualization, String> tooltips = new Function<ModelVisualization, String>() {
            @Override
            public String apply(ModelVisualization val) {
                return val.getDescriptionShort();
            }
        };
        Action1<ModelVisualization> onSelect = new Action1<ModelVisualization>() {
            @Override
            public void call(ModelVisualization key) {
                baseView.SignalInputSelectedVis.dispatch(key);
                selectedVis = key;
                updateWidget();
            }
        };
        return PopoutSettingsPanelUiFactory.createTitleWrap(
            "Visualization:", 
            PopoutSettingsPanelUiFactory.constructTogglePanel(
                ModelVisualization.values(), 2, mapVis2Btn,
                names, tooltips, onSelect
        ));
    }

    private Component _createDirControls() {
        Function<GraphDirection, String> names = new Function<GraphDirection, String>() {
            @Override
            public String apply(GraphDirection val) {
                if (val == GraphDirection.topDown) {
                    return "Top - Bottom";
                } else {
                    return "Left - Right";
                }
            }
        };
        Action1<GraphDirection> onSelect = new Action1<GraphDirection>() {
            @Override
            public void call(GraphDirection key) {
                baseView.SignalInputDirection.dispatch(key);
            }
        };
        return PopoutSettingsPanelUiFactory.createTitleWrap(
            "Direction:", 
            PopoutSettingsPanelUiFactory.constructTogglePanel(
                new GraphDirection[] {
                    GraphDirection.topDown, GraphDirection.leftRight
                }, 2, mapDir2Btn,
                names, names, onSelect
        ));
    }

    @SuppressWarnings("unchecked")
    private Component _createOptionControls() {
        SlickerFactory f = SlickerFactory.instance();

        // Activity labels:
        inputActivityLabeler = (JComboBox<ActivityLabeler>) f.createComboBox(ActivityLabeler.values());
        inputActivityLabeler.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                baseView.SignalInputActivityLabeler.dispatch((ActivityLabeler) e.getItem());
                updateWidget();
            }
        });
        
        // Show Recursion Back-arrow
        chkRecurseArrow = f.createCheckBox(
                "Show Recursion Back-arrow", false);
        chkRecurseArrow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                baseView.SignalInputUseRecurseArrow.dispatch(chkRecurseArrow.isSelected());
            }
        });
        
        // Reduction on vis model
        chkSCReduct = f.createCheckBox(
                "Apply Statechart Reductions", false);
        chkSCReduct.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                baseView.SignalInputUseSCReduct.dispatch(chkSCReduct.isSelected());
            }
        });
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(UiFactory.createKeyValWrap(
            Pair.<String, Component>of(null, new JLabel(" ")),
            Pair.<String, Component>of("Activity labels:", inputActivityLabeler),
            Pair.<String, Component>of(null, UiFactory.leftJustify(f.createLabel(
                "<html>To change the selected classifier used for discovery, "
                + "go back to the Log Hierarchy tab (the top left corner).</html>"
            ))),
            Pair.<String, Component>of(null, UiFactory.leftJustify(chkRecurseArrow)),
            Pair.<String, Component>of(null, UiFactory.leftJustify(chkSCReduct))
        ), BorderLayout.NORTH);
        return panel;
    }

    @Override
    protected void updateWidget() {
        String valVis = "?";
        String valLabel = "?";
        if (selectedVis != null) {
            valVis = selectedVis.getName();
        }
        if (inputActivityLabeler != null) {
            ActivityLabeler labeler = (ActivityLabeler) inputActivityLabeler.getSelectedItem();
            valLabel = labeler.getName();
        }
        
        widgetButton.setText(PopoutSettingsPanelUiFactory.constructWidgetText(
                "Visualization settings", 
                "Visualization: " + valVis,
                "Activity labels: " + valLabel
            ));
    }

    public void setActivityLabeler(ActivityLabeler activityLabeler) {
        inputActivityLabeler.setSelectedItem(activityLabeler);
        updateWidget();
    }

    public void setSelectedVis(ModelVisualization visOption) {
        mapVis2Btn.get(visOption).setSelected(true);
        selectedVis = visOption;
        updateWidget();
    }

    public void setInputDirection(GraphDirection dir) {
        mapDir2Btn.get(dir).setSelected(true);
    }

    public void setUseRecurseArrow(boolean use) {
        chkRecurseArrow.setSelected(use);
    }
    
    public void setUseSCReduct(boolean use) {
        chkSCReduct.setSelected(use);
    }
    
}
