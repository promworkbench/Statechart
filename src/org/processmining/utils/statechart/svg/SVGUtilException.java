package org.processmining.utils.statechart.svg;

public class SVGUtilException extends IllegalStateException {

    private static final long serialVersionUID = -393112715603695064L;

    public SVGUtilException() {
        super();
    }
    
    public SVGUtilException(String msg) {
        super(msg);
    }
    
    public SVGUtilException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
