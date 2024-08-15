package org.processmining.models.statechart.properties.abstractproperty;

import gnu.trove.set.hash.THashSet;

import java.util.Collections;
import java.util.Set;

@SuppressWarnings("serial")
public abstract class PropertySet<T> extends PropertyCollection<Set<T>> {

    @Override
    public Set<T> getDefaultValue() {
        return Collections.emptySet();
    }

    @Override
    protected Set<T> createInstance(Set<T> element) {
        return new THashSet<>(element);
    }

    @Override
    protected Set<T> createInstance(String[] split) {
        Set<T> result = new THashSet<>(split.length);
        for (String e : split) {
            result.add(convertElement(e));
        }
        return result;
    }

    protected abstract T convertElement(String e);

}
