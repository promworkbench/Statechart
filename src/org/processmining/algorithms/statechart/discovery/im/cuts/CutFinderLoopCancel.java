package org.processmining.algorithms.statechart.discovery.im.cuts;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.algorithms.statechart.discovery.im.cuts.CutExtended.OperatorExtended;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.graphs.Components;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderLoopCancel implements CutFinder, DfgCutFinder {

    private final IQueryCancelError queryCatchError;

    public CutFinderLoopCancel(IQueryCancelError queryCatchError) {
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
        // ADAPTED FROM CutFinderIMLoop
        
        //initialise the components: each activity gets its own
        Components<XEventClass> components = new Components<XEventClass>(dfg.getActivities());

        if (!dfg.hasStartActivities() || !dfg.hasEndActivities()) {
                return null;
        }

        //merge all start and end activities into one component
        {
                int pivot = dfg.getStartActivityIndices()[0];
                for (int e : dfg.getStartActivityIndices()) {
                        components.mergeComponentsOf(pivot, e);
                }
                for (int e : dfg.getEndActivityIndices()) {
                        components.mergeComponentsOf(pivot, e);
                }
        }

        //merge the other connected components
        for (long edgeIndex : dfg.getDirectlyFollowsEdges()) {
                int source = dfg.getDirectlyFollowsEdgeSourceIndex(edgeIndex);
                int target = dfg.getDirectlyFollowsEdgeTargetIndex(edgeIndex);
                if (!dfg.isStartActivity(source)) {
                    if (!queryCatchError.isCatchError(dfg.getActivityOfIndex(target))) {
                       if (!dfg.isStartActivity(target)) {
                           components.mergeComponentsOf(source, target);
                       }
                    }
                } else {
                  if (!dfg.isEndActivity(source)) {
                    if (!queryCatchError.isCatchError(dfg.getActivityOfIndex(target))) {
                      //source is a start but not an end activity
                      //a redo cannot be reachable from a start activity that is not an end activity
                      components.mergeComponentsOf(source, target);
                    }
                  }
                }
                
//                if (!dfg.isStartActivity(source)) {
//                        if (!dfg.isEndActivity(source)) {
//                                if (!dfg.isStartActivity(target)) {
//                                        //if (!dfg.isEndActivity(target)) { //optimisation: do not perform this check
//                                        //this is an edge inside a sub-component
//                                        components.mergeComponentsOf(source, target);
//                                        //} else {
//                                        //target is an end but not a start activity
//                                        //a redo cannot reach an end activity that is not a start activity
//                                        //      components.mergeComponentsOf(source, target);
//                                        //}
//                                }
//                        }
//                } else {
//                        if (!dfg.isEndActivity(source)) {
//                                //source is a start but not an end activity
//                                //a redo cannot be reachable from a start activity that is not an end activity
//                                components.mergeComponentsOf(source, target);
//                        }
//                }
        }

        /*
         * We have merged all sub-components. We only have to find out whether
         * each sub-component belongs to the body or the redo.
         */
        
        int pivotIndex = dfg.getStartActivityIndices()[0];
        
        //make a list of sub-start and sub-endactivities
//        TIntSet subStartActivities = new TIntHashSet();
        TIntSet subEndActivities = new TIntHashSet();
        for (long edgeIndex : dfg.getDirectlyFollowsEdges()) {
                int source = dfg.getDirectlyFollowsEdgeSourceIndex(edgeIndex);
                int target = dfg.getDirectlyFollowsEdgeTargetIndex(edgeIndex);

                if (!components.areInSameComponent(source, target)) {
                        //target is an sub-end activity and source is a sub-start activity
                        subEndActivities.add(source);
//                        subStartActivities.add(target);
                        
                        // only error edges from body to error redo
                        if (components.areInSameComponent(source, pivotIndex)
                            && !queryCatchError.isCatchError(dfg.getActivityOfIndex(target))) {
                            components.mergeComponentsOf(source, target);
                        }
                }
        }

        //a sub-end activity of a redo should have connections to all start activities
        for (int subEndActivity : subEndActivities.toArray()) {
                for (int startActivity : dfg.getStartActivityIndices()) {
                        if (components.areInSameComponent(subEndActivity, startActivity)) {
                                //this subEndActivity is already in the body
                                break;
                        }
                        if (!dfg.containsDirectlyFollowsEdge(subEndActivity, startActivity)) {
                                components.mergeComponentsOf(subEndActivity, startActivity);
                                break;
                        }
                }
        }

        //a sub-start activity of a redo should be connections from all end activities
//        for (int subStartActivity : subStartActivities.toArray()) {
//                for (int endActivity : dfg.getEndActivityIndices()) {
//                        if (components.areInSameComponent(subStartActivity, endActivity)) {
//                                //this subStartActivity is already in the body
//                                break;
//                        }
//                        if (!dfg.containsDirectlyFollowsEdge(endActivity, subStartActivity)) {
//                                components.mergeComponentsOf(subStartActivity, endActivity);
//                                break;
//                        }
//                }
//        }
        

        //put the start and end activity component first
        List<Set<XEventClass>> partition = components.getComponents();
        Set<XEventClass> partZero = null;
        XEventClass pivot = dfg.getStartActivities().iterator().next();
        for (int i = 0; i < partition.size(); i++) {
                if (partition.get(i).contains(pivot)) {
                        Set<XEventClass> swap = partition.get(0);
                        partZero = partition.get(i);
                        partition.set(0, partZero);
                        partition.set(i, swap);
                        break;
                }
        }
        if (partZero == null) {
            return null;
        }
        
        TIntSet baseActs = new TIntHashSet();
        for (int index : dfg.getActivityIndices()) {
            if (partZero.contains(dfg.getActivityOfIndex(index))) {
                baseActs.add(index);
            }
        }
        
        // check if there are the error edges from 0 to i>0
        TIntSet todos = new TIntHashSet();
        for (int i = 1; i <= partition.size(); i++) {
            todos.add(i);
        }
        for (long edgeIndex : dfg.getDirectlyFollowsEdges()) {
            XEventClass source = dfg.getActivityOfIndex(dfg.getDirectlyFollowsEdgeSourceIndex(edgeIndex));
            XEventClass target = dfg.getActivityOfIndex(dfg.getDirectlyFollowsEdgeTargetIndex(edgeIndex));
            
            int partitionFrom = -1, partitionTo = -1;
            for (int i = 0; i < partition.size(); i++) {
                Set<XEventClass> part = partition.get(i);
                if (part.contains(source)) {
                    partitionFrom = i;
                }
                if (part.contains(target)) {
                    partitionTo = i;
                }
            }
            if (partitionFrom == 0) {
                todos.remove(partitionTo);
            }
        }
        if (!todos.isEmpty()) {
            return null;
        }

//        return new Cut(Operator.loop, partition);
        return new CutExtended(OperatorExtended.LoopCancel, partition);
    }
}
