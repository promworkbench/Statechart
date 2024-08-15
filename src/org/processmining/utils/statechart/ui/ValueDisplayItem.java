package org.processmining.utils.statechart.ui;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Function;

public class ValueDisplayItem<T> {

    public static <T> Set<T> getValueSet(Collection<ValueDisplayItem<T>> collection) {
        Set<T> result = new THashSet<T>(collection.size());
        for (ValueDisplayItem<T> entry : collection) {
            result.add(entry.getValue());
        }
        return result;
    }
    
    public static <T, V> Set<V> getValueSet(Collection<ValueDisplayItem<T>> collection, Function<T, V> transform) {
        Set<V> result = new THashSet<V>(collection.size());
        for (ValueDisplayItem<T> entry : collection) {
            result.add(transform.apply(entry.getValue()));
        }
        return result;
    }
    
    private final T value;
    private final String display;

    public ValueDisplayItem(T value, String display) {
        this.value = value;
        this.display = display;
    }

    public T getValue() {
        return value;
    }
    
    public String getDisplay() {
        return display;
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return value.equals(o);
    }
    
    @Override
    public String toString() {
        return display;
    }
}
