package org.processmining.recipes.statechart.align;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.algorithms.statechart.align.AlignTreeEventIntervals;
import org.processmining.models.statechart.align.XAlignedTreeLog;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.recipes.statechart.AbstractRecipe;

public class AlignTreeRecipe extends AbstractRecipe<Pair<XAlignedTreeLog, IEPTree>, IEPTree, AlignTreeRecipe.Parameters> {

    public static class Parameters {
        
    }
    
    public AlignTreeRecipe() {
        super(new Parameters());
    }

    @Override
    protected IEPTree execute(Pair<XAlignedTreeLog, IEPTree> input) {
        // No clone needed, do in-place annotation
        AlignTreeEventIntervals annoter = new AlignTreeEventIntervals();
        annoter.apply(input);
        return input.getRight();
    }
}
