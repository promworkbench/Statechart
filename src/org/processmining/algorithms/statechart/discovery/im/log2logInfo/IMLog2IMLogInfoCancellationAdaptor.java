package org.processmining.algorithms.statechart.discovery.im.log2logInfo;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.statechart.decorate.error.IMErrorTriggerDecorator;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;

import com.google.common.base.Optional;

public class IMLog2IMLogInfoCancellationAdaptor implements IMLog2IMLogInfo {

    private IMLog2IMLogInfo baseL2linfo;

    public IMLog2IMLogInfoCancellationAdaptor(IMLog2IMLogInfo baseL2linfo) {
        this.baseL2linfo = baseL2linfo;
    }

    @Override
    public IMLogInfo createLogInfo(IMLog log) {
        // create basic l2l info
        IMLogInfo l2linfo = baseL2linfo.createLogInfo(log);
        Dfg dfg = l2linfo.getDfg();
        
        // adjust for cancelation
        for (IMTrace trace : log) {
            Optional<IMErrorTriggerDecorator> optDecorator = IMErrorTriggerDecorator.getDecorator(trace);
            XEvent lastEvent = null;
            for (XEvent e : trace) {
                lastEvent = e;
            }
            if (lastEvent != null) {
                boolean isErrorTriggerEvent = IMErrorTriggerDecorator.
                        hasAnyErrorTrigger(optDecorator, lastEvent);
                if (isErrorTriggerEvent) {
                    // remove this instance as end activity in case of error trigger
                    XEventClass ec = log.classify(trace, lastEvent);
                    dfg.addEndActivity(ec, -1);
                    
                    // bug fix: DfgImpl uses if (value < 0) then remove activity as end
                    // instead of removing activities when value == 0
                    if (dfg.getEndActivityCardinality(ec) == 0) {
                        dfg.addEndActivity(ec, -1);
                    }
                }
            }
        }
        
        // return result
        return l2linfo;
    }

    @Override
    public boolean useLifeCycle() {
        return baseL2linfo.useLifeCycle();
    }

}
