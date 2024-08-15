package org.processmining.models.statechart.im.log;

import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;

public interface IMLogHierarchy extends IMLog, Cloneable {

    public IMLogHierarchy deriveLowerlevel();
    
    public boolean addLog(IMLogHierarchy other);
    
    public int getTraceCaseId(IMTrace trace);
}
