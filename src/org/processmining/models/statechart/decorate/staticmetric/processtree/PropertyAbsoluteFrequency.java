package org.processmining.models.statechart.decorate.staticmetric.processtree;

import java.util.HashSet;
import java.util.Set;

import org.processmining.plugins.properties.enumerations.Constructs;
import org.processmining.plugins.properties.processmodel.abstractproperty.PropertyInteger;
import org.processmining.processtree.Node;

public class PropertyAbsoluteFrequency extends PropertyInteger {

    private static final long serialVersionUID = -4211820914646988488L;

    public Long getID() {
        return serialVersionUID;
    }

    public String getName() {
        return "Absolute Frequency";
    }

    public Integer getDefaultValue() {
        return 0;
    }

    public Set<Constructs> getMeaningfulTypeConstructs() {
        Set<Constructs> constr = new HashSet<Constructs>();
        constr.add(Constructs.Node);
        return constr;
    }

    public static void setValue(Node node, int value)
            throws IllegalAccessException, InstantiationException {
        node.setIndependentProperty(PropertyAbsoluteFrequency.class, value);
    }

    public static int getValue(Node node) throws IllegalAccessException,
            InstantiationException {
        Object value = node
                .getIndependentProperty(PropertyAbsoluteFrequency.class);
        if (value != null) {
            return (Integer) value;
        } else {
            return Integer.MIN_VALUE;
        }
    }

    public static boolean hasValue(Node node) throws IllegalAccessException,
            InstantiationException {
        Object value = node
                .getIndependentProperty(PropertyAbsoluteFrequency.class);
        return (value != null);
    }

    public static void unsetValue(Node node) throws IllegalAccessException,
            InstantiationException {
        node.removeIndependentProperty(PropertyAbsoluteFrequency.class);
    }
}
