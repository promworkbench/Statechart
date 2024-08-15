package org.processmining.protocols.statechart.saw;

public enum SawServerStatus {
    Offline("Offline"),
    Starting("Starting"),
    Running("Running"),
    Shutdown("Shutdown"),
    Error("Error");
    
    private final String msg;

    private SawServerStatus(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
 