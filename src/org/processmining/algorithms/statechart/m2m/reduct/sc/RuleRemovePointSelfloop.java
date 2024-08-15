package org.processmining.algorithms.statechart.m2m.reduct.sc;

import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.sc.ISCRegion;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.ISCTransition;
import org.processmining.models.statechart.sc.SCStateType;
import org.processmining.models.statechart.sc.Statechart;

/**
 * SC Reduction Rule: Remove point self loops
 * @author mleemans
 *
 * given a point pseudo-state a with a \in in(a) and a \in out(a)
 * remove all transitions (a, a)
 */
public class RuleRemovePointSelfloop implements IReductionRule {

    @Override
    public boolean reduce(Statechart statechart, ISCRegion region) {
        Decorations<ISCTransition> decTrans = statechart
                .getTransitionDecorations();
        
        for (ISCTransition t : region.getTransitions()) {
            ISCState a = t.getFrom();
            ISCState b = t.getTo();

            if (a == null || b == null) {
                throw new NullPointerException();
            }
            
            if (a.getStateType() == SCStateType.PointPseudo
                && b.getStateType() == SCStateType.PointPseudo
                && a == b) {
                t.getParentRegion().removeTransition(t);
                decTrans.removeDecorations(t);
                return true;
            }
        }
        
        return false;
    }

}
