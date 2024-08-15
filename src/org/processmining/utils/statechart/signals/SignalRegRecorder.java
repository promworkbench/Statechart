package org.processmining.utils.statechart.signals;

import java.util.ArrayList;
import java.util.List;

/**
 * Signal Registration Recorder
 * 
 * @author mleemans
 *
 *         Container recording a group of registrations, allowing easy
 *         registration management: quickly unregistering all listeners in this
 *         group.
 */
public class SignalRegRecorder {

    @SuppressWarnings("rawtypes")
    private List<SignalRegistration> registrations = new ArrayList<>();

    public <T> void register(AbstractSignal<T, ?> signal, T listener) {
        registrations.add(signal.register(listener));
    }

    public <T, S extends AbstractSignal<T, S>> void connect(S signalFrom, S signalTo) {
        registrations.add(signalFrom.connect(signalTo));
    }
    
    @SuppressWarnings("rawtypes")
    public void unregisterAll() {
        for (SignalRegistration reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
    }

}
