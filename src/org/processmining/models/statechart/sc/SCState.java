package org.processmining.models.statechart.sc;

import gnu.trove.set.hash.THashSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.tree.impl.TreeIterator;

import com.google.common.base.Preconditions;

public class SCState implements ISCState {

    private static int cnt = 0;
    
    protected static String genNewId() {
        return String.format("s%d", cnt++);
    }

    private final String id;
    private final String label;
    private final SCStateType type;
    private final ISCRegion parentRegion;

    private final Set<ISCTransition> transitions;
    private final Set<ISCState> preset;
    private final Set<ISCState> postset;

    public SCState(ISCRegion parentRegion) {
        this(parentRegion, SCStateType.Simple);
    }
    
    public SCState(ISCRegion parentRegion, SCStateType type) {
        this(parentRegion, type, "");
    }

    public SCState(ISCRegion parentRegion, SCStateType type, String label) {
        this(parentRegion, type, label, genNewId());
    }

    public SCState(ISCRegion parentRegion, SCStateType type, String label, String id) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(label);
        Preconditions.checkNotNull(id);
        
        this.id = id;
        this.label = label;
        this.type = type;
        this.parentRegion = parentRegion;
        
        transitions = new THashSet<>();
        preset = new THashSet<>();
        postset = new THashSet<>();
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
    public ISCRegion getParentRegion() {
        return parentRegion;
    }

    @Override
    public boolean isPseudoState() {
        return type.isPseudostate();
    }


    @Override
    public SCStateType getStateType() {
        return type;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SCState) {
            return ((SCState) other).id.equals(id);
        }
        return false;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(type.getSymbol());
        buf.append("'");
        buf.append(label);
        buf.append("'");
        return buf.toString();
    }

    @Override
    public Set<ISCState> getPostset() {
        return postset;
    }

    @Override
    public Set<ISCState> getPreset() {
        return preset;
    }

    @Override
    public boolean isInitialState() {
        return parentRegion.getInitialState() == this;
    }

    @Override
    public boolean isEndState() {
        return parentRegion.getEndStates().contains(this);
    }

    @Override
    public void recordTransition(ISCTransition transition) {
        transitions.add(transition);
        if (transition.getFrom() == this) {
            postset.add(transition.getTo());
        }
        if (transition.getTo() == this) {
            preset.add(transition.getFrom());
        }
    }

    @Override
    public void unrecordTransition(ISCTransition transition) {
        transitions.remove(transition);
        preset.remove(transition.getFrom());
        postset.remove(transition.getTo());
    }

    @Override
    public Set<ISCTransition> getInvolvedTransitions() {
        return transitions;
    }

    @Override
    public Iterable<ISCState> iteratePreOrder() {
        return new TreeIterator<ISCState>(this, true);
    }
    
    @Override
    public Iterable<ISCState> iteratePostOrder() {
        return new TreeIterator<ISCState>(this, false);
    }

    @Override
    public ISCState getParent() {
        return getParentRegion().getParentState();
    }

    @Override
    public List<ISCState> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public void visitDepthFirstOrder(Action1<ISCState> preOrderOp,
            Action1<ISCState> postOrderOp) {
        preOrderOp.call(this);
        for (ISCState child : getChildren()) {
            child.visitDepthFirstOrder(preOrderOp, postOrderOp);
        }
        postOrderOp.call(this);
    }
}
