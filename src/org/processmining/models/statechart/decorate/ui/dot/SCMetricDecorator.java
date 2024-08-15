package org.processmining.models.statechart.decorate.ui.dot;

import org.processmining.algorithms.statechart.m2m.ui.decorate.IUiDecorator;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.ISCTransition;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.plugins.graphviz.dot.DotCluster;
import org.processmining.plugins.graphviz.dot.DotNode;

public class SCMetricDecorator 
    extends DotMetricDecorator<ISCState, ISCTransition, Statechart> {

    public SCMetricDecorator(IUiDecorator<ISCState, ISCTransition> uiDecorator) {
        super(uiDecorator);
    }
    
    @Override
    public void decorateNode(ISCState node, DotNode dotNode) {
        if (!node.isPseudoState() && !(dotNode instanceof DotCluster)) {
            super.decorateNode(node, dotNode);
        }
    }

}
