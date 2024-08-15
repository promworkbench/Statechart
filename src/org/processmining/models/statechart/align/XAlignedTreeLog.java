package org.processmining.models.statechart.align;

import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignedLog;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public interface XAlignedTreeLog extends XAlignedLog  {

    public IEPTree getTree();
    
    public PetrinetDecorated getPerinet();
    
    public IEPTreeNode getNode(XAlignmentMove move);
    
    public Map<String, IEPTreeNode> getMapTreeNodes();

    public Transition getTransition(XAlignmentMove move);

    public double getAveragePrecision();
    
    public XAlignedTreeLog copy();
    
}
