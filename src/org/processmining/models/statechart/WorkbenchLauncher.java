package org.processmining.models.statechart;

import java.lang.ref.SoftReference;

import org.deckfour.xes.model.XLog;

public class WorkbenchLauncher {
    public final SoftReference<XLog> xLog;
    
    public WorkbenchLauncher(XLog log) {
        xLog = new SoftReference<XLog>(log);
    }
}
