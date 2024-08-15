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
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class LogSplitterLoopCancel implements LogSplitter {

    private final IQueryCancelError queryCatchError;

    public LogSplitterLoopCancel(IQueryCancelError queryCatchError) {
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
        result.add(_split(log, it.next(), minerState, true, queryCatchError));
        while (it.hasNext()) {
            result.add(_split(log, it.next(), minerState, false, null));
        }
        
        return result;
    }

    private static IMLog _split(IMLog log, Set<XEventClass> sigma, MinerState minerState, 
            boolean firstSigma, IQueryCancelError queryCatchError) {
        IMLog sublog = log.clone();
        for (Iterator<IMTrace> itTrace = sublog.iterator(); itTrace.hasNext();) {

            if (minerState.isCancelled()) {
                return null;
            }

            IMTrace trace = itTrace.next();

            Optional<IMErrorTriggerDecorator> optDecorator = IMErrorTriggerDecorator
                    .getDecorator(trace, true);

            XEvent lastEvent = null;
            boolean lastIn = firstSigma; //whether the last seen event was in sigma
            boolean anyIn = false; //whether there is any event in this subtrace
            MultiSet<XEventClass> openActivityInstances = new MultiSet<>();
            
            for (IMEventIterator itEvent = trace.iterator(); itEvent.hasNext();) {
                XEvent event = itEvent.next();
                XEventClass eventClass = sublog.classify(trace, event);
                XLifeCycleClassifier.Transition transition = log.getLifeCycle(event);
                
                //keep track of open activity instances (by consistency assumption, should work out)
                switch (transition) {
                case start :
                    openActivityInstances.add(eventClass);
                    break;
                case complete :
                    openActivityInstances.remove(eventClass, 1);
                    break;
                case other :
                    break;
                }

                if (sigma.contains(log.classify(trace, event))) {
                    //event of the sigma under consideration

                    if (!lastIn && (firstSigma || anyIn)) {
                        //this is the first activity of a new subtrace, so the part up till now is a completed subtrace

                        itEvent.split();
//                              System.out.println("   split trace " + newTrace + " | " + trace);
                    }
                    lastIn = true;
                    anyIn = true;

                } else {
                    //event of another sigma
                    if (lastEvent != null && optDecorator.isPresent()
                            && queryCatchError != null
                            && queryCatchError.isCatchError(eventClass)) {
                        optDecorator.get().addError(lastEvent, eventClass);
                    }
                    
                    //remove
                    itEvent.remove();

                    //the last seen event was not in sigma
                    if (openActivityInstances.isEmpty()) {
                        //if there are no activity instances open, we can split the trace further ahead
                        lastIn = false;
                    }
                }
                
                lastEvent = event;
            }
            if (!firstSigma && !anyIn) {
                itTrace.remove();
            }
        }
        return sublog;
    }
}
