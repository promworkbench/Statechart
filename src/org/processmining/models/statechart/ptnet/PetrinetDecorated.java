package org.processmining.models.statechart.ptnet;

import gnu.trove.map.hash.THashMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.ExpandableSubNet;
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.AbstractResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecorated;
import org.processmining.models.statechart.decorate.tracing.IEdgeSemanticTraced;
import org.processmining.models.statechart.decorate.tracing.TraceUniqueDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;

import com.google.common.base.Preconditions;

public class PetrinetDecorated implements PetrinetGraph, Petrinet, ResetNet, InhibitorNet, ResetInhibitorNet, AcceptingPetriNet, IDecorated<Transition> {

    private final Decorations<Transition> decorations;
    
    private AbstractResetInhibitorNet wrap;
    private Marking source;
    private Set<Marking> sinks;

    private Map<Arc, IEdgeSemanticTraced<Transition>> edgeSemantics = new THashMap<>();

    public PetrinetDecorated(AbstractResetInhibitorNet wrap) {
        this(wrap, new Decorations<Transition>());
    }
    
    public PetrinetDecorated(AbstractResetInhibitorNet wrap, Decorations<Transition> decorations) {
        Preconditions.checkNotNull(wrap);
        this.decorations = decorations;
        this.wrap = wrap;
    }
    
    public PetrinetDecorated(AbstractResetInhibitorNet wrap, Place source, Place sink) {
        this(wrap, source, sink, new Decorations<Transition>());
    }

    public PetrinetDecorated(PetrinetGraph petrinet, Marking initialMarking, Marking finalMarking) {
        this((AbstractResetInhibitorNet) petrinet, initialMarking, finalMarking, new Decorations<Transition>());
    }

    public PetrinetDecorated(AbstractResetInhibitorNet wrap, Place source, Place sink,
            Decorations<Transition> decorations) {
        this(wrap, new Marking(Arrays.asList(source)), new Marking(Arrays.asList(sink)), decorations);
    }

    public PetrinetDecorated(AbstractResetInhibitorNet wrap, Marking initialMarking, Marking finalMarking,
            Decorations<Transition> decorations) {
        this(wrap, initialMarking, new HashSet<>(Arrays.asList(finalMarking)), decorations);
    }
    
    public PetrinetDecorated(AbstractResetInhibitorNet wrap, Marking initialMarking, Set<Marking> finalMarkings,
            Decorations<Transition> decorations) {
        Preconditions.checkNotNull(wrap);
        this.decorations = decorations;
        this.wrap = wrap;
        this.source = initialMarking;
        this.sinks = finalMarkings;

        @SuppressWarnings("unchecked")
        TraceUniqueDecorator<Transition, IEPTreeNode> treeTracer 
            = decorations.getForType(TraceUniqueDecorator.class);
        if (treeTracer == null) {
            treeTracer = new TraceUniqueDecorator<Transition, IEPTreeNode>();
            decorations.registerDecorator(treeTracer);
        }
    }

    public AbstractResetInhibitorNet getWrap() {
        return wrap;
    }
    
    @Override
    public Set<PetrinetNode> getNodes() {
        return wrap.getNodes();
    }

    @Override
    public Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> getEdges() {
        return wrap.getEdges();
    }

    @Override
    public Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> getInEdges(
            DirectedGraphNode node) {
        return wrap.getInEdges(node);
    }

    @Override
    public Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> getOutEdges(
            DirectedGraphNode node) {
        return wrap.getOutEdges(node);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void removeEdge(DirectedGraphEdge edge) {
        wrap.removeEdge(edge);
    }

    @Override
    public void removeNode(DirectedGraphNode cell) {
        wrap.removeNode(cell);
    }

    @Override
    public DirectedGraph<?, ?> getGraph() {
        return wrap.getGraph();
    }

    @Override
    public AttributeMap getAttributeMap() {
        return wrap.getAttributeMap();
    }

    @Override
    public int compareTo(
            DirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> o) {
        return wrap.compareTo(o);
    }

    @Override
    public Decorations<Transition> getDecorations() {
        return decorations;
    }

    @Override
    public String getLabel() {
        return wrap.getLabel();
    }

    @Override
    public Transition addTransition(String label) {
        return wrap.addTransition(label);
    }

    @Override
    public Transition addTransition(String label, ExpandableSubNet parent) {
        return wrap.addTransition(label, parent);
    }

    @Override
    public Transition removeTransition(Transition transition) {
        return wrap.removeTransition(transition);
    }

    @Override
    public Collection<Transition> getTransitions() {
        return wrap.getTransitions();
    }

    @Override
    public ExpandableSubNet addGroup(String label) {
        return wrap.addGroup(label);
    }

    @Override
    public ExpandableSubNet addGroup(String label, ExpandableSubNet parent) {
        return wrap.addGroup(label, parent);
    }

    @Override
    public ExpandableSubNet removeGroup(ExpandableSubNet transition) {
        return wrap.removeGroup(transition);
    }

    @Override
    public Collection<ExpandableSubNet> getGroups() {
        return wrap.getGroups();
    }

    @Override
    public Place addPlace(String label) {
        return wrap.addPlace(label);
    }

    @Override
    public Place addPlace(String label, ExpandableSubNet parent) {
        return wrap.addPlace(label, parent);
    }

    @Override
    public Place removePlace(Place place) {
        return wrap.removePlace(place);
    }

    @Override
    public Collection<Place> getPlaces() {
        return wrap.getPlaces();
    }

    @Override
    public Arc addArc(Place p, Transition t, int weight) {
        return wrap.addArc(p, t, weight);
    }

    @Override
    public Arc addArc(Place p, Transition t) {
        return wrap.addArc(p, t);
    }

    @Override
    public Arc addArc(Transition t, Place p, int weight) {
        return wrap.addArc(t, p, weight);
    }

    @Override
    public Arc addArc(Transition t, Place p) {
        return wrap.addArc(t, p);
    }

    @Override
    public Arc addArc(Place p, Transition t, int weight, ExpandableSubNet parent) {
        return wrap.addArc(p, t, weight, parent);
    }

    @Override
    public Arc addArc(Place p, Transition t, ExpandableSubNet parent) {
        return wrap.addArc(p, t, parent);
    }

    @Override
    public Arc addArc(Transition t, Place p, int weight, ExpandableSubNet parent) {
        return wrap.addArc(t, p, weight, parent);
    }

    @Override
    public Arc addArc(Transition t, Place p, ExpandableSubNet parent) {
        return wrap.addArc(t, p, parent);
    }

    @Override
    public Arc removeArc(PetrinetNode source, PetrinetNode target) {
        return wrap.removeArc(source, target);
    }

    @Override
    public Arc getArc(PetrinetNode source, PetrinetNode target) {
        return wrap.getArc(source, target);
    }

    @Override
    public void init(Petrinet net) {
        AcceptingPetriNetImpl acc = new AcceptingPetriNetImpl(net);
        this.wrap = (AbstractResetInhibitorNet) acc.getNet();
        this.source = acc.getInitialMarking();
        this.sinks = acc.getFinalMarkings();
    }

    @Override
    public void init(PluginContext context, Petrinet net) {
        AcceptingPetriNetImpl acc = new AcceptingPetriNetImpl(context, net);
        this.wrap = (AbstractResetInhibitorNet) acc.getNet();
        this.source = acc.getInitialMarking();
        this.sinks = acc.getFinalMarkings();
    }

    @Override
    public void setInitialMarking(Marking initialMarking) {
        this.source = initialMarking;
    }

    @Override
    public void setFinalMarkings(Set<Marking> finalMarkings) {
        this.sinks = finalMarkings;
    }

    @Override
    public Petrinet getNet() {
        return this;
    }

    public ResetNet getResetNet() {
        return this;
    }

    public InhibitorNet getInhibitorNet() {
        return this;
    }

    public ResetInhibitorNet getResetInhibitorNet() {
        return this;
    }

    @Override
    public Marking getInitialMarking() {
        return source;
    }

    @Override
    public Set<Marking> getFinalMarkings() {
        return sinks;
    }

    public Marking[] getFinalMarkingsAsArray() {
        return sinks.toArray(new Marking[sinks.size()]);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void importFromStream(PluginContext context, InputStream input)
            throws Exception {
        AcceptingPetriNetImpl acc = new AcceptingPetriNetImpl(new PetrinetImpl(""));
        acc.importFromStream(context, input);
        this.wrap = (AbstractResetInhibitorNet) acc.getNet();
        this.source = acc.getInitialMarking();
        this.sinks = acc.getFinalMarkings();
    }

    @Override
    public void exportToFile(PluginContext context, File file)
            throws IOException {
        AcceptingPetriNetImpl acc = new AcceptingPetriNetImpl(getNet(), source, sinks);
        acc.exportToFile(context, file);
    }

    @Override
    public ResetArc addResetArc(Place p, Transition t, String label) {
        return wrap.addResetArc(p, t, label);
    }

    @Override
    public ResetArc addResetArc(Place p, Transition t) {
        return wrap.addResetArc(p, t);
    }

    @Override
    public ResetArc removeResetArc(Place p, Transition t) {
        return wrap.removeResetArc(p, t);
    }

    @Override
    public ResetArc getResetArc(Place p, Transition t) {
        return wrap.getResetArc(p, t);
    }

    @Override
    public ResetArc addResetArc(Place p, Transition t, String label,
            ExpandableSubNet parent) {
        return wrap.addResetArc(p, t, label, parent);
    }

    @Override
    public ResetArc addResetArc(Place p, Transition t, ExpandableSubNet parent) {
        return wrap.addResetArc(p, t, parent);
    }

    @Override
    public InhibitorArc addInhibitorArc(Place p, Transition t, String label) {
        return wrap.addInhibitorArc(p, t, label);
    }

    @Override
    public InhibitorArc addInhibitorArc(Place p, Transition t) {
        return wrap.addInhibitorArc(p, t);
    }

    @Override
    public InhibitorArc removeInhibitorArc(Place p, Transition t) {
        return wrap.removeInhibitorArc(p, t);
    }

    @Override
    public InhibitorArc getInhibitorArc(Place p, Transition t) {
        return wrap.getInhibitorArc(p, t);
    }

    @Override
    public InhibitorArc addInhibitorArc(Place p, Transition t, String label,
            ExpandableSubNet parent) {
        return wrap.addInhibitorArc(p, t, label, parent);
    }

    @Override
    public InhibitorArc addInhibitorArc(Place p, Transition t,
            ExpandableSubNet parent) {
        return wrap.addInhibitorArc(p, t, parent);
    }

    public IEdgeSemanticTraced<Transition> getEdgeSemantics(Arc arc) {
        return edgeSemantics.get(arc);
    }
    
    public void setEdgeSemantics(Arc arc, IEdgeSemanticTraced<Transition> sem) {
        edgeSemantics.put(arc, sem);
    }

}
