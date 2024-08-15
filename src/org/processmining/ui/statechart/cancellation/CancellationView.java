package org.processmining.ui.statechart.cancellation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.processmining.algorithms.statechart.align.FitnessPrecision;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.ui.statechart.svg.SvgIcons;
import org.processmining.ui.statechart.workbench.common.TwoUpListSelectionUi;
import org.processmining.ui.statechart.workbench.util.UiFactory;
import org.processmining.utils.statechart.generic.CompareComparator;
import org.processmining.utils.statechart.svg.SVGCollection;
import org.processmining.utils.statechart.ui.ReadonlyTableModel;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.google.common.base.Function;
import com.jidesoft.swing.RangeSlider;
import com.kitfox.svg.SVGDiagram;

public class CancellationView extends CancellationController.View {

    private static final int ErrorListVisibleRows = 5;
    private static final Dimension PreferredListDim = new Dimension(
        200, 300
    );
    
    private static final String CardMain = "CardMain";
    private static final String CardWait = "CardWait";

    public static final int WaitDisplayDelayDefault = 60;

    private final JPanel panelRoot;
    private final JPanel panelCenter;

    private CardLayout cardManager;
    Timer waitDisplayTimer;
    
    private NavigableSVGPanel svgSC;
    private NavigableSVGPanel svgPTnet;
    private JSlider inputSliderPaths;
    
    private Map<GraphDirection, JRadioButton> mapDir2Btn = new LinkedHashMap<>();
    private ReadonlyTableModel tableModel;
    private TwoUpListSelectionUi<String> inputSelectCalcellation;

    private static enum MetricsTable {
        Fitness("Fitness"), 
        Precision("Precision");

        private final String header;

        private MetricsTable(String header) {
            this.header = header;
        }

        public String getHeaderTitle() {
            return header;
        }
    }
    
    
    public CancellationView() {
        panelRoot = new JPanel();
        panelRoot.setLayout(new BorderLayout());

        // Center
        panelCenter = new JPanel();
        cardManager = new CardLayout();
        panelCenter.setLayout(cardManager);

        JPanel panelWait = new JPanel();
        panelWait.setLayout(new BorderLayout());
        panelWait.add(new JLabel("<html><h2>Discovering model...</h2></html>",
                SwingConstants.CENTER), BorderLayout.CENTER);
        panelCenter.add(panelWait, CardWait);

        waitDisplayTimer = new Timer(WaitDisplayDelayDefault,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent paramActionEvent) {
                        cardManager.show(panelCenter, CardWait);
                    }
                });
        waitDisplayTimer.setRepeats(false);

        JPanel subviewModel = new JPanel();
        _buildSubviewModel(subviewModel);
        panelCenter.add(subviewModel, CardMain);
        
        cardManager.show(panelCenter, CardWait);
        
        panelRoot.add(panelCenter, BorderLayout.CENTER);
        
        // Sidebar Right
        JPanel subviewSettingsPanel = new JPanel();
        _buildSubviewSettings(subviewSettingsPanel);
        panelRoot.add(subviewSettingsPanel, BorderLayout.EAST);
        
    }
    
    private void _buildSubviewSettings(JPanel sidebarWrap) {
//        JPanel sidebarWrap = new JPanel();
        sidebarWrap.setLayout(new BorderLayout());

        JPanel sidebarMain = new JPanel();
        sidebarMain.setLayout(new GridLayout(1, 3));
        _createSidebarSliders(sidebarMain);
        sidebarWrap.add(sidebarMain, BorderLayout.CENTER);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(UiFactory.createPaddingBorder());
        sidebarWrap.add(sidebar, BorderLayout.SOUTH);

        JXTaskPaneContainer container = new JXTaskPaneContainer();
        container.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        container.setBackground(sidebar.getBackground());
        sidebar.add(container);

        _createRenderControls(container, false);
        _createErrorControls(container, false);
        _createMetrics(container, false);
    }

    private void _createErrorControls(JPanel container, boolean collapsed) {
        JXTaskPane wrap = new JXTaskPane();
        wrap.setTitle("Error options");
        container.add(wrap);
        wrap.setCollapsed(collapsed);
        
        Container opts = wrap.getContentPane();
        opts.setLayout(new BoxLayout(opts, BoxLayout.Y_AXIS));
        
        inputSelectCalcellation = new TwoUpListSelectionUi<String>(
                "Cancel triggers:", false, ErrorListVisibleRows, PreferredListDim);
        opts.add(inputSelectCalcellation.getComponent());

        SlickerFactory f = SlickerFactory.instance();
        JButton btnApply = f.createButton("Apply");
        opts.add(btnApply);
        btnApply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SignalInputErrors.dispatch(inputSelectCalcellation.getSelected());
            }
        });
    }

    
    private void _createRenderControls(JPanel container, boolean collapsed) {
        JXTaskPane wrap = new JXTaskPane();
        wrap.setTitle("Rendering & Layout");
        container.add(wrap);
        wrap.setCollapsed(collapsed);
        
        Container opts = wrap.getContentPane();
        opts.setLayout(new BoxLayout(opts, BoxLayout.Y_AXIS));
        
        JPanel dirOpts = new JPanel();
        dirOpts.setLayout(new GridLayout(1, 2, 0, 0));
        opts.add(dirOpts);
        
        SlickerFactory f = SlickerFactory.instance();
        mapDir2Btn.put(GraphDirection.topDown,
                f.createRadioButton("Top - Bottom"));
        mapDir2Btn.put(GraphDirection.leftRight,
                f.createRadioButton("Left - Right"));
        // mapDir2Btn.put(GraphDirection.bottomTop,
        // f.createRadioButton("Bottom - Top"));
        // mapDir2Btn.put(GraphDirection.rightLeft,
        // f.createRadioButton("Right - Left"));

        ButtonGroup group = new ButtonGroup();
        ActionListener lst = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (GraphDirection key : mapDir2Btn.keySet()) {
                    if (mapDir2Btn.get(key).isSelected()) {
                        SignalInputDirection.dispatch(key);
                    }
                }
            }
        };
        
        for (JRadioButton btn : mapDir2Btn.values()) {
            group.add(btn);
            btn.addActionListener(lst);
            dirOpts.add(btn);
        }

//        chkRecurseArrow = SlickerFactory.instance().createCheckBox(
//                "Show Recursion Back-arrow", false);
//        chkRecurseArrow.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                baseView.SignalInputUseRecurseArrow.dispatch(chkRecurseArrow.isSelected());
//            }
//        });
//        wrap.add(UiFactory.leftJustify(chkRecurseArrow));
    }

    private void _createSidebarSliders(JPanel sidebarMain) {
//        inputSliderActivities = new JSlider(JSlider.VERTICAL, 0, 100, 100);
//        inputSliderActivities.addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                double value = (double) inputSliderActivities.getValue() / 100.0;
//                baseView.SignalInputActivityThreshold.dispatch(value);
//            }
//        });

        inputSliderPaths = new JSlider(JSlider.VERTICAL, 0, 100, 100);
        inputSliderPaths.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double value = (double) inputSliderPaths.getValue() / 100.0;
                SignalInputPathThreshold.dispatch(value);
            }
        });

//        inputSliderDepth = new RangeSlider(0, 100);
//        inputSliderDepth.setOrientation(JSlider.VERTICAL);
//        inputSliderDepth.setValue(0);
//        inputSliderDepth.setUpperValue(100);
//        inputSliderDepth.addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                double min = (double) inputSliderDepth.getValue() / 100.0;
//                double max = (double) inputSliderDepth.getUpperValue() / 100.0;
//                baseView.SignalInputDepthThreshold.dispatch(min, max);
//            }
//        });

//        sidebarMain.add(_wrapSlider(inputSliderActivities, "Activities"));
        sidebarMain.add(_wrapSlider(inputSliderPaths, "Paths"));
//        sidebarMain.add(_wrapSlider(inputSliderDepth, "Level Depth"));
    }

    private Component _wrapSlider(final JSlider slider, String title) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BorderLayout());

        JLabel labelTitle = new JLabel(title, SwingConstants.CENTER);

        final JLabel labelValue = new JLabel("", SwingConstants.CENTER);
        _updateLabel(slider, labelValue);

        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                _updateLabel(slider, labelValue);
            }
        });

        wrap.add(labelTitle, BorderLayout.NORTH);
        wrap.add(slider, BorderLayout.CENTER);
        wrap.add(labelValue, BorderLayout.SOUTH);

        return wrap;
    }

    protected void _updateLabel(JSlider slider, JLabel labelValue) {
        if (slider instanceof RangeSlider) {
            labelValue.setText(String.format("%d %% - %d %%",
                    ((RangeSlider) slider).getLowValue(), ((RangeSlider) slider).getHighValue()));
        } else {
            labelValue.setText(String.format("%d %%", slider.getValue()));
        }
    }

    private void _createMetrics(JPanel sidebar, boolean collapsed) {
        JXTaskPaneContainer container = new JXTaskPaneContainer();
        container.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        container.setBackground(sidebar.getBackground());
        sidebar.add(container, BorderLayout.SOUTH);

        JXTaskPane wrap = new JXTaskPane();
        wrap.setTitle("Metrics");
        container.add(wrap);
        wrap.setCollapsed(collapsed);

        Container content = wrap.getContentPane();
        
        tableModel = new ReadonlyTableModel(MetricsTable.values().length, 2);
        for (MetricsTable header : MetricsTable.values()) {
            tableModel.setValueAt(header.getHeaderTitle(), header.ordinal(), 0);
        }

        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(1).setPreferredWidth(10);
        content.add(UiFactory.leftJustify(table));
    }
    
    private void _buildSubviewModel(JPanel container) {
        SVGCollection svgcol = new SVGCollection();
        SVGDiagram svg = svgcol.loadSVG(SvgIcons.Blank);
        
        svgSC = new NavigableSVGPanel(svg);
        svgPTnet = new NavigableSVGPanel(svg);
        
        container.setLayout(new GridLayout(2, 1));
        container.add(svgSC);
        container.add(svgPTnet);
    }

    @Override
    public JComponent getComponent() {
        return panelRoot;
    }

    @Override
    public void setInputDirection(GraphDirection dir) {
        mapDir2Btn.get(dir).setSelected(true);
    }

    @Override
    public void setPathThreshold(double threshold) {
        inputSliderPaths.setValue((int) (threshold * 100.0));
    }

    @Override
    public void setInputErrors(Set<String> values) {
        inputSelectCalcellation.setSelected(values, new Function<String, String>() {
            @Override
            public String apply(String arg0) {
                return arg0;
            }
        });
    }

    @Override
    public void setErrorOptions(Set<String> values) {
        inputSelectCalcellation.setOptions(values, new Function<String, String>() {
            @Override
            public String apply(String arg0) {
                return arg0;
            }
        }, new CompareComparator<String>());
    }

    @Override
    public void displayDiscovering() {
        waitDisplayTimer.start();
    }

    @Override
    public void displayModel(SVGDiagram imageSC, SVGDiagram imagePTnet,
            boolean resetView) {
        waitDisplayTimer.stop();
        svgSC.setImage(imageSC, resetView);
        svgPTnet.setImage(imagePTnet, resetView);
        cardManager.show(panelCenter, CardMain);
    }

    @Override
    public void setMetrics(FitnessPrecision metrics) {
        if (metrics == null) {
            tableModel.setValueAt("?",
                    MetricsTable.Fitness.ordinal(), 1);
            tableModel.setValueAt("?", MetricsTable.Precision.ordinal(),
                    1);
        } else {
            tableModel.setValueAt(metrics.getFitness(),
                    MetricsTable.Fitness.ordinal(), 1);
            tableModel.setValueAt(metrics.getPrecision(), MetricsTable.Precision.ordinal(),
                    1);
        }
    }

}
