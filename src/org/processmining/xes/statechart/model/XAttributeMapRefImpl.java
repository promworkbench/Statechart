package org.processmining.xes.statechart.model;

import java.util.Objects;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.impl.XAttributeImpl;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class XAttributeMapRefImpl extends XAttributeImpl implements
        XAttributeMapRef {

    private static final long serialVersionUID = -5669792261878770351L;

    private XAttributeMap value;

    public XAttributeMapRefImpl(String key) {
        super(key);
    }

    public XAttributeMapRefImpl(String key, XExtension extension) {
        super(key, extension);
    }

    public void setAttributeMap(XAttributeMap value) {
        this.value = value;
    }

    @Override
    public XAttributeMap getAttributeMap() {
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
        if ((obj instanceof XAttributeMapRef)) {
            XAttributeMapRef other = (XAttributeMapRef) obj;
            return (super.equals(other)) && (this.value.equals(other.getAttributeMap()));
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
