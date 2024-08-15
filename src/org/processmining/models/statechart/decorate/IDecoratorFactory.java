package org.processmining.models.statechart.decorate;

import org.processmining.models.statechart.eptree.IEPTreeNode;

public interface IDecoratorFactory {

    public Decorations<IEPTreeNode> createEPTreeDecorations();
}
