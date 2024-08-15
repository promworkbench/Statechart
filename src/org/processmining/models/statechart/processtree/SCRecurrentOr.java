package org.processmining.models.statechart.processtree;

import java.util.Collection;
import java.util.UUID;

import org.processmining.processtree.Originator;
import org.processmining.processtree.Task;
import org.processmining.processtree.impl.AbstractTask.Manual;

public class SCRecurrentOr extends Manual implements ISCRecurrentOr {


    public SCRecurrentOr(String name, Collection<Originator> originators) {
            super(name, originators);
    }
    
    public SCRecurrentOr(UUID id, String name, Collection<Originator> originators) {
            super(id, name, originators);
    }

    public SCRecurrentOr(String name, Originator... originators) {
            super(name, originators);
    }
    
    public SCRecurrentOr(UUID id, String name, Originator... originators) {
            super(id, name, originators);
    }
    
    public SCRecurrentOr(Task.Manual t){
            super(t);
    }
    
}
