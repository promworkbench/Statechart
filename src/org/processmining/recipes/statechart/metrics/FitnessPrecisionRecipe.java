package org.processmining.recipes.statechart.metrics;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.align.FitnessPrecision;
import org.processmining.dataawarereplayer.precision.PrecisionMeasureException;
import org.processmining.dataawarereplayer.precision.projection.ProcessProjectionException;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.plugins.balancedconformance.controlflow.ControlFlowAlignmentException;
import org.processmining.plugins.balancedconformance.dataflow.exception.DataAlignmentException;
import org.processmining.recipes.statechart.AbstractRecipe;

public class FitnessPrecisionRecipe extends
AbstractRecipe<Pair<XLog, PetrinetDecorated>, FitnessPrecision, FitnessPrecisionRecipe.Parameters> {

    public static class Parameters {
        public XEventClassifier classifier = new XEventNameClassifier();
    }
    
    public FitnessPrecisionRecipe() {
        super(new Parameters());
    }

    @Override
    protected FitnessPrecision execute(Pair<XLog, PetrinetDecorated> input) {
        try {
            return new FitnessPrecision(input.getRight(), input.getLeft(), getParameters().classifier);
        } catch (ControlFlowAlignmentException | DataAlignmentException
                | PrecisionMeasureException | ProcessProjectionException e) {
            e.printStackTrace();
            return null;
        }
    }

}
