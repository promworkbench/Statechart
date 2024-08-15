package org.processmining.algorithms.statechart.m2m.reduct.sc;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.sc.ISCRegion;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.ISCTransition;
import org.processmining.models.statechart.sc.SCStateType;
import org.processmining.models.statechart.sc.Statechart;

/**
 * SC Reduction Rule: Remove extra Point-Tau-Point
 * 
 * @author mleemans
 *
 * given two point pseudo-states a and b with b \in out(a) and a \in in(b)
 * keep only one transition (a, b), removing all other (a, b) transitions 
 */
public class RuleRemoveExtraPointTauPoint implements IReductionRule {

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

            // Check if b \in out(a) and a \in in(b)
            if (a.getStateType() == SCStateType.PointPseudo
                && b.getStateType() == SCStateType.PointPseudo
                && a != b) {

                Set<ISCState> aPost = a.getPostset();
                Set<ISCState> bPre = b.getPreset();

                // check if out(a) == {b}
                if (aPost.contains(b) && bPre.contains(a)) {
                    
                    boolean changes = false;

                    for (ISCTransition ta : new THashSet<>(
                            a.getInvolvedTransitions())) {
                        if (ta != t // keep original t (at least one)
                            && ta.getFrom() == a && ta.getTo() == b) {
                            ta.getParentRegion().removeTransition(ta);
                            decTrans.removeDecorations(ta);
                            changes = true;
                        }
                    }
                    for (ISCTransition tb : new THashSet<>(
                            b.getInvolvedTransitions())) {
                        if (tb != t // keep original t (at least one)
                            && tb.getFrom() == a && tb.getTo() == b) {
                            tb.getParentRegion().removeTransition(tb);
                            decTrans.removeDecorations(tb);
                            changes = true;
                        }
                    }
                    
                    return changes;
                }
            }
        }
        
        return false;
    }

}
