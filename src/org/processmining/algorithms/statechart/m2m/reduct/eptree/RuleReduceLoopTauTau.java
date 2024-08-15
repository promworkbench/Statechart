package org.processmining.algorithms.statechart.m2m.reduct.eptree;

import org.processmining.models.statechart.decorate.staticmetric.EPTreeFreqMetricDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.eptree.EPNodeType;

public class RuleReduceLoopTauTau implements IReductionRule {

    @Override
    public boolean reduce(IEPTree tree, IEPTreeNode node) {
        EPNodeType nodeOp = node.getNodeType();
        EPTreeFreqMetricDecorator dec = tree.getDecorations().getForType(
                EPTreeFreqMetricDecorator.class);

        if (nodeOp == EPNodeType.Loop) {
            // loop(tau, tau) --> tau
            IEPTreeNode body = node.getChildren().get(0);
            IEPTreeNode redo = node.getChildren().get(1);
            if (body.getNodeType() == EPNodeType.Silent
                    && redo.getNodeType() == EPNodeType.Silent) {
                IEPTreeNode parent = node.getParent();

                if (parent == null) {
                    tree.setRoot(body);
                    body.setParent(null);
                } else {
                    int index = parent.getChildren().indexOf(node);
                    parent.replaceNode(index, body);
                    body.setParent(parent);
                }
                dec.removeDecoration(node);
                dec.removeDecoration(redo); // TODO body --
                return true;
            }
        }

        return false;
    }

}
