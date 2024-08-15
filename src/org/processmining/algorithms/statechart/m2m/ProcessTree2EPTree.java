package org.processmining.algorithms.statechart.m2m;

import java.util.List;

import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.DecoratorFactory;
import org.processmining.models.statechart.decorate.IDecoratorFactory;
import org.processmining.models.statechart.eptree.EPNodeType;
import org.processmining.models.statechart.eptree.EPTree;
import org.processmining.models.statechart.eptree.EPTreeNode;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.processtree.IErrorTrigger;
import org.processmining.models.statechart.processtree.ILoopCancel;
import org.processmining.models.statechart.processtree.ISCCompositeOr;
import org.processmining.models.statechart.processtree.ISCRecurrentOr;
import org.processmining.models.statechart.processtree.ISeqCancel;
import org.processmining.plugins.InductiveMiner.mining.interleaved.Interleaved;
import org.processmining.processtree.Block;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task;
import org.processmining.processtree.Task.Automatic;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class ProcessTree2EPTree implements Function<ProcessTree, IEPTree> {

    private Decorations<IEPTreeNode> decorations;

    @Override
    public IEPTree apply(ProcessTree input) {
        return transform(input);
    }
    
    public IEPTree transform(ProcessTree input) {
        return transform(input, DecoratorFactory.getDefaultInst());
    }
    
    public IEPTree transform(ProcessTree input, IDecoratorFactory decFact) {
        Preconditions.checkNotNull(input);

        // setup supported decorators, will be derived automatically
        decorations = decFact.createEPTreeDecorations();
        
        // transform model
        IEPTree tree = new EPTree(input.getName(), decorations);
        tree.setRoot(_transform(input.getRoot(), tree, null));
        return tree;
    }

    private IEPTreeNode _transform(Node node, IEPTree tree, IEPTreeNode parent) {
        IEPTreeNode newNode;
        if (node instanceof Task) {
            if (node instanceof ISCRecurrentOr) {
                newNode = _transformSCRecurrentOr((ISCRecurrentOr) node, tree, parent);
            } else if (node instanceof IErrorTrigger) {
                    newNode = _transformErrorTrigger((IErrorTrigger) node, tree, parent);
            } else if (_isSilentTask(node)) {
                newNode = _transformLeafSilent((Task) node, tree, parent);
            } else {
                newNode = _transformLeafAction((Task) node, tree, parent);
            }
        } else if (node instanceof Block) {
            if (node instanceof Seq) {
                newNode = _transformSeq((Seq) node, tree, parent);
            } else if (node instanceof Xor) {
                newNode = _transformXor((Xor) node, tree, parent);
            } else if (node instanceof Interleaved) {
                newNode = _transformInterleaved((Interleaved) node, tree, parent);
            } else if (node instanceof And) {
                newNode = _transformAnd((And) node, tree, parent);
            } else if (node instanceof XorLoop) {
                newNode = _transformXorLoop((XorLoop) node, tree, parent);
            } else if (node instanceof ISCCompositeOr) {
                newNode = _transformSCCompositeOr((ISCCompositeOr) node, tree, parent);
            } else if (node instanceof ISeqCancel) {
                newNode = _transformSeqCancel((ISeqCancel) node, tree, parent);
            } else if (node instanceof ILoopCancel) {
                newNode = _transformLoopCancel((ILoopCancel) node, tree, parent);
            } else {
                throw new TransformationException(
                        "Process Tree block not supported: "
                                + node.toStringShort());
            }
        } else {
            throw new TransformationException(
                    "Process Tree construct not supported: "
                            + node.toStringShort());
        }

        _decorate(newNode, node);
        return newNode;
    }

    private boolean _isSilentTask(Node node) {
        return node instanceof Automatic || node.getName().equals("tau");
    }

    private IEPTreeNode _transformLeafAction(Task node, IEPTree tree, IEPTreeNode parent) {
        return new EPTreeNode(tree, parent, EPNodeType.Action, node.getName());
    }

    private IEPTreeNode _transformLeafSilent(Task node, IEPTree tree, IEPTreeNode parent) {
        return new EPTreeNode(tree, parent, EPNodeType.Silent, node.getName());
    }

    private IEPTreeNode _transformSCRecurrentOr(ISCRecurrentOr node,
            IEPTree tree, IEPTreeNode parent) {
        return new EPTreeNode(tree, parent, EPNodeType.Recurrent, node.getName());
    }

    private IEPTreeNode _transformErrorTrigger(IErrorTrigger node,
            IEPTree tree, IEPTreeNode parent) {
        return new EPTreeNode(tree, parent, EPNodeType.ErrorTrigger, node.getName());
    }

    private IEPTreeNode _transformSeq(Seq node, IEPTree tree, IEPTreeNode parent) {
        IEPTreeNode current = new EPTreeNode(tree, parent, EPNodeType.Seq, "");

        List<Node> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            current.addNode(_transform(children.get(i), tree, current));
        }

        return current;
    }

    private IEPTreeNode _transformXor(Xor node, IEPTree tree, IEPTreeNode parent) {
        IEPTreeNode current = new EPTreeNode(tree, parent, EPNodeType.Choice, "");

        List<Node> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            current.addNode(_transform(children.get(i), tree, current));
        }

        return current;
    }

    private IEPTreeNode _transformInterleaved(Interleaved node, IEPTree tree, IEPTreeNode parent) {
        IEPTreeNode current = new EPTreeNode(tree, parent, EPNodeType.AndInterleaved, "");

        List<Node> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            current.addNode(_transform(children.get(i), tree, current));
        }

        return current;
    }

    private IEPTreeNode _transformAnd(And node, IEPTree tree, IEPTreeNode parent) {
        IEPTreeNode current = new EPTreeNode(tree, parent, EPNodeType.AndComposite, "");

        List<Node> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            current.addNode(_transform(children.get(i), tree, current));
        }

        return current;
    }

    private IEPTreeNode _transformXorLoop(XorLoop node, IEPTree tree, IEPTreeNode parent) {
        List<Node> children = node.getChildren();
        if (children.size() != 3) {
            throw new TransformationException(
                    "Process Tree XorLoop doesn't have 3 children");
        }

        // Process Tree is loop(0: do, 1: redo, 2: exit)
        // EPTree is loop(0: do, 1: redo)
        // so convert exit to sequence(loop(0: do, 1: redo), 2: exit)
        if (!_isSilentTask(children.get(2))) {
            parent = new EPTreeNode(tree, parent, EPNodeType.Seq, "");
            parent.addNode(_transform(children.get(2), tree, parent));
        }

        IEPTreeNode current = new EPTreeNode(tree, parent, EPNodeType.Loop, "");
        current.addNode(_transform(children.get(0), tree, current));
        current.addNode(_transform(children.get(1), tree, current));

        return current;
    }

    private IEPTreeNode _transformSCCompositeOr(ISCCompositeOr node,
            IEPTree tree, IEPTreeNode parent) {
        IEPTreeNode current = new EPTreeNode(tree, parent, EPNodeType.OrComposite, node.getName());

        List<Node> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            current.addNode(_transform(children.get(i), tree, current));
        }

        return current;
    }

    private IEPTreeNode _transformSeqCancel(ISeqCancel node, IEPTree tree, IEPTreeNode parent) {
        IEPTreeNode current = new EPTreeNode(tree, parent, EPNodeType.SeqCancel, node.getName());

        List<Node> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            current.addNode(_transform(children.get(i), tree, current));
        }

        return current;
    }

    private IEPTreeNode _transformLoopCancel(ILoopCancel node,
            IEPTree tree, IEPTreeNode parent) {
        IEPTreeNode current = new EPTreeNode(tree, parent, EPNodeType.LoopCancel, node.getName());

        List<Node> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            current.addNode(_transform(children.get(i), tree, current));
        }

        return current;
    }

    private void _decorate(IEPTreeNode newNode, Node node) {
        decorations.deriveDecorations(newNode, node, null);
    }
}
