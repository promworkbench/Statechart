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
 * SC Reduction Rule: Merge Simple-Tau-Point
 * 
 * @author mleemans
 *
 *
 *         given simple state a and point pseudo-state b with b \in out(a) ,
 *         in(b) == {a} and region(a) == region(b) merge a and b, resulting in c
 *         with in(c) = (in(a) u in(b)) \ {a} , out(c) = out(b) \ {a}
 */
public class RuleMergeSimpleTauPoint implements IReductionRule {

    private static final Logger logger = LogManager
            .getLogger(RuleMergeSimpleTauPoint.class.getName());

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
                    && a.getStateType() == SCStateType.Simple
                    && b.getStateType() == SCStateType.PointPseudo) {
                Set<ISCState> aPost = a.getPostset();
                Set<ISCState> bPre = b.getPreset();

                // check if b \in out(a) , in(b) == {a}
                if (aPost.contains(b) && bPre.size() == 1 && bPre.contains(a)) {

                    // Merge b into a, removing b
                    if (b.isEndState()) {
                        a.getParentRegion().addEndState(a);
                    }

                    b.getParentRegion().removeState(b);
                    decState.removeDecorations(b);
                    
                    THashSet<ISCTransition> Btrans = new THashSet<>(
                            b.getInvolvedTransitions());
                    for (ISCTransition tb : Btrans) {
                        if (tb.getFrom() == a && tb.getTo() == b) {
                            tb.getParentRegion().removeTransition(tb);
                            decTrans.removeDecorations(tb);
                        }
                        if (tb.getFrom() != a && tb.getTo() == b) {
                            if (tb.getFrom() == b) {
                                // TODO special case of self-loop should have been avoided,
                                // why is b not in the preset of b?
                                tb.getParentRegion().removeTransition(tb);
                                decTrans.removeDecorations(tb);
                                logger.warn("RuleMergeSimpleTauPoint - special case of self-loop should have been avoided, why is b not in the preset of b?");
                            } else {
                                throw new IllegalStateException(
                                        "RuleMergeSimpleTauPoint: We should have ruled this case out by design");
                            }
                        }
                        if (tb.getFrom() == b) {
                            tb.setFromTo(a, tb.getTo());
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
