package org.processmining.algorithms.statechart.discovery.im.cuts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.algorithms.statechart.discovery.im.cuts.CutExtended.OperatorExtended;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.graphs.Components;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.utils.statechart.generic.SetUtil;

public class CutFinderSeqCancel implements CutFinder, DfgCutFinder {

    private final IQueryCancelError queryCatchError;

    public CutFinderSeqCancel(IQueryCancelError queryCatchError) {
        this.queryCatchError = queryCatchError;
    }

    @Override
    public Cut findCut(final IMLog log, final IMLogInfo logInfo,
            final MinerState minerState) {
        return findCut(logInfo.getDfg(), queryCatchError);
    }

    @Override
    public Cut findCut(final Dfg dfg, final DfgMinerState minerState) {
        return findCut(dfg, queryCatchError);
    }

    public static Cut findCut(final Dfg dfg, IQueryCancelError queryCatchError) {
        @SuppressWarnings("deprecation")
        Graph<XEventClass> graph = dfg.getDirectlyFollowsGraph();
        Components<XEventClass> components = new Components<XEventClass>(
                graph.getVertices());

        // connected components (XOR cut), ignoring (x, Error)
        // covers rules 4 & 5
        for (long edgeIndex : graph.getEdges()) {
            int source = graph.getEdgeSourceIndex(edgeIndex);
            int target = graph.getEdgeTargetIndex(edgeIndex);

            if (!queryCatchError.isCatchError(graph.getVertexOfIndex(target))) {
                components.mergeComponentsOf(source, target);
            }
        }
        
        // build datasets
        List<Set<XEventClass>> partition = components.getComponents();
        List<XEventClass> startActivities = new ArrayList<XEventClass>();
        //List<XEventClass> startErrorTargets = new ArrayList<XEventClass>();
//        List<Set<XEventClass>> startErrorTargets = new ArrayList<>();
        for (XEventClass act : dfg.getStartActivities()) {
            startActivities.add(act);
            
//            Set<XEventClass> errorTargets = new HashSet<XEventClass>();
//            startErrorTargets.add(errorTargets);
//
//            for (Long edge : graph.getOutgoingEdgesOf(act)) {
//                XEventClass target = graph.getEdgeTarget(edge);
//                if (queryCatchError.isCatchError(target)) {
//                    errorTargets.add(target);
//                }
//            }
        }

        // order components, such that the part with the start
        // activities (the try body) is the first partition
        // covers rule 2
        boolean hasEntryPart = false;
        for (int i = 0; i < partition.size(); i++) {
            Set<XEventClass> part = partition.get(i);
            if (part.containsAll(startActivities)) {
                Set<XEventClass> swap = partition.get(0);
                partition.set(0, part);
                partition.set(i, swap);
                hasEntryPart = true;
                break;
            }
        }
        
        // if no entry part is found, we already lost
        if (!hasEntryPart) {
            return null;
        }

        Set<XEventClass> errorTargets = new HashSet<>();
        for (XEventClass v : partition.get(0)) {
            for (Long edge : graph.getOutgoingEdgesOf(v)) {
                XEventClass target = graph.getEdgeTarget(edge);
                if (queryCatchError.isCatchError(target)) {
                    errorTargets.add(target);
                }
            }
        }

        // check all components for type
        // checking rules 1 & 3
//        Set<XEventClass> startPart = partition.get(0);
        for (int i = 1; i < partition.size(); i++) {
            Set<XEventClass> part = partition.get(i);
            if (!SetUtil.hasIntersection(part, errorTargets)) {
                return null;
            }
//            boolean ok = true;
//            for (Set<XEventClass> targets : startErrorTargets) {
//                ok = ok && SetUtil.hasIntersection(part, targets);
//            }
//            if (!ok) {
//                return null;
//            }
        }

        return new CutExtended(OperatorExtended.SeqCancel, partition);
        
//        if (hasBase && hasError) {
//            return new CutExtended(OperatorExtended.trycatch, partition);
//        } else {
//            return null;
//        }
//        return null;
    }

}
