package org.processmining.algorithms.statechart.m2m.reduct.sc;

import java.util.ArrayList;
import java.util.List;

import org.processmining.models.statechart.sc.ISCRegion;
import org.processmining.models.statechart.sc.Statechart;

public class ReductionEngine {

    private final List<IReductionRule> reductionRules;

    public ReductionEngine() {
        reductionRules = new ArrayList<>();
    }

    public void addRule(IReductionRule rule) {
        reductionRules.add(rule);
    }
    
    public void reduce(Statechart sc) {
        while (_reduce(sc)) {}
    }

    private boolean _reduce(Statechart model) {
        final int size = reductionRules.size();
        
        // apply reduce on all regions 
        for (ISCRegion region : model.regionIterator()) {
            // call and try all reduction rules
            for (int i = 0; i < size; i++) {
                if (reductionRules.get(i).reduce(model, region)) {
                    return true;
                }
            }
        }
        
        return false;
    }

}
