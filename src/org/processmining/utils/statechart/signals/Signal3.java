package org.processmining.utils.statechart.signals;

public class Signal3<T, U, V> extends AbstractSignal<Action3<T, U, V>, Signal3<T, U, V>> {

    public void dispatch(T t, U u, V v) {
        final int size = listeners.size();
        for (int i = 0; i < size; i++) {
            listeners.get(i).call(t, u, v);
        }
    }

    @Override
    protected Action3<T, U, V> _createSignalAction(final Signal3<T, U, V> signalToConnect) {
        return new Action3<T, U, V>() {
            @Override
            public void call(T t, U u, V v) {
                signalToConnect.dispatch(t, u, v);
            }
        
        };
    }
    
}
