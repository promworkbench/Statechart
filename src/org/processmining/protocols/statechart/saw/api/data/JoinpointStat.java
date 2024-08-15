package org.processmining.protocols.statechart.saw.api.data;

public class JoinpointStat {

    private Joinpoint joinpoint;
    
    private int frequency;

    public JoinpointStat() {
        
    }
    
    public JoinpointStat(Joinpoint joinpoint, int frequency) {
        this.joinpoint = joinpoint;
        this.frequency = frequency;
    }
    
    public Joinpoint getJoinpoint() {
        return joinpoint;
    }

    public void setJoinpoint(Joinpoint joinpoint) {
        this.joinpoint = joinpoint;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void increaseFrequency() {
        this.frequency++;
    }
    
}
