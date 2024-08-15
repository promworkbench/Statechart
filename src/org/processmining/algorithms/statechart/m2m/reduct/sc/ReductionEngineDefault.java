package org.processmining.algorithms.statechart.m2m.reduct.sc;

public class ReductionEngineDefault extends ReductionEngine {

    public ReductionEngineDefault() {
        addRule(new RuleRemovePointSelfloop());
        addRule(new RuleRemoveExtraPointTauPoint());
        
        addRule(new RuleMergePointTauPoint());
        addRule(new RuleMergeTauPointTau());
        addRule(new RuleMergePointTauSimple());
        addRule(new RuleMergeSimpleTauPoint());
    }
    
}
