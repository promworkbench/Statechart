package org.processmining.algorithms.statechart.m2m;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.tracing.TraceUniqueDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.sc.ISCRegion;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.ISCTransition;
import org.processmining.models.statechart.sc.SCCompositeState;
import org.processmining.models.statechart.sc.SCRegion;
import org.processmining.models.statechart.sc.SCState;
import org.processmining.models.statechart.sc.SCStateType;
import org.processmining.models.statechart.sc.Statechart;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class EPTree2StatechartStates implements Function<IEPTree, Statechart> {

    private static class SrcSnk {
        public final ISCState src;
        public final ISCState snk;

        public SrcSnk(ISCState src, ISCState snk) {
            this.src = src;
            this.snk = snk;
        }
    }

    private Decorations<IEPTreeNode> treeDecorations;

    private Decorations<ISCState> stateDecorations;
    private Decorations<ISCTransition> transitionDecorations;

    private boolean reverseEdgeMod;
    
    private TraceUniqueDecorator<ISCState, IEPTreeNode> treeTracer;
    private Map<IEPTreeNode, ISCState> node2state;

    private Map<ISCTransition, Pair<IEPTreeNode, IEPTreeNode>> edgeDelaySem;

    @Override
    public Statechart apply(IEPTree input) {
        return transform(input);
    }
    
    @SuppressWarnings("unchecked")
    public Statechart transform(IEPTree input) {
        Preconditions.checkNotNull(input);

        treeDecorations = input.getDecorations();
        reverseEdgeMod = false;

        // setup supported decorators, will be derived automatically
        stateDecorations = treeDecorations.deriveDecorationInstance(ISCState.class);
        transitionDecorations = treeDecorations.deriveDecorationInstance(ISCTransition.class);

        // transform model
        Statechart output = new Statechart(input.getName(), stateDecorations,
                transitionDecorations);

        treeTracer = stateDecorations.getForType(TraceUniqueDecorator.class);
        node2state = new THashMap<>();
        edgeDelaySem = new THashMap<>();
        
        SCRegion region = new SCRegion(output);
        output.addRegion(region);

        SCState source = new SCState(region);
        SCState sink = new SCState(region);

        region.addState(source);
        region.addState(sink);
        region.setInitialState(source);
        region.addEndState(sink);

        SrcSnk srcsnk = _transform(input.getRoot(), region);
        ISCTransition t1 = _createTransition(region, source, srcsnk.src);
        ISCTransition t2 = _createTransition(region, srcsnk.snk, sink);
        _decorate(t1, null, input.getRoot());
        _decorate(t2, input.getRoot(), null);

        completeEdgeSemantics();
        
        return output;
    }

    private ISCTransition _createTransition(ISCRegion region, ISCState from,
            ISCState to) {
        return _createTransition(region, from, to, false);
    }

    private ISCTransition _createTransition(ISCRegion region, ISCState from,
            ISCState to, boolean isReverse) {
        if (reverseEdgeMod) {
            isReverse = !isReverse;
        }
        return region.addTransition(from, to, "", isReverse);
    }

    
    private SrcSnk _transform(IEPTreeNode node, ISCRegion parent, boolean flipEdges) {
        if (flipEdges) {
            reverseEdgeMod = !reverseEdgeMod; 
        }
        SrcSnk srcsnk = _transform(node, parent);
        if (flipEdges) {
            reverseEdgeMod = !reverseEdgeMod; 
        }
        return srcsnk;
    }
    
    private SrcSnk _transform(IEPTreeNode node, ISCRegion parent) {
        switch (node.getNodeType()) {
        case Action:
            return _transformAction(node, parent);

        case Collapsed:
            return _transformCollapsed(node, parent);

        case Recurrent:
            return _transformRecurrent(node, parent);

        case Silent:
            return _transformSilent(node, parent);

        case Seq:
            return _transformSeq(node, parent);

        case Choice:
            return _transformChoice(node, parent);

        case Loop:
            return _transformLoop(node, parent);

        case AndComposite:
        case AndInterleaved:
            return _transformAndComposite(node, parent);

        case OrComposite:
            return _transformOrComposite(node, parent);

        case SeqCancel:
            return _transformSeqCancel(node, parent);

        case LoopCancel:
            return _transformLoopCancel(node, parent);

        case ErrorTrigger:
            return _transformErrorTrigger(node, parent);

        case Log:
            return _transformLog(node, parent);
            
        default:
            throw new TransformationException("Node type not supported: "
                    + node.getNodeType());
        }
    }

    private SrcSnk _transformAction(IEPTreeNode node, ISCRegion parent) {
        ISCState state = new SCState(parent, SCStateType.Simple,
                node.getLabel(), node.getId());
        parent.addState(state);
        treeTracer.setDecoration(state, node);
        _decorate(state, node);
        return new SrcSnk(state, state);
    }

    private SrcSnk _transformCollapsed(IEPTreeNode node, ISCRegion parent) {
        ISCState state = new SCState(parent, SCStateType.Collapsed,
                node.getLabel(), node.getId());
        parent.addState(state);
        treeTracer.setDecoration(state, node);
        _decorate(state, node);
        return new SrcSnk(state, state);
    }

    private SrcSnk _transformRecurrent(IEPTreeNode node, ISCRegion parent) {
        ISCState state = new SCState(parent, SCStateType.Recurrent,
                node.getLabel(), node.getId());
        parent.addState(state);
        treeTracer.setDecoration(state, node);
        _decorate(state, node);
        return new SrcSnk(state, state);
    }

    private SrcSnk _transformErrorTrigger(IEPTreeNode node, ISCRegion parent) {
        ISCState state = new SCState(parent, SCStateType.ErrorTrigger,
                node.getLabel(), node.getId());
        parent.addState(state);
        treeTracer.setDecoration(state, node);
        _decorate(state, node);
        return new SrcSnk(state, state);
    }

    private SrcSnk _transformLog(IEPTreeNode node, ISCRegion parent) {
        ISCState state = new SCState(parent, SCStateType.LogSimple,
                node.getLabel(), node.getId());
        parent.addState(state);
        treeTracer.setDecoration(state, node);
        _decorate(state, node);
        return new SrcSnk(state, state);
    }

    private SrcSnk _transformSilent(IEPTreeNode node, ISCRegion parent) {
        ISCState state = new SCState(parent, SCStateType.PointPseudo, "",
                node.getId());
        parent.addState(state);
        treeTracer.setDecoration(state, node);
        _decorate(state, node);
        return new SrcSnk(state, state);
    }

    private SrcSnk _transformSeq(IEPTreeNode node, ISCRegion parent) {
        SrcSnk first = null;
        SrcSnk prev = null;
        SrcSnk last = null;

        IEPTreeNode prevChild = null;
        for (IEPTreeNode child : node.getChildren()) {
            last = _transform(child, parent);
            if (prev != null) {
                ISCTransition t = _createTransition(parent, prev.snk, last.src);
                _decorate(t, prevChild, child);
            } else {
                first = last;
            }
            prev = last;
            prevChild = child;
        }

        return new SrcSnk(first.src, last.snk);
    }

    private SrcSnk _transformChoice(IEPTreeNode node, ISCRegion parent) {
        ISCState src = new SCState(parent, SCStateType.PointPseudo);
        ISCState snk = new SCState(parent, SCStateType.PointPseudo);
        parent.addState(src);
        parent.addState(snk);
        treeTracer.setDecoration(src, node);
        treeTracer.setDecoration(snk, node);

        for (IEPTreeNode child : node.getChildren()) {
            SrcSnk cur = _transform(child, parent);
            ISCTransition t1 = _createTransition(parent, src, cur.src);
            ISCTransition t2 = _createTransition(parent, cur.snk, snk);
            _decorate(t1, null, child);
            _decorate(t2, child, null);
        }

        return new SrcSnk(src, snk);
    }

    private SrcSnk _transformLoop(IEPTreeNode node, ISCRegion parent) {
        List<IEPTreeNode> children = node.getChildren();

        if (children.size() != 2) {
            throw new TransformationException(
                    "EPTree Loop doesn't have 2 children");
        }

        ISCState src = new SCState(parent, SCStateType.PointPseudo);
        ISCState snk = new SCState(parent, SCStateType.PointPseudo);
        parent.addState(src);
        parent.addState(snk);
        treeTracer.setDecoration(src, node);
        treeTracer.setDecoration(snk, node);

        SrcSnk body = _transform(children.get(0), parent);
        SrcSnk redo = _transform(children.get(1), parent, true);

        ISCTransition tb1 = _createTransition(parent, src, body.src);
        ISCTransition tb2 = _createTransition(parent, body.snk, snk);
        ISCTransition tr1 = _createTransition(parent, snk, redo.src, true); // dir=back
        ISCTransition tr2 = _createTransition(parent, redo.snk, src, true); // dir=back

        _decorate(tb1, null, children.get(0));
        _decorate(tb2, children.get(0), null);
        _decorate(tr1, null, children.get(1));
        _decorate(tr2, children.get(1), null);

        return new SrcSnk(src, snk);
    }

    private SrcSnk _transformAndComposite(IEPTreeNode node, ISCRegion parent) {

        SCCompositeState comp = new SCCompositeState(parent,
                SCStateType.AndComposite, node.getLabel(), node.getId());
        SCState split = new SCState(parent, SCStateType.SplitPseudo);
        SCState join = new SCState(parent, SCStateType.JoinPseudo);

        parent.addState(split);
        parent.addState(join);
        parent.addState(comp);
//        treeTracer.setDecoration(split, node);
//        treeTracer.setDecoration(join, node);
//        treeTracer.setDecoration(comp, node);

        for (IEPTreeNode child : node.getChildren()) {
            ISCRegion region = new SCRegion(comp);
            comp.addRegion(region);

            SrcSnk cur = _transform(child, region);
            region.setInitialState(cur.src);
            region.addEndState(cur.snk);

            ISCTransition t1 = _createTransition(parent, split, cur.src);
            ISCTransition t2 = _createTransition(parent, cur.snk, join);
            _decorate(t1, null, child);
            _decorate(t2, node, null);
        }

//        _decorate(comp, node);
//        _decorate(split, node);
//        _decorate(join, node);

        return new SrcSnk(split, join);
    }

    private SrcSnk _transformOrComposite(IEPTreeNode node, ISCRegion parent) {
        List<IEPTreeNode> children = node.getChildren();

        if (children.size() != 1) {
            throw new TransformationException(
                    "EPTree OrComposite doesn't have 1 child");
        }

        SCCompositeState wrap = new SCCompositeState(parent,
                SCStateType.OrComposite, node.getLabel(), node.getId());
        parent.addState(wrap);
        
        SCRegion regionWrap = new SCRegion(wrap);
        wrap.addRegion(regionWrap);
        
        treeTracer.setDecoration(wrap, node);
        _decorate(wrap, node);
        
        // New:
        ISCState src = new SCState(regionWrap, SCStateType.OrStartPseudo);
        ISCState snk = new SCState(regionWrap, SCStateType.OrEndPseudo);
        regionWrap.addState(src);
        regionWrap.addState(snk);
        regionWrap.setInitialState(src);
        regionWrap.addEndState(snk);
        treeTracer.setDecoration(src, node);
        treeTracer.setDecoration(snk, node);

        IEPTreeNode child = children.get(0);
        SrcSnk cur = _transform(child, regionWrap);

        ISCTransition t1 = _createTransition(parent, src, cur.src);
        ISCTransition t2 = _createTransition(parent, cur.snk, snk);
        _decorate(t1, null, child);
        _decorate(t2, child, null);

        return new SrcSnk(src, snk);
        
        // Old:
//        SrcSnk cur = _transform(children.get(0), regionWrap);
//        regionWrap.setInitialState(cur.src);
//        regionWrap.addEndState(cur.snk);
//
//        _decorate(wrap, node);
//        
//        return cur;
    }

    private SrcSnk _transformSeqCancel(IEPTreeNode node, ISCRegion parent) {
        

//        SCCompositeState comp = new SCCompositeState(parent,
//                SCStateType.AndComposite, node.getLabel(), node.getId());
//        SCState split = new SCState(parent, SCStateType.SplitPseudo);
//        SCState join = new SCState(parent, SCStateType.JoinPseudo);
//
//        parent.addState(split);
//        parent.addState(join);
//        parent.addState(comp);
//
//        for (IEPTreeNode child : node.getChildren()) {
//            ISCRegion region = new SCRegion();
//            comp.addRegion(region);
//
//            SrcSnk cur = _transform(child, region);
//            region.setInitialState(cur.src);
//            region.addEndState(cur.snk);
//
//            ISCTransition t1 = _createTransition(parent, split, cur.src);
//            ISCTransition t2 = _createTransition(parent, cur.snk, join);
//            _decorate(t1, child);
//            _decorate(t2, child);
//        }
//
//        _decorate(comp, node);
//        _decorate(split, node);
//        _decorate(join, node);
//
//        return new SrcSnk(split, join);
        
        
        // ---
        
        SCCompositeState comp = new SCCompositeState(parent,
                SCStateType.SeqCancel, node.getLabel(), node.getId());
        ISCState snk = new SCState(parent, SCStateType.PointPseudo);
        
        parent.addState(snk);
        parent.addState(comp);
        treeTracer.setDecoration(snk, node);
        treeTracer.setDecoration(comp, node);

        ISCState src = null;
//        boolean firstRegion = true;
        IEPTreeNode firstChild = null;
        
        for (IEPTreeNode child : node.getChildren()) {
            ISCRegion region = new SCRegion(comp);
            comp.addRegion(region);

            SrcSnk cur = _transform(child, region);
            if (firstChild == null) {
                src = cur.src;
            }
            region.setInitialState(cur.src);
            region.addEndState(cur.snk);
            
            if (firstChild != null) {
                // cancelation edge from cluster to source
                ISCTransition t1 = _createTransition(parent, comp, cur.src);
                _decorate(t1, null, child);
            }
            ISCTransition t2 = _createTransition(parent, cur.snk, snk);
            _decorate(t2, child, null);
            

            if (firstChild == null) {
                firstChild = child;
            }
//            firstRegion = false;
        }

        return new SrcSnk(src, snk);
    }

    private SrcSnk _transformLoopCancel(IEPTreeNode node, ISCRegion parent) {
        SCCompositeState comp = new SCCompositeState(parent,
                SCStateType.LoopCancel, node.getLabel(), node.getId());
        ISCState src = new SCState(parent, SCStateType.PointPseudo);
        ISCState snk = new SCState(parent, SCStateType.PointPseudo);

        parent.addState(src);
        parent.addState(snk);
        parent.addState(comp);
        treeTracer.setDecoration(src, node);
        treeTracer.setDecoration(snk, node);
        treeTracer.setDecoration(comp, node);

//        ISCState src = null;
//        boolean firstRegion = true;
        IEPTreeNode firstChild = null;
        
        for (IEPTreeNode child : node.getChildren()) {
            ISCRegion region = new SCRegion(comp);
            comp.addRegion(region);

            SrcSnk cur = _transform(child, region, firstChild != null);
            if (firstChild == null) {
                ISCTransition tb1 = _createTransition(parent, src, cur.src);
                _decorate(tb1, null, child);
            }
            region.setInitialState(cur.src);
            region.addEndState(cur.snk);
            
            if (firstChild == null) {
                // exit
                ISCTransition t2 = _createTransition(parent, cur.snk, snk);
                _decorate(t2, child, null);
            } else {
                // cancelation edge from cluster to source
                ISCTransition t1 = _createTransition(parent, comp, cur.src, true);
                _decorate(t1, firstChild, child);
                
                // loop back
                ISCTransition t2 = _createTransition(parent, cur.snk, src, true);
                _decorate(t2, child, firstChild);
            }
            
            if (firstChild == null) {
                firstChild = child;
            }
//            if (!firstRegion) {
//            }
//            
//            firstRegion = false;
        }

        return new SrcSnk(src, snk);
    }

    private void _decorate(ISCState state, IEPTreeNode node) {
        stateDecorations.deriveDecorations(state, node, treeDecorations);
        node2state.put(node, state);
    }

    private void _decorate(ISCTransition transition, IEPTreeNode sourceSubtree, IEPTreeNode targetSubtree) {
        if (targetSubtree != null) {
            transitionDecorations.deriveDecorations(transition, targetSubtree, treeDecorations);
        } else if (sourceSubtree != null) {
            transitionDecorations.deriveDecorations(transition, sourceSubtree, treeDecorations);
        }
        
        edgeDelaySem.put(transition, Pair.of(sourceSubtree, targetSubtree));
    }

    private void completeEdgeSemantics() {
        for (ISCTransition transition : edgeDelaySem.keySet()) {
            Pair<IEPTreeNode, IEPTreeNode> data = edgeDelaySem.get(transition);
            IEPTreeNode sourceSubtree = data.getLeft();
            IEPTreeNode targetSubtree = data.getRight();
            
            Set<ISCState> from = new THashSet<>();
            if (sourceSubtree != null) {
                for (IEPTreeNode tTo : sourceSubtree.getEndSemantics()) {
                    from.add(node2state.get(tTo));
                }
            } else if (targetSubtree != null) {
                for (IEPTreeNode tFrom : targetSubtree.getEdgeFromSemantics()) {
                    from.add(node2state.get(tFrom));
                }
            }
            
            // TODO: tricky part with cancelation edges ...
            // see also remarks at EPTreeSemantics
            Set<ISCState> to = new THashSet<>();
            if (targetSubtree != null) {
                for (IEPTreeNode tFrom : targetSubtree.getStartSemantics()) {
                    to.add(node2state.get(tFrom));
                }
            } else if (sourceSubtree != null) {
                for (IEPTreeNode tTo : sourceSubtree.getEdgeToSemantics()) {
                    to.add(node2state.get(tTo));
                }
            }
            
            transition.setEdgeSemantics(from, to);
        }
    }

}
