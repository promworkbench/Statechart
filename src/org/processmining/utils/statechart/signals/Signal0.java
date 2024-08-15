package org.processmining.utils.statechart.signals;

public class Signal0 extends AbstractSignal<Action0, Signal0> {

    public void dispatch() {
        final int size = listeners.size();
        for (int i = 0; i < size; i++) {
            listeners.get(i).call();
        }
    }

    @Override
    protected Action0 _createSignalAction(final Signal0 signalToConnect) {
        return new Action0() {
            @Override
            public void call() {
                signalToConnect.dispatch();
            }
            
        };
    }
}
