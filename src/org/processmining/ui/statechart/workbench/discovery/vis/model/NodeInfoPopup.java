package org.processmining.ui.statechart.workbench.discovery.vis.model;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.algorithms.statechart.align.metric.IMetric;
import org.processmining.algorithms.statechart.align.metric.MetricId;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricsTreeDecorator;
import org.processmining.models.statechart.decorate.error.EPTreeErrorTriggerDecorator;
import org.processmining.models.statechart.decorate.staticmetric.EPTreeFreqMetricDecorator;
import org.processmining.models.statechart.decorate.staticmetric.FreqMetric;
import org.processmining.models.statechart.decorate.swapp.EPTreeSwAppDecorator;
import org.processmining.models.statechart.decorate.swapp.SwAppDecoration;
import org.processmining.models.statechart.eptree.EPTreeUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.recipes.statechart.align.AnalysisAlgorithm;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlay;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlayManager;
import org.processmining.ui.statechart.gfx.GfxIcons;
import org.processmining.ui.statechart.workbench.WorkbenchColors;
import org.processmining.ui.statechart.workbench.WorkbenchSideTopTabUi;
import org.processmining.ui.statechart.workbench.discovery.DiscoveryWorkbenchController;
import org.processmining.ui.statechart.workbench.util.UiFactory;
import org.processmining.utils.statechart.software.JoinpointStructure;

public class NodeInfoPopup {

    protected DiscoveryWorkbenchController.View baseView;
    
//    public static final int DefaultWindowWidth = 350;
//    public static final int DefaultWindowHeight = 400;
    public static final int DefaultWindowWidth = 300;
    public static final int DefaultWindowHeight = 350;

    private static final int RootPadding = 5;

    private JDialog popupWindow;
    private int windowWidth, windowHeight;
//    private boolean openPopup;
    
    private IEPTreeNode currentNode;
    
    // info labels:
    private JLabel labelName;
    
    private JLabel labelHierarchy;
    private JLabel labelCancelation;
    
    private JLabel labelSwJoinpoint;
    private JLabel labelSwFilename;
    private JLabel labelSwLinenr;
    private JLabel labelSwPackage;
    private JLabel labelSwClass;
    private JLabel labelSwMethod;
    private JLabel labelSwIsConstructor;


    private static final String MetricCardApprox = "CardApprox";
    private static final String MetricCardAlign = "CardAlign";
    private CardLayout metricsCardManager;

    private JPanel metricsRootPanel;


    private JLabel labelMetricApproxAbsFreq;
    private JLabel labelMetricApproxCaseFreq;

    private Map<MetricId, JLabel> metricLabels;
    private Map<MetricId, JLabel> metricValues;
    private Set<MetricId> metrics;

    
    public NodeInfoPopup(DiscoveryWorkbenchController.View baseView,
            AnalysisAlignMetricOverlayManager overlayManager) {
        this(baseView, overlayManager,DefaultWindowWidth, DefaultWindowHeight);
    }
    
    protected NodeInfoPopup(DiscoveryWorkbenchController.View baseView, 
            AnalysisAlignMetricOverlayManager overlayManager,
            int windowWidth, int windowHeight) {
        this.baseView = baseView;
        
        // Window
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        popupWindow = new JDialog();
        popupWindow.setUndecorated(true);
        popupWindow.setAlwaysOnTop(true);
        popupWindow.setLayout(new BorderLayout());
        popupWindow.add(createContent(overlayManager), BorderLayout.CENTER);
        popupWindow.pack();
        popupWindow.setBounds(0, 0, windowWidth, windowHeight);
    }
    
    public void hide() {
        popupWindow.setVisible(false);
    }
    
    public JDialog getPopupWindow() {
        return popupWindow;
    }
    
    public void viewAtLocation(int x, int y) {
        popupWindow.setLocation(x, y);
        popupWindow.setVisible(true);
    }

    public double getWidth() {
        return windowWidth;
    }

    public double getHeight() {
        return windowHeight;
    }
    
    public void setData(IEPTree tree, IEPTreeNode node, 
            IActivityLabeler dataActivityLabeler) {
        currentNode = node;
        
        updateTitle(tree, node, dataActivityLabeler);
        updateMetrics(tree, node, dataActivityLabeler);
        updateCancelation(tree, node, dataActivityLabeler);
        updateHierarchy(tree, node, dataActivityLabeler);
        updateSoftware(tree, node, dataActivityLabeler);
    }

    private void updateTitle(IEPTree tree, IEPTreeNode node,
            IActivityLabeler dataActivityLabeler) {
        String label;
        if (node.getNodeType().isLabelled()) {
            label = dataActivityLabeler.getLabel(node);
        } else {
            label = "Operator: " + node.getNodeType().getSymbol();
        }
        labelName.setText("<html><h3>" + label + "</h3></html>");
    }

    private void updateMetrics(IEPTree tree, IEPTreeNode node,
            IActivityLabeler dataActivityLabeler) {
        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(tree);
        if (decAlign != null) {
            // Base on alignments
            for (MetricId metricKey : metrics) {
                JLabel label = metricLabels.get(metricKey);
                JLabel value = metricValues.get(metricKey);
                
                label.setText(decAlign.getLabelName(node, metricKey) + ":");
                value.setText(decAlign.getMetricValueString(node, metricKey));
            }
            metricsCardManager.show(metricsRootPanel, MetricCardAlign);
        } else {
            // Base on Approximations
            labelMetricApproxAbsFreq.setText("<unkown>");
            labelMetricApproxCaseFreq.setText("<unkown>");
            
            EPTreeFreqMetricDecorator decApprox = tree.getDecorations()
                    .getForType(EPTreeFreqMetricDecorator.class);
            if (decApprox != null) {
                FreqMetric freqDec = decApprox.getDecoration(node);
                if (freqDec != null) {
                    labelMetricApproxAbsFreq.setText(
                        String.format("%d", freqDec.getFreqAbsolute()));
                    labelMetricApproxCaseFreq.setText(
                        String.format("%d", freqDec.getFreqCase()));
                }
            }
            metricsCardManager.show(metricsRootPanel, MetricCardApprox);
        }
    }

    private void updateCancelation(IEPTree tree, IEPTreeNode node,
            IActivityLabeler dataActivityLabeler) {
        Set<String> labels = null;
        
        EPTreeErrorTriggerDecorator decorator = tree.getDecorations()
                .getForType(EPTreeErrorTriggerDecorator.class);
        if (decorator != null) {
            labels = decorator.getDecoration(node);
        }
        
        if (labels != null) {
            StringBuilder bld = new StringBuilder();
            bld.append("<html>");
            bld.append("<ul style=\"margin-left: 15px; margin-top: 0px;\">");
            for (String label : labels) {
                bld.append("<li>");
                bld.append(label);
                bld.append("</li>");
            }
            bld.append("</ul></html>");
            labelCancelation.setText(bld.toString());
        } else {
            labelCancelation.setText("");
        }
    }
    
    private void updateHierarchy(IEPTree tree, IEPTreeNode node,
            IActivityLabeler dataActivityLabeler) {
        List<String> labels = EPTreeUtil.getHierarchyPath(node, dataActivityLabeler);
//        Collections.reverse(labels);
        
        StringBuilder bld = new StringBuilder();
        bld.append("<html>");
        bld.append("<ol style=\"margin-left: 15px; margin-top: 0px;\">");
        for (String label : labels) {
            bld.append("<li>");
            bld.append(label);
            bld.append("</li>");
        }
        bld.append("</ol></html>");
        labelHierarchy.setText(bld.toString());
    }

    private void updateSoftware(IEPTree tree, IEPTreeNode node,
            IActivityLabeler dataActivityLabeler) {
        labelSwJoinpoint.setText("No joinpoint info available");
        labelSwFilename.setText("");
        labelSwLinenr.setText("");

        labelSwPackage.setText("");
        labelSwClass.setText("");
        labelSwMethod.setText("");
        labelSwIsConstructor.setText("");
        
        EPTreeSwAppDecorator decorator = tree.getDecorations()
                .getForType(EPTreeSwAppDecorator.class);
        if (decorator != null) {
            SwAppDecoration swDec = decorator.getDecoration(node);
            if (swDec != null) {
                String joinpoint = swDec.getJoinpoint();
                
                labelSwJoinpoint.setText(joinpoint);
                labelSwFilename.setText(swDec.getFilename());
                labelSwLinenr.setText(Integer.toString(swDec.getLinenr()));

                JoinpointStructure jpstruct = new JoinpointStructure(joinpoint);
                labelSwPackage.setText(jpstruct.getJpPackage());
                labelSwClass.setText(jpstruct.getJpClass());
                labelSwMethod.setText(jpstruct.getJpMethod() + "(" + jpstruct.getJpParamStr() + ")");
                labelSwIsConstructor.setText(Boolean.toString(jpstruct.isConstructor()));
            }
        }
    }

    private Component createContent(AnalysisAlignMetricOverlayManager overlayManager) {
        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout());
        rootPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WorkbenchColors.BackPane),
            new EmptyBorder(RootPadding, RootPadding, RootPadding, RootPadding)
        ));
        
        labelName = UiFactory.createTitleLabel("Node Title");
        rootPanel.add(labelName, BorderLayout.NORTH);
        
        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
        tabbedPane.setUI(new WorkbenchSideTopTabUi());
        tabbedPane.addTab("Metrics", wrapScroll(createContentMetrics(overlayManager)));
        tabbedPane.addTab("Hierarchy", wrapScroll(createContentHierarchy()));
        tabbedPane.addTab("Cancelation", wrapScroll(createContentCancelation()));
        tabbedPane.addTab("Software", wrapScroll(createContentSoftware()));
        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        
        return rootPanel;
    }

    private Component wrapScroll(Component content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    @SuppressWarnings("unchecked")
    private Component createContentMetrics(AnalysisAlignMetricOverlayManager overlayManager) {
        metricsRootPanel = new JPanel();
        metricsCardManager = new CardLayout();
        metricsRootPanel.setLayout(metricsCardManager);
        
        // Approx card
        {
            JPanel panel = new JPanel();
            metricsRootPanel.add(panel, MetricCardApprox);

            labelMetricApproxAbsFreq = UiFactory.createLabelTopLeft("");
            labelMetricApproxCaseFreq = UiFactory.createLabelTopLeft("");

            JButton btnEnableAlign = new JButton("Enable Alignments");
            btnEnableAlign.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    baseView.SignalInputUseAlignAlg.dispatch(AnalysisAlgorithm.Align);
                }
            });
            
            panel.setLayout(new BorderLayout());
            panel.add(UiFactory.createLeftRightWrap(
                Pair.<Component, Component>of(
                    new JLabel("Abs. Freq.:", GfxIcons.IconOptStat.getImageIcon(""), JLabel.LEFT), 
                    labelMetricApproxAbsFreq),
                Pair.<Component, Component>of(
                    new JLabel("Case Freq.:", GfxIcons.IconOptStat.getImageIcon(""), JLabel.LEFT), 
                    labelMetricApproxCaseFreq),
                Pair.<Component, Component>of(null, new JLabel(" ")),
                Pair.<Component, Component>of(null, UiFactory.leftJustify(new JLabel(
                          "<html>"
                        + "Note: The above metrics are approximations. <br>"
                        + "Enable alignments to get more metrics with <br>"
                        + "a higher reliability."
                        + "</html>"))),
                Pair.<Component, Component>of(null, new JLabel(" ")),
                Pair.<Component, Component>of(null, UiFactory.leftJustify(btnEnableAlign))
            ), BorderLayout.NORTH);
        }

        // Align card
        {
            JPanel panel = new JPanel();
            metricsRootPanel.add(panel, MetricCardAlign);

            metrics = new THashSet<>();
            metricLabels = new THashMap<>();
            metricValues = new THashMap<>();
            
            List<Pair<Component, Component>> uiComps = new ArrayList<>();
            for (AnalysisAlignMetricOverlay overlay : overlayManager.getOverlays()) {
                IMetric metric = overlay.getPrimaryMetric();
                MetricId metricKey = metric.getId();

                JLabel label = new JLabel(overlay.getIcon().getImageIcon(""));
                JLabel value = new JLabel();
                
                uiComps.add(Pair.of(
                    (Component) UiFactory.leftJustify(label), 
                    (Component) UiFactory.leftJustify(value)));
                metrics.add(metricKey);
                metricLabels.put(metricKey, label);
                metricValues.put(metricKey, value);
            }
            
            panel.setLayout(new BorderLayout());
            panel.add(UiFactory.createLeftRightWrap(
                uiComps.toArray(new Pair[uiComps.size()])
            ), BorderLayout.NORTH);
        }

        metricsCardManager.show(metricsRootPanel, MetricCardApprox);
        return metricsRootPanel;
    }

    private Component createContentHierarchy() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(UiFactory.createLabelTopLeft("<html><h4>Hierarchy Path / Call Stack:</h4></html>"), BorderLayout.NORTH);
        
        labelHierarchy = UiFactory.createLabelTopLeft("");
        panel.add(labelHierarchy, BorderLayout.CENTER);
        
        return panel;
    }

    private Component createContentCancelation() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(UiFactory.createLabelTopLeft("<html><h4>Cancelation triggers:</h4></html>"), BorderLayout.NORTH);
        
        labelCancelation = UiFactory.createLabelTopLeft("");
        panel.add(labelCancelation, BorderLayout.CENTER);
        
        return panel;
    }

    private Component createContentSoftware() {
        
        labelSwJoinpoint = UiFactory.createLabelTopLeft("");
        labelSwFilename = UiFactory.createLabelTopLeft("");
        labelSwLinenr = UiFactory.createLabelTopLeft("");
        
        labelSwPackage = UiFactory.createLabelTopLeft("");
        labelSwClass = UiFactory.createLabelTopLeft("");
        labelSwMethod = UiFactory.createLabelTopLeft("");
        labelSwIsConstructor = UiFactory.createLabelTopLeft("");
        
        JButton btnSAW = new JButton("Open in Eclipse SAW");
        btnSAW.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentNode != null) {
                    baseView.SignalClickInspectNode.dispatch(currentNode.getId());
                }
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(UiFactory.createKeyValWrap(
            Pair.<String, Component>of("Joinpoint:", labelSwJoinpoint),
            Pair.<String, Component>of("File:", labelSwFilename),
            Pair.<String, Component>of("Line number:", labelSwLinenr),
            Pair.<String, Component>of(null, new JLabel(" ")),
            Pair.<String, Component>of("Package:", labelSwPackage),
            Pair.<String, Component>of("Class:", labelSwClass),
            Pair.<String, Component>of("Method:", labelSwMethod),
            Pair.<String, Component>of("Constructor:", labelSwIsConstructor),
            Pair.<String, Component>of(null, new JLabel(" ")),
            Pair.<String, Component>of(null, UiFactory.leftJustify(btnSAW))
        ), BorderLayout.NORTH);
        return panel;
    }


/*
    // window size
    private static final int WIDTH = 350;
    private static final int HEIGHT = 200;

//    private static final int HierarchyHeight = 80;
//    private static final int HierarchyWidth = WIDTH - InfoRootPanel.MARGIN * 2 - 20;
    
    // background color
    // borrowed color from NavigableSVGPanel.drawHelperControls()
    public static final Color COLOR = new Color(80, 80, 80);
    
    // font color
    public static final Color FONTCOLOR = Color.white;
    
    private JDialog popupWindow;
    
//    private List<Component> focusComponents;
    
    private JLabel labelName;
    private JLabel labelHierarchy;
    private JLabel labelAbsoluteFrequency;
    private JLabel labelCaseFrequency;
    private JLabel labelSourceLocation;

    public NodeInfoPopup() {
        popupWindow = new JDialog();
        popupWindow.setUndecorated(true);
        popupWindow.setAlwaysOnTop(true);
//        popupWindow.setBackground(Color.blue);
//        popupWindow.getContentPane().setBackground(Color.blue);
        
        InfoRootPanel rootPanel = new InfoRootPanel(COLOR);
        popupWindow.setLayout(new BorderLayout());
        popupWindow.add(rootPanel, BorderLayout.CENTER);
//        focusComponents.add(popupWindow);
        
        JPanel contentPane = new JPanel();
        contentPane.setBackground(COLOR);
        contentPane.setBorder(BorderFactory.createEmptyBorder());
        
//        JScrollPane scrollPane = new JScrollPane(contentPane,
//                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, 
//                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//        scrollPane.setBackground(COLOR);
//        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        rootPanel.setLayout(new BorderLayout());
//      rootPanel.add(scrollPane, BorderLayout.CENTER);
        rootPanel.add(contentPane, BorderLayout.CENTER);
        
        contentPane.setLayout(new BorderLayout());

        JPanel contentTopPane = new JPanel();
        contentTopPane.setBackground(COLOR);
        contentTopPane.setBorder(BorderFactory.createEmptyBorder());
        contentTopPane.setLayout(new BoxLayout(contentTopPane, BoxLayout.Y_AXIS));
        contentPane.add(contentTopPane, BorderLayout.NORTH);
        
        createTitle(contentTopPane);
        contentTopPane.add(Box.createVerticalStrut(10));
        createMetrics(contentTopPane);
//        contentTopPane.add(Box.createVerticalStrut(2));
//        createSoftware(contentTopPane);
        contentTopPane.add(Box.createVerticalStrut(2));
        contentPane.add(createHierarchy(), BorderLayout.CENTER);
        
        popupWindow.pack();
        popupWindow.setBounds(0, 0, WIDTH, HEIGHT);
    }

    private void createTitle(JPanel parent) {
        labelName = createJLabel();
        labelName.setFont(labelName.getFont().deriveFont(Font.BOLD, 14));
        labelName.setHorizontalAlignment(SwingConstants.CENTER);
        parent.add(UiFactory.leftJustify(labelName));
    }

    private void createMetrics(JPanel parent) {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR);
        panel.setLayout(new SpringLayout());
        parent.add(UiFactory.leftJustify(panel));
        
        panel.add(createJLabel("Absolute frequency: "));
        labelAbsoluteFrequency = createJLabel("<unkown>");
        panel.add(labelAbsoluteFrequency);
        
        panel.add(createJLabel("Case frequency: "));
        labelCaseFrequency = createJLabel("<unkown>");
        panel.add(labelCaseFrequency);

        panel.add(createJLabel("Source location: "));
        labelSourceLocation = createJLabel("<unkown>");
        panel.add(labelSourceLocation);

        SpringUtilities.makeCompactGrid(panel, 3, 2, 0, 0, 6, 1);
    }

//    private void createSoftware(JPanel parent) {
//        JPanel panel = new JPanel();
//        panel.setBackground(COLOR);
//        panel.setLayout(new SpringLayout());
//        parent.add(UiFactory.leftJustify(panel));
//        
//        panel.add(createJLabel("Source location: "));
//        labelSourceLocation = createJLabel("<unkown>");
//        panel.add(labelSourceLocation);
//      
//        SpringUtilities.makeCompactGrid(panel, 1, 2, 0, 0, 6, 1);
//    }
    
    private JPanel createHierarchy() {
        JPanel contentPane = new JPanel();
        contentPane.setBackground(COLOR);
        contentPane.setLayout(new BorderLayout());
        contentPane.add(
            UiFactory.leftJustify(createJLabel("Hierarchy / call stack:")),
            BorderLayout.NORTH);
        
        labelHierarchy = createJLabel();
        labelHierarchy.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelHierarchy.setHorizontalAlignment(JLabel.LEFT);
        
        JPanel wrapper = new JPanel();
        wrapper.setBackground(COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder());
        wrapper.setLayout(new BorderLayout());
        wrapper.add(labelHierarchy, BorderLayout.CENTER);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBackground(COLOR);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPane.add(scrollPane, BorderLayout.CENTER);
//        focusComponents.add(scrollPane);
        
        return contentPane;
    }
    
    private JLabel createJLabel(String text) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(JLabel.LEFT);
        label.setVerticalAlignment(JLabel.TOP);
        label.setBackground(COLOR);
        label.setForeground(FONTCOLOR);
        return label;
    }

    private JLabel createJLabel() {
        return createJLabel("");
    }

    public void hide() {
        popupWindow.setVisible(false);
    }
    
    public JDialog getPopupWindow() {
        return popupWindow;
    }
    
    public void viewAtLocation(int x, int y) {
        popupWindow.setLocation(x, y);
        popupWindow.setVisible(true);
    }

    public double getWidth() {
        return WIDTH;
    }

    public double getHeight() {
        return HEIGHT;
    }
    
    public void setData(IEPTree tree, IEPTreeNode node, IActivityLabeler dataActivityLabeler) {
        setDataTitle(tree, node, dataActivityLabeler);
        setDataMetrics(tree, node, dataActivityLabeler);
        setDataSoftware(tree, node, dataActivityLabeler);
        setDataHierarachy(tree, node, dataActivityLabeler);
        
        popupWindow.validate();
    }

    private void setDataTitle(IEPTree tree, IEPTreeNode node,
            IActivityLabeler dataActivityLabeler) {
        String label = dataActivityLabeler.getLabel(node);
        labelName.setText(label);
    }

    private void setDataMetrics(IEPTree tree, IEPTreeNode node,
            IActivityLabeler dataActivityLabeler) {
        labelAbsoluteFrequency.setText("<unkown>");
        labelCaseFrequency.setText("<unkown>");
        
        EPTreeFreqMetricDecorator decorator = tree.getDecorations()
                .getForType(EPTreeFreqMetricDecorator.class);
        if (decorator != null) {
            FreqMetric freqDec = decorator.getDecoration(node);
            if (freqDec != null) {
                labelAbsoluteFrequency.setText(
                    String.format("%d", freqDec.getFreqAbsolute()));
                labelCaseFrequency.setText(
                    String.format("%d", freqDec.getFreqCase()));
            }
        }
    }

    private void setDataSoftware(IEPTree tree, IEPTreeNode node,
            IActivityLabeler dataActivityLabeler) {
        labelSourceLocation.setText("<unkown>");
        
        EPTreeSwAppDecorator decorator = tree.getDecorations()
                .getForType(EPTreeSwAppDecorator.class);
        if (decorator != null) {
            SwAppDecoration swDec = decorator.getDecoration(node);
            if (swDec != null) {
                labelSourceLocation.setText(
                    String.format("%s @ %d", 
                            swDec.getFilename(), 
                            swDec.getLinenr()));
            }
        }
    }

    private void setDataHierarachy(IEPTree tree, IEPTreeNode node,
            IActivityLabeler dataActivityLabeler) {
        List<String> labels = EPTreeUtil.getHierarchyPath(node, dataActivityLabeler);
        Collections.reverse(labels);
        
        StringBuilder bld = new StringBuilder();
        bld.append("<html>");
        for (String label : labels) {
            bld.append(" - ");
            bld.append(label);
            bld.append("<br>");
        }
        bld.append("</html>");
        labelHierarchy.setText(bld.toString());
    }
*/
}
