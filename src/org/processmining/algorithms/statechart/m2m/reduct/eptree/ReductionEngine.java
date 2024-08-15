package org.processmining.algorithms.statechart.m2m.reduct.eptree;

import java.util.ArrayList;
import java.util.List;

import org.processmining.algorithms.statechart.m2m.reduct.eptree.IReductionRule;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public class ReductionEngine {

    private final List<IReductionRule> reductionRules;

    public ReductionEngine() {
        reductionRules = new ArrayList<>();
    }

    public void addRule(IReductionRule rule) {
        reductionRules.add(rule);
    }
    
    public void reduce(IEPTree tree) {
        while (_reduce(tree)) {}
    }

    private boolean _reduce(IEPTree tree) {
        final int size = reductionRules.size();
        
        // apply reduce on all regions 
        for (IEPTreeNode node : tree.iteratePreOrder()) {
            // call and try all reduction rules
            for (int i = 0; i < size; i++) {
                if (reductionRules.get(i).reduce(tree, node)) {
                    return true;
                }
            }
        }
        
        return false;
    }

}
