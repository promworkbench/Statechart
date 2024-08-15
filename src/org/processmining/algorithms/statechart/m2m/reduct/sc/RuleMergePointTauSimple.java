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
 * SC Reduction Rule: Merge Point-Tau-Simple
 * 
 * @author mleemans
 *
 *
 *         given point pseudo-state a and simple state b with out(a) == {b} , a
 *         \in in(b) and region(a) == region(b) merge a and b, resulting in c
 *         with in(c) = (in(a) u in(b)) \ {a} , out(c) = out(b) \ {a}
 */
public class RuleMergePointTauSimple implements IReductionRule {

    private static final Logger logger = LogManager
            .getLogger(RuleMergePointTauSimple.class.getName());

    @Override
    public boolean reduce(Statechart statechart, ISCRegion region) {
        Decorations<ISCState> decState = statechart.getStateDecorations();
        Decorations<ISCTransition> decTrans = statechart
                .getTransitionDecorations();
        
        for (ISCTransition t : region.getTransitions()) {
            ISCState a = t.getFrom();
            ISCState b = t.getTo();

            if (a == null || b == null) {
                throw new NullPointerException();
            }

            // Check if region(a) == region(b)
            if (a.getParentRegion() == b.getParentRegion()
                    && a.getStateType() == SCStateType.PointPseudo
                    && b.getStateType() == SCStateType.Simple) {
                Set<ISCState> aPost = a.getPostset();

                // check if out(a) == {b}
                if (aPost.size() == 1 && aPost.contains(b)) {

                    // Merge a into b, removing a
                    if (a.isInitialState()) {
                        b.getParentRegion().setInitialState(b);
                    }

                    a.getParentRegion().removeState(a);
                    decState.removeDecorations(a);
                    
                    for (ISCTransition ta : new THashSet<>(
                            a.getInvolvedTransitions())) {
                        if (ta.getFrom() == a && ta.getTo() == b) {
                            ta.getParentRegion().removeTransition(ta);
                            decTrans.removeDecorations(ta);
                        }
                        if (ta.getFrom() == a && ta.getTo() != b) {
                            if (ta.getTo() == a) {
                                // TODO special case of self-loop should have been avoided,
                                // why is a not in the postset of a?
                                ta.getParentRegion().removeTransition(ta);
                                decTrans.removeDecorations(ta);
                                logger.warn("RuleMergePointTauSimple - special case of self-loop should have been avoided, why is a not in the postset of a?");
                            } else {
                                // TODO why is this triggered?
                                throw new IllegalStateException(
                                        "RuleMergePointTauSimple: We should have ruled this case out by design");
                            }
                        }
                        if (ta.getFrom() != a && ta.getTo() == a) {
                            ta.setFromTo(ta.getFrom(), b);
                        }
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("RuleMergePointTauPoint - Removed: " + a);
                    }
                    return true;
                }
            }
        }

        return false;
    }

}
