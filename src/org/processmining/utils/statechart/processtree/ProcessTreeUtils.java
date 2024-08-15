package org.processmining.utils.statechart.processtree;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.processmining.models.statechart.processtree.ErrorTrigger;
import org.processmining.models.statechart.processtree.LoopCancel;
import org.processmining.models.statechart.processtree.SCCompositeOr;
import org.processmining.models.statechart.processtree.SCRecurrentOr;
import org.processmining.models.statechart.processtree.SeqCancel;
import org.processmining.plugins.InductiveMiner.mining.interleaved.Interleaved;
import org.processmining.plugins.InductiveMiner.mining.interleaved.MaybeInterleaved;
import org.processmining.plugins.properties.processmodel.Property;
import org.processmining.processtree.Block;
import org.processmining.processtree.Edge;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractBlock.Def;
import org.processmining.processtree.impl.AbstractBlock.DefLoop;
import org.processmining.processtree.impl.AbstractBlock.Or;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;

public class ProcessTreeUtils {

    public static void cleanupNode(ProcessTree tree, Node current) {
        if (current instanceof Block) {
            cleanupSubtree(tree, (Block) current);
        }
        tree.removeNode(current);
    }

    public static void cleanupSubtree(ProcessTree tree, Block current) {
        // remove childs
        for (Node child : current.getChildren()) {
            if (child instanceof Block) {
                cleanupSubtree(tree, (Block) child);
            }
            tree.removeNode(child);
        }

        // remove edges
        for (Edge e : new ArrayList<>(current.getOutgoingEdges())) {
            current.removeOutgoingEdge(e);
            tree.removeEdge(e);
        }
    }

    public static Node cloneNode(ProcessTree tree, Node node,
            Map<Node, Node> mapNewToOld) {
        // Instantiante a new node, with a new ID, of the right type
        Node newNode;
        Block newBlock = null;
        if (node instanceof Block) {
            if (node instanceof MaybeInterleaved) {
                newBlock = new MaybeInterleaved(UUID.randomUUID(),
                        node.getName());
            } else if (node instanceof Xor) {
                newBlock = new Xor(UUID.randomUUID(), node.getName());
            } else if (node instanceof Or) {
                newBlock = new Or((Or) node);
            } else if (node instanceof Interleaved) {
                newBlock = new Interleaved(UUID.randomUUID(), node.getName());
            } else if (node instanceof And) {
                newBlock = new And(UUID.randomUUID(), node.getName());
            } else if (node instanceof Seq) {
                newBlock = new Seq(UUID.randomUUID(), node.getName());
            } else if (node instanceof XorLoop) {
                newBlock = new XorLoop(UUID.randomUUID(), node.getName());
            } else if (node instanceof Def) {
                newBlock = new Def(UUID.randomUUID(), node.getName());
            } else if (node instanceof DefLoop) {
                newBlock = new DefLoop(UUID.randomUUID(), node.getName());
            } else if (node instanceof SCCompositeOr) {
                newBlock = new SCCompositeOr(UUID.randomUUID(), node.getName());
            } else if (node instanceof SeqCancel) {
                newBlock = new SeqCancel(UUID.randomUUID(), node.getName());
            } else if (node instanceof LoopCancel) {
                newBlock = new LoopCancel(UUID.randomUUID(), node.getName());
            } else {
                throw new IllegalArgumentException(
                        "Type of ProcessTree node not recognized: " + node);
            }
            newNode = newBlock;

        } else if (node instanceof SCRecurrentOr) {
            newNode = new SCRecurrentOr(UUID.randomUUID(), node.getName());
        } else if (node instanceof ErrorTrigger) {
            newNode = new ErrorTrigger(UUID.randomUUID(), node.getName());
        } else if (node instanceof Manual) {
            newNode = new Manual(UUID.randomUUID(), node.getName());
        } else if (node instanceof Automatic) {
            newNode = new Automatic(UUID.randomUUID(), node.getName());
        } else {
            throw new IllegalArgumentException(
                    "Type of ProcessTree node not recognized: " + node);
        }

        // add the new node to the tree
        newNode.setProcessTree(tree);
        tree.addNode(newNode);

        if (mapNewToOld != null) {
            mapNewToOld.put(newNode, node);
        }

        // copy properties
        try {
            AbstractMap<Property<?>, Object> propsDep = node
                    .getDependentProperties();
            for (Property<?> key : propsDep.keySet()) {
                newNode.setDependentProperty(key, propsDep.get(key));
            }
            AbstractMap<Property<?>, Object> propsIndep = node
                    .getIndependentProperties();
            for (Property<?> key : propsIndep.keySet()) {
                newNode.setIndependentProperty(key, propsIndep.get(key));
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

        // for blocks, add subtree
        if (newBlock != null) {
            for (Node child : ((Block) node).getChildren()) {
                newBlock.addChild(cloneNode(tree, child, mapNewToOld));
            }
        }

        return newNode;
    }
}
