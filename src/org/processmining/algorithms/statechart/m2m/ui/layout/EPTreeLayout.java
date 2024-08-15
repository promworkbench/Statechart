package org.processmining.algorithms.statechart.m2m.ui.layout;

import org.abego.treelayout.Configuration;
import org.abego.treelayout.NodeExtentProvider;
import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.processmining.algorithms.statechart.m2m.ui.style.EPTreeStyle;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

import com.google.common.base.Function;

public class EPTreeLayout implements Function<IEPTree, TreeLayout<IEPTreeNode>> {
    
    private EPTreeStyle style;

    public EPTreeLayout(EPTreeStyle style) {
        this.style = style;
    }

    @Override
    public TreeLayout<IEPTreeNode> apply(IEPTree input) {
        return calculate(input);
    }
    
    public TreeLayout<IEPTreeNode> calculate(IEPTree inputTree) {
        
        TreeForTreeLayout<IEPTreeNode> tree = new EPTreeLayoutAdapter(inputTree);
        NodeExtentProvider<IEPTreeNode> nodeExtentProvider = style;
        Configuration<IEPTreeNode> configuration = new DefaultConfiguration<>(
                style.getGapBetweenLevels(), style.getGapBetweenNodes(), style.getLayoutDir());
        boolean useIdentity = true;
        
        return new TreeLayout<>(tree, nodeExtentProvider,
                configuration, useIdentity);
    }
}
