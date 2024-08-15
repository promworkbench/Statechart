package org.processmining.utils.statechart.signals;

public class Signal2<T, U> extends AbstractSignal<Action2<T, U>, Signal2<T, U>> {

    public void dispatch(T t, U u) {
        final int size = listeners.size();
        for (int i = 0; i < size; i++) {
            listeners.get(i).call(t, u);
        }
    }

    @Override
    protected Action2<T, U> _createSignalAction(final Signal2<T, U> signalToConnect) {
        return new Action2<T, U>() {
            @Override
            public void call(T t, U u) {
                signalToConnect.dispatch(t, u);
            }
            
        };
    }
    
}
