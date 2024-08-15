package org.processmining.ui.statechart.workbench.discovery.vis.info;

import gnu.trove.set.hash.THashSet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.processmining.models.statechart.align.XAlignedTreeLog;
import org.processmining.models.statechart.decorate.staticmetric.SCComplexityMetric;
import org.processmining.models.statechart.eptree.EPNodeType;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.ui.statechart.color.ColorSets;
import org.processmining.ui.statechart.gfx.GfxIcons;
import org.processmining.ui.statechart.workbench.common.PieChartLabel;
import org.processmining.ui.statechart.workbench.discovery.DiscoveryWorkbenchController;
import org.processmining.ui.statechart.workbench.discovery.ISubview;
import org.processmining.ui.statechart.workbench.util.UiFactory;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.ui.FilteredTreeModel;
import org.processmining.utils.statechart.ui.ReadonlyTableModel;
import org.processmining.utils.statechart.ui.SearchFilterAdapter;

import com.google.common.base.Predicate;

public class SubviewSearchtree implements ISubview {
    
    private static enum MetricsTable {
        NoActivities("No. activities"), 
        Depth("Hierarchy depth"), 
        NoSimpleStates("No. simples states"), 
        NoCompositeStates("No. composite states"), 
        NoTransitions("No. transitions"), 
        CyclomaticComplexity("Cyclomatic complexity");//,
//        AlignFitness("Fitness (alignment-based)"),
//        AlignPrecision("Precision (alignment-based)");

        private final String header;

        private MetricsTable(String header) {
            this.header = header;
        }

        public String getHeaderTitle() {
            return header;
        }
    }
    
    private DiscoveryWorkbenchController.View baseView;
    
    private JPanel searchPane;
    
    private DefaultTableModel tableModel;
    private JTextField searchTextbox;
    private JTree searchTree;
    private DefaultMutableTreeNode searchTreeRoot;
    private boolean isUpdatingSearchTree;
    private FilteredTreeModel<DefaultMutableTreeNode> searchTreeModel;

    private PieChartLabel metricFitness;

    private PieChartLabel metricPrecision;

//    private IEPTree dataTree;
//    private IActivityLabeler dataActivityLabeler;

    public SubviewSearchtree(DiscoveryWorkbenchController.View baseView) {
        this.baseView = baseView;
        
        searchPane = new JPanel();
        searchPane.setLayout(new BorderLayout());
        _createSearchTree(searchPane);
        _createSCMetricInfo(searchPane, false);
    }

    @Override
    public JComponent getRootComponent() {
        return searchPane;
    }
    
    public void displayDiscovering() {
        for (MetricsTable header : MetricsTable.values()) {
            tableModel.setValueAt("...", header.ordinal(), 1);
        }

        metricFitness.setValue(Double.NaN);
        metricPrecision.setValue(Double.NaN);
        
        searchTreeRoot.removeAllChildren();
        _reloadTreeModel();
    }

    public void setStatechartMetric(SCComplexityMetric metric) {
        tableModel.setValueAt(metric.getNoActivities(),
                MetricsTable.NoActivities.ordinal(), 1);
        tableModel.setValueAt(metric.getDepth(), MetricsTable.Depth.ordinal(),
                1);
        tableModel.setValueAt(metric.getNoSimpleStates(),
                MetricsTable.NoSimpleStates.ordinal(), 1);
        tableModel.setValueAt(metric.getNoCompositeStates(),
                MetricsTable.NoCompositeStates.ordinal(), 1);
        tableModel.setValueAt(metric.getNoTransitions(),
                MetricsTable.NoTransitions.ordinal(), 1);
        tableModel.setValueAt(metric.getCyclomaticComplexity(),
                MetricsTable.CyclomaticComplexity.ordinal(), 1);
    }

    public void setAlignMetrics(XAlignedTreeLog alignedLog) {
//        tableModel.setValueAt(alignedLog.getAverageFitness(),
//                MetricsTable.AlignFitness.ordinal(), 1);
//        tableModel.setValueAt(alignedLog.getAveragePrecision(),
//                MetricsTable.AlignPrecision.ordinal(), 1);
        metricFitness.setValue(alignedLog.getAverageFitness() * 100.0);
        metricPrecision.setValue(alignedLog.getAveragePrecision() * 100.0);
    }
    
    public void setEPTree(IEPTree tree,
            IActivityLabeler activityLabeler) {
        isUpdatingSearchTree = true;
//        this.dataTree = tree;
//        this.dataActivityLabeler = activityLabeler;

        // build model
        searchTreeRoot.removeAllChildren();
        _buildTreeModel(tree.getRoot(), searchTreeRoot, activityLabeler);

        _reloadTreeModel();

        isUpdatingSearchTree = false;
    }
    
    public void setSelectedNodes(Set<String> selectedNodes) {
        isUpdatingSearchTree = true;

        TreePath[] selectionPaths = new TreePath[selectedNodes.size()];
        int i = 0;

        @SuppressWarnings("unchecked")
        Enumeration<?> enu = searchTreeRoot
                .depthFirstEnumeration();
        while (enu.hasMoreElements()) {
            Object viewNode = enu.nextElement();
            if (viewNode instanceof EPTreeJTreeNode) {
                IEPTreeNode treeNode = ((EPTreeJTreeNode) viewNode)
                        .getEPTreeNode();

                if (treeNode != null && selectedNodes.contains(treeNode.getId())) {
                    selectionPaths[i] = new TreePath(((EPTreeJTreeNode) viewNode).getPath());
                    i++;
                }
            }
        }

        searchTree.setSelectionPaths(selectionPaths);

        isUpdatingSearchTree = false;
    }
    
    private void _createSearchTree(JPanel container) {
        searchTreeRoot = new DefaultMutableTreeNode("Root");
        searchTreeModel = new FilteredTreeModel<DefaultMutableTreeNode>(
                new DefaultTreeModel(searchTreeRoot));
        searchTree = new JTree(searchTreeModel);
        searchTree.setRootVisible(false);
        searchTree.setShowsRootHandles(true);
        searchTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event)
                    throws ExpandVetoException {
                _searchTreeChangeTriggered(event.getPath());
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event)
                    throws ExpandVetoException {
                _searchTreeChangeTriggered(event.getPath());
            }
        });
        searchTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                _searchTreeSelectionTriggered(searchTree.getSelectionPaths());
            }
        });
        searchTreeModel.installRenderer(searchTree);

        JScrollPane searchScrollPane = new JScrollPane(searchTree);
        container.add(searchScrollPane, BorderLayout.CENTER);

        searchTextbox = new JTextField();
        Border outsideBorder = BorderFactory.createLineBorder(Color.black, 1);
        Border insideBorder = BorderFactory.createMatteBorder(0, 17, 0, 0,
                GfxIcons.IconSearch.getImageIcon(""));
        Border border = BorderFactory.createCompoundBorder(outsideBorder,
                insideBorder);
        searchTextbox.setBorder(border);

        // link searchbox input to tree filter
        SearchFilterAdapter.installChangeFilter(searchTextbox, new Action1<Predicate<String>>() {
            @Override
            public void call(final Predicate<String> filter) {
                if (filter != null) {
                    searchTreeModel.setFilter(new Predicate<DefaultMutableTreeNode>() {
                        @Override
                        public boolean apply(@Nullable DefaultMutableTreeNode input) {
                            if (input instanceof EPTreeJTreeNode) {
                                String label = input.toString();
                                return filter.apply(label);
                            } else {
                                return true;
                            }
                        }
                    });
                } else {
                    searchTreeModel.setFilter(null);
                }
                _reloadTreeModel();
            }
        });
        container.add(searchTextbox, BorderLayout.NORTH);
    }

    private void _createSCMetricInfo(JPanel sidebar, boolean collapsed) {
        JXTaskPaneContainer container = new JXTaskPaneContainer();
        container.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        container.setBackground(sidebar.getBackground());
        sidebar.add(container, BorderLayout.SOUTH);

        JXTaskPane wrap = new JXTaskPane();
        wrap.setTitle("Metrics");
        container.add(wrap);
        wrap.setCollapsed(collapsed);

        Container content = wrap.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        
        JPanel alignMetricWrap = new JPanel();
        alignMetricWrap.setLayout(new GridLayout(1, 2));
        metricFitness = new PieChartLabel("Fitness", 0.0, 100.0,
            ColorSets.NeutralGrey, ColorSets.RedOrangeGreenscale.deriveWithValueRanges(0.0, 75.0, 100.0));
        metricPrecision = new PieChartLabel("Precision", 0.0, 100.0,
            ColorSets.NeutralGrey, ColorSets.RedOrangeGreenscale.deriveWithValueRanges(0.0, 75.0, 100.0));
        
        alignMetricWrap.add(UiFactory.leftJustify(metricFitness));
        alignMetricWrap.add(UiFactory.leftJustify(metricPrecision));
        content.add(UiFactory.leftJustify(alignMetricWrap));
        content.add(Box.createVerticalStrut(6));
        
        tableModel = new ReadonlyTableModel(MetricsTable.values().length, 2);
        for (MetricsTable header : MetricsTable.values()) {
            tableModel.setValueAt(header.getHeaderTitle(), header.ordinal(), 0);
        }

        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(1).setPreferredWidth(10);
        content.add(UiFactory.leftJustify(table));
    }
    

    private void _reloadTreeModel() {
        isUpdatingSearchTree = true;

        DefaultTreeModel model = (DefaultTreeModel) searchTreeModel
                .getTreeModel();
        model.reload();

        // update expand/collapse
        @SuppressWarnings("unchecked")
        Enumeration<?> enu = searchTreeRoot
                .depthFirstEnumeration();
        while (enu.hasMoreElements()) {
            Object viewNode = enu.nextElement();
            if (viewNode instanceof EPTreeJTreeNode) {
                IEPTreeNode treeNode = ((EPTreeJTreeNode) viewNode)
                        .getEPTreeNode();

                if (treeNode != null
                        && treeNode.getNodeType() == EPNodeType.Collapsed) {
                    searchTree.collapsePath(new TreePath(((EPTreeJTreeNode) viewNode).getPath()));
                } else {
                    searchTree.expandPath(new TreePath(((EPTreeJTreeNode) viewNode).getPath()));
                }
            }
        }

        isUpdatingSearchTree = false;
    }

    private void _buildTreeModel(IEPTreeNode node,
            DefaultMutableTreeNode parent,
            IActivityLabeler activityLabeler) {
        EPNodeType type = node.getNodeType();
        if (type.isLabelled()) {
            EPTreeJTreeNode viewNode = new EPTreeJTreeNode(node, activityLabeler);
            viewNode.setUserObject(node);
            parent.add(viewNode);

            if (type == EPNodeType.Collapsed) {
                viewNode.add(new DefaultMutableTreeNode("Loading...", false));
            }

            parent = viewNode;
        }

        List<IEPTreeNode> children = node.getChildren();
        final int size = children.size();
        for (int i = 0; i < size; i++) {
            _buildTreeModel(children.get(i), parent, activityLabeler);
        }
    }

    protected void _searchTreeChangeTriggered(TreePath path) {
        if (isUpdatingSearchTree) {
            return;
        }

        DefaultMutableTreeNode viewNode = (DefaultMutableTreeNode) path
                .getLastPathComponent();
        if (viewNode instanceof EPTreeJTreeNode) {
            IEPTreeNode treeNode = ((EPTreeJTreeNode) viewNode)
                    .getEPTreeNode();
            baseView.SignalClickCollapsibleNode.dispatch(treeNode.getId());
        }
    }

    protected void _searchTreeSelectionTriggered(TreePath[] selectionPaths) {
        if (isUpdatingSearchTree) {
            return;
        }

        Set<String> selectedNodes = new THashSet<String>();
        if (selectionPaths != null) {
            for (TreePath path : selectionPaths) {
                DefaultMutableTreeNode viewNode = (DefaultMutableTreeNode) path
                        .getLastPathComponent();
                if (viewNode instanceof EPTreeJTreeNode) {
                    IEPTreeNode treeNode = ((EPTreeJTreeNode) viewNode)
                            .getEPTreeNode();
                    selectedNodes.add(treeNode.getId());
                }

            }
        }

        baseView.SignalSelectedNodes.dispatch(selectedNodes);
    }

}
