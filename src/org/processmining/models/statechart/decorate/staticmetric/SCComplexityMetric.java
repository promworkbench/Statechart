package org.processmining.models.statechart.decorate.staticmetric;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.models.statechart.sc.ISCCompositeState;
import org.processmining.models.statechart.sc.ISCRegion;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.SCStateType;
import org.processmining.models.statechart.sc.Statechart;

public class SCComplexityMetric {

    private int depth;
    private int noActivities;
    private int noSimpleStates;
    private int noCompositeStates;
    private int noTransitions;
    private int cyclomaticComplexity;

    public SCComplexityMetric(Statechart model) {
        depth = 0;
        noSimpleStates = 0;
        noCompositeStates = 0;
        noTransitions = 0;

        // calculate metrics
        Set<String> activitySet = new HashSet<>();
        _investigate(model.getRegions(), activitySet, 1);
        activitySet.remove("");
        this.noActivities = activitySet.size();
        
        // Ref:
        // "The Impact of Structural Complexity on the Understandability of UML Statechart Diagrams"
        // by Hose A. Cruz-Lemus, Ann Maes, Marcela Genero, Geert Pels and Mario
        // Piattini
        this.cyclomaticComplexity = Math
                .abs(noSimpleStates - noTransitions + 2);
    }

    private void _investigate(List<ISCRegion> regions, Set<String> activitySet, int curDepth) {
        depth = Math.max(depth, curDepth);
        
        for (int i = 0; i < regions.size(); i++) {
            ISCRegion region = regions.get(i);
            
            for (ISCState state : region.getStates()) {
                if (state instanceof ISCCompositeState) {
                    noCompositeStates++;
                    
                    // recurse
                    int newDepth = curDepth;
                    if (state.getStateType() == SCStateType.OrComposite) {
                        newDepth++;
                    }
                    _investigate(((ISCCompositeState) state).getRegions(), activitySet, newDepth);
                    
                } else {
                    noSimpleStates++;
                }
                activitySet.add(state.getLabel());
            }
            
            noTransitions += region.getTransitions().size();
        }
    }

    public int getDepth() {
        return depth;
    }

    public int getNoActivities() {
        return noActivities;
    }

    public int getNoSimpleStates() {
        return noSimpleStates;
    }

    public int getNoCompositeStates() {
        return noCompositeStates;
    }

    public int getNoTransitions() {
        return noTransitions;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }
}
