package org.processmining.algorithms.statechart.m2m.reduct.eptree;

public class ReductionEngineDefault extends ReductionEngine {

    public ReductionEngineDefault() {
        addRule(new RuleReduceSingleChild());
        addRule(new RuleReduceSameOperator());
        addRule(new RuleReduceXorTau());
        addRule(new RuleRemoveTauChild());
        addRule(new RuleReduceLoopTauTau());
    }
    
}
