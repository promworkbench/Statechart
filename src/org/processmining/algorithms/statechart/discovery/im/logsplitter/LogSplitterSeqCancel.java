package org.processmining.algorithms.statechart.discovery.im.logsplitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.models.statechart.decorate.error.IMErrorTriggerDecorator;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class LogSplitterSeqCancel implements LogSplitter {

    private final IQueryCancelError queryCatchError;

    public LogSplitterSeqCancel(IQueryCancelError queryCatchError) {
        this.queryCatchError = queryCatchError;
    }

    public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut,
            MinerState minerState) {
        return new LogSplitResult(split(log, cut.getPartition(), minerState,
                queryCatchError), new MultiSet<XEventClass>());
    }

    public static List<IMLog> split(IMLog log,
            Collection<Set<XEventClass>> partition, MinerState minerState,
            IQueryCancelError queryCatchError) {
        // MODIFIED VERSION -- basis taken from LogSplitterXor
        // ADDED -- decorate with ErrorTriggerDecorator
        Preconditions.checkArgument(partition.size() > 1);
        
        List<IMLog> result = new ArrayList<>();
        
        Iterator<Set<XEventClass>> it = partition.iterator();
        result.add(_split(log, it.next(), minerState, queryCatchError));
        while (it.hasNext()) {
            result.add(_split(log, it.next(), minerState, null));
        }
        
        return result;
    }

    private static IMLog _split(IMLog log, Set<XEventClass> sigma,
            MinerState minerState, IQueryCancelError queryCatchError) {
        IMLog sublog = log.clone();
        for (Iterator<IMTrace> itTrace = sublog.iterator(); itTrace.hasNext();) {
                
                if (minerState.isCancelled()) {
                        return null;
                }
                
                IMTrace trace = itTrace.next();

                Optional<IMErrorTriggerDecorator> optDecorator = 
                        IMErrorTriggerDecorator.getDecorator(trace, true);
                
                XEvent lastEvent = null;
                for (Iterator<XEvent> it = trace.iterator(); it.hasNext();) {
                        XEvent event = it.next();
                        XEventClass c = sublog.classify(trace, event);
                        if (!sigma.contains(c)) {
                            if (lastEvent != null && optDecorator.isPresent()
                                    && queryCatchError != null
                                    && queryCatchError.isCatchError(c)) {
                                optDecorator.get().addError(lastEvent, c);
                            }
                            it.remove();
                        }
                        lastEvent = event;
                }
                if (trace.isEmpty()) {
                        itTrace.remove();
                }
        }
        return sublog;
    }

}
