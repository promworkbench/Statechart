package org.processmining.protocols.statechart.saw.api.data;

import java.util.Objects;

import org.processmining.models.statechart.decorate.swapp.SwAppDecoration;

public class Joinpoint {

    private String joinpoint;
    private String filename;
    private int linenr;

    public Joinpoint() {
        
    }
    
    public Joinpoint(String joinpoint, String filename, int linenr) {
        this.joinpoint = joinpoint;
        this.filename = filename;
        this.linenr = linenr;
    }

    public Joinpoint(SwAppDecoration data) {
        this(data.getJoinpoint(), data.getFilename(), data.getLinenr());
    }

    public String getJoinpoint() {
        return joinpoint;
    }

    public void setJoinpoint(String joinpoint) {
        this.joinpoint = joinpoint;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getLinenr() {
        return linenr;
    }

    public void setLinenr(int linenr) {
        this.linenr = linenr;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Joinpoint) {
            Joinpoint inst = ((Joinpoint) other);
            return Objects.equals(joinpoint, inst.joinpoint)
                    && Objects.equals(filename, inst.filename)
                    && linenr == inst.linenr;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(joinpoint, filename, linenr);
    }
}
