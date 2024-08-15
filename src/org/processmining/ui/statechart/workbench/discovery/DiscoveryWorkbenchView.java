package org.processmining.ui.statechart.workbench.discovery;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;
import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.models.statechart.align.XAlignedTreeLog;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;
import org.processmining.models.statechart.decorate.staticmetric.SCComplexityMetric;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.labeling.ActivityLabeler;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.models.statechart.log.HierarchyActivityInfo;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.protocols.statechart.saw.SawServerStatus;
import org.processmining.recipes.statechart.align.AnalysisAlgorithm;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlay;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlayManager;
import org.processmining.recipes.statechart.discovery.DiscoverEPTreeRecipe.DiscoveryAlgorithm;
import org.processmining.ui.statechart.color.IColorMap;
import org.processmining.ui.statechart.workbench.discovery.vis.info.SubviewSearchtree;
import org.processmining.ui.statechart.workbench.discovery.vis.info.SubviewStatusBar;
import org.processmining.ui.statechart.workbench.discovery.vis.model.SubviewSvgModel;
import org.processmining.ui.statechart.workbench.discovery.vis.settings.SubviewSettingsPanel;
import org.processmining.ui.statechart.workbench.util.UiFactory;

import com.kitfox.svg.SVGDiagram;

public class DiscoveryWorkbenchView extends DiscoveryWorkbenchController.View {

    private static final String CardMain = "CardMain";
    private static final String CardWaitDiscovering = "CardWaitDiscovering";
    private static final String CardWaitAligning = "CardWaitAligning";
    private static final String CardModelNotSupported = "CardModelNotSupported";
    private static final String CardAlignmentFailed = "CardAlignmentFailed";
    
    public static final int WaitDisplayDelayDefault = 60;
    public static final int WaitDisplayDelayFeedback = 3000;
    public static final int SearchModelSplitAt = 250;
    public static final int AlignProgressBarWidth = 230;
    public static final int AlignProgressBarHeight = 20;
    
    private final JPanel panelRoot;
    private final JPanel panelCenter;

    private CardLayout cardManager;
    private Timer waitDisplayDiscoveringTimer;
    private Timer waitDisplayAligningTimer;
    private Timer waitDisplayModelTimer;
    
    private SubviewSvgModel subviewSvgModel;
    private SubviewSettingsPanel subviewSettingsPanel;
    private SubviewSearchtree subviewSearchtree;
    private SubviewStatusBar subviewStatusBar;
    
    private JProgressBar alignProgress;
    private JLabel alignText;

    public DiscoveryWorkbenchView(AnalysisAlignMetricOverlayManager overlayManager) {
        panelRoot = new JPanel();
        panelRoot.setLayout(new BorderLayout());

        // Center
        panelCenter = new JPanel();
        cardManager = new CardLayout();
        panelCenter.setLayout(cardManager);

        {
            // Card: Wait Discovery
            JPanel panelWaitDisc = new JPanel();
            panelWaitDisc.setLayout(new BorderLayout());
            panelWaitDisc.add(new JLabel("<html><h2>Discovering model...</h2></html>",
                    SwingConstants.CENTER), BorderLayout.CENTER);
            panelCenter.add(panelWaitDisc, CardWaitDiscovering);
        }
        
        {
            // Card: Wait Alignments
            alignProgress = new JProgressBar();
            UiFactory.forceSize(alignProgress, AlignProgressBarWidth, AlignProgressBarHeight);
            alignText = new JLabel();
            alignText.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
            JPanel panelWaitAlign = new JPanel();
            panelWaitAlign.setLayout(new BoxLayout(panelWaitAlign, BoxLayout.Y_AXIS));
            panelWaitAlign.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            panelWaitAlign.add(Box.createVerticalGlue());
            panelWaitAlign.add(UiFactory.centerJustify(new JLabel("<html><h2>Aligning model...</h2></html>", SwingConstants.CENTER)));
            panelWaitAlign.add(UiFactory.centerJustify(alignText));
            panelWaitAlign.add(UiFactory.centerJustify(alignProgress));
            panelWaitAlign.add(Box.createVerticalGlue());
            panelCenter.add(UiFactory.centerJustify(panelWaitAlign), CardWaitAligning);
        }
        
        {
            // Card: Model Not Supported
            JPanel panelModelNotSupported = new JPanel();
            panelModelNotSupported.setLayout(new BorderLayout());
            panelModelNotSupported.add(new JLabel("<html><h2>Model visualization not supported</h2>"
                    + "<p>Some features in the discovered model cannot be displayed <br> "
                    + "using the current discovery and visualization settings. <br> "
                    + "Hint: Check your setup for recursion and cancelation.</p></html>",
                    SwingConstants.CENTER), BorderLayout.CENTER);
            panelCenter.add(panelModelNotSupported, CardModelNotSupported);
        }

        {
            // Card: Alignment Failed
            JPanel panelAlignmentFailed = new JPanel();
            panelAlignmentFailed.setLayout(new BorderLayout());
            panelAlignmentFailed.add(new JLabel("<html><h2>Alignment unsuccessful</h2>"
                    + "<p>Could not compute alignments on this model. <br>"
                    + "Attempting to fall back to approximation analysis...</p></html>",
                    SwingConstants.CENTER), BorderLayout.CENTER);
            panelCenter.add(panelAlignmentFailed, CardAlignmentFailed);
        }

        // Setup timers
        waitDisplayDiscoveringTimer = new Timer(WaitDisplayDelayDefault,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent paramActionEvent) {
                        _displayCard(CardWaitDiscovering);
                    }
                });
        waitDisplayDiscoveringTimer.setRepeats(false);
        
        waitDisplayAligningTimer = new Timer(WaitDisplayDelayDefault,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent paramActionEvent) {
                        _displayCard(CardWaitAligning);
                    }
                });
        waitDisplayAligningTimer.setRepeats(false);
        
        waitDisplayModelTimer = new Timer(WaitDisplayDelayFeedback, null);
        waitDisplayModelTimer.setRepeats(false);
        
        // Setup model center
        subviewSvgModel = new SubviewSvgModel(this, overlayManager);
        panelCenter.add(subviewSvgModel.getRootComponent(), CardMain);
        
        cardManager.show(panelCenter, CardWaitDiscovering);

        // Sidebar Right
        subviewSettingsPanel = new SubviewSettingsPanel(this, subviewSvgModel, overlayManager);
        panelRoot.add(subviewSettingsPanel.getRootComponent(), BorderLayout.EAST);
        
        // Search Left
        subviewSearchtree = new SubviewSearchtree(this);

        // Split combining center and left
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                subviewSearchtree.getRootComponent(), panelCenter);
        panelRoot.add(splitPane, BorderLayout.CENTER);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(SearchModelSplitAt);
        
        // Bottom status bar
        subviewStatusBar = new SubviewStatusBar(this);
        panelRoot.add(subviewStatusBar.getRootComponent(), BorderLayout.SOUTH);
    }
    
    @Override
    public JComponent getComponent() {
        return panelRoot;
    }

    @Override
    public void displayDiscovering() {
        waitDisplayDiscoveringTimer.start();
        subviewSearchtree.displayDiscovering();
    }
    
    @Override
    public void displayAligning() {
        alignText.setText("Starting alignments...");
        waitDisplayAligningTimer.start();
    }
    
    @Override
    public void displayModelNotSupported() {
        waitDisplayDiscoveringTimer.stop();
        waitDisplayAligningTimer.stop();
        
        waitDisplayModelTimer.stop();
        waitDisplayModelTimer.start();
        
        subviewSearchtree.displayDiscovering();
        cardManager.show(panelCenter, CardModelNotSupported);
    }

    @Override
    public void displayAlignmentFailed() {
        waitDisplayDiscoveringTimer.stop();
        waitDisplayAligningTimer.stop();
        
        waitDisplayModelTimer.stop();
        waitDisplayModelTimer.start();
        
        subviewSearchtree.displayDiscovering();
        cardManager.show(panelCenter, CardAlignmentFailed);
    }

    @Override
    public void updateAligningStatus(int min, int max, int value) {
        if (value == max) {
            alignText.setText("Processing results...");
            alignProgress.setIndeterminate(true);
        } else {
            min = Math.min(min, value);
            max = Math.max(max, value);
            alignProgress.setIndeterminate(false);
            alignProgress.setMinimum(min);
            alignProgress.setMaximum(max);
            alignProgress.setValue(value);
            
            if (max > 0) {
                double percent = (double) (value - min) / (double) (max - min);
                alignText.setText(String.format("Computing alignment (%.0f%%)", percent * 100.0));
            } else {
                alignText.setText("Computing alignment...");
            }
        }
    }

    @Override
    public void displayModel(SVGDiagram image, boolean resetView) {
        waitDisplayDiscoveringTimer.stop();
        waitDisplayAligningTimer.stop();
        
        subviewSvgModel.displayModel(image, resetView);
        _displayCard(CardMain);
    }

    private void _displayCard(final String cardName) {
        for (ActionListener act : waitDisplayModelTimer.getActionListeners()) {
            waitDisplayModelTimer.removeActionListener(act);
        }
        
        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardManager.show(panelCenter, cardName);
                waitDisplayModelTimer.removeActionListener(this);
            }
        };
        waitDisplayModelTimer.addActionListener(action);
        if (!waitDisplayModelTimer.isRunning()) {
            action.actionPerformed(null);
        }
    }

    @Override
    public void setInputDirection(GraphDirection dir) {
        subviewSettingsPanel.setInputDirection(dir);
    }

    @Override
    public void setDiscoveryAlgorithm(DiscoveryAlgorithm algorithm) {
        subviewSettingsPanel.setDiscoveryAlgorithm(algorithm);
    }
    
    @Override
    public void setUseEPTreeReduct(boolean use) {
        subviewSettingsPanel.setUseEPTreeReduct(use);
    }

    @Override
    public void setUseSCReduct(boolean use) {
        subviewSettingsPanel.setUseSCReduct(use);
    }
    
    @Override
    public void setUseRecurseArrow(boolean use) {
        subviewSettingsPanel.setUseRecurseArrow(use);
    }
    
    @Override
    public void setActivityThreshold(double threshold) {
        subviewSettingsPanel.setActivityThreshold(threshold);
    }

    @Override
    public void setPathThreshold(double threshold) {
        subviewSettingsPanel.setPathThreshold(threshold);
    }

    @Override
    public void setDepthThreshold(double min, double max) {
        subviewSettingsPanel.setDepthThreshold(min, max);
    }

    @Override
    public void setStatechartMetric(SCComplexityMetric metric) {
        subviewSearchtree.setStatechartMetric(metric);
    }

    @Override
    public void setAlignMetrics(XAlignedTreeLog alignedLog) {
        subviewSearchtree.setAlignMetrics(alignedLog);
    }

    @Override
    public void setEPTree(IEPTree tree,
            IActivityLabeler activityLabeler) {
        subviewSvgModel.setEPTree(tree, activityLabeler);
        subviewSearchtree.setEPTree(tree, activityLabeler);
    }

    @Override
    public void setActivityInfo(HierarchyActivityInfo actInfo, IActivityLabeler labeler) {
        subviewSettingsPanel.setActivityInfo(actInfo, labeler);
    }
    
    @Override
    public void setSelectedNodes(Set<String> selectedNodes) {
        subviewSearchtree.setSelectedNodes(selectedNodes);
    }

    @Override
    public void setSawServerStatus(SawServerStatus status) {
        subviewStatusBar.setSawServerStatus(status);
    }

    @Override
    public void setMemoryUsage(long currentBytes, long maxBytes) {
        subviewStatusBar.setMemoryUsage(currentBytes, maxBytes);
    }

    @Override
    public void setLogExportEnabled(boolean enabled) {
        subviewSettingsPanel.setLogExportEnabled(enabled);
    }

    @Override
    public void setStatus(String status) {
        subviewStatusBar.setStatus(status);
    }

    @Override
    public void setSawStatus(String status) {
        subviewStatusBar.setSawStatus(status);
    }

    @Override
    public void setActivityLabeler(ActivityLabeler activityLabeler) {
        subviewSettingsPanel.setActivityLabeler(activityLabeler);
    }

    @Override
    public void setSelectedVis(ModelVisualization visOption) {
        subviewSettingsPanel.setSelectedVis(visOption);
    }
    
    @Override
    public void setInputUseCancelation(boolean value) {
        subviewSettingsPanel.setInputUseCancelation(value);
    }
    
    @Override
    public void setInputErrors(Set<String> values) {
        subviewSettingsPanel.setInputErrors(values);
    }

    @Override
    public void setAlignmentAlgorithm(AnalysisAlgorithm algorithm) {
        subviewSettingsPanel.setAlignmentAlgorithm(algorithm);
    }
    
    @Override
    public void setAlignmentMetric(AnalysisAlignMetricOverlay metric) {
        subviewSettingsPanel.setAlignmentMetric(metric);
    }

    @Override
    public void setEvent2TimeOptions(List<IEvent2Time> options) {
        subviewSettingsPanel.setEvent2TimeOptions(options);
    }

    @Override
    public void setEvent2Time(IEvent2Time event2time) {
        subviewSettingsPanel.setEvent2Time(event2time);
    }

    @Override
    public void setOverlayColorLegend(IColorMap nodeColorMap, IMetricValueConvertor nodeValueConvertor,
            IColorMap edgeColorMap, IMetricValueConvertor edgeValueConvertor) {
        subviewSettingsPanel.setOverlayColorLegend(nodeColorMap, nodeValueConvertor, edgeColorMap, edgeValueConvertor);
    }

    @Override
    public void setAnalysisStatOptions(StatMode[] values) {
        subviewSettingsPanel.setAnalysisStatOptions(values);
    }

    @Override
    public void setAnalysisStatMode(StatMode value) {
        subviewSettingsPanel.setAnalysisStatMode(value);
    }

    @Override
    public void setResourceAttributeOptions(List<String> attrOptions) {
        subviewSettingsPanel.setResourceAttributeOptions(attrOptions);
    }

    @Override
    public void setResourceAttribute(String resourceAttribute) {
        subviewSettingsPanel.setResourceAttribute(resourceAttribute);
    }

    @Override
    public void setShowLogMoves(boolean use) {
        subviewSettingsPanel.setShowLogMoves(use);
    }
}
