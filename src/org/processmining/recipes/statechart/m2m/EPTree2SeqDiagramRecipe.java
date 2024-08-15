package org.processmining.recipes.statechart.m2m;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.algorithms.statechart.m2m.EPTree2SeqDiagram;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.labeling.ClassNameActivityLabeler;
import org.processmining.models.statechart.labeling.EPTreeSWActivityLabelAdapter;
import org.processmining.models.statechart.labeling.MethodNameActivityLabeler;
import org.processmining.models.statechart.msd.ISeqDiagram;
import org.processmining.recipes.statechart.AbstractRecipe;

import com.google.common.base.Function;

public class EPTree2SeqDiagramRecipe extends
        AbstractRecipe<IEPTree, ISeqDiagram, EPTree2SeqDiagramRecipe.Parameters> {

    public static class Parameters {
        public Function<Pair<IEPTree, IEPTreeNode>, String> labelfncLifeline = 
            new EPTreeSWActivityLabelAdapter(new ClassNameActivityLabeler());
        
        public Function<Pair<IEPTree, IEPTreeNode>, String> labelfncMessage = 
            new EPTreeSWActivityLabelAdapter(new MethodNameActivityLabeler());
    }
    
    public EPTree2SeqDiagramRecipe() {
        super(new Parameters());
    }

    @Override
    protected ISeqDiagram execute(IEPTree input) {
        Parameters params = getParameters();
        
        EPTree2SeqDiagram m2m = new EPTree2SeqDiagram();
        return m2m.transform(input, params.labelfncLifeline, params.labelfncMessage);
    }

}
