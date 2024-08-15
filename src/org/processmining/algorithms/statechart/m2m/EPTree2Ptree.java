package org.processmining.algorithms.statechart.m2m;

import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.plugins.InductiveMiner.mining.interleaved.Interleaved;
import org.processmining.processtree.Block;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.Task.Manual;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;
import org.processmining.processtree.impl.ProcessTreeImpl;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class EPTree2Ptree implements Function<IEPTree, ProcessTree> {

//    private static final Logger logger = LogManager
//            .getLogger(EPTree2Ptree.class.getName());
    
    private boolean lifecycleHierarchySupport;

    public EPTree2Ptree() {
        this(true);
    }
    
    public EPTree2Ptree(boolean lifecycleHierarchySupport) {
        this.lifecycleHierarchySupport = lifecycleHierarchySupport;
    }

    @Override
    public ProcessTree apply(IEPTree input) {
        return transform(input);
    }

    public ProcessTree transform(IEPTree input) {
        Preconditions.checkNotNull(input);
        
        ProcessTree tree = new ProcessTreeImpl();
        Node root = _transform(tree, input.getRoot());
        root.setProcessTree(tree);
        tree.setRoot(root);

        return tree;
    }

    private Node _transform(ProcessTree tree, IEPTreeNode eNode) {
        switch (eNode.getNodeType()) {
        case Action:
            return _transformAction(tree, eNode);
        case Silent:
            return _transformSilent(tree, eNode);
        case Collapsed:
            return _transformCollapsed(tree, eNode);
        case Recurrent:
            return _transformRecurrent(tree, eNode);
        case Seq:
            return _transformSeq(tree, eNode);
        case Choice:
            return _transformChoice(tree, eNode);
        case Loop:
            return _transformLoop(tree, eNode);
        case AndComposite:
            return _transformAndComposite(tree, eNode);
        case AndInterleaved:
            return _transformAndInterleaved(tree, eNode);
        case OrComposite:
            return _transformOrComposite(tree, eNode);
        case Log:
            return _transformLog(tree, eNode);
        default:
            throw new TransformationException("Node type not supported: " + eNode.getNodeType());
        }
    }

    private Node _transformAction(ProcessTree tree, IEPTreeNode eNode) {
        if (lifecycleHierarchySupport) {
            
            Seq seq = new AbstractBlock.Seq("");
            addNode(tree, seq);
            
            Manual nodeStart = new AbstractTask.Manual(eNode.getLabel() + "+start");
            addNode(tree, nodeStart);
            addChild(seq, nodeStart);

            Manual nodeEnd = new AbstractTask.Manual(eNode.getLabel() + "+complete");
            addNode(tree, nodeEnd);
            addChild(seq, nodeEnd);
            return seq;
        } else {
            Manual node = new AbstractTask.Manual(eNode.getLabel());
            addNode(tree, node);
            return node;
        }
    }

    private Node _transformLog(ProcessTree tree, IEPTreeNode eNode) {
        Automatic node = new AbstractTask.Automatic("log");
        addNode(tree, node);
        return node;
    }

    private Node _transformSilent(ProcessTree tree, IEPTreeNode eNode) {
        Automatic node = new AbstractTask.Automatic("tau");
        addNode(tree, node);
        return node;
    }

    private Node _transformCollapsed(ProcessTree tree, IEPTreeNode eNode) {
        return _transformAction(tree, eNode);
    }

    private Node _transformRecurrent(ProcessTree tree, IEPTreeNode eNode) {
        // TODO warning: not the way to deal with this
        return _transformAction(tree, eNode);
    }

    private Node _transformSeq(ProcessTree tree, IEPTreeNode eNode) {
        return _transformBlock(tree, eNode, new AbstractBlock.Seq(""));
    }

    private Node _transformChoice(ProcessTree tree, IEPTreeNode eNode) {
        return _transformBlock(tree, eNode, new AbstractBlock.Xor(""));
    }

    private Node _transformLoop(ProcessTree tree, IEPTreeNode eNode) {
        AbstractBlock parentBlock = new AbstractBlock.XorLoop("");
        addNode(tree, parentBlock);
        
        addChild(parentBlock, _transform(tree, eNode.getChildren().get(0)));
        addChild(parentBlock, _transform(tree, eNode.getChildren().get(1)));

        Automatic node = new AbstractTask.Automatic("tau");
        addNode(tree, node);
        addChild(parentBlock, node);
        
        return parentBlock;
    }

    private Node _transformAndComposite(ProcessTree tree, IEPTreeNode eNode) {
        return _transformBlock(tree, eNode, new AbstractBlock.And(""));
    }

    private Node _transformAndInterleaved(ProcessTree tree, IEPTreeNode eNode) {
        return _transformBlock(tree, eNode, new Interleaved(""));
    }

    private Node _transformBlock(ProcessTree tree, IEPTreeNode eNode, AbstractBlock parentBlock) {
        addNode(tree, parentBlock);
        for (IEPTreeNode child : eNode.getChildren()) {
            addChild(parentBlock, _transform(tree, child));
        }
        return parentBlock;
    }

    private Node _transformOrComposite(ProcessTree tree, IEPTreeNode eNode) {
        if (eNode.getChildren().isEmpty()) {
            throw new TransformationException("No OrComposite children found");
        }
        if (lifecycleHierarchySupport) {
            Seq seq = new AbstractBlock.Seq("");
            addNode(tree, seq);
            
            Manual nodeStart = new AbstractTask.Manual(eNode.getLabel() + "+start");
            addNode(tree, nodeStart);
            addChild(seq, nodeStart);
            
            addChild(seq, _transform(tree, eNode.getChildren().get(0)));

            Manual nodeEnd = new AbstractTask.Manual(eNode.getLabel() + "+complete");
            addNode(tree, nodeEnd);
            addChild(seq, nodeEnd);
            return seq;
        } else {
            return _transform(tree, eNode.getChildren().get(0));
        }
    }

    private void addNode(ProcessTree tree, Node node) {
        node.setProcessTree(tree);
        tree.addNode(node);
    }
    
    private void addChild(Block parent, Node child) {
        parent.addChild(child);
    }
}
