package org.processmining.algorithms.statechart.m2m.reduct.sc;

import org.processmining.models.statechart.sc.ISCRegion;
import org.processmining.models.statechart.sc.Statechart;

public interface IReductionRule {

    public boolean reduce(Statechart statechart, ISCRegion region);
    
}
