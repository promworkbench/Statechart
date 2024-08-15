package org.processmining.algorithms.statechart.m2m;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.ExpandableSubNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.error.EPTreeErrorTriggerDecorator;
import org.processmining.models.statechart.decorate.tracing.BasicEdgeSemanticTraced;
import org.processmining.models.statechart.decorate.tracing.TraceUniqueDecorator;
import org.processmining.models.statechart.eptree.EPNodeType;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.utils.statechart.generic.SetUtil;
import org.processmining.utils.statechart.petrinet.PetrinetPlaceIterator;

public class EPTree2Petrinet {

    public static final String Key_NodeId = "node-id";
    
    protected static final Logger logger = LogManager
            .getLogger(EPTree2Petrinet.class.getName());

    public static final String LabelTauExit = "tau-exit";
    public static final String LabelTauErrorTrigger = "tau-error-trigger";
    public static final String LabelTauErrorClear = "tau-error-clear";

    public static final String KeyErrorTriggerPlace = "ErrorTriggerPlace";
    public static final String KeyErrorTrigger = "ErrorTrigger";
    public static final String KeyCancelRegion = "CancelRegion";

    public static enum HierarchyMode {
        Flat(false, false),
        LifecycleHierarchy(true, false),
        Subprocess(false, true);
        
        private final boolean useExplicitLifecycle;
        private final boolean useSubprocess;

        private HierarchyMode(boolean useExplicitLifecycle, boolean useSubprocess) {
            this.useExplicitLifecycle = useExplicitLifecycle;
            this.useSubprocess = useSubprocess;
        }

        public boolean useExplicitLifecycle() {
            return useExplicitLifecycle;
        }

        public boolean useSubprocess() {
            return useSubprocess;
        }
    }
    
    public static enum RecursionMode {
        IgnoreConstraint,
        InhibitorArcs;
    }
    
    public static enum CancelationMode {
        MimicResetArcs,
        ResetArcs,
        CancelationSubnet;
    }
    
    protected static class SrcSnk {
        public final Place src;
        public final Place snk;

        public SrcSnk(Place src, Place snk) {
            this.src = src;
            this.snk = snk;
        }
    }
    
    public static class SrcSnkMarking {
        public Marking source = new Marking();
        public Marking sink = new Marking();
    }

    protected IQueryCancelError queryCatchError;

    protected int placeCounter = 0;

    private HierarchyMode hierarchyMode;
    private RecursionMode recursionMode;
    private CancelationMode cancelationMode;

    private Decorations<IEPTreeNode> treeDecorations;
    private Decorations<Transition> transitionDecorations;
    private TraceUniqueDecorator<Transition, IEPTreeNode> treeTracer;

    private Map<IEPTreeNode, Transition> node2transition;
    private Map<Arc, Pair<IEPTreeNode, IEPTreeNode>> edgeDelaySem;
    
    public EPTree2Petrinet(HierarchyMode hierarchyMode, 
            RecursionMode recursionMode, 
            CancelationMode cancelationMode) {
        this.hierarchyMode = hierarchyMode;
        this.recursionMode = recursionMode;
        this.cancelationMode = cancelationMode;
    }

    public void setQueryCatchError(IQueryCancelError queryCatchError) {
        this.queryCatchError = queryCatchError;
    }

    @SuppressWarnings("unchecked")
    public PetrinetDecorated transform(IEPTree input) {
        ResetInhibitorNetImpl ptnet = new ResetInhibitorNetImpl(input.getName());
        Place source = ptnet.addPlace("Source");
        Place sink = ptnet.addPlace("Sink");

        treeDecorations = input.getDecorations();
        transitionDecorations = treeDecorations.deriveDecorationInstance(Transition.class);
        
        PetrinetDecorated model = new PetrinetDecorated(ptnet, source, sink, transitionDecorations);
        treeTracer = transitionDecorations.getForType(TraceUniqueDecorator.class);
        node2transition = new THashMap<>();
        edgeDelaySem = new THashMap<>();
        
        Map<String, SrcSnk> context = new HashMap<>();
        _transform(input.getRoot(), ptnet, null, context, source, sink);
        
        completeEdgeSemantics(model);
        
        return model;
    }

    protected void _transform(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent, 
            Map<String, SrcSnk> context, Place source, Place sink) {
        switch (node.getNodeType()) {
        case Action:
            _transformAction(node, ptnet, parent, context, source, sink);
            break;

        case Collapsed:
            _transformCollapsed(node, ptnet, parent, context, source, sink);
            break;

        case Recurrent:
            _transformRecurrent(node, ptnet, parent, context, source, sink);
            break;

        case Silent:
            _transformSilent(node, ptnet, parent, context, source, sink);
            break;

        case Seq:
            _transformSeq(node, ptnet, parent, context, source, sink);
            break;

        case Choice:
            _transformChoice(node, ptnet, parent, context, source, sink);
            break;

        case Loop:
            _transformLoop(node, ptnet, parent, context, source, sink);
            break;

        case AndComposite:
            _transformAndComposite(node, ptnet, parent, context, source, sink);
            break;

        case AndInterleaved:
            _transformAndInterleaved(node, ptnet, parent, context, source, sink);
            break;

        case OrComposite:
            _transformOrComposite(node, ptnet, parent, context, source, sink);
            break;

        case SeqCancel:
            _transformSeqCancel(node, ptnet, parent, context, source, sink);
            break;

        case LoopCancel:
            _transformLoopCancel(node, ptnet, parent, context, source, sink);
            break;

        case ErrorTrigger:
            _transformErrorTrigger(node, ptnet, parent, context, source, sink);
            break;

        case Log:
            _transformLog(node, ptnet, parent, context, source, sink);
            break;
            
        default:
            throw new TransformationException("Node type not supported: "
                    + node.getNodeType());
        }
    }

    protected Set<Place> middlePlaces = new THashSet<>();
    
    protected void _transformAction(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent, 
            Map<String, SrcSnk> context, Place source, Place sink) {
        if (hierarchyMode.useExplicitLifecycle()) {
            Place middle = ptnet.addPlace(node.getLabel() + "|" + (++placeCounter), parent);
            Transition start = ptnet.addTransition(node.getLabel() + "+start", parent);
            Transition complete = ptnet.addTransition(node.getLabel() + "+complete", parent);
            
            middlePlaces.add(middle);
            _decorate(start, node);
            _decorate(complete, node);
            
            _decorate(ptnet.addArc(source, start, parent), node);
            ptnet.addArc(start, middle, parent);
            ptnet.addArc(middle, complete, parent);
            ptnet.addArc(complete, sink, parent);
        } else {
            Transition t = ptnet.addTransition(node.getLabel(), parent);
            _decorate(t, node);
            _decorate(ptnet.addArc(source, t, parent), node);
            ptnet.addArc(t, sink, parent);
        }
    }

    protected void _transformLog(IEPTreeNode node, ResetInhibitorNet ptnet,
            ExpandableSubNet parent, Map<String, SrcSnk> context, Place source,
            Place sink) {
        // TODO move italic markup logic to DOT conversion
        Transition t = ptnet.addTransition("<<i>Log Move</i>>", parent);
        _decorate(t, node);
        ptnet.addArc(source, t, parent);
        ptnet.addArc(t, sink, parent);
    }

    protected void _transformCollapsed(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent,
            Map<String, SrcSnk> context, Place source, Place sink) {
        _transformAction(node, ptnet, parent, context, source, sink);
    }

    protected void _transformErrorTrigger(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent,
            Map<String, SrcSnk> context, Place source, Place sink) {
        Transition t;
        if (hierarchyMode.useExplicitLifecycle()) {
            Place middle = ptnet.addPlace(node.getLabel() + "|" + (++placeCounter), parent);
            Transition start = ptnet.addTransition(node.getLabel() + "+start", parent);
            Transition complete = ptnet.addTransition(node.getLabel() + "+complete", parent);
            t = complete;

            middlePlaces.add(middle);
            _decorate(start, node);
            _decorate(complete, node);
            
            ptnet.addArc(source, start, parent);
            ptnet.addArc(start, middle, parent);
            ptnet.addArc(middle, complete, parent);
            ptnet.addArc(complete, sink, parent);
        } else {
            t = ptnet.addTransition(node.getLabel(), parent);
            _decorate(t, node);
            _decorate(ptnet.addArc(source, t, parent), node);
            ptnet.addArc(t, sink, parent);
        }

        Set<String> errorTriggers = Collections.emptySet();
        EPTreeErrorTriggerDecorator decorator = treeDecorations.getForType(EPTreeErrorTriggerDecorator.class);
        if (decorator != null) {
            errorTriggers = decorator.getDecoration(node);
        }
        
        t.getAttributeMap().put(KeyErrorTrigger, errorTriggers);
        sink.getAttributeMap().put(KeyErrorTriggerPlace, errorTriggers);
    }

    protected void _transformRecurrent(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent,
            Map<String, SrcSnk> context, Place source, Place sink) {
        if (hierarchyMode == HierarchyMode.LifecycleHierarchy
                || hierarchyMode == HierarchyMode.Subprocess) {
            Place middle = ptnet.addPlace(node.getLabel() + "|" + (++placeCounter), parent);
            middlePlaces.add(middle);
            
            Transition start, complete;
            if (hierarchyMode == HierarchyMode.Subprocess) {
                start = ptnet.addTransition(node.getLabel(), parent);
                complete = ptnet.addTransition(node.getLabel() + "+complete", parent);
                _decorate(start, node);
                complete.setInvisible(true);
            } else {
                start = ptnet.addTransition(node.getLabel() + "+start", parent);
                complete = ptnet.addTransition(node.getLabel() + "+complete", parent);
                _decorate(start, node);
                _decorate(complete, node);
            }
            
            ptnet.addArc(source, start, parent);
            ptnet.addArc(start, middle, parent);
            ptnet.addArc(middle, complete, parent);
            ptnet.addArc(complete, sink, parent);
            
            SrcSnk referenced = context.get(node.getLabel());
            
            if (referenced == null) {
                logger.warn("EPTree2Petrinet - Transform Recurrent warning, "
                        + "referenced subtree not found");
            } else {
                ptnet.addArc(start, referenced.src, parent);
                ptnet.addArc(referenced.snk, complete, parent);
                
                if (recursionMode == RecursionMode.InhibitorArcs) {
                    Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> snkOutE 
                        = ptnet.getOutEdges(referenced.snk);
                    if (snkOutE.isEmpty()) {
                        throw new TransformationException("Failure to handle InhibitorArcs, sink not connected to sink transition");
                    } else {
                        Transition endT = (Transition) snkOutE.iterator().next().getTarget();
                        ptnet.addInhibitorArc(middle, endT, parent);
                    }
                }
            }
        } else {
            throw new TransformationException("Hierarchy not supported for hierarchyMode '" + hierarchyMode.name() 
                    + "', node type not supported: " + node.getNodeType());
        }
    }

    protected void _transformSilent(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent,
            Map<String, SrcSnk> context, Place source, Place sink) {
        String name = node.getLabel();
        if (!name.contains("tau")) {
            name = "tau - " + name;
        }
        
        Transition silent = ptnet.addTransition(name, parent);
        silent.setInvisible(true);

        _decorate(silent, node);
        
        ptnet.addArc(source, silent, parent);
        ptnet.addArc(silent, sink, parent);
    }

    protected void _transformSeq(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent,
            Map<String, SrcSnk> context, Place source, Place sink) {
        Place prev = source;
        
        List<IEPTreeNode> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            IEPTreeNode child = children.get(i);
            Place current = sink;
            if (i < children.size() - 1) {
                current = ptnet.addPlace(node.getLabel() + "-" + i + "|" + (++placeCounter), parent);
            }
            
            _transform(child, ptnet, parent, context, prev, current);
            prev = current;
        }
    }

    protected void _transformChoice(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent,
            Map<String, SrcSnk> context, Place source, Place sink) {
        for (IEPTreeNode child : node.getChildren()) {
            _transform(child, ptnet, parent, context, source, sink);
        }
    }

    protected void _transformLoop(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent,
            Map<String, SrcSnk> context, Place source, Place sink) {
        Place src2 = ptnet.addPlace(node.getLabel() + "|" + (++placeCounter), parent);
        Place snk2 = ptnet.addPlace(node.getLabel() + "|" + (++placeCounter), parent);
        
        Transition start = ptnet.addTransition(node.getLabel() + "-start", parent);
        Transition end = ptnet.addTransition(node.getLabel() + "-end", parent);
        start.setInvisible(true);
        end.setInvisible(true);
        
        ptnet.addArc(source, start, parent);
        ptnet.addArc(start, src2, parent);
        
        ptnet.addArc(snk2, end, parent);
        ptnet.addArc(end, sink, parent);

        List<IEPTreeNode> children = node.getChildren();
        _transform(children.get(0), ptnet, parent, context, src2, snk2);
        _transform(children.get(1), ptnet, parent, context, snk2, src2);
    }

    protected void _transformAndComposite(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent,
            Map<String, SrcSnk> context, Place source, Place sink) {
        
        Transition start = ptnet.addTransition(node.getLabel() + "-start", parent);
        Transition end = ptnet.addTransition(node.getLabel() + "-end", parent);
        start.setInvisible(true);
        end.setInvisible(true);

        ptnet.addArc(source, start, parent);
        ptnet.addArc(end, sink, parent);
        
        for (IEPTreeNode child : node.getChildren()) {
            Place src2 = ptnet.addPlace(node.getLabel() + "|" + (++placeCounter), parent);
            Place snk2 = ptnet.addPlace(node.getLabel() + "|" + (++placeCounter), parent);
            
            _transform(child, ptnet, parent, context, src2, snk2);
            
            ptnet.addArc(start, src2, parent);
            ptnet.addArc(snk2, end, parent);
        }
    }
    
    protected void _transformAndInterleaved(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent,
            Map<String, SrcSnk> context, Place source, Place sink) {
        
        Transition start = ptnet.addTransition(node.getLabel() + "-start", parent);
        Transition end = ptnet.addTransition(node.getLabel() + "-end", parent);
        start.setInvisible(true);
        end.setInvisible(true);
        
        Place placeCritical = ptnet.addPlace(node.getLabel() + "-critical" + "|" + (++placeCounter), parent);

        ptnet.addArc(source, start, parent);
        ptnet.addArc(end, sink, parent);

        ptnet.addArc(start, placeCritical, parent);
        ptnet.addArc(placeCritical, end, parent);
        
        
        for (IEPTreeNode child : node.getChildren()) {
            Place enter = ptnet.addPlace("cEnter|" + (++placeCounter), parent);
            Place leave = ptnet.addPlace("cLeave|" + (++placeCounter), parent);

            Transition tEnter = ptnet.addTransition(node.getLabel() + "-cEnter", parent);
            Transition tLeave = ptnet.addTransition(node.getLabel() + "-cLeave", parent);
            tEnter.setInvisible(true);
            tLeave.setInvisible(true);
            
            Place src2 = ptnet.addPlace(node.getLabel() + "|" + (++placeCounter), parent);
            Place snk2 = ptnet.addPlace(node.getLabel() + "|" + (++placeCounter), parent);
            
            _transform(child, ptnet, parent, context, src2, snk2);
            
//            ptnet.addArc(start, src2);
//            ptnet.addArc(snk2, end);

            ptnet.addArc(start, enter, parent);
            ptnet.addArc(enter, tEnter, parent);
            ptnet.addArc(placeCritical, tEnter, parent);
            ptnet.addArc(tEnter, src2, parent);
            
            ptnet.addArc(snk2, tLeave, parent);
            ptnet.addArc(tLeave, placeCritical, parent);
            ptnet.addArc(tLeave, leave, parent);
            ptnet.addArc(leave, end, parent);
        }
    }
    
    protected void _transformOrComposite(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent,
            Map<String, SrcSnk> context, Place source, Place sink) {
        ExpandableSubNet newParent = parent;
        if (hierarchyMode == HierarchyMode.Subprocess) {
            newParent = ptnet.addGroup(node.getLabel(), parent);
            _decorate(newParent, node);
        }
        
        if (hierarchyMode == HierarchyMode.LifecycleHierarchy
            || hierarchyMode == HierarchyMode.Subprocess) {
            Place src2 = ptnet.addPlace(node.getLabel() + "|" + (++placeCounter), newParent);
            Place snk2 = ptnet.addPlace(node.getLabel() + "|" + (++placeCounter), newParent);
            
            Transition start, complete;
            if (hierarchyMode == HierarchyMode.Subprocess) {
                start = ptnet.addTransition(node.getLabel() + "+start", newParent);
                complete = ptnet.addTransition(node.getLabel() + "+complete", newParent);
                start.setInvisible(true);
                complete.setInvisible(true);
            } else {
                start = ptnet.addTransition(node.getLabel() + "+start", newParent);
                complete = ptnet.addTransition(node.getLabel() + "+complete", newParent);
            }
            _decorate(start, node);
            _decorate(complete, node);
            
            IEPTreeNode child = node.getChildren().get(0);
            _decorate(ptnet.addArc(source, start, parent), node);
            ptnet.addArc(start, src2, parent);
            
            _decorate(ptnet.addArc(snk2, complete, parent), child, null);
            ptnet.addArc(complete, sink, parent);
            
            context.put(node.getLabel(), new SrcSnk(src2, snk2));
            _transform(child, ptnet, newParent, context, src2, snk2);
            context.remove(node.getLabel());
        } else {
            throw new TransformationException("Hierarchy not supported for hierarchyMode '" + hierarchyMode.name() 
                    + "', node type not supported: " + node.getNodeType());
        }
    }

    protected void _transformSeqCancel(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent,
            Map<String, SrcSnk> context, Place source, Place sink) {

        // Add a sink for the left subtree, and connect to real sink
        Place normalSnk = ptnet.addPlace("snk" + "|" + (++placeCounter), parent);

        Transition exit = ptnet.addTransition(LabelTauExit, parent);
        exit.setInvisible(true);
        ptnet.addArc(normalSnk, exit, parent);
        ptnet.addArc(exit, sink, parent);
        
        // create left subtree model
        List<IEPTreeNode> children = node.getChildren();
        if (cancelationMode == CancelationMode.CancelationSubnet) {
            ExpandableSubNet cancelSubnet = ptnet.addGroup("", parent);
            cancelSubnet.getAttributeMap().put(KeyCancelRegion, Boolean.TRUE);
            _transform(children.get(0), ptnet, cancelSubnet, context, source, normalSnk);
        } else {
            _transform(children.get(0), ptnet, parent, context, source, normalSnk);
        }

        // figure out errors handled in left subtree
        boolean useGenericCancel = (queryCatchError == null) || !_containsErrorTrigger(children.get(0));
        Set<String> errorHandled = new THashSet<>();
        for (int i = 1; i < children.size(); i++) {
            _addErrorsHandled(children.get(i), errorHandled);
        }
        
        // the start of the error subtrees 
        Place errorSrc = ptnet.addPlace("error" + "|" + (++placeCounter), parent);

        // create cancel trigger and cancellation reset/cleanup transitions
        if (useGenericCancel) {
            throw new TransformationException("Cannot build cancelation into Petri net "
                    + "without the proper IQueryCancelError information");
//            _createCancelTransitionsGeneric(ptnet, parent, source, normalSnk, errorSrc);
        } else {
            _createCancelTransitionsSpecific(ptnet, parent, source, normalSnk, errorSrc, errorHandled);
        }
        
        // create error subtrees
        for (int i = 1; i < children.size(); i++) {
            _transform(children.get(i), ptnet, parent, context, errorSrc, sink);
        }
    }

    protected void _transformLoopCancel(IEPTreeNode node, ResetInhibitorNet ptnet, ExpandableSubNet parent,
            Map<String, SrcSnk> context, Place source, Place sink) {
        
        // Create new source and sink, just like normal loop, and connect to real source and sink
        Place src2 = ptnet.addPlace(node.getLabel() + "|" + (++placeCounter), parent);
        Place snk2 = ptnet.addPlace(node.getLabel() + "|" + (++placeCounter), parent);

        Transition start = ptnet.addTransition(node.getLabel() + "-start", parent);
        Transition end = ptnet.addTransition(node.getLabel() + "-end", parent);
        start.setInvisible(true);
        end.setInvisible(true);
        
        ptnet.addArc(source, start, parent);
        ptnet.addArc(start, src2, parent);
        
        ptnet.addArc(snk2, end, parent);
        ptnet.addArc(end, sink, parent);

        // create left subtree model
        List<IEPTreeNode> children = node.getChildren();
        if (cancelationMode == CancelationMode.CancelationSubnet) {
            ExpandableSubNet cancelSubnet = ptnet.addGroup("", parent);
            cancelSubnet.getAttributeMap().put(KeyCancelRegion, Boolean.TRUE);
            _transform(children.get(0), ptnet, cancelSubnet, context, src2, snk2);
        } else {
            _transform(children.get(0), ptnet, parent, context, src2, snk2);
        }

        // figure out errors handled in left subtree
        boolean useGenericCancel = (queryCatchError == null) || !_containsErrorTrigger(children.get(0));
        Set<String> errorHandled = new THashSet<>();
        for (int i = 1; i < children.size(); i++) {
            _addErrorsHandled(children.get(i), errorHandled);
        }

        // the start of the error subtrees 
        Place errorSrc = ptnet.addPlace("error" + "|" + (++placeCounter), parent);

        // create cancel trigger and cancellation reset/cleanup transitions
        if (useGenericCancel) {
            throw new TransformationException("Cannot build cancelation into Petri net "
                    + "without the proper IQueryCancelError information");
//            _createCancelTransitionsGeneric(ptnet, parent, source, snk2, errorSrc);
        } else {
            _createCancelTransitionsSpecific(ptnet, parent, source, snk2, errorSrc, errorHandled);
        }

        // create error subtrees
        for (int i = 1; i < children.size(); i++) {
            _transform(children.get(i), ptnet, parent, context, errorSrc, src2);
        }
    }

//    protected void _createCancelTransitionsGeneric(ResetInhibitorNet ptnet, ExpandableSubNet parent, Place source,
//            Place normalSnk, Place errorSrc) {
//        PetrinetMarkingIterator it = new PetrinetMarkingIterator(ptnet, source, normalSnk, true);
//        it.next(); // skip start
//        for (Set<Place> marking : it) {
//            Transition clear = ptnet.addTransition("tau-error", parent);
//            clear.setInvisible(true);
//            ptnet.addArc(clear, errorSrc, parent);
//            for (Place p : marking) {
//                ptnet.addArc(p, clear, parent);
//            }
//        }
//    }

    protected void _createCancelTransitionsSpecific(ResetInhibitorNet ptnet, ExpandableSubNet parent, Place source,
            Place normalSnk, Place errorSrc, Set<String> errorHandled) {
        // TODO: somewhere add arc for cancel region? Maybe in DOT?
        Set<Transition> cancelTransitions = null;
        if (cancelationMode == CancelationMode.ResetArcs) {
            cancelTransitions = new THashSet<>();
        }
        
        for (Place p : new PetrinetPlaceIterator(ptnet, source, normalSnk, true)) {
            if (_isErrorTriggerPlace(p, errorHandled)) {
                Transition trigger = ptnet.addTransition(LabelTauErrorTrigger, parent);
                trigger.setInvisible(true);
                ptnet.addArc(trigger, errorSrc, parent);
                ptnet.addArc(p, trigger, parent);
                
                if (cancelationMode == CancelationMode.ResetArcs) {
                    // Pass 1: collect cancel transitions
                    cancelTransitions.add(trigger);
                }
            }
            
            if (cancelationMode == CancelationMode.MimicResetArcs) {
                // TODO: smart way of handling aligned precision issues?
                if (!middlePlaces.contains(p)) {
                    Transition clear = ptnet.addTransition(LabelTauErrorClear, parent);
                    clear.setInvisible(true);
                    ptnet.addArc(clear, errorSrc, parent);
                    ptnet.addArc(errorSrc, clear, parent);
                    ptnet.addArc(p, clear, parent);
                }
            }
        }

        if (cancelationMode == CancelationMode.ResetArcs) {
            // Pass 2: add reset arcs
            for (Place p : new PetrinetPlaceIterator(ptnet, source, normalSnk, true)) {
                for (Transition trigger : cancelTransitions) {
                    ptnet.addResetArc(p, trigger, parent);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected boolean _isErrorTriggerPlace(Place p, Set<String> errorHandled) {
        return p.getAttributeMap().containsKey(KeyErrorTriggerPlace)
            && SetUtil.hasIntersection(
                (Set<String>) p.getAttributeMap().get(KeyErrorTriggerPlace), 
                errorHandled
            );
    }

    protected boolean _containsErrorTrigger(IEPTreeNode node) {
        if (node.getNodeType() == EPNodeType.ErrorTrigger) {
            return true;
        }
        
        boolean result = false;
        for (IEPTreeNode child : node.getChildren()) {
            result = result || _containsErrorTrigger(child);
        }
        
        return result;
    }

    protected void _addErrorsHandled(IEPTreeNode node, Set<String> result) {
        if (queryCatchError == null) {
            return;
        }
        
        List<IEPTreeNode> children = node.getChildren();
        
        if (node.isLeaf() && queryCatchError.isCatchError(node.getLabel())) {
            result.add(node.getLabel());
        }
        if (node.getNodeType() == EPNodeType.SeqCancel) {
            _addErrorsHandled(children.get(0), result);
        } else {
            for (int i = 0; i < children.size(); i++) {
                _addErrorsHandled(children.get(i), result);
            }
        }
    }

    private void _decorate(Transition t, IEPTreeNode node) {
        t.getAttributeMap().put(Key_NodeId, node.getId());
        treeTracer.setDecoration(t, node);
        transitionDecorations.deriveDecorations(t, node, treeDecorations);
        node2transition.put(node, t);
    }
    
    private void _decorate(ExpandableSubNet t, IEPTreeNode node) {
        t.getAttributeMap().put(Key_NodeId, node.getId());
    }

    private void _decorate(Arc arc, IEPTreeNode targetSubtree) {
        _decorate(arc, null, targetSubtree);
    }

    private void _decorate(Arc arc, IEPTreeNode sourceSubtree, IEPTreeNode targetSubtree) {
        edgeDelaySem.put(arc, Pair.of(sourceSubtree,  targetSubtree));
    }
    
    private void completeEdgeSemantics(PetrinetDecorated model) {
        for (Arc arc : edgeDelaySem.keySet()) {
            Pair<IEPTreeNode, IEPTreeNode> data = edgeDelaySem.get(arc);
            IEPTreeNode sourceSubtree = data.getLeft();
            IEPTreeNode targetSubtree = data.getRight();
            
            Set<Transition> from = new THashSet<>();
            if (sourceSubtree != null) {
                for (IEPTreeNode tTo : sourceSubtree.getEndSemantics()) {
                    from.add(node2transition.get(tTo));
                }
            } else if (targetSubtree != null) {
                for (IEPTreeNode tFrom : targetSubtree.getEdgeFromSemantics()) {
                    from.add(node2transition.get(tFrom));
                }
            }
            
            // TODO: tricky part with cancelation edges ...
            // see also remarks at EPTreeSemantics
            Set<Transition> to = new THashSet<>();
            if (targetSubtree != null) {
                for (IEPTreeNode tFrom : targetSubtree.getStartSemantics()) {
                    to.add(node2transition.get(tFrom));
                }
            } else if (sourceSubtree != null) {
                for (IEPTreeNode tTo : sourceSubtree.getEdgeToSemantics()) {
                    to.add(node2transition.get(tTo));
                }
            }
            
            model.setEdgeSemantics(arc, new BasicEdgeSemanticTraced<>(from, to));
        }
    }
}
