package org.processmining.models.statechart.decorate.error;

import java.util.HashSet;
import java.util.Set;

import org.processmining.models.statechart.properties.abstractproperty.PropertySet;
import org.processmining.plugins.properties.enumerations.Constructs;
import org.processmining.processtree.Node;

public class PropertyErrorTrigger extends PropertySet<String> {

    private static final long serialVersionUID = 1430154772263582413L;

    @Override
    public Long getID() {
        return serialVersionUID;
    }

    @Override
    public String getName() {
        return "Error Trigger";
    }

    @Override
    protected String convertElement(String e) {
        return e;
    }
    
    public Set<Constructs> getMeaningfulTypeConstructs() {
        Set<Constructs> constr = new HashSet<Constructs>();
        constr.add(Constructs.Node);
        return constr;
    }

    public static void setValue(Node node, Set<String> value)
            throws IllegalAccessException, InstantiationException {
        node.setIndependentProperty(PropertyErrorTrigger.class, value);
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getValue(Node node) throws IllegalAccessException,
            InstantiationException {
        Object value = node
                .getIndependentProperty(PropertyErrorTrigger.class);
        if (value != null) {
            return (Set<String>) value;
        } else {
            return null;
        }
    }

    public static boolean hasValue(Node node) throws IllegalAccessException,
            InstantiationException {
        Object value = node
                .getIndependentProperty(PropertyErrorTrigger.class);
        return (value != null);
    }

    public static void unsetValue(Node node) throws IllegalAccessException,
            InstantiationException {
        node.removeIndependentProperty(PropertyErrorTrigger.class);
    }
}
