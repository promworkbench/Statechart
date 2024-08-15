package org.processmining.algorithms.statechart.m2m.reduct.eptree;

import org.processmining.models.statechart.decorate.staticmetric.EPTreeFreqMetricDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.eptree.EPNodeType;

public class RuleRemoveTauChild implements IReductionRule {

    @Override
    public boolean reduce(IEPTree tree, IEPTreeNode node) {
        EPNodeType nodeOp = node.getNodeType();
        EPTreeFreqMetricDecorator dec = tree.getDecorations().getForType(
                EPTreeFreqMetricDecorator.class);

        if (nodeOp == EPNodeType.Seq || nodeOp == EPNodeType.AndComposite) {
            // op(A, ..., tau) --> op(A, ...)
            if (node.getChildren().size() > 1) {
                for (IEPTreeNode child : node.getChildren()) {
                    if (child.getNodeType() == EPNodeType.Silent) {
                        node.removeNode(child);
                        child.setParent(null);
                        dec.removeDecoration(child);
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
