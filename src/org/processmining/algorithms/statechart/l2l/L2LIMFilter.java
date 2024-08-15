package org.processmining.algorithms.statechart.l2l;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;

import com.google.common.base.Preconditions;

public class L2LIMFilter {

    public static class Parameters {
        public double threshold; // percentage of activities to keep (0.0 - 1.0)
        
        public Parameters() {
            threshold = 1.0;
        }
    }

    private final Parameters params;
    
    public L2LIMFilter(Parameters params) {
        Preconditions.checkNotNull(params, "No parameters set");
        this.params = params;
    }
    
    public IMLog transform(IMLog input) {
        IMLog2IMLogInfo info = new IMLog2IMLogInfoDefault();
        IMLogInfo loginfo = info.createLogInfo(input);
        // original: IvM  FilterLeastOccurringActivities
        // Note: to filter correctly across list levels,
        // make sure to use the XEventListLabelClassifier classifier
//        return filter(input, loginfo, params.threshold, info);
        return filter(input, loginfo, params.threshold);
    }

    public IMLog filter(IMLog log, IMLogInfo logInfo, double threshold) {
        List<XEventClass> list = logInfo.getActivities().sortByCardinality();
        int lastIndex = (int) Math.floor((1 - threshold) * list.size());
        
        Set<XEventClass> keep = new HashSet<XEventClass>(list.subList(lastIndex, list.size()));
        return filter(log, keep);
        
    }
    // org.processmining.plugins.inductiveVisualMiner.logFiltering.FilterLeastOccurringActivities
    // TODO make nicer or reuse existing implementation
//    public IMLog filter(IMLog log, IMLogInfo logInfo, double threshold,
//            IMLog2IMLogInfo log2logInfo) {
//    List<XEventClass> list = logInfo.getActivities().sortByCardinality();
//    int lastIndex = (int) Math.floor((1 - threshold) * list.size());

    //make a cut to filter
//    Set<XEventClass> remove = new HashSet<XEventClass>(list.subList(0, lastIndex));
//    Set<XEventClass> keep = new HashSet<XEventClass>(list.subList(lastIndex, list.size()));
//    return filter(log, keep);
//    List<Set<XEventClass>> partition = new ArrayList<Set<XEventClass>>();
//    partition.add(keep);
//    partition.add(remove);
//    Cut cut = new Cut(Operator.concurrent, partition);
//    MinerState minerState = new MinerState(new MiningParametersSC(), new Canceller() {
//            public boolean isCancelled() {
//                    return false;
//            }
//    });
//    LogSplitResult result = Miner.splitLog(log, logInfo, cut, minerState);
//    
//    //IMLogInfo filteredLogInfo = log2logInfo.createLogInfo(result.sublogs.get(0));
//    
//    return result.sublogs.get(0);
//}

    private IMLog filter(IMLog log, Set<XEventClass> keep) {
        IMLog sublog = log.clone();
        for (IMTrace trace : sublog) {
            for (Iterator<XEvent> it = trace.iterator(); it.hasNext();) {
                    XEventClass c = sublog.classify(trace, it.next());
                    if (!keep.contains(c)) {
                            it.remove();
                    }
            }
        }
        return sublog;
    }
}
