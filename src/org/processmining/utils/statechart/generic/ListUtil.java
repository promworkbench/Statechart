package org.processmining.utils.statechart.generic;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ListUtil {

    public static <T> void addIfNew(Collection<T> list, T value) {
        if (!list.contains(value)) {
            list.add(value);
        }
    }

    public static <T> T findEqual(List<T> list, T value) {
        for (T opt : list) {
            if (opt.equals(value)) {
                return opt;
            }
        }
        return null;
    }

    public static <K, V, E extends Map.Entry<K, V>> E findEqualByKey(
            List<E> list, K value) {
        for (E opt : list) {
            if (opt.getKey().equals(value)) {
                return opt;
            }
        }
        return null;
    }
}
