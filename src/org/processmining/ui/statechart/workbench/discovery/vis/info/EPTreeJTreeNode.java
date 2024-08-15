package org.processmining.ui.statechart.workbench.discovery.vis.info;

import javax.swing.tree.DefaultMutableTreeNode;

import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.labeling.IActivityLabeler;

public class EPTreeJTreeNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = -1105129961597837321L;
    
    private IActivityLabeler activityLabeler;

    public EPTreeJTreeNode(IEPTreeNode node, IActivityLabeler activityLabeler) {
        super(node);
        this.activityLabeler = activityLabeler;
    }

    @Override
    public String toString() {
        return activityLabeler.getLabel(getEPTreeNode());
    }

    public IEPTreeNode getEPTreeNode() {
        return (IEPTreeNode) getUserObject();
    }
}
