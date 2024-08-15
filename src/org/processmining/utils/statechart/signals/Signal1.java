package org.processmining.utils.statechart.signals;

public class Signal1<T> extends AbstractSignal<Action1<T>, Signal1<T>> {

    public void dispatch(T t) {
        final int size = listeners.size();
        for (int i = 0; i < size; i++) {
            listeners.get(i).call(t);
        }
    }

    @Override
    protected Action1<T> _createSignalAction(final Signal1<T> signalToConnect) {
        return new Action1<T>() {
            @Override
            public void call(T t) {
                signalToConnect.dispatch(t);
            }
        };
    }
}
