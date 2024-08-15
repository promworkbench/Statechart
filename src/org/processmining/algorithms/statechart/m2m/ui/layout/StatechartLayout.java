package org.processmining.algorithms.statechart.m2m.ui.layout;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.algorithms.statechart.layout.PGLayoutConfiguration;
import org.processmining.algorithms.statechart.layout.PGNodeExtendProvider;
import org.processmining.algorithms.statechart.layout.ProcessGraphLayout;
import org.processmining.algorithms.statechart.m2m.ui.style.StatechartStyle;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.sc.Statechart;

import com.google.common.base.Function;

public class StatechartLayout implements Function<Pair<IEPTree, Statechart>, ProcessGraphLayout<StatechartLayoutNode>> {

    private StatechartStyle style;

    public StatechartLayout(StatechartStyle style) {
        this.style = style;
    }
    
    @Override
    public ProcessGraphLayout<StatechartLayoutNode> apply(
            Pair<IEPTree, Statechart> input) {
        return calculate(input.getLeft(), input.getRight());
    }

    public ProcessGraphLayout<StatechartLayoutNode> calculate(IEPTree inputTree,
            Statechart inputStatechart) {
        
        StatechartLayoutAdapter model = new StatechartLayoutAdapter(inputTree, inputStatechart);
        PGNodeExtendProvider<StatechartLayoutNode> nodeExtentProvider = style;
        PGLayoutConfiguration<StatechartLayoutNode> configuration = style;
        boolean useIdentity = true;
        
        return new ProcessGraphLayout<StatechartLayoutNode>(
                model, nodeExtentProvider, configuration, useIdentity);
    }

}
