package org.processmining.protocols.statechart.saw.api.data;

import java.util.Collection;

import com.google.common.base.Objects;


public class DataPackage {

    public static enum Type {
        SelectJoinpoint,
        JoinpointStats
    }
    
    private Type type;
    
    private Joinpoint joinpoint;
    private Collection<JoinpointStat> joinpointStats;

    public static DataPackage createSelectJoinpoint(Joinpoint joinpoint) {
        DataPackage inst = new DataPackage();
        inst.setType(Type.SelectJoinpoint);
        inst.setJoinpoint(joinpoint);
        return inst;
    }

    public static DataPackage createJoinpointStats(Collection<JoinpointStat> joinpointStats) {
        DataPackage inst = new DataPackage();
        inst.setType(Type.JoinpointStats);
        inst.setJoinpointStats(joinpointStats);
        return inst;
    }
    
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Joinpoint getJoinpoint() {
        return joinpoint;
    }

    public void setJoinpoint(Joinpoint joinpoint) {
        this.joinpoint = joinpoint;
    }

    public Collection<JoinpointStat> getJoinpointStats() {
        return joinpointStats;
    }

    public void setJoinpointStats(Collection<JoinpointStat> joinpointStats) {
        this.joinpointStats = joinpointStats;
    }
 
    @Override
    public boolean equals(Object other) {
        if (other instanceof DataPackage) {
            DataPackage inst = ((DataPackage) other);
            return Objects.equal(type, inst.type)
                    && Objects.equal(joinpoint, inst.joinpoint)
                    && Objects.equal(joinpointStats, inst.joinpointStats);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(type, joinpoint, joinpointStats);
    }
}
