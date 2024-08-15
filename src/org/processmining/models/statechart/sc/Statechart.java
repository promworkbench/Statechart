package org.processmining.models.statechart.sc;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecorated;
import org.processmining.models.statechart.decorate.tracing.TraceUniqueDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public class Statechart extends SCCompositeState implements IDecorated<ISCState> {

    private final Decorations<ISCState> stateDecorations;
    private final Decorations<ISCTransition> transitionDecorations;
    
    public Statechart(String label) {
        this(label, genNewId(), new Decorations<ISCState>(), new Decorations<ISCTransition>());
    }
    
    public Statechart(String label, Decorations<ISCState> stateDecorations,
            Decorations<ISCTransition> transitionDecorations) {
        this(label, genNewId(), stateDecorations, transitionDecorations);
    }

    public Statechart(String label, String id,
            Decorations<ISCState> stateDecorations,
            Decorations<ISCTransition> transitionDecorations) {
        super(null, SCStateType.OrComposite, label, id);
        this.stateDecorations = stateDecorations;
        this.transitionDecorations = transitionDecorations;
        
        @SuppressWarnings("unchecked")
        TraceUniqueDecorator<ISCState, IEPTreeNode> treeTracer 
            = stateDecorations.getForType(TraceUniqueDecorator.class);
        if (treeTracer == null) {
            treeTracer = new TraceUniqueDecorator<ISCState, IEPTreeNode>();
            stateDecorations.registerDecorator(treeTracer);
        }
    }

    public StatechartIterator regionIterator() {
        return new StatechartIterator(this);
    }
    
    public ISCState getStateByLabel(String label) {
        for (ISCRegion region : regionIterator()) {
            for (ISCState state : region.getStates()) {
                if (label.equals(state.getLabel())) {
                    return state;
                }
            }
        }
        return null;
    }

    public ISCState getStateById(String id) {
        for (ISCRegion region : regionIterator()) {
            for (ISCState state : region.getStates()) {
                if (id.equals(state.getId())) {
                    return state;
                }
            }
        }
        return null;
    }
    
    public Statechart createCopy() {
        Decorations<ISCState> newStateDecorations = stateDecorations
                .deepNewInstance();
        Decorations<ISCTransition> newTransitionDecorations = transitionDecorations
                .deepNewInstance();

        Statechart sc = new Statechart(this.getLabel(), this.getId(),
                newStateDecorations, newTransitionDecorations);
        Map<ISCState, ISCState> old2newS = new THashMap<>();
        Map<ISCTransition, ISCTransition> old2newT = new THashMap<>();

        // copy graph
        _createCopy(old2newS, old2newT, this, sc);

        // copy decorations
        for (ISCState oldS : old2newS.keySet()) {
            newStateDecorations.copyDecorations(old2newS.get(oldS), oldS,
                    stateDecorations);
        }
        for (ISCTransition oldT : old2newT.keySet()) {
            newTransitionDecorations.copyDecorations(old2newT.get(oldT), oldT,
                    transitionDecorations);
        }

        return sc;
    }

    private void _createCopy(Map<ISCState, ISCState> old2newS,
            Map<ISCTransition, ISCTransition> old2newT, ISCCompositeState from,
            ISCCompositeState to) {
        // copy for all regions
        for (ISCRegion region : from.getRegions()) {
            to.addRegion(_createCopy(old2newS, old2newT, region, to));
        }
    }

    private ISCRegion _createCopy(Map<ISCState, ISCState> old2newS,
            Map<ISCTransition, ISCTransition> old2newT, ISCRegion fromRegion, ISCCompositeState toState) {
        ISCRegion toRegion = new SCRegion(toState);

        // copy states
        for (ISCState oldState : fromRegion.getStates()) {
            ISCState newState;

            if (oldState instanceof ISCCompositeState) {
                // copy composite state
                newState = new SCCompositeState(toRegion,
                        oldState.getStateType(), oldState.getLabel(),
                        oldState.getId());
                _createCopy(old2newS, old2newT, (ISCCompositeState) oldState,
                        (ISCCompositeState) newState);
            } else {
                // copy simple state
                newState = new SCState(toRegion, oldState.getStateType(),
                        oldState.getLabel(), oldState.getId());
            }

            // complete state administration
            old2newS.put(oldState, newState);
            toRegion.addState(newState);
        }

        // copy transitions
        for (ISCTransition t : fromRegion.getTransitions()) {
            ISCTransition newT = toRegion.addTransition(
                    old2newS.get(t.getFrom()), old2newS.get(t.getTo()),
                    t.getLabel(), t.isReverse());
            newT.setEdgeSemantics(t.getEdgeFromSemantics(), t.getEdgeToSemantics());
            old2newT.put(t, newT);
        }
        
        // start and end states
        ISCState oldInitState = fromRegion.getInitialState();
        toRegion.setInitialState(old2newS.get(oldInitState));
        
        for (ISCState oldEndState : fromRegion.getEndStates()) {
            toRegion.addEndState(old2newS.get(oldEndState));
        }
        
        return toRegion;
    }

    @Override
    public Decorations<ISCState> getDecorations() {
        return stateDecorations;
    }

    public Decorations<ISCState> getStateDecorations() {
        return stateDecorations;
    }

    public Decorations<ISCTransition> getTransitionDecorations() {
        return transitionDecorations;
    }

    public boolean isInitialState(ISCState state) {
        return !getRegions().isEmpty() 
                && getRegions().get(0).getInitialState() == state;
    }

    public boolean isEndState(ISCState state) {
        return !getRegions().isEmpty() 
                && getRegions().get(0).getEndStates().contains(state);
    }

}
