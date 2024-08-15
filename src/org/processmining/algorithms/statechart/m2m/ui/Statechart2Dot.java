package org.processmining.algorithms.statechart.m2m.ui;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.algorithms.statechart.m2m.TransformationException;
import org.processmining.models.statechart.decorate.ui.dot.IDotDecorator;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.models.statechart.sc.ISCCompositeState;
import org.processmining.models.statechart.sc.ISCRegion;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.ISCTransition;
import org.processmining.models.statechart.sc.SCStateType;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotCluster;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class Statechart2Dot implements Function<Statechart, Dot> {

    private boolean layoutHorizontal;
    private GraphDirection layoutDir;

    // Dot option maps
    private final Map<String, String> optNodeInvis;
    private final Map<String, String> optEdgeInvis;
    private final Map<String, String> optEdgeTransition;
    private final Map<String, String> optEdgeTransitionReverse;
    private final Map<String, String> optEdgeRecursionBack;
    private final Map<String, String> optEdgeAndDiv;
    private final Map<String, String> optStateSimple;
    private final Map<String, String> optStateEntry;
    private final Map<String, String> optStateExit;
    private final Map<String, String> optStateSplitJoin;
    private final Map<String, String> optStateChoice;
    private final Map<String, String> optStatePoint;
    private final Map<String, String> optStateOrStartPseudo;
    private final Map<String, String> optStateOrEndPseudo;
    private final Map<String, String> optStateLogSimple;

    // Dot element trackers
    // Note: fields shared with friend class Dot2Svg
    // are marked as package private
    private Map<ISCState, DotNode> sc2dot;
    private Map<ISCState, DotCluster> sc2parentdot;
    Set<String> trackAndClusters;
    Map<String, Set<String>> trackAndDivs;
    Set<String> trackStartNodes;
    Set<String> trackEndNodes;
    Set<String> trackRecurrentNodes, trackErrorNodes;
    Set<String> trackCollapsedNodes;
    Set<String> trackExpandedNodes;
    Map<String, ISCState> dotid2sc;
    Map<DotEdge, ISCTransition> trackStartEdges;
    private Map<DotEdge, ISCTransition> decorateEdges;

    private Set<String> selectedIds;

    private boolean recursionBackArrow;
    
    private IDotDecorator<ISCState, ISCTransition, Statechart> dotDecorator;
    private IActivityLabeler activityLabeler;

    public Statechart2Dot(
            IDotDecorator<ISCState, ISCTransition, Statechart> dotDecorator,
            Dot.GraphDirection layoutDir, 
            Set<String> selectedIds, 
            boolean recursionBackArrow, 
            IActivityLabeler activityLabeler) {
        this.dotDecorator = dotDecorator;
        this.layoutDir = layoutDir;
        this.layoutHorizontal = (layoutDir == GraphDirection.leftRight || layoutDir == GraphDirection.rightLeft);
        this.selectedIds = selectedIds;
        this.recursionBackArrow = recursionBackArrow;
        this.activityLabeler = activityLabeler;
        
        // Setup Dot classes
        optNodeInvis = new THashMap<>();
        optNodeInvis.put("style", "invis");
        optNodeInvis.put("shape", "point");

        optEdgeInvis = new THashMap<>();
        optEdgeInvis.put("style", "invis");

        optEdgeTransition = new THashMap<>();
        optEdgeTransition.put("arrowhead", "normal");

        optEdgeTransitionReverse = new THashMap<>();
        optEdgeTransitionReverse.put("arrowhead", "normal");
        optEdgeTransitionReverse.put("dir", "back");

        optEdgeRecursionBack = new THashMap<>();
        optEdgeRecursionBack.put("arrowhead", "normal");
        optEdgeRecursionBack.put("style", "dashed");
        optEdgeRecursionBack.put("dir", "back");
        optEdgeRecursionBack.put("color", "gray");
//        optEdgeRecursionBack.put("weight", "0");
        
        optEdgeAndDiv = new THashMap<>();
        optEdgeAndDiv.put("style", "dashed");
        optEdgeAndDiv.put("dir", "none");

        optStateSimple = new THashMap<>();
        optStateSimple.put("style", "rounded");
        optStateSimple.put("shape", "box");

        optStateLogSimple = new THashMap<>();
        //optStateLogSimple.put("style", "rounded");
//        optStateLogSimple.put("shape", "octagon");
        optStateLogSimple.put("style", "dotted");
        optStateLogSimple.put("shape", "box");
         
        
        optStateEntry = new THashMap<>();
        optStateEntry.put("shape", "circle");

        optStateExit = new THashMap<>();
        optStateExit.put("shape", "doublecircle");

        optStateSplitJoin = new THashMap<>();
        optStateSplitJoin.put("shape", "box");
        optStateSplitJoin.put("style", "filled");
        optStateSplitJoin.put("fillcolor", "black");
        final String sizeSurface = "0.5";
        final String sizeThickness = "0.075";
        if (layoutHorizontal) {
            optStateSplitJoin.put("width", sizeThickness);
            optStateSplitJoin.put("height", sizeSurface);
        } else {
            optStateSplitJoin.put("width", sizeSurface);
            optStateSplitJoin.put("height", sizeThickness);
        }

        optStateChoice = new THashMap<>();
        optStateChoice.put("shape", "diamond");

        optStatePoint = new THashMap<>();
        optStatePoint.put("shape", "point");
        // optStatePoint.put("shape", "circle");
        // optStatePoint.put("style", "filled");
        // optStatePoint.put("width", "0.05");
        // optStatePoint.put("fixedsize", "true");

        optStateOrStartPseudo = new THashMap<>();
        optStateOrStartPseudo.put("shape", "point");
        
        optStateOrEndPseudo = new THashMap<>();
        optStateOrEndPseudo.put("shape", "doublecircle");
        optStateOrEndPseudo.put("style", "filled");
        optStateOrEndPseudo.put("fillcolor", "black");
        optStateOrEndPseudo.put("label", "");
        optStateOrEndPseudo.put("width", "0.05");
        optStateOrEndPseudo.put("height", "0.05");

    }

    @Override
    public Dot apply(Statechart input) {
        return transform(input);
    }

    public Dot transform(Statechart input) {
        Preconditions.checkNotNull(input);

        Dot dot = new Dot();
        dot.setOption("rankdir", layoutDir.getName());
        dot.setOption("forcelabels", "true");
        dot.setOption("compound", "true"); // TODO impact?
        
        // Clear element trackers
        sc2dot = new THashMap<>();
        sc2parentdot = new THashMap<>();
        trackAndClusters = new THashSet<>();
        trackAndDivs = new THashMap<>();

        trackStartNodes = new THashSet<>();
        trackEndNodes = new THashSet<>();
        trackRecurrentNodes = new THashSet<>();
        trackErrorNodes = new THashSet<>();

        trackCollapsedNodes = new THashSet<>();
        trackExpandedNodes = new THashSet<>();
        dotid2sc = new THashMap<>();
        decorateEdges = new THashMap<>();
        sc2dot.put(input, dot);
        
        trackStartEdges = new THashMap<>();
        
        // Apply transformations
        dotDecorator.visitModel(input, dot);

        _transformCompositeContent(input, dot);
        _transformTransitions(input, new ArrayDeque<ISCCompositeState>());

        dotDecorator.finishVisit();
        
        // Decorate
        for(ISCState state : sc2dot.keySet()) {
            DotNode node = sc2dot.get(state);
            dotDecorator.decorateNode(state, node);
            _applySelectionStyle(state, node);
        }
        
        for (DotEdge edge : decorateEdges.keySet()) {
            ISCTransition t = decorateEdges.get(edge);
            if (t != null) {
                dotDecorator.decorateEdge(t, edge);
            }
        }
        
        // finish trackers
        for (ISCRegion region : input.getRegions()) {
            DotNode startNode = sc2dot.get(region.getInitialState());
            trackStartNodes.add(startNode.getId());
            _setOptions(startNode, optStateEntry);

            for (ISCState end : region.getEndStates()) {
                DotNode endNode = sc2dot.get(end);
                trackEndNodes.add(endNode.getId());
                _setOptions(endNode, optStateExit);
            }
        }

        return dot;
    }

    private void _setOptions(DotNode dotNode, Map<String, String> options) {
        for (String key : options.keySet()) {
            if (key.equals("label")) {
                dotNode.setLabel(options.get(key));
            } else {
                dotNode.setOption(key, options.get(key));
            }
        }
    }

    private void _transform(ISCState state, DotCluster parent) {
        if (state instanceof ISCCompositeState) {
            ISCCompositeState comp = (ISCCompositeState) state;
            switch (comp.getStateType()) {
            case AndComposite:
                _transformAndComposite(comp, parent);
                break;
            case OrComposite:
                _transformOrComposite(comp, parent);
                break;
            case SeqCancel:
            case LoopCancel:
                _transformCancel(comp, parent);
                break;
            default:
                throw new TransformationException(
                        "Unexpected State Type for Composite State: "
                                + comp.getStateType());
            }
        } else {
            switch (state.getStateType()) {
            case Simple:
                _transformSimple(state, parent);
                break;
            case Collapsed:
                _transformCollapsed(state, parent);
                break;
            case Recurrent:
                _transformRecurrent(state, parent);
                break;
            case ErrorTrigger:
                _transformErrorTrigger(state, parent);
                break;
            case LogSimple:
                _transformLogSimple(state, parent);
                break;
            case ChoicePseudo:
                _transformChoicePseudo(state, parent);
                break;
            case PointPseudo:
                _transformPointPseudo(state, parent);
                break;
            case OrStartPseudo:
                _transformOrStartPseudo(state, parent);
                break;
            case OrEndPseudo:
                _transformOrEndPseudo(state, parent);
                break;
            case SplitPseudo:
            case JoinPseudo:
                _transformSplitJoinPseudo(state, parent);
                break;
            default:
                throw new TransformationException(
                        "Unexpected State Type for State: "
                                + state.getStateType());
            }
        }
    }

    private void _transformSimple(ISCState state, DotCluster parent) {
        String label = activityLabeler.getLabel(state);
        DotNode node = parent.addNode(label, optStateSimple);
        dotDecorator.visitNode(state);
        sc2dot.put(state, node);
        dotid2sc.put(node.getId(), state);
    }

    private void _transformCollapsed(ISCState state, DotCluster parent) {
        String label = activityLabeler.getLabel(state);
        // space for icon added to label
        DotNode node = parent.addNode("    " + label, optStateSimple);
        dotDecorator.visitNode(state);
        sc2dot.put(state, node);
        trackCollapsedNodes.add(node.getId());
        dotid2sc.put(node.getId(), state);
    }

    private void _transformRecurrent(ISCState state, DotCluster parent) {
        String label = activityLabeler.getLabel(state);
        // space for icon added to label
        DotNode node = parent.addNode("    " + label, optStateSimple);
        dotDecorator.visitNode(state);
        sc2dot.put(state, node);
        trackRecurrentNodes.add(node.getId());
        dotid2sc.put(node.getId(), state);
    }

    private void _transformErrorTrigger(ISCState state, DotCluster parent) {
        String label = activityLabeler.getLabel(state);
        DotNode node = parent.addNode("    " + label, optStateSimple);
        dotDecorator.visitNode(state);
        sc2dot.put(state, node);
        trackErrorNodes.add(node.getId());
        dotid2sc.put(node.getId(), state);
    }

    private void _transformLogSimple(ISCState state, DotCluster parent) {
//        String label = activityLabeler.getLabel(state);
        DotNode node = parent.addNode("<<i>Log Move</i>>", optStateLogSimple);
        dotDecorator.visitNode(state);
        sc2dot.put(state, node);
//        trackErrorNodes.add(node.getId());
        dotid2sc.put(node.getId(), state);
    }

    private void _transformChoicePseudo(ISCState state, DotCluster parent) {
        String label = activityLabeler.getLabel(state);
        DotNode node = parent.addNode(label, optStateChoice);
        dotDecorator.visitNode(state);
        sc2dot.put(state, node);
    }

    private void _transformPointPseudo(ISCState state, DotCluster parent) {
        String label = activityLabeler.getLabel(state);
        DotNode node = parent.addNode(label, optStatePoint);
        dotDecorator.visitNode(state);
        sc2dot.put(state, node);
    }

    private void _transformOrEndPseudo(ISCState state, DotCluster parent) {
        String label = activityLabeler.getLabel(state);
        DotNode node = parent.addNode(label, optStateOrEndPseudo);
        dotDecorator.visitNode(state);
        sc2dot.put(state, node);
    }

    private void _transformOrStartPseudo(ISCState state, DotCluster parent) {
        String label = activityLabeler.getLabel(state);
        DotNode node = parent.addNode(label, optStateOrStartPseudo);
        dotDecorator.visitNode(state);
        sc2dot.put(state, node);
    }

    private void _transformSplitJoinPseudo(ISCState state, DotCluster parent) {
        String label = activityLabeler.getLabel(state);
        DotNode node = parent.addNode(label, optStateSplitJoin);
        dotDecorator.visitNode(state);
        sc2dot.put(state, node);
    }

    private void _transformAndComposite(ISCCompositeState comp,
            DotCluster parent) {
        // add cluster for composite state
        DotCluster andCluster = parent.addCluster();
        dotDecorator.visitNode(comp);
        sc2dot.put(comp, andCluster);
        _setOptsComposite(andCluster, comp.getLabel());
        
        String clusterId = andCluster.getId();
        trackAndClusters.add(clusterId);
        Set<String> clusterDivSet = new THashSet<>();
        trackAndDivs.put(clusterId, clusterDivSet);

        // add contents
        List<ISCRegion> regions = comp.getRegions();
        for (int i = 0; i < regions.size(); i++) {
            // add region
            ISCRegion region = regions.get(i);
            DotCluster curRegionCluster = andCluster.addCluster();
//            _setOptsRegion(curRegionCluster);

            String curRegionClusterId = curRegionCluster.getId();

            // add tracker for dotted AND division
            clusterDivSet.add(curRegionClusterId);

            for (ISCState child : region.getStates()) {
                _transform(child, curRegionCluster);
            }
        }
    }

    private void _transformOrComposite(ISCCompositeState comp, DotCluster parent) {
        // add cluster for composite state
        DotCluster cluster = parent.addCluster();
        dotDecorator.visitNode(comp);
        sc2dot.put(comp, cluster);
        String label = activityLabeler.getLabel(comp);
        // space for icon added to label
        _setOptsComposite(cluster, "    " + label);
        

        String clusterId = cluster.getId();
        trackExpandedNodes.add(clusterId);
        dotid2sc.put(clusterId, comp);

        // add contents
        _transformCompositeContent(comp, cluster);
    }

    private void _transformCompositeContent(ISCCompositeState comp,
            DotCluster parent) {
        // root Statechart is of type Or

        // add contents
        for (ISCRegion region : comp.getRegions()) {
            // add region
            for (ISCState child : region.getStates()) {
                _transform(child, parent);
            }
        }
    }

    private void _transformCancel(ISCCompositeState comp, DotCluster parent) {
        // add cluster for cancelation region
        DotCluster cluster = parent.addCluster();
        dotDecorator.visitNode(comp);
        sc2dot.put(comp, cluster);
        sc2parentdot.put(comp, parent);
        _setOptsComposite(cluster, "");

//        String clusterId = cluster.getId();
//        trackExpandedNodes.add(clusterId);
//        dotid2sc.put(clusterId, comp);
        
        // add contents
        List<ISCRegion> regions = comp.getRegions();
        
        // first region is try, inside the cancelation region
        ISCRegion region = regions.get(0);
        for (ISCState child : region.getStates()) {
            _transform(child, cluster);
        }
        
        // other regions are catch, outside the cancelation region
        for (int i = 1; i < regions.size(); i++) {
            region = regions.get(i);
            for (ISCState child : region.getStates()) {
                _transform(child, parent);
            }
        }
    }

    private void _setOptsComposite(DotCluster cluster, String label) {
        cluster.setOption("style", "rounded");
        cluster.setOption("shape", "box");
        cluster.setOption("label", label);
        cluster.setOption("labeljust", "l");
    }

//    private void _setOptsRegion(DotCluster cluster) {
//        // cluster.setOption("style", "invis");
//        // keep border to compute correct bounding-box on svg side
//        // border is removed again on the svg side
//    }

    private void _transformTransitions(ISCCompositeState comp, Deque<ISCCompositeState> path) {
        DotCluster parent = (DotCluster) sc2dot.get(comp);

        // add contents
        boolean firstRegion = true;
        for (ISCRegion region : comp.getRegions()) {

            // track path of named composites
            if (comp.getStateType() == SCStateType.OrComposite) {
                path.push(comp);
            }
            for (ISCState child : region.getStates()) {
                // recurse for more transitions
                if (child.getStateType().isCompositeType()) {
                    _transformTransitions((ISCCompositeState) child, path);
                }

                // recursion back-arrow
                if (child.getStateType() == SCStateType.Recurrent 
                        && recursionBackArrow) {
                    Iterator<ISCCompositeState> it = path.iterator();
                    ISCCompositeState referencedState = null;
                    while (referencedState == null && it.hasNext()) {
                        ISCCompositeState candidate = it.next();
                        if (candidate.getLabel().equals(child.getLabel())) {
                            referencedState = candidate;
                        }
                    }

                    if (referencedState != null
                            && !referencedState.getRegions().isEmpty()) {
                        ISCRegion refRegion = referencedState.getRegions().get(0);
                        ISCState entryState = refRegion.getInitialState();
                        DotCluster refParent = (DotCluster) sc2dot.get(referencedState);

                        DotNode from = sc2dot.get(entryState);
                        DotNode to = sc2dot.get(child);
                        if (from == null || to == null) {
                            throw new TransformationException("Could not find endpoints for recursion back edge");
                        }

                        DotEdge e = refParent.addEdge(from, to, "",
                                optEdgeRecursionBack);
//                        dotDecorator.decorateRecursionBackArrow(child, entryState, e);
                        decorateEdges.put(e, null); // TODO how to decorate back arrows -> model explicitly?
                    }
                }
            }
            // track path of named composites - cleanup
            if (comp.getStateType() == SCStateType.OrComposite) {
                path.pop();
            }
            
            // actual transitions transformation
            for (ISCTransition edge : region.getTransitions()) {
                DotNode from = sc2dot.get(edge.getFrom());
                DotNode to = sc2dot.get(edge.getTo());
                if (from == null || to == null) {
                    throw new TransformationException(
                            String.format(
                                    "Edge endpoints not found for Composite. "
                                    + "Edge region: %s, From %s (%b) region: %s, To %s (%b) region: %s",
                                    region.getId(), edge.getFrom().getId(),
                                    from != null, edge.getFrom()
                                            .getParentRegion().getId(), edge
                                            .getTo().getId(), to != null, edge
                                            .getTo().getParentRegion().getId()));
                }

                // lift transition parent for cancellation cases
                DotCluster edgeContainer = parent;
                if ((comp.getStateType() == SCStateType.SeqCancel 
                        || comp.getStateType() == SCStateType.LoopCancel
                    ) && !firstRegion) {
                    edgeContainer = sc2parentdot.get(comp);
                }
                
                // check for case where transition is from a cluster
                // (e.g., in the case of a cancellation arc in a cancellation setup)
                String ltail = null;
                if (from instanceof DotCluster) {
                    ltail = from.getId();
                    if (!((DotCluster) from).getNodes().isEmpty()) {
                        from = ((DotCluster) from).getNodes().get(0);
                    }
                }
                
                // check for case where transition is to a named cluster
                // (e.g., in the case of an arc to a OrStartPseudo named subtree
                /* TODO: DOT Error: trouble in init_rank for JUnit 4.12, RAD, path 45%
                String lhead = null;
                if (edge.getTo().getStateType() == SCStateType.OrStartPseudo) {
                    lhead = sc2dot.get(edge.getTo().getParent()).getId();
                }
                */
                
                // create edge transition
                DotEdge e;
                if (!edge.isReverse()) {
                    e = edgeContainer.addEdge(from, to, edge.getLabel(),
                            optEdgeTransition);
                } else {
                    e = edgeContainer.addEdge(to, from, edge.getLabel(),
                            optEdgeTransitionReverse);
                }
                if (ltail != null) {
                    if (edge.isReverse()) {
                        e.setOption("lhead", ltail);
                    } else {
                        e.setOption("ltail", ltail);
                    }
                }
                /* 
                if (lhead != null) {
                    if (edge.isReverse()) {
                        e.setOption("ltail", lhead);
                    } else {
                        e.setOption("lhead", lhead);
                    }
                }
                */
                dotDecorator.visitEdge(edge);
                decorateEdges.put(e, edge);

                if (edge.getTo().getStateType() == SCStateType.OrStartPseudo) {
                    trackStartEdges.put(e, edge);
                }
            }

            firstRegion = false;
        }
    }

    private void _applySelectionStyle(ISCState state, DotNode node) {
        // TODO: move to the SVG part?
        if (selectedIds.contains(state.getId())) {
            node.setOption("color", "red");
            node.setOption("penwidth", "3");
//            node.setOption("fontcolor", "red");
        } else {
            node.setOption("penwidth", "1");
            String color = node.getOption("color");
            if (color == null || color.isEmpty()) {
                node.setOption("color", "black");
            }
        }
    }

//    private Map<ISCState, SummaryStatistics> stateOutState = new THashMap<>();
//
//    private void _applyAnnotationStyle(ISCState state, DotNode node) {
//        node.setOption("color", "black");
//        node.setOption("fontcolor", "black");
//        if (!state.isPseudoState() && !(node instanceof DotCluster)) {
////            node.setOption("fontcolor", "black");
////            node.setOption("color", "black");
//            node.setOption("style", "rounded,filled");
//
//            @SuppressWarnings("unchecked")
//            SCFreqMetricDecorator<ISCState> stateFreqMetrics = stateDecorations
//                    .getForType(SCFreqMetricDecorator.class);
//
//            FreqMetric metrics = stateFreqMetrics.getDecoration(state);
//            if (metrics != null) {
//                // compute state metrics
//                StatisticalSummary stats = stateFreqMetrics.getStatsAbsolute();
//                int freq = metrics.getFreqAbsolute();
//                double freqPercent = 1.0;
//                double freqScale = 1.0;
//                if (stats.getMax() > 0 && stats.getMax() > stats.getMin()) {
//                    freqPercent = (double) freq / (double) stats.getMax();
//                    freqScale = (double) (freq - stats.getMin())
//                            / (double) (stats.getMax() - stats.getMin());
//                }
//
//                // set state style
//                Color color = ColorUtil.lerp(new Color(241, 238, 246),
//                        new Color(4, 86, 134), freqScale);
//                Color textColor = ColorUtil.switchContrasting(color,
//                        Color.white, Color.black);
//                node.setOption("fillcolor", ColorUtil.rgbToHexString(color));
//                node.setOption("fontcolor", ColorUtil.rgbToHexString(textColor));
//                node.setLabel(String.format("%s\n%d (%.1f%%)", node.getLabel(),
//                        freq, freqPercent * 100.0));
//            } else {
//                node.setOption("fillcolor", "white");
//            }
//        }
//    }
//
//    private void _applyAnnotationStyle(ISCTransition transition, DotEdge edge) {
//
//        @SuppressWarnings("unchecked")
//        SCFreqMetricDecorator<ISCTransition> transFreqMetrics = transitionDecorations
//                .getForType(SCFreqMetricDecorator.class);
//        FreqMetric metrics = transFreqMetrics.getDecoration(transition);
//        if (metrics != null) {
//            // compute transition metrics
//            ISCState fromState = transition.getFrom();
//            SummaryStatistics outStats = stateOutState.get(fromState);
//
//            // compute stats from oudgoing transitions
//            if (outStats == null) {
//                outStats = new SummaryStatistics();
//                for (ISCTransition t : fromState.getInvolvedTransitions()) {
//                    if (t.getFrom() == fromState) {
//                        FreqMetric Tmetrics = transFreqMetrics.getDecoration(t);
//                        if (Tmetrics != null) {
//                            outStats.addValue(Tmetrics.getFreqAbsolute());
//                        }
//                    }
//                }
//                stateOutState.put(fromState, outStats);
//            }
//
//            int freqLowerBound = (int) outStats.getMin();
//            int freqUpperBound = (int) outStats.getSum();
//            if (fromState.getStateType() == SCStateType.SplitPseudo) {
//                freqUpperBound = (int) outStats.getMax();
//            }
//
//            int freq = metrics.getFreqAbsolute();
//            double freqPercent = (double) freq / (double) freqUpperBound;
//            double freqScale = 1.0;
//            if (freqUpperBound != freqLowerBound) {
//                freqScale = (double) (freq - freqLowerBound)
//                        / (double) (freqUpperBound - freqLowerBound);
//            }
//
//            // set state style
//            Color color = ColorUtil.lerp(new Color(137, 137, 137), Color.black,
//                    freqScale);
//            edge.setOption("color", ColorUtil.rgbToHexString(color));
//            edge.setOption("penwidth", Double.toString(1.0 + freqScale * 2.0));
//            edge.setLabel(String.format("%d\n(%.1f%%)", freq,
//                    freqPercent * 100.0));
////            edge.setOption("headlabel", String.format("%d\n(%.1f%%)", freq,
////                    freqPercent * 100.0));
////            edge.setOption("label", " ");
//
//        }
//    }

}
