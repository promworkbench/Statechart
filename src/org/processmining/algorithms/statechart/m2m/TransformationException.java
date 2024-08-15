package org.processmining.algorithms.statechart.m2m;

public class TransformationException extends IllegalStateException {

    private static final long serialVersionUID = 2187222048038233331L;

    public TransformationException() {
        super();
    }
    
    public TransformationException(String msg) {
        super(msg);
    }
    
    public TransformationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
