package org.processmining.utils.statechart.signals;

public interface Action3<T, U, V> {
	void call(T t, U u, V v);
}