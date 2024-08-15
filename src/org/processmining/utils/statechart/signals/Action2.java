package org.processmining.utils.statechart.signals;

public interface Action2<T, U> {
	void call(T t, U u);
}
