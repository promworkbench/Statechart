package org.processmining.xes.statechart.model;

import java.util.Objects;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeImpl;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class XAttributeSubtraceImpl extends XAttributeImpl implements
        XAttributeSubtrace {

    private static final long serialVersionUID = -5669792261878770351L;

    private XTrace value;

    public XAttributeSubtraceImpl(String key) {
        super(key);
    }

    public XAttributeSubtraceImpl(String key, XExtension extension) {
        super(key, extension);
    }

    public void setSubtrace(XTrace value) {
        this.value = value;
    }

    @Override
    public XTrace getSubtrace() {
        return value;
    }

    public String toString() {
        return value.toString();
    }

    public Object clone() {
        return super.clone();
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if ((obj instanceof XAttributeSubtrace)) {
            XAttributeSubtrace other = (XAttributeSubtrace) obj;
            return (super.equals(other)) && (this.value.equals(other.getSubtrace()));
        }

        return false;
    }

    public int hashCode() {
        return Objects.hash(new Object[] { getKey(), value });
    }

    public int compareTo(XAttribute other) {
        throw new NotImplementedException();
        
//        if (!(other instanceof XAttributeMapRef)) {
//            throw new ClassCastException();
//        }
//        int result = super.compareTo(other);
//        if (result != 0) {
//            return result;
//        }
        //return this.value.compareTo(((XAttributeMapRef) other).getAttributeMap());
    }
}
