package org.processmining.protocols.statechart.saw;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.processmining.utils.statechart.signals.Signal1;

public class SawBus {

    public final Signal1<String> bus = new Signal1<String>();
    
    private final Map<String, String> startupMsgs = new HashMap<>();

    public Collection<String> getStartupMessages() {
        return startupMsgs.values();
    }
    
    public void setStartupMessage(String key, String value, boolean broadcastUpdate) {
        startupMsgs.put(key, value);
        if (broadcastUpdate) {
            bus.dispatch(value);
        }
    }
    
}
