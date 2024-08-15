package org.processmining.utils.statechart.signals;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSignal<T, S extends AbstractSignal<T, S>> {
    
    protected final List<T> listeners = new ArrayList<>();
    
    public SignalRegistration<T> register(T listener) {
        listeners.add(listener);
        return new SignalRegistration<T>(this, listener);
    }

    public SignalRegistration<T> connect(S signal) {
        T listener = _createSignalAction(signal);
        listeners.add(listener);
        return new SignalRegistration<T>(this, listener);
    }

    public void unregister(T listener) {
        listeners.remove(listener);
    }

    protected abstract T _createSignalAction(final S signalToConnect);
}
