package org.processmining.algorithms.statechart.discovery.im.cuts;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.plugins.InductiveMiner.ArrayUtilities;
import org.processmining.plugins.InductiveMiner.Sets;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.graphs.Components;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;
import org.processmining.plugins.InductiveMiner.graphs.StronglyConnectedComponents;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMSequenceReachability;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMSequenceStrict;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIMSequenceCancelAware implements CutFinder, DfgCutFinder {

    private IQueryCancelError queryCatchError;

    public CutFinderIMSequenceCancelAware(IQueryCancelError queryCatchError) {
        this.queryCatchError = queryCatchError;
    }

    public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
            return findCut(logInfo.getDfg(), queryCatchError);
    }

    public Cut findCut(Dfg dfg, DfgMinerState minerState) {
            return findCut(dfg, queryCatchError);
    }

    public Cut findCut(Dfg dfg, IQueryCancelError queryCatchError) {
            return _findCut(dfg, queryCatchError);
    }

    private Cut _findCut(Dfg dfg, IQueryCancelError queryCatchError) {
        @SuppressWarnings("deprecation")
        Graph<XEventClass> graph = dfg.getDirectlyFollowsGraph();

        //compute the strongly connected components of the directly-follows graph
        Set<Set<XEventClass>> SCCs = StronglyConnectedComponents.compute(graph);

        //condense the strongly connected components
        Graph<Set<XEventClass>> condensedGraph1 = GraphFactory.create(Set.class, SCCs.size());
        {

                //15-3-2016: optimisation to look up strongly connected components faster
                TIntIntMap node2sccIndex = new TIntIntHashMap();
                {
                        int i = 0;
                        for (Set<XEventClass> scc : SCCs) {
                                for (XEventClass e : scc) {
                                        node2sccIndex.put(graph.getIndexOfVertex(e), i);
                                }
                                i++;
                        }
                }

                //add vertices (= components)
                for (Set<XEventClass> SCC : SCCs) {
                        condensedGraph1.addVertex(SCC);
                }
                //add edges
                for (long edge : graph.getEdges()) {
                        if (graph.getEdgeWeight(edge) >= 0) {
                                //find the connected components belonging to these nodes
                                int u = graph.getEdgeSourceIndex(edge);
                                int SCCu = node2sccIndex.get(u);
                                int v = graph.getEdgeTargetIndex(edge);
                                int SCCv = node2sccIndex.get(v);

                                //add an edge if it is not internal
                                if (SCCv != SCCu) {
                                        condensedGraph1.addEdge(SCCu, SCCv, 1); //this returns null if the edge was already present
                                }
                        }
                }
        }

        //debug("  nodes in condensed graph 1 " + condensedGraph1.getVertices());

        Collection<Set<Set<XEventClass>>> xorCondensedNodes;
        {
            Components<Set<XEventClass>> components = new Components<Set<XEventClass>>(condensedGraph1.getVertices());

            //condense the pairwise unreachable nodes
            {
                CutFinderIMSequenceReachability scr1 = new CutFinderIMSequenceReachability(condensedGraph1);

                for (int node : condensedGraph1.getVertexIndices()) {
                        TIntSet reachableFromTo = scr1.getReachableFromTo(node);

                        //debug("nodes pairwise reachable from/to " + node.toString() + ": " + reachableFromTo.toString());

                        for (int node2 : condensedGraph1.getVertexIndices()) {
                                if (node != node2 && !reachableFromTo.contains(node2)) {
                                        components.mergeComponentsOf(node, node2);
                                }
                        }

                }
            }
            
            // condense error regions
            {
                CutFinderIMSequenceReachabilityCancelAware scr1 = new CutFinderIMSequenceReachabilityCancelAware(condensedGraph1, queryCatchError);
                
                for (long edgeIndex : condensedGraph1.getEdges()) {
                    int src = condensedGraph1.getEdgeSourceIndex(edgeIndex);
                    int trg = condensedGraph1.getEdgeTargetIndex(edgeIndex);
                    
                    // edge to error
                    if (queryCatchError.containsCatchError(condensedGraph1.getVertexOfIndex(trg))) {
                        // merge source and target
                        components.mergeComponentsOf(src, trg);
                        
                        // merge every node reachable from error, but not from src
                        TIntSet reachableSrcTo = scr1.getReachableTo(src);
                        TIntSet reachableTrgTo = scr1.getReachableTo(trg);
                        for (int node2 : condensedGraph1.getVertexIndices()) {
                            if (src != node2 && !reachableSrcTo.contains(node2)
                                && trg != node2 && reachableTrgTo.contains(node2)) {
                                components.mergeComponentsOf(trg, node2);
                            }
                        }
                    }
                }
            }
            
            //find the connected components to find the condensed xor nodes
            xorCondensedNodes = components.getComponents();
        }
        

        //debug("sccs voor xormerge " + xorCondensedNodes.toString());

        //make a new condensed graph
        final Graph<Set<XEventClass>> condensedGraph2 = GraphFactory.create(Set.class, xorCondensedNodes.size());
        for (Set<Set<XEventClass>> node : xorCondensedNodes) {

                //we need to flatten this s to get a new list of nodes
                condensedGraph2.addVertex(Sets.flatten(node));
        }

        //debug("sccs na xormerge " + condensedGraph2.getVertices().toString());

        //add the edges
        Set<Set<XEventClass>> set = ArrayUtilities.toSet(condensedGraph2.getVertices());
        for (long edge : condensedGraph1.getEdges()) {
                //find the condensed node belonging to this activity
                Set<XEventClass> u = condensedGraph1.getEdgeSource(edge);
                Set<XEventClass> SCCu = Sets.findComponentWith(set, u.iterator().next());
                Set<XEventClass> v = condensedGraph1.getEdgeTarget(edge);
                Set<XEventClass> SCCv = Sets.findComponentWith(set, v.iterator().next());

                //add an edge if it is not internal
                if (SCCv != SCCu) {
                        condensedGraph2.addEdge(SCCu, SCCv, 1); //this returns null if the edge was already present
                        //debug ("nodes in condensed graph 2 " + Sets.implode(condensedGraph2.vertexSet(), ", "));
                }
        }

        // START FIX for cancellation - final SCC merger 
        Set<Set<Set<XEventClass>>> SCCs2 = StronglyConnectedComponents.compute(condensedGraph2);
        
        final Graph<Set<XEventClass>> condensedGraph3 = GraphFactory.create(Set.class, SCCs2.size());
        for (Set<Set<XEventClass>> node : SCCs2) {

                //we need to flatten this s to get a new list of nodes
            condensedGraph3.addVertex(Sets.flatten(node));
        }
        
        Set<Set<XEventClass>> set2 = ArrayUtilities.toSet(condensedGraph3.getVertices());
        for (long edge : condensedGraph2.getEdges()) {
                //find the condensed node belonging to this activity
                Set<XEventClass> u = condensedGraph2.getEdgeSource(edge);
                Set<XEventClass> SCCu = Sets.findComponentWith(set2, u.iterator().next());
                Set<XEventClass> v = condensedGraph2.getEdgeTarget(edge);
                Set<XEventClass> SCCv = Sets.findComponentWith(set2, v.iterator().next());

                //add an edge if it is not internal
                if (SCCv != SCCu) {
                    condensedGraph3.addEdge(SCCu, SCCv, 1); //this returns null if the edge was already present
                        //debug ("nodes in condensed graph 2 " + Sets.implode(condensedGraph2.vertexSet(), ", "));
                }
        }
     // END FIX for cancellation
        
        //now we have a condensed graph. we need to return a sorted list of condensed nodes.
        final CutFinderIMSequenceReachability scr2 = new CutFinderIMSequenceReachability(condensedGraph3);
        List<Set<XEventClass>> result = new ArrayList<Set<XEventClass>>();
        result.addAll(Arrays.asList(condensedGraph3.getVertices()));
        Collections.sort(result, new Comparator<Set<XEventClass>>() {

                public int compare(Set<XEventClass> arg0, Set<XEventClass> arg1) {
                        if (scr2.getReachableFrom(condensedGraph3.getIndexOfVertex(arg0)).contains(
                                condensedGraph3.getIndexOfVertex(arg1))) {
                                return 1;
                        } else {
                                return -1;
                        }
                }

        });

        if (result.size() <= 1) {
                return null;
        }

        /**
         * Optimisation 4-8-2015: do not greedily use the maximal cut, but
         * choose the one that minimises the introduction of taus.
         * 
         * This solves the case {<a, b, c>, <c>}, where choosing the cut {a,
         * b}{c} increases precision over choosing the cut {a}{b}{c}.
         * 
         * Correction 11-7-2016: identify optional sub sequences and merge them.
         */
        Cut newCut = new Cut(Operator.sequence, CutFinderIMSequenceStrict.merge(dfg, result));
        if (newCut.isValid()) {
                return newCut;
        } else {
                return new Cut(Operator.sequence, result);
        }
    }

}
