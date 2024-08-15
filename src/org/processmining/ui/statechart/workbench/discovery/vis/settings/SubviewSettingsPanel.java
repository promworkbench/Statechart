package org.processmining.ui.statechart.workbench.discovery.vis.settings;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;
import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;
import org.processmining.models.statechart.labeling.ActivityLabeler;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.models.statechart.log.HierarchyActivityInfo;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.recipes.statechart.align.AnalysisAlgorithm;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlay;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlayManager;
import org.processmining.recipes.statechart.discovery.DiscoverEPTreeRecipe.DiscoveryAlgorithm;
import org.processmining.ui.statechart.color.IColorMap;
import org.processmining.ui.statechart.workbench.discovery.DiscoveryWorkbenchController;
import org.processmining.ui.statechart.workbench.discovery.ISubview;
import org.processmining.ui.statechart.workbench.discovery.ModelVisualization;
import org.processmining.ui.statechart.workbench.discovery.vis.model.SubviewSvgModel;
import org.processmining.ui.statechart.workbench.util.UiFactory;

import com.jidesoft.swing.RangeSlider;

public class SubviewSettingsPanel implements ISubview {

    private DiscoveryWorkbenchController.View baseView;
    
    private JPanel sidebarWrap;

    private JSlider inputSliderActivities;
    private JSlider inputSliderPaths;
    private RangeSlider inputSliderDepth;
    
    private DiscoverySettingsPanel panelDiscoverySettings;
    private VisualizationSettingsPanel panelVisualizationSettings;
    private AnalysisSettingsPanel panelAnalysisSettings;
    private ExportSettingsPanel panelExportSettings;

    private AnalysisSettingsWidget widgetAnalysisSettings;
    
    public SubviewSettingsPanel(DiscoveryWorkbenchController.View baseView,
            SubviewSvgModel subviewSvgModel,
            AnalysisAlignMetricOverlayManager overlayManager) {
        this.baseView = baseView;

        sidebarWrap = new JPanel();
        sidebarWrap.setLayout(new BorderLayout());

        JPanel sidebarMain = new JPanel();
        sidebarMain.setLayout(new BoxLayout(sidebarMain, BoxLayout.X_AXIS));
//        sidebarMain.setLayout(new GridLayout(1, 3));
        _createSidebarSliders(sidebarMain);
        sidebarWrap.add(sidebarMain, BorderLayout.CENTER);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(UiFactory.createPaddingBorder());
        sidebarWrap.add(sidebar, BorderLayout.SOUTH);

        panelDiscoverySettings = new DiscoverySettingsPanel(baseView);
        panelVisualizationSettings = new VisualizationSettingsPanel(baseView);
        panelAnalysisSettings = new AnalysisSettingsPanel(baseView, overlayManager);
        panelExportSettings = new ExportSettingsPanel(baseView, subviewSvgModel);
        
        widgetAnalysisSettings = new AnalysisSettingsWidget(panelAnalysisSettings);
        
        sidebar.add(new PopoutSettingsWidget(panelDiscoverySettings));
        sidebar.add(new PopoutSettingsWidget(panelVisualizationSettings));
        sidebar.add(widgetAnalysisSettings);
        sidebar.add(new PopoutSettingsWidget(panelExportSettings));
    }

    @Override
    public JComponent getRootComponent() {
        return sidebarWrap;
    }
    
    public void setActivityInfo(HierarchyActivityInfo actInfo,
            IActivityLabeler labeler) {
        panelDiscoverySettings.setDataActivityLabeler(labeler);
        setErrorOptions(actInfo.getActivities());
    }

    public void setInputDirection(GraphDirection dir) {
        panelVisualizationSettings.setInputDirection(dir);
    }

    public void setDiscoveryAlgorithm(DiscoveryAlgorithm algorithm) {
        panelDiscoverySettings.setDiscoveryAlgorithm(algorithm);
    }
    
    public void setUseEPTreeReduct(boolean use) {
        panelDiscoverySettings.setUseEPTreeReduct(use);
    }

    public void setUseSCReduct(boolean use) {
        panelVisualizationSettings.setUseSCReduct(use);
    }
    
    public void setUseRecurseArrow(boolean use) {
        panelVisualizationSettings.setUseRecurseArrow(use);
    }
    
    public void setActivityThreshold(double threshold) {
        inputSliderActivities.setValue((int) (threshold * 100.0));
    }

    public void setPathThreshold(double threshold) {
        inputSliderPaths.setValue((int) (threshold * 100.0));
    }

    public void setDepthThreshold(double min, double max) {
        inputSliderDepth.setLowValue((int) (min * 100.0));
        inputSliderDepth.setHighValue((int) (max * 100.0));
    }
    
    public void setLogExportEnabled(boolean enabled) {
        panelExportSettings.setLogExportEnabled(enabled);
    }
    
    public void setActivityLabeler(ActivityLabeler activityLabeler) {
        panelDiscoverySettings.setDataActivityLabeler(activityLabeler.getLabeler());
        panelVisualizationSettings.setActivityLabeler(activityLabeler);
    }

    public void setSelectedVis(ModelVisualization visOption) {
        panelVisualizationSettings.setSelectedVis(visOption);
    }

    public void setInputUseCancelation(boolean value) {
        panelDiscoverySettings.setInputUseCancelation(value);
    }

    public void setInputErrors(Set<String> values) {
        panelDiscoverySettings.setInputErrors(values);
    }
    
    public void setAlignmentAlgorithm(AnalysisAlgorithm algorithm) {
        panelAnalysisSettings.setAlignmentAlgorithm(algorithm);
    }

    public void setAlignmentMetric(AnalysisAlignMetricOverlay metric) {
        panelAnalysisSettings.setAlignmentMetric(metric);
    }

    public void setEvent2TimeOptions(List<IEvent2Time> options) {
        panelAnalysisSettings.setEvent2TimeOptions(options);
    }

    public void setEvent2Time(IEvent2Time event2time) {
        panelAnalysisSettings.setEvent2Time(event2time);
    }

    public void setAnalysisStatOptions(StatMode[] values) {
        panelAnalysisSettings.setAnalysisStatOptions(values);
    }

    public void setAnalysisStatMode(StatMode value) {
        panelAnalysisSettings.setAnalysisStatMode(value);
    }

    public void setResourceAttributeOptions(List<String> attrOptions) {
        panelAnalysisSettings.setResourceAttributeOptions(attrOptions);
    }

    public void setResourceAttribute(String resourceAttribute) {
        panelAnalysisSettings.setResourceAttribute(resourceAttribute);
    }

    public void setOverlayColorLegend(IColorMap nodeColorMap, IMetricValueConvertor nodeValueConvertor,
            IColorMap edgeColorMap, IMetricValueConvertor edgeValueConvertor) {
        widgetAnalysisSettings.setOverlayColorLegend(nodeColorMap, nodeValueConvertor, edgeColorMap, edgeValueConvertor);
    }
    
    protected void setErrorOptions(Set<String> values) {
        panelDiscoverySettings.setErrorOptions(values);
    }

    private void _createSidebarSliders(JPanel sidebarMain) {
        inputSliderActivities = new JSlider(JSlider.VERTICAL, 0, 100, 100);
        inputSliderActivities.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double value = (double) inputSliderActivities.getValue() / 100.0;
                baseView.SignalInputActivityThreshold.dispatch(value);
            }
        });

        inputSliderPaths = new JSlider(JSlider.VERTICAL, 0, 100, 100);
        inputSliderPaths.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double value = (double) inputSliderPaths.getValue() / 100.0;
                baseView.SignalInputPathThreshold.dispatch(value);
            }
        });

        inputSliderDepth = new RangeSlider(0, 100);
        inputSliderDepth.setOrientation(JSlider.VERTICAL);
        inputSliderDepth.setLowValue(0);
        inputSliderDepth.setHighValue(100);
        inputSliderDepth.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double min = (double) inputSliderDepth.getLowValue() / 100.0;
                double max = (double) inputSliderDepth.getHighValue() / 100.0;
                baseView.SignalInputDepthThreshold.dispatch(min, max);
            }
        });

        sidebarMain.add(Box.createHorizontalGlue());
        sidebarMain.add(new SliderLabelCombo(inputSliderActivities, "Activities"));
        sidebarMain.add(Box.createHorizontalStrut(4));
        sidebarMain.add(new SliderLabelCombo(inputSliderPaths, "Paths"));
        sidebarMain.add(Box.createHorizontalStrut(4));
        sidebarMain.add(new SliderLabelCombo(inputSliderDepth, "Level Depth"));
        sidebarMain.add(Box.createHorizontalGlue());
    }

    public void setShowLogMoves(boolean use) {
        panelAnalysisSettings.setShowLogMoves(use);
    }

}
