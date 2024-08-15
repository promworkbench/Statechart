package org.processmining.utils.statechart.signals;

public class Signal4<T, U, V, W> extends AbstractSignal<Action4<T, U, V, W>, Signal4<T, U, V, W>> {

    public void dispatch(T t, U u, V v, W w) {
        final int size = listeners.size();
        for (int i = 0; i < size; i++) {
            listeners.get(i).call(t, u, v, w);
        }
    }

    @Override
    protected Action4<T, U, V, W> _createSignalAction(final Signal4<T, U, V, W> signalToConnect) {
        return new Action4<T, U, V, W>() {
            @Override
            public void call(T t, U u, V v, W w) {
                signalToConnect.dispatch(t, u, v, w);
            }
            
        };
    }
    
}
