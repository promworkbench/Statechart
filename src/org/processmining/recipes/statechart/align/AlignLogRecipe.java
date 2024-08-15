package org.processmining.recipes.statechart.align;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.align.AlignLog2Tree;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.models.statechart.align.XAlignedTreeLog;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.recipes.statechart.AbstractRecipe;
import org.processmining.utils.statechart.signals.Signal3;

public class AlignLogRecipe extends AbstractRecipe<Pair<XLog, IEPTree>, XAlignedTreeLog, AlignLogRecipe.Parameters> {

    public static class Parameters {
//        public XEventClassifier classifier; // event activity label
        public IQueryCancelError queryCatchError;
        
        public Parameters() {
            // Note: we align using hierarchy lifecycles
            // that means a star and end of a subtree is modeled via
            // explicit start and complete transitions
            // hence the And(name, lifecycle) classifier.
//            classifier = new XEventAndClassifier(
//                new XEventNameClassifier(), new XEventLifeTransClassifier());
        }
    }
    
    // min, max, value
    public final Signal3<Integer, Integer, Integer> ProgressUpdate = new Signal3<>();
    
    public AlignLogRecipe() {
        super(new Parameters());
    }

    @Override
    protected XAlignedTreeLog execute(Pair<XLog, IEPTree> input) {
        final Parameters params = getParameters();
        
        AlignLog2Tree aligner = new AlignLog2Tree();
        aligner.ProgressUpdate.connect(ProgressUpdate);
        aligner.setQueryCatchError(params.queryCatchError);
//        aligner.setClassifier(params.classifier);
        
        return aligner.apply(input);
    }
}
