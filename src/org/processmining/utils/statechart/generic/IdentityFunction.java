package org.processmining.utils.statechart.generic;

import com.google.common.base.Function;

public class IdentityFunction<T> implements Function<T, T> {

    @Override
    public T apply(T value) {
        return value;
    }

}
