package org.processmining.models.statechart.processtree;

import java.util.Collection;
import java.util.UUID;

import org.processmining.processtree.Originator;
import org.processmining.processtree.Task;
import org.processmining.processtree.impl.AbstractTask.Manual;

public class ErrorTrigger extends Manual implements IErrorTrigger {

    public ErrorTrigger(String name, Collection<Originator> originators) {
            super(name, originators);
    }
    
    public ErrorTrigger(UUID id, String name, Collection<Originator> originators) {
            super(id, name, originators);
    }

    public ErrorTrigger(String name, Originator... originators) {
            super(name, originators);
    }
    
    public ErrorTrigger(UUID id, String name, Originator... originators) {
            super(id, name, originators);
    }
    
    public ErrorTrigger(Task.Manual t){
            super(t);
    }
    
}

