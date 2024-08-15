package org.processmining.algorithms.statechart.m2m.reduct.eptree;

import java.util.List;

import org.processmining.models.statechart.decorate.staticmetric.EPTreeFreqMetricDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.eptree.EPNodeType;

public class RuleReduceSameOperator implements IReductionRule {

    @Override
    public boolean reduce(IEPTree tree, IEPTreeNode node) {
        EPNodeType nodeOp = node.getNodeType();
        EPTreeFreqMetricDecorator dec = tree.getDecorations().getForType(
                EPTreeFreqMetricDecorator.class);

        if (nodeOp == EPNodeType.Seq || nodeOp == EPNodeType.Choice
                || nodeOp == EPNodeType.AndComposite) {
            // op(A, op(B, C), D) --> op(A, B, C, D)
            List<IEPTreeNode> children = node.getChildren();
            for (int i = 0; i < children.size(); i++) {
                IEPTreeNode child = children.get(i);
                
                if (child.getNodeType() == nodeOp) {
                    // insert subtree
                    List<IEPTreeNode> subchildren = child.getChildren();
                    for (int j = subchildren.size() - 1; j >= 0; j--) {
                        IEPTreeNode subchild = subchildren.get(j);
                        node.insertNode(i, subchild);
                        subchild.setParent(node);
                    }

                    // cleanup
                    node.removeNode(child);
                    dec.removeDecoration(child);
                    return true;
                }
            }
        }

        return false;
    }

}
