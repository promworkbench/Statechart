package org.processmining.algorithms.statechart.discovery.im.cuts;

import java.util.Set;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.plugins.InductiveMiner.graphs.Graph;

public class CutFinderIMSequenceReachabilityCancelAware {

    private TIntObjectMap<TIntSet> reachableTo;
    private TIntObjectMap<TIntSet> reachableFrom;
    private Graph<Set<XEventClass>> condensedGraph;
    
    private IQueryCancelError queryCatchError;

    public CutFinderIMSequenceReachabilityCancelAware(Graph<Set<XEventClass>> graph, IQueryCancelError queryCatchError) {
            reachableTo = new TIntObjectHashMap<>();
            reachableFrom = new TIntObjectHashMap<>();
            this.condensedGraph = graph;
            this.queryCatchError = queryCatchError;
    }

    public TIntSet getReachableFromTo(int node) {
            TIntSet r = new TIntHashSet(findReachableTo(node));
            r.addAll(findReachableFrom(node));
            return r;
    }

    public TIntSet getReachableFrom(int node) {
            return findReachableFrom(node);
    }

    public TIntSet getReachableTo(int node) {
            return findReachableTo(node);
    }

    private TIntSet findReachableTo(int from) {
            if (!reachableTo.containsKey(from)) {
                    TIntSet reached = new TIntHashSet();

                    reachableTo.put(from, reached);

                    for (long edge : condensedGraph.getOutgoingEdgesOf(from)) {
                            int target = condensedGraph.getEdgeTargetIndex(edge);
                            Set<XEventClass> targetV = condensedGraph.getVertexOfIndex(target);
                            if (!queryCatchError.containsCatchError(targetV)) {
                                reached.add(target);
    
                                //recurse
                                reached.addAll(findReachableTo(target));
                            }
                    }
            }
            return reachableTo.get(from);
    }

    private TIntSet findReachableFrom(int to) {
            Set<XEventClass> targetV = condensedGraph.getVertexOfIndex(to);
            if (!reachableFrom.containsKey(to)) {
                TIntSet reached = new TIntHashSet();

                reachableFrom.put(to, reached);

                if (!queryCatchError.containsCatchError(targetV)) {
                    for (long edge : condensedGraph.getIncomingEdgesOf(to)) {
                            int source = condensedGraph.getEdgeSourceIndex(edge);
                            reached.add(source);

                            //recurse
                            reached.addAll(findReachableFrom(source));
                    }
                }
            }
            return reachableFrom.get(to);
    }
}
