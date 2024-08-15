package org.processmining.utils.statechart.signals;

/**
 * Signal Registration
 * @author mleemans
 *
 * References a signal registration
 *
 * @param <T>
 */
public class SignalRegistration<T> {

    private final AbstractSignal<T, ?> signal;
    private final T listener;

    public SignalRegistration(AbstractSignal<T, ?> signal, T listener) {
        this.signal = signal;
        this.listener = listener;
    }
    
    public void unregister() {
        signal.unregister(listener);
    }
}
