package org.processmining.models.statechart.align;

import gnu.trove.map.hash.THashMap;

import java.util.AbstractList;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.m2m.EPTree2Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.xesalignmentextension.XAlignmentExtension;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignedLog;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignment;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public class XAlignedTreeLogImpl 
    extends AbstractList<XAlignment> 
    implements XAlignedTreeLog {

    private final XAlignedLog alignedLog;
    private final IEPTree tree;
    private final PetrinetDecorated net;
    
    private final Map<String, IEPTreeNode> mapTreeNodes;
    private final Map<String, Transition> mapTransitions;
    
    private double precision;

    public XAlignedTreeLogImpl(XAlignedLog alignedLog, double precision, IEPTree tree, 
            PetrinetDecorated net) {
        this.alignedLog = alignedLog;
        this.precision = precision;
        this.tree = tree;
        this.net = net;
        
        // Build quick id lookup map
        Map<String, IEPTreeNode> mapTreeNode = new THashMap<>();
        for (IEPTreeNode node : tree.iteratePreOrder()) {
            mapTreeNode.put(node.getId(), node);
        }
        
        // Build map from alignment move to tree node
        mapTreeNodes = new THashMap<>();
        mapTransitions = new THashMap<>();
        for (Transition t : net.getTransitions()) {
            String localId = t.getLocalID().toString();
            mapTransitions.put(localId, t);
            
            if (t.getAttributeMap().containsKey(EPTree2Petrinet.Key_NodeId)) {
                String nodeId = (String) t.getAttributeMap().get(EPTree2Petrinet.Key_NodeId);
                IEPTreeNode node = mapTreeNode.get(nodeId);
                if (node != null) {
                    mapTreeNodes.put(localId, node);
                }
            }
        }
    }

    public int size() {
        return alignedLog.size();
    }

    public double getAverageFitness() {
        return alignedLog.getAverageFitness();
    }

    public XAlignment get(int index) {
        return alignedLog.get(index);
    }

    public XLog getLog() {
        return alignedLog.getLog();
    }

    @Override
    public IEPTree getTree() {
        return tree;
    }

    @Override
    public PetrinetDecorated getPerinet() {
        return net;
    }

    @Override
    public IEPTreeNode getNode(XAlignmentMove move) {
        return mapTreeNodes.get(move.getActivityId());
    }

    @Override
    public Map<String, IEPTreeNode> getMapTreeNodes() {
        return mapTreeNodes;
    }

    @Override
    public Transition getTransition(XAlignmentMove move) {
        return mapTransitions.get(move.getActivityId());
    }

    @Override
    public double getAveragePrecision() {
        return precision;
    }

    @Override
    public XAlignedTreeLog copy() {
        XLog clonedLog = (XLog) alignedLog.getLog().clone();
        XAlignedLog clonedAlignedLog = XAlignmentExtension.instance().extendLog(clonedLog);
        return new XAlignedTreeLogImpl(clonedAlignedLog, precision, tree, net);
    }

}
