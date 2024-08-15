package org.processmining.models.statechart.msd;

import org.processmining.utils.statechart.tree.ITreeNode;

public interface IMSDNode extends ITreeNode<IMSDNode> {

    public String getId();
    
    public String getName();
}
