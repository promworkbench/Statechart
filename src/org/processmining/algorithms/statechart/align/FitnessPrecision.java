package org.processmining.algorithms.statechart.align;

import java.util.Collections;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.dataawarereplayer.precision.DataAwarePrecisionPlugin;
import org.processmining.dataawarereplayer.precision.PrecisionConfig;
import org.processmining.dataawarereplayer.precision.PrecisionMeasureException;
import org.processmining.dataawarereplayer.precision.PrecisionResult;
import org.processmining.dataawarereplayer.precision.projection.ProcessProjectionException;
import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.balancedconformance.BalancedDataXAlignmentPlugin;
import org.processmining.plugins.balancedconformance.config.BalancedProcessorConfiguration;
import org.processmining.plugins.balancedconformance.controlflow.ControlFlowAlignmentException;
import org.processmining.plugins.balancedconformance.dataflow.exception.DataAlignmentException;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult.ProjectedMeasuresFailedException;
import org.processmining.xesalignmentextension.XAlignmentExtension;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignedLog;

public class FitnessPrecision {

    private final double fitness;
    private final double precision;

    public FitnessPrecision(AcceptingPetriNet net, XLog log, XEventClassifier classifier)
            throws ControlFlowAlignmentException, DataAlignmentException,
            PrecisionMeasureException, ProcessProjectionException {
        this(net.getNet(), net.getInitialMarking(), net.getFinalMarkings(), log, classifier);
    }

    public FitnessPrecision(ProjectedRecallPrecisionResult result) throws ProjectedMeasuresFailedException {
        this(result.getRecall(), result.getPrecision());
    }

    public FitnessPrecision(double fitness, double precision) {
        this.fitness = fitness;
        this.precision = precision;
    }

    public FitnessPrecision(PetrinetGraph net, Marking source, Marking sink,
            XLog log, XEventClassifier classifier)
            throws ControlFlowAlignmentException, DataAlignmentException,
            PrecisionMeasureException, ProcessProjectionException {
        this(net, source, new Marking[] { sink }, log, classifier);
    }
    
    public FitnessPrecision(PetrinetGraph net, Marking source, Set<Marking> sinks,
            XLog log, XEventClassifier classifier)
            throws ControlFlowAlignmentException, DataAlignmentException,
            PrecisionMeasureException, ProcessProjectionException {
        this(net, source, sinks.toArray(new Marking[sinks.size()]), log, classifier);
    }
    
    public FitnessPrecision(PetrinetGraph net, Marking source, Marking[] sink,
            XLog log, XEventClassifier classifier)
            throws ControlFlowAlignmentException, DataAlignmentException,
            PrecisionMeasureException, ProcessProjectionException {
         
         DataPetriNet dataNet = DataPetriNet.Factory.viewAsDataPetriNet(net);
        
          // Fitness
          BalancedProcessorConfiguration alignConfig = BalancedProcessorConfiguration
                  .newDefaultInstance(dataNet, source, sink, log, classifier, 1, 1,
                          0, 0);
          BalancedDataXAlignmentPlugin alignPlugin = new BalancedDataXAlignmentPlugin();
          XLog alignResult = alignPlugin.alignLog(dataNet, log, alignConfig);

          XAlignedLog alignLog = XAlignmentExtension.instance().extendLog(
                  alignResult);

          fitness = alignLog.getAverageFitness();

          // Precision
          PrecisionConfig precisionConfig = new PrecisionConfig(source,
                  DataAwarePrecisionPlugin.convertMapping(alignConfig
                          .getActivityMapping()), classifier,
                  Collections.<String, String> emptyMap());
          DataAwarePrecisionPlugin precisionPlugin = new DataAwarePrecisionPlugin();
          PrecisionResult precisionResult = precisionPlugin
                  .doMeasurePrecisionWithAlignment(dataNet, alignResult,
                          alignLog, precisionConfig);

          precision = precisionResult.getPrecision();

      }

    public double getFitness() {
        return fitness;
    }
    
    public double getPrecision() {
        return precision;
    }
}
