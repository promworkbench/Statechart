package org.processmining.algorithms.statechart.align.metric;

public class MetricId {

    private final String id;

    public MetricId(String id) {
        this.id = id;
    }
    
    public boolean equals(Object other) {
        return id.equals(other);
    }
    
    public int hashcode() {
        return id.hashCode();
    }
    
    public String toString() {
        return id;
    }
}
