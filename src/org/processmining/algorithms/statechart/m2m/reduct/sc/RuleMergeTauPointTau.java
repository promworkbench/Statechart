package org.processmining.algorithms.statechart.m2m.reduct.sc;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.sc.ISCRegion;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.ISCTransition;
import org.processmining.models.statechart.sc.SCStateType;
import org.processmining.models.statechart.sc.Statechart;

/**
 * SC Reduction Rule: Merge Tau-Point-Tau
 * 
 * @author mleemans
 *
 *
 *         given a point pseudo-states a with |in(a)| == |out(a)| == 1 let in(a)
 *         = {x} and out(a) = {y} remove a and create transition (x, y)
 */
public class RuleMergeTauPointTau implements IReductionRule {

    private static final Logger logger = LogManager
            .getLogger(RuleMergeTauPointTau.class.getName());

    @Override
    public boolean reduce(Statechart statechart, ISCRegion region) {
        Decorations<ISCState> decState = statechart.getStateDecorations();
        Decorations<ISCTransition> decTrans = statechart
                .getTransitionDecorations();
        
        // Beware: avoid empty regions
        if (region.getStates().size() <= 1) {
            return false;
        }
        
        for (ISCState a : region.getStates()) {
            if (a.getStateType() == SCStateType.PointPseudo) {
                Set<ISCState> aPre = a.getPreset();
                Set<ISCState> aPost = a.getPostset();

                if (aPre.size() == 1 && aPost.size() == 1) {
                    ISCRegion aRegion = a.getParentRegion();
                    ISCTransition at = a.getInvolvedTransitions().iterator()
                            .next();
                    ISCState x = aPre.iterator().next();
                    ISCState y = aPost.iterator().next();

                    if (a.isEndState() && x.getParentRegion() == aRegion) {
                        aRegion.addEndState(x);
                    }
                    if (a.isInitialState() && y.getParentRegion() == aRegion) {
                        aRegion.setInitialState(y);
                    }
                    
                    // Beware: take care of self-loops
                    if (x != a && y != a) {
                        ISCTransition newT = aRegion.addTransition(x, y, "", at.isReverse());
                        decTrans.copyDecorations(newT, at);
                    }

                    aRegion.removeState(a);
                    decState.removeDecorations(a);
                    // aRegion.removeTransitions(a);
                    for (ISCTransition t : new THashSet<>(
                            a.getInvolvedTransitions())) {
                        t.getParentRegion().removeTransition(t);
                        decTrans.removeDecorations(t);
                        // TODO: what about frequency decoration?
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug(String
                                .format("RuleMergeTauPointTau - Removed: %s, Added: (%s -> %s)",
                                        a.getId(), x.getId(), y.getId()));
                    }
                    return true;
                }
            }
        }

        return false;
    }

}
