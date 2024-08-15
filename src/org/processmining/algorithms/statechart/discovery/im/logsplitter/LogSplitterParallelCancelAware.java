package org.processmining.algorithms.statechart.discovery.im.logsplitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.statechart.decorate.error.IMErrorTriggerDecorator;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;

import com.google.common.base.Optional;

public class LogSplitterParallelCancelAware implements LogSplitter {

    public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
            return new LogSplitResult(split(log, cut.getPartition(), minerState), new MultiSet<XEventClass>());
    }

    public static List<IMLog> split(IMLog log, Collection<Set<XEventClass>> partition, MinerState minerState) {
        // MODIFIED VERSION -- variant of LogSplitterParallel
        // ADDED -- remove post-error-trigger empty traces
        
            List<IMLog> result = new ArrayList<>();
            for (Set<XEventClass> sigma : partition) {
                    IMLog sublog = log.clone();
                    
                    Iterator<IMTrace> itTrace = sublog.iterator();
                    //for (IMTrace trace : sublog) {
                    while (itTrace.hasNext()) {
                        IMTrace trace  = itTrace.next();

                        if (minerState.isCancelled()) {
                                return null;
                        }

                        Optional<IMErrorTriggerDecorator> optDecorator = IMErrorTriggerDecorator.getDecorator(trace);
                        
                        boolean endedWithErrorTrigger = false;
                        
                        for (Iterator<XEvent> it = trace.iterator(); it.hasNext();) {
                            XEvent event = it.next();
                            XEventClass c = sublog.classify(trace, event);
                            if (!sigma.contains(c)) {
                                    it.remove();
                            }
                            
                            endedWithErrorTrigger = IMErrorTriggerDecorator.hasAnyErrorTrigger(optDecorator, event);
                        }

                        // check if trace ended with error (i.e., a partial trace)
                        // is from now on empty, if so, recognize as a partial trace
                        // and remove from this sigma's event log
                        if (endedWithErrorTrigger && trace.isEmpty()) {
                            itTrace.remove();
                        }
                    }
                    result.add(sublog);
            }
            return result;
    }
}
