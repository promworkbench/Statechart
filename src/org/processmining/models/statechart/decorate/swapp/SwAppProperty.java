package org.processmining.models.statechart.decorate.swapp;

import org.processmining.plugins.properties.processmodel.abstractproperty.HighLevelProperty;
import org.processmining.processtree.Node;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SwAppProperty extends HighLevelProperty<SwAppDecoration> {

    private static final long serialVersionUID = 518592945870296038L;

    @Override
    public Long getID() {
        return serialVersionUID;
    }

    @Override
    public String getName() {
        return "SwAppProperty";
    }

    @Override
    public SwAppDecoration clone(Object element) {
        return new SwAppDecoration((SwAppDecoration) element);
    }

    @Override
    public SwAppDecoration getDefaultValue() {
        return new SwAppDecoration();
    }

    @Override
    public String marshall(Object value) {
        throw new NotImplementedException();
        //return "<valueEntry value=\"" + String.valueOf(value) + "\"/>\n";
    }

    @Override
    public Object unmarshall(String xml) {
        throw new NotImplementedException();
        //return new SwAppDecoration(new String(xml.split("<valueEntry value=\"")[1].split("\"")[0]));
    }

    public static void setValue(Node node, SwAppDecoration value)
            throws IllegalAccessException, InstantiationException {
        node.setIndependentProperty(SwAppProperty.class, value);
    }

    public static SwAppDecoration getValue(Node node) throws IllegalAccessException,
            InstantiationException {
        Object value = node
                .getIndependentProperty(SwAppProperty.class);
        if (value != null) {
            return (SwAppDecoration) value;
        } else {
            return null;
        }
    }

    public static boolean hasValue(Node node) throws IllegalAccessException,
            InstantiationException {
        Object value = node
                .getIndependentProperty(SwAppProperty.class);
        return (value != null);
    }

    public static void unsetValue(Node node) throws IllegalAccessException,
            InstantiationException {
        node.removeIndependentProperty(SwAppProperty.class);
    }
}
