package org.processmining.algorithms.statechart.m2m.reduct.eptree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.processmining.models.statechart.decorate.staticmetric.EPTreeFreqMetricDecorator;
import org.processmining.models.statechart.eptree.EPNodeType;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public class RuleReduceXorTau implements IReductionRule {

    @Override
    public boolean reduce(IEPTree tree, IEPTreeNode node) {
        EPNodeType nodeOp = node.getNodeType();
        EPTreeFreqMetricDecorator dec = tree.getDecorations().getForType(
                EPTreeFreqMetricDecorator.class);

        if (nodeOp == EPNodeType.Choice) {
            // op(A, ..., tau) --> op(A, ...)
            boolean seenTau = false;
            @SuppressWarnings("unused")
            IEPTreeNode tauNode = null;
            List<IEPTreeNode> taus = new ArrayList<IEPTreeNode>();
            
            Iterator<IEPTreeNode> it = node.getChildren().iterator();
            while (it.hasNext()) {
                IEPTreeNode child = it.next();
                
                if (!seenTau) {
                    seenTau = findEpsilonTau(child, taus);
                    if (seenTau) {
                        tauNode = taus.get(0);
                    }
                } else {
                    // already seen one tau before
                    if (child.getNodeType() == EPNodeType.Silent) {
                        node.removeNode(child);
                        child.setParent(null);
                        
                        //FreqMetric decs = dec.getDecoration(child);
                        dec.removeDecoration(child);
                        //dec.setDecoration(tauNode, null); // TODO ++
                        
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean findEpsilonTau(IEPTreeNode node, List<IEPTreeNode> result) {
        EPNodeType nodeOp = node.getNodeType();
        
        if (nodeOp == EPNodeType.Silent) {
            result.add(node);
            return true;
        } else if (nodeOp == EPNodeType.Seq || nodeOp == EPNodeType.AndComposite) {
            boolean outcome = true;
            for (IEPTreeNode child : node.getChildren()) {
                outcome = outcome && findEpsilonTau(child, result);
            }
            return outcome;
        } else if (nodeOp == EPNodeType.Choice) {
            boolean outcome = false;
            for (IEPTreeNode child : node.getChildren()) {
                outcome = outcome || findEpsilonTau(child, result);
            }
            return outcome;
        } else if (nodeOp == EPNodeType.Loop) {
            return findEpsilonTau(node.getChildren().get(0), result);
        }
        
        return false;
    }

}
