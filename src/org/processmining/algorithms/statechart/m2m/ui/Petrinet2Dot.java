package org.processmining.algorithms.statechart.m2m.ui;

import gnu.trove.map.hash.THashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.processmining.algorithms.statechart.m2m.EPTree2Petrinet;
import org.processmining.models.graphbased.directed.AbstractDirectedGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.ExpandableSubNet;
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.statechart.decorate.ui.dot.IDotDecorator;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotCluster;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.utils.statechart.svg.DotUtil;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class Petrinet2Dot implements Function<PetrinetDecorated, Dot> {

    private boolean layoutHorizontal;
    private GraphDirection layoutDir;
    
    // Dot option maps
    private final Map<String, String> optTransitionTau;
    private final Map<String, String> optTransitionTauShade;
    private final Map<String, String> optTransition;
    private final Map<String, String> optPlace;
    private final Map<String, String> optEdge, optEdgeReset, optEdgeInhibit;
    private final Map<String, String> optEdgeShade;
    private final Map<String, String> optPlaceExit;
    private final Map<String, String> optSubnet;
    private final Map<String, String> optCancelRegion;
    private final Map<String, String> optCancelEdge;

    private Set<String> selectedIds;
    private IActivityLabeler activityLabeler;
    
    private IDotDecorator<Transition, Arc, PetrinetDecorated> dotDecorator;

    // Dot element trackers
    // Note: fields shared with friend class Dot2Svg
    // are marked as package private
    Map<String, String> dotid2nodeid;
    
    public Petrinet2Dot(GraphDirection layoutDir,
            Set<String> selectedIds, 
            IActivityLabeler activityLabeler,
            IDotDecorator<Transition, Arc, PetrinetDecorated> dotDecorator) {
        this.layoutDir = layoutDir;
        this.layoutHorizontal = (layoutDir == GraphDirection.leftRight || layoutDir == GraphDirection.rightLeft);
        this.selectedIds = selectedIds;
        this.activityLabeler = activityLabeler;
        this.dotDecorator = dotDecorator;
        
        optEdge = new THashMap<>();
        optEdge.put("arrowhead", "normal");

        optEdgeReset = new THashMap<>();
        optEdgeReset.put("arrowhead", "vee");
        optEdgeReset.put("color", "gray");

        optEdgeInhibit = new THashMap<>();
        optEdgeInhibit.put("arrowhead", "odot");
        optEdgeInhibit.put("color", "gray");
        
        optEdgeShade = new THashMap<>();
        optEdgeShade.put("arrowhead", "normal");
        optEdgeShade.put("color", "gray");

        optTransition = new THashMap<>();
        optTransition.put("shape", "box");

        optPlace = new THashMap<>();
        optPlace.put("shape", "circle");
        
        optTransitionTau = new THashMap<>();
        optTransitionTau.put("shape", "box");
        optTransitionTau.put("style", "filled");
        optTransitionTau.put("fillcolor", "black");
        final String sizeSurface = "0.4";
        final String sizeThickness = "0.075";
        if (layoutHorizontal) {
            optTransitionTau.put("width", sizeThickness);
            optTransitionTau.put("height", sizeSurface);
        } else {
            optTransitionTau.put("width", sizeSurface);
            optTransitionTau.put("height", sizeThickness);
        }
        
        optTransitionTauShade = new THashMap<>(optTransitionTau);
        optTransitionTauShade.put("fillcolor", "gray");
        
        optPlaceExit = new THashMap<>();
        optPlaceExit.put("shape", "doublecircle");
        
        optSubnet = new THashMap<>();
        optSubnet.put("style", "rounded");
        optSubnet.put("shape", "box");
        optSubnet.put("labeljust", "l");
        
        optCancelRegion = new THashMap<>();
        optCancelRegion.put("style", "rounded,dashed");
        optCancelRegion.put("shape", "box");
        optCancelRegion.put("labeljust", "l");
        
        optCancelEdge = new THashMap<>();
        optCancelEdge.put("style", "dashed");
        optCancelEdge.put("arrowhead", "none");
        optCancelEdge.put("color", "gray");
    }
    
    @Override
    public Dot apply(PetrinetDecorated input) {
        return transform(input);
    }

    public Dot transform(PetrinetDecorated input) {
        Preconditions.checkNotNull(input);
        Marking source = input.getInitialMarking();
        Set<Marking> sinks = input.getFinalMarkings();

        dotid2nodeid = new THashMap<>();
        
        Dot dot = new Dot();
        dot.setOption("rankdir", layoutDir.getName());
        dot.setOption("forcelabels", "true");
        dot.setOption("compound", "true");

        // decorations precalc
        dotDecorator.visitModel(input, dot);
        for (Transition t : input.getTransitions()) {
            if (!t.isInvisible()) {
                dotDecorator.visitNode(t);
            }
        }
        for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e 
                : input.getEdges()) {
            if (e instanceof Arc) {
                dotDecorator.visitEdge((Arc) e);
            }
        }
        dotDecorator.finishVisit();
        
        // deal with groups / subnets
        Map<ExpandableSubNet, DotCluster> mapGroups = new HashMap<>();
        for (ExpandableSubNet group : input.getGroups()) {
            DotCluster parentDot = mapGroups.get(group.getParent());
            if (parentDot == null) {
                parentDot = dot;
            }
            DotCluster cluster = parentDot.addCluster();
            String label = group.getLabel();
            label = activityLabeler.getLabel(label);
            
            if (group.getAttributeMap().containsKey(EPTree2Petrinet.KeyCancelRegion)) {
                DotUtil.setOptions(cluster, optCancelRegion);
                cluster.setLabel(label);
            } else {
                DotUtil.setOptions(cluster, optSubnet);
                cluster.setLabel(label);
            }
            
            Object nodeId = group.getAttributeMap().get(EPTree2Petrinet.Key_NodeId);
            if (nodeId != null && nodeId instanceof String) {
                dotid2nodeid.put(cluster.getId(), (String) nodeId);
            }
            
            mapGroups.put(group, cluster);
        }
        
        Map<PetrinetNode, DotNode> mapNodes = new HashMap<>();
        
        for (Place p : input.getPlaces()) {
            DotCluster parentDot = mapGroups.get(p.getParent());
            if (parentDot == null) {
                parentDot = dot;
            }
            
            DotNode node = null;
            if (source != null && source.contains(p)) {
                String label = source.occurrences(p).toString();
                node = dot.addNode(label, optPlace);
            } else {
                boolean foundSink = false;
                if (sinks != null) {
                    for (Marking s : sinks) {
                        if (s.contains(p)) {
                            foundSink = true;
                        }
                    }
                }
                if (foundSink) {
                    node = dot.addNode("", optPlaceExit);
                } else {
                    node = parentDot.addNode("", optPlace);
                }
            }
            
            mapNodes.put(p, node);
        }

        for (Transition t : input.getTransitions()) {
            DotCluster parentDot = mapGroups.get(t.getParent());
            if (parentDot == null) {
                parentDot = dot;
            }
            
            DotNode node = null;
            if (t.isInvisible()) {
                if (t.getLabel().equals(EPTree2Petrinet.LabelTauErrorClear)) {
                    node = parentDot.addNode("", optTransitionTauShade);
                } else {
                    node = parentDot.addNode("", optTransitionTau);
                }
                if (t.getLabel().equals(EPTree2Petrinet.LabelTauErrorTrigger)) {
                    // source is in cancel region, target is not
                    // create cancel region trigger edge using LCA - 1 on left subtree 
                    // to determine cluster to link to
                    Place fromPlace = getFromPlace(t);
                    DotNode from = mapNodes.get(fromPlace);
                    DotNode to = node;
                    if (fromPlace != null && from != null) {
                        ExpandableSubNet cancelRegion = determineRegion(fromPlace, t);
                        if (cancelRegion != null) {
                            String ltail = mapGroups.get(cancelRegion).getId();
                            if (from != null) {
                                DotEdge e = parentDot.addEdge(
                                    from, to, "",
                                    optCancelEdge);
                                e.setOption("ltail", ltail);
                            }
                        }
                    }
                }
            } else {
                String label = t.getLabel();
                label = activityLabeler.getLabel(label);
                if (t.getAttributeMap().containsKey(EPTree2Petrinet.KeyErrorTrigger)) {
                    label = " * " + label;
                }
                node = parentDot.addNode(label, optTransition);
                dotDecorator.decorateNode(t, node);
                _applySelectionStyle(t, node);
            }

            Object nodeId = t.getAttributeMap().get(EPTree2Petrinet.Key_NodeId);
            if (nodeId != null && nodeId instanceof String) {
                dotid2nodeid.put(node.getId(), (String) nodeId);
            }
            
            mapNodes.put(t, node);
        }
        
        for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e 
                : input.getEdges()) {
            DotCluster parentDot = mapGroups.get(e.getParent());
            if (parentDot == null) {
                parentDot = dot;
            }
            
            DotEdge edge;
            if (e.getSource().getLabel().equals(EPTree2Petrinet.LabelTauErrorClear)
                || e.getTarget().getLabel().equals(EPTree2Petrinet.LabelTauErrorClear)) {
                edge = parentDot.addEdge(
                    mapNodes.get(e.getSource()), 
                    mapNodes.get(e.getTarget()),
                    "",
                    optEdgeShade
                );
            } else {
                Map<String, String> edgeStyle = optEdge;
                if (e instanceof ResetArc) {
                    edgeStyle = optEdgeReset;
                }
                if (e instanceof InhibitorArc) {
                    edgeStyle = optEdgeInhibit;
                }
                edge = parentDot.addEdge(
                    mapNodes.get(e.getSource()), 
                    mapNodes.get(e.getTarget()),
                    "",
                    edgeStyle
                );
            }

            if (e instanceof Arc) {
                dotDecorator.decorateEdge((Arc) e, edge);
            }
        }
        
        return dot;
    }
    
    private ExpandableSubNet determineRegion(Place fromPlace, Transition t) {
        ExpandableSubNet target = t.getParent();
        ExpandableSubNet result = fromPlace.getParent();
        while (result != null && result.getParent() != target) {
            result = result.getParent();
        }
        return result;
    }

    private Place getFromPlace(Transition t) {
        AbstractDirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> 
            g = t.getGraph();
        Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> 
            edges = g.getInEdges(t);
        if (edges != null && !edges.isEmpty()) {
            return (Place) edges.iterator().next().getSource();
        }
        return null;
    }

    private void _applySelectionStyle(Transition t, DotNode node) {
        // TODO: move to the SVG part?
        if (selectedIds.contains(t.getAttributeMap().get(EPTree2Petrinet.Key_NodeId))) {
            node.setOption("color", "red");
            node.setOption("penwidth", "3");
//            node.setOption("fontcolor", "red");
        } else {
            node.setOption("penwidth", "1");
        }
    }
}
