package org.processmining.models.statechart.sc;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.models.statechart.decorate.tracing.BasicEdgeSemanticTraced;
import org.processmining.models.statechart.decorate.tracing.IEdgeSemanticTraced;

import com.google.common.base.Preconditions;

public class SCTransition implements ISCTransition {

    protected static int cnt = 0;

    private final String id;
    
    private ISCState from;
    private ISCState to;
    private final String label;
    private final boolean isReverse;
    private final ISCRegion parentRegion;

    private final IEdgeSemanticTraced<ISCState> semantics = 
            new BasicEdgeSemanticTraced<>();

    public SCTransition(ISCRegion parentRegion, ISCState from, ISCState to, String label) {
        this(parentRegion, from, to, label, false);
    }

    public SCTransition(ISCRegion parentRegion, ISCState from, ISCState to, String label,
            boolean isReverse) {
        this(parentRegion, from, to, label, isReverse, String.format("t%d", cnt++));
    }
    
    public SCTransition(ISCRegion parentRegion, ISCState from, ISCState to, String label,
            boolean isReverse, String id) {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(label);

        this.id = id;
        this.label = label;
        this.isReverse = isReverse;
        this.parentRegion = parentRegion;
        
        setFromTo(from, to);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ISCState getFrom() {
        return from;
    }

    @Override
    public ISCState getTo() {
        return to;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SCTransition) {
            return ((SCTransition) other).id.equals(id);
        }
        return false;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("'");
        buf.append(label);
        buf.append("'");
        buf.append("(");
        buf.append(from.toString());
        buf.append(", ");
        buf.append(to.toString());
        buf.append(")");
        return buf.toString();
    }

    @Override
    public boolean isReverse() {
        return isReverse;
    }

    @Override
    public boolean isInvolved(ISCState state) {
        return from == state || to == state;
    }

    @Override
    public void unregister() {
        setFromTo(null, null);
    }

    @Override
    public void setFromTo(ISCState from, ISCState to) {
        if (this.from != null) {
            this.from.unrecordTransition(this);
        }
        if (this.to != null) {
            this.to.unrecordTransition(this);
        }
        
        this.from = from;
        this.to = to;

        if (from != null && from == to) {
            from.recordTransition(this);
        } else {
            if (from != null) {
                from.recordTransition(this);
            }
            if (to != null) {
                to.recordTransition(this);
            }
        }
    }

    @Override
    public ISCRegion getParentRegion() {
        return parentRegion;
    }

    @Override
    public Pair<Set<ISCState>, Set<ISCState>> getEdgeSemantics() {
        return semantics.getEdgeSemantics();
    }

    @Override
    public Set<ISCState> getEdgeFromSemantics() {
        return semantics.getEdgeFromSemantics();
    }

    @Override
    public Set<ISCState> getEdgeToSemantics() {
        return semantics.getEdgeToSemantics();
    }

    @Override
    public void setEdgeSemantics(Set<ISCState> from, Set<ISCState> to) {
        semantics.setEdgeSemantics(from, to);
    }
}
