package org.processmining.algorithms.statechart.m2m.reduct.eptree;

import org.processmining.models.statechart.decorate.staticmetric.EPTreeFreqMetricDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.eptree.EPNodeType;

public class RuleReduceSingleChild implements IReductionRule {

    @Override
    public boolean reduce(IEPTree tree, IEPTreeNode node) {
        EPNodeType nodeOp = node.getNodeType();
        EPTreeFreqMetricDecorator dec = tree.getDecorations().getForType(
                EPTreeFreqMetricDecorator.class);

        if (nodeOp == EPNodeType.Seq || nodeOp == EPNodeType.Choice
                || nodeOp == EPNodeType.AndComposite) {
            // op(A) --> A
            if (node.getChildren().size() == 1) {
                IEPTreeNode child = node.getChildren().get(0);
                IEPTreeNode parent = node.getParent();
                
                if (parent == null) {
                    tree.setRoot(child);
                    child.setParent(null);
                } else {
                    int index = parent.getChildren().indexOf(node);
                    parent.replaceNode(index, child);
                    child.setParent(parent);
                }
                dec.removeDecoration(node);
                return true;
            }
        }

        return false;
    }

}
