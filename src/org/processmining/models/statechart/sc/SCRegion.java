package org.processmining.models.statechart.sc;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.tree.impl.TreeIterator;

import com.google.common.base.Preconditions;

public class SCRegion implements ISCRegion {

    protected static int cnt = 0;

    private final String id;
    private final String label;

    private final ISCState parentState;
    private final Set<ISCState> states;
    private final Set<ISCTransition> transitions;
    private ISCState initialState;
    private final Set<ISCState> endStates;

    public SCRegion(ISCState parentState) {
        this(parentState, "");
    }

    public SCRegion(ISCState parentState, String label) {
        this(parentState, label, String.format("r%d", cnt++));
    }

    public SCRegion(ISCState parentState, String label, String id) {
        Preconditions.checkNotNull(parentState);
        Preconditions.checkNotNull(label);
        Preconditions.checkNotNull(id);

        this.id = id;
        this.label = label;
        this.parentState = parentState;

        states = new THashSet<ISCState>();
        transitions = new THashSet<ISCTransition>();
        endStates = new THashSet<ISCState>();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public ISCState getParentState() {
        return parentState;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SCRegion) {
            return ((SCRegion) other).id.equals(id);
        }
        return false;
    }

    @Override
    public void addState(ISCState state) {
        states.add(state);
    }

    @Override
    public Set<ISCState> getStates() {
        return states;
    }

    @Override
    public ISCTransition addTransition(ISCTransition transition) {
        transitions.add(transition);
        return transition;
    }

    @Override
    public ISCTransition addTransition(ISCState from, ISCState to, String label) {
        return addTransition(new SCTransition(this, from, to, label));
    }

    @Override
    public ISCTransition addTransition(ISCState from, ISCState to,
            String label, boolean isReverse) {
        return addTransition(new SCTransition(this, from, to, label, isReverse));
    }

    @Override
    public Set<ISCTransition> getTransitions() {
        return transitions;
    }

    @Override
    public void setInitialState(ISCState state) {
        initialState = state;
    }

    @Override
    public ISCState getInitialState() {
        return initialState;
    }

    @Override
    public void addEndState(ISCState state) {
        endStates.add(state);
    }

    @Override
    public void addEndStates(Collection<ISCState> states) {
        endStates.addAll(states);
    }

    @Override
    public Set<ISCState> getEndStates() {
        return endStates;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append("[");
        String sep = "";
        for (ISCState child : getStates()) {
            buf.append(sep);
            buf.append(child.toString());
            sep = ", ";
        }
        buf.append("]");

        return buf.toString();
    }

    @Override
    public void removeState(ISCState state) {
        states.remove(state);
        endStates.remove(state);
        if (initialState == state) {
            initialState = null;
        }
    }

    @Override
    public void removeTransition(ISCTransition transition) {
        transitions.remove(transition);
        transition.unregister();
    }

    @Override
    public void removeEndState(ISCState state) {
        endStates.remove(state);
    }

    @Override
    public ISCRegion getParent() {
        return getParentState().getParentRegion();
    }

    @Override
    public List<ISCRegion> getChildren() {
//        int size = 0;
//        for (ISCState state : states) {
//            if (state instanceof ISCCompositeState) {
//                size += ((ISCCompositeState) state).getRegions().size();
//            }
//        }
//        List<ISCRegion> children = new ArrayList<>(size);
        List<ISCRegion> children = new ArrayList<>();
        for (ISCState state : states) {
            if (state instanceof ISCCompositeState) {
                children.addAll(((ISCCompositeState) state).getRegions());
            }
        }
        return children;
    }

    @Override
    public void visitDepthFirstOrder(Action1<ISCRegion> preOrderOp,
            Action1<ISCRegion> postOrderOp) {
        preOrderOp.call(this);
        for (ISCRegion child : getChildren()) {
            child.visitDepthFirstOrder(preOrderOp, postOrderOp);
        }
        postOrderOp.call(this);
    }

    @Override
    public Iterable<ISCRegion> iteratePreOrder() {
        return new TreeIterator<ISCRegion>(this, true);
    }

    @Override
    public Iterable<ISCRegion> iteratePostOrder() {
        return new TreeIterator<ISCRegion>(this, false);
    }
}
