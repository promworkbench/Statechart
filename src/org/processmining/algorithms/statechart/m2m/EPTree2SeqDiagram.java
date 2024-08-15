package org.processmining.algorithms.statechart.m2m;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.tracing.TraceUniqueDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.msd.Activation;
import org.processmining.models.statechart.msd.FragmentType;
import org.processmining.models.statechart.msd.IActivation;
import org.processmining.models.statechart.msd.IMSDMessage;
import org.processmining.models.statechart.msd.ISeqDiagram;
import org.processmining.models.statechart.msd.Lifeline;
import org.processmining.models.statechart.msd.LifelineType;
import org.processmining.models.statechart.msd.MSDFragment;
import org.processmining.models.statechart.msd.MSDFragmentPart;
import org.processmining.models.statechart.msd.MessageType;
import org.processmining.models.statechart.msd.SeqDiagram;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class EPTree2SeqDiagram implements Function<IEPTree, ISeqDiagram> {

    private IEPTree input;
    private Function<Pair<IEPTree, IEPTreeNode>, String> labelfncLifeline;
    private Function<Pair<IEPTree, IEPTreeNode>, String> labelfncMessage;
    private Map<String, Lifeline> mapLifelines;
    
    private Decorations<IEPTreeNode> treeDecorations;
    private Decorations<IActivation> actDecorations;
    private TraceUniqueDecorator<IActivation, IEPTreeNode> treeTracer;
    private Map<IEPTreeNode, IActivation> node2act;
    private Map<IMSDMessage, IEPTreeNode> edgeDelaySem;

    @Override
    public ISeqDiagram apply(IEPTree input) {
        return transform(input);
    }
    
    public void setLabelFnc(
            Function<Pair<IEPTree, IEPTreeNode>, String> labelfncLifeline, 
            Function<Pair<IEPTree, IEPTreeNode>, String> labelfncMessage) {
        this.labelfncLifeline = labelfncLifeline;
        this.labelfncMessage = labelfncMessage;
    }

    public ISeqDiagram transform(IEPTree input, 
            Function<Pair<IEPTree, IEPTreeNode>, String> labelfncLifeline, 
            Function<Pair<IEPTree, IEPTreeNode>, String> labelfncMessage) {
        setLabelFnc(labelfncLifeline, labelfncMessage);
        return transform(input);
    }

    @SuppressWarnings("unchecked")
    public ISeqDiagram transform(IEPTree input) {
        Preconditions.checkNotNull(input);
        
        this.input = input;
        this.mapLifelines = new THashMap<String, Lifeline>();
        this.node2act = new THashMap<IEPTreeNode, IActivation>();

        treeDecorations = input.getDecorations();
        
        // setup supported decorators, will be derived automatically
        actDecorations = treeDecorations.deriveDecorationInstance(IActivation.class);
        
        SeqDiagram model = new SeqDiagram(actDecorations);

        treeTracer = actDecorations.getForType(TraceUniqueDecorator.class);
        edgeDelaySem = new THashMap<>();
        
        Lifeline objectEnvironment = new Lifeline("Environment", LifelineType.Environment);
        Activation actEnvironment = objectEnvironment.createActivation();
        model.addLifeline(objectEnvironment);
        
        MSDFragment root = new MSDFragment(null, "Root", FragmentType.Root);
        MSDFragmentPart rootPart = root.createPart("root");
        model.setRoot(root);
        
        _transform(input.getRoot(), actEnvironment, rootPart);
        
        for (Lifeline val : mapLifelines.values()) {
            model.addLifeline(val);
        }
        
        model.sortLifelines();
        
        completeEdgeSemantics();
        
        return model;
    }

    private void _transform(IEPTreeNode node, IActivation currentAct, MSDFragmentPart parentFragmentPart) {
        switch (node.getNodeType()) {
        case Action:
            _transformAction(node, currentAct, parentFragmentPart);
            break;
        case AndComposite:
        case AndInterleaved:
            _transformAnd(node, currentAct, parentFragmentPart);
            break;
        case Choice:
            _transformChoice(node, currentAct, parentFragmentPart);
            break;
        case Collapsed:
            _transformAction(node, currentAct, parentFragmentPart);
            break;
        case ErrorTrigger:
            _transformErrorTrigger(node, currentAct, parentFragmentPart);
            break;
        case Loop:
            _transformLoop(node, currentAct, parentFragmentPart);
            break;
        case OrComposite:
            _transformOrComposite(node, currentAct, parentFragmentPart);
            break;
        case Recurrent:
            _transformRecurrent(node, currentAct, parentFragmentPart);
            break;
        case Seq:
            _transformSeq(node, currentAct, parentFragmentPart);
            break;
        case Silent:
            // NOP
            break;
        case SeqCancel:
            _transformSeqCancel(node, currentAct, parentFragmentPart);
            break;
        case LoopCancel:
            _transformLoopCancel(node, currentAct, parentFragmentPart);
            break;
        case Log:
            _transformLog(node, currentAct, parentFragmentPart);
            break;
        default:
            throw new TransformationException("Node type not supported: "
                    + node.getNodeType());
//            break;
        
        }
    }

    private void _transformAction(IEPTreeNode node, IActivation currentAct,
            MSDFragmentPart parentFragmentPart) {
        Pair<IEPTree, IEPTreeNode> info = Pair.of(input, node);
        String lblLifeline = labelfncLifeline.apply(info);
        String lblMessage = labelfncMessage.apply(info);
        
        _createActionMessage(node, currentAct, parentFragmentPart, 
                lblLifeline, lblMessage);
    }

    private void _transformLog(IEPTreeNode node, IActivation currentAct,
            MSDFragmentPart parentFragmentPart) {
//        Pair<IEPTree, IEPTreeNode> info = Pair.of(input, node);
//        String lblLifeline = labelfncLifeline.apply(info);
//        String lblMessage = labelfncMessage.apply(info);
        String lblLifeline = "Log";
        String lblMessage = "Log Move";
                
        _createActionMessage(node, currentAct, parentFragmentPart, 
                lblLifeline, lblMessage);
    }

    private void _transformErrorTrigger(IEPTreeNode node,
            IActivation currentAct, MSDFragmentPart parentFragmentPart) {
        Pair<IEPTree, IEPTreeNode> info = Pair.of(input, node);
        String lblLifeline = labelfncLifeline.apply(info);
        String lblMessage = "Error Trigger: " + labelfncMessage.apply(info);
        
        _createActionMessage(node, currentAct, parentFragmentPart, 
                lblLifeline, lblMessage);
    }

    private void _transformRecurrent(IEPTreeNode node, IActivation currentAct,
            MSDFragmentPart parentFragmentPart) {
        Pair<IEPTree, IEPTreeNode> info = Pair.of(input, node);
        String lblLifeline = labelfncLifeline.apply(info);
        String lblMessage = "Recursive call: " + labelfncMessage.apply(info);
        
        _createActionMessage(node, currentAct, parentFragmentPart, 
                lblLifeline, lblMessage);
    }
    
    private void _createActionMessage(IEPTreeNode node, IActivation currentAct, 
            MSDFragmentPart parentFragmentPart, 
            String lblLifeline, String lblMessage) {
        Lifeline lifeline = _provideLifeline(lblLifeline, LifelineType.Normal);
        Activation newAct = lifeline.createActivation();

        _decorate(newAct, node);
        
        if (currentAct.getLifeline() != lifeline) {
            _decorate(parentFragmentPart.createMessage(lblMessage, MessageType.Call, currentAct, newAct, node.getId()), node);
            _decorate(parentFragmentPart.createMessage(lblMessage, MessageType.Return, newAct, currentAct, node.getId()), node);
        } else {
            _decorate(parentFragmentPart.createMessage(lblMessage, MessageType.CallSelf, currentAct, newAct, node.getId()), node);
            _decorate(parentFragmentPart.createMessage(lblMessage, MessageType.ReturnSelf, newAct, currentAct, node.getId()), node);
        }
    }

    private void _transformOrComposite(IEPTreeNode node,
            IActivation currentAct, MSDFragmentPart parentFragmentPart) {
        Pair<IEPTree, IEPTreeNode> info = Pair.of(input, node);
        String lblLifeline = labelfncLifeline.apply(info);
        String lblMessage = labelfncMessage.apply(info);
        
        Lifeline lifeline = _provideLifeline(lblLifeline, LifelineType.Normal);
        Activation newAct = lifeline.createActivation();

        _decorate(newAct, node);
        
        if (currentAct.getLifeline() != lifeline) {
            _decorate(parentFragmentPart.createMessage(lblMessage, MessageType.Call, currentAct, newAct, node.getId()), node);
            for (IEPTreeNode child : node.getChildren()) {
                _transform(child, newAct, parentFragmentPart);
            }
            _decorate(parentFragmentPart.createMessage(lblMessage, MessageType.Return, newAct, currentAct, node.getId()), node);
        } else {
            _decorate(parentFragmentPart.createMessage(lblMessage, MessageType.CallSelf, currentAct, newAct, node.getId()), node);
            for (IEPTreeNode child : node.getChildren()) {
                _transform(child, newAct, parentFragmentPart);
            }
            _decorate(parentFragmentPart.createMessage(lblMessage, MessageType.ReturnSelf, newAct, currentAct, node.getId()), node);
        }
    }

    private void _transformSeq(IEPTreeNode node, IActivation currentAct,
            MSDFragmentPart parentFragmentPart) {
        for (IEPTreeNode child : node.getChildren()) {
            _transform(child, currentAct, parentFragmentPart);
        }
    }

    private void _transformAnd(IEPTreeNode node, IActivation currentAct,
            MSDFragmentPart parentFragmentPart) {
        _createFragment(node, currentAct, parentFragmentPart, FragmentType.Parallel);
    }

    private void _transformChoice(IEPTreeNode node, IActivation currentAct,
            MSDFragmentPart parentFragmentPart) {
        _createFragment(node, currentAct, parentFragmentPart, FragmentType.Alt);
    }

    private void _transformLoop(IEPTreeNode node, IActivation currentAct,
            MSDFragmentPart parentFragmentPart) {
        _createFragment(node, currentAct, parentFragmentPart, FragmentType.Loop);
    }

    private void _transformLoopCancel(IEPTreeNode node, IActivation currentAct,
            MSDFragmentPart parentFragmentPart) {
        _createFragment(node, currentAct, parentFragmentPart, FragmentType.LoopCancel);
    }

    private void _transformSeqCancel(IEPTreeNode node, IActivation currentAct,
            MSDFragmentPart parentFragmentPart) {
        _createFragment(node, currentAct, parentFragmentPart, FragmentType.SeqCancel);
    }

    private void _createFragment(IEPTreeNode node, IActivation currentAct,
            MSDFragmentPart parentFragmentPart, FragmentType fragmentType) {
        MSDFragment fragment = parentFragmentPart.createFragment(node.getLabel(), fragmentType);
        
        for (IEPTreeNode child : node.getChildren()) {
            MSDFragmentPart part = fragment.createPart("");
            _transform(child, currentAct, part);
        }
    }

    private Lifeline _provideLifeline(String lblLifeline, LifelineType type) {
        Lifeline inst = mapLifelines.get(lblLifeline);
        if (inst == null) {
            inst = new Lifeline(lblLifeline, type);
            mapLifelines.put(lblLifeline, inst);
        }
        return inst;
    }

    private void _decorate(IActivation newAct, IEPTreeNode node) {
        treeTracer.setDecoration(newAct, node);
        node2act.put(node, newAct);
        actDecorations.deriveDecorations(newAct, node, treeDecorations);
    }

    private void _decorate(IMSDMessage msgEdge, IEPTreeNode node) {
        edgeDelaySem.put(msgEdge,  node);
    }
    
    private void completeEdgeSemantics() {
        for (IMSDMessage msgEdge : edgeDelaySem.keySet()) {
            IEPTreeNode node = edgeDelaySem.get(msgEdge);
            
            Set<IActivation> from = new THashSet<>();
            Set<IActivation> to = new THashSet<>();
            
            if (msgEdge.isStartActivation()) {
                for (IEPTreeNode tFrom : node.getEdgeFromSemantics()) {
                    from.add(node2act.get(tFrom));
                }
                to.add(node2act.get(node));
            } else {
                from.add(node2act.get(node));
                for (IEPTreeNode tTo : node.getEdgeToSemantics()) {
                    to.add(node2act.get(tTo));
                }
            }
            
            msgEdge.setEdgeSemantics(from, to);
        }
    }
}
