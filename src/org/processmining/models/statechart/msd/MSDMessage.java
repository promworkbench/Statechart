package org.processmining.models.statechart.msd;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.models.statechart.decorate.tracing.BasicEdgeSemanticTraced;
import org.processmining.models.statechart.decorate.tracing.IEdgeSemanticTraced;

import com.google.common.base.Preconditions;

public class MSDMessage extends MSDNode implements IMSDMessage {

    private final MessageType type;
    private final IActivation source;
    private final IActivation target;
    private final String nodeId;
    
    private final IEdgeSemanticTraced<IActivation> semantics = 
            new BasicEdgeSemanticTraced<>();

    public MSDMessage(IMSDNode parent, String name, MessageType type, 
            IActivation source, IActivation target, String nodeId) {
        super(parent, name);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(target);
        this.type = type;
        this.source = source;
        this.target = target;
        this.nodeId = nodeId;
    }
    
    @Override
    public IActivation getSource() {
        return source;
    }

    @Override
    public IActivation getTarget() {
        return target;
    }

    @Override
    public MessageType getMessageType() {
        return type;
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public Pair<Set<IActivation>, Set<IActivation>> getEdgeSemantics() {
        return semantics.getEdgeSemantics();
    }

    @Override
    public Set<IActivation> getEdgeFromSemantics() {
        return semantics.getEdgeFromSemantics();
    }

    @Override
    public Set<IActivation> getEdgeToSemantics() {
        return semantics.getEdgeToSemantics();
    }

    @Override
    public void setEdgeSemantics(Set<IActivation> from, Set<IActivation> to) {
        semantics.setEdgeSemantics(from, to);
    }

    @Override
    public boolean isStartActivation() {
        return type.isStartActivation();
    }

}
