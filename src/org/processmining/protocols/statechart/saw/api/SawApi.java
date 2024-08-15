package org.processmining.protocols.statechart.saw.api;

import java.util.Collection;

import org.processmining.protocols.statechart.saw.api.data.Joinpoint;
import org.processmining.protocols.statechart.saw.api.data.JoinpointStat;

/**
 * API for SAW commands
 * @author mleemans
 * 
 * Used by Eclipse SAW plugin for Java IDE interactions
 */
public abstract class SawApi {

    public static final String KeyJoinpointStats = "KeyJoinpointStats";
    
    public abstract void selectJoinpoint(Joinpoint joinpoint);
    
    public abstract void setJoinpointStats(Collection<JoinpointStat> joinpointStats);
}
