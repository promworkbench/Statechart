package org.processmining.algorithms.statechart.align;

import java.util.Collections;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.algorithms.statechart.m2m.EPTree2Petrinet;
import org.processmining.algorithms.statechart.m2m.EPTree2Petrinet.CancelationMode;
import org.processmining.algorithms.statechart.m2m.EPTree2Petrinet.HierarchyMode;
import org.processmining.algorithms.statechart.m2m.EPTree2Petrinet.RecursionMode;
import org.processmining.dataawarereplayer.precision.DataAwarePrecisionPlugin;
import org.processmining.dataawarereplayer.precision.PrecisionConfig;
import org.processmining.dataawarereplayer.precision.PrecisionMeasureException;
import org.processmining.dataawarereplayer.precision.PrecisionResult;
import org.processmining.dataawarereplayer.precision.projection.ProcessProjectionException;
import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.framework.plugin.Progress;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.statechart.align.XAlignedTreeLog;
import org.processmining.models.statechart.align.XAlignedTreeLogImpl;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.plugins.balancedconformance.BalancedDataXAlignmentPlugin;
import org.processmining.plugins.balancedconformance.config.BalancedProcessorConfiguration;
import org.processmining.plugins.balancedconformance.controlflow.ControlFlowAlignmentException;
import org.processmining.plugins.balancedconformance.dataflow.exception.DataAlignmentException;
import org.processmining.utils.statechart.signals.Signal3;
import org.processmining.xesalignmentextension.XAlignmentExtension;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignedLog;

import com.google.common.base.Function;

public class AlignLog2Tree implements Function<Pair<XLog, IEPTree>, XAlignedTreeLog> {

    private static final Logger logger = LogManager.getLogger(AlignLog2Tree.class.getName());

    // min, max, value
    public final Signal3<Integer, Integer, Integer> ProgressUpdate = new Signal3<>();
    
    private class ProgressHandler implements Progress {

        private int min = 0;
        private int max = 0;
        private int inc = 0;
        private String caption = "";
        private boolean cancel = false;
        
        @Override
        public void setMinimum(int value) {
            min = value;
            ProgressUpdate.dispatch(min, max, inc);
            logger.debug(" -- Alignment progress -- min = " + Integer.toString(value));
        }

        @Override
        public void setMaximum(int value) {
            max = value;
            ProgressUpdate.dispatch(min, max, inc);
            logger.debug(" -- Alignment progress -- max = " + Integer.toString(value));
        }

        @Override
        public void setValue(int value) {
            inc = value;
            ProgressUpdate.dispatch(min, max, inc);
            logger.debug(" -- Alignment progress -- value = " + Integer.toString(value));
        }

        @Override
        public void setCaption(String message) {
            caption = message;
            logger.debug(" -- Alignment progress -- message = " + message);
        }

        @Override
        public String getCaption() {
            return caption;
        }

        @Override
        public int getValue() {
            return inc;
        }

        @Override
        public void inc() {
            inc++;
            ProgressUpdate.dispatch(min, max, inc);
            logger.debug(" -- Alignment progress -- increment -> " + Integer.toString(inc));
        }

        @Override
        public void setIndeterminate(boolean makeIndeterminate) {
            logger.debug(" -- Alignment progress -- setIndeterminate = " + Boolean.toString(makeIndeterminate));
        }

        @Override
        public boolean isIndeterminate() {
            return false;
        }

        @Override
        public int getMinimum() {
            // TODO Auto-generated method stub
            return min;
        }

        @Override
        public int getMaximum() {
            return max;
        }

        @Override
        public boolean isCancelled() {
            return cancel;
        }

        @Override
        public void cancel() {
            cancel = true;
        }
        
    };
    private ProgressHandler progressHandler = new ProgressHandler();
    
    protected IQueryCancelError queryCatchError = null;

    // Note: we align using hierarchy lifecycles
    // that means a star and end of a subtree is modeled via
    // explicit start and complete transitions
    // hence the And(name, lifecycle) classifier.
    protected XEventClassifier classifier = new XEventAndClassifier(
            new XEventNameClassifier(), new XEventLifeTransClassifier());
//    protected boolean lifecycleHierarchySupport = true;

    public void setQueryCatchError(IQueryCancelError queryCatchError) {
        this.queryCatchError = queryCatchError;
    }
    
//    public void setClassifier(XEventClassifier classifier) {
//        this.classifier = classifier;
//    }

//    public void setLifecycleHierarchySupport(boolean lifecycleHierarchySupport) {
//        this.lifecycleHierarchySupport = lifecycleHierarchySupport;
//        
//        if (!lifecycleHierarchySupport) {
//            classifier = new XEventNameClassifier();
//        } else {
//            classifier = new XEventAndClassifier(
//                new XEventNameClassifier(), new XEventLifeTransClassifier());
//        }
//    }
    
    @Override
    public XAlignedTreeLog apply(Pair<XLog, IEPTree> input) {
        return performAlignment(input.getLeft(), input.getRight());
    }

    public XAlignedTreeLog performAlignment(XLog log, IEPTree tree) {
        try {
            // Convert tree to Petri net
            EPTree2Petrinet tree2petrinet = new EPTree2Petrinet(
                HierarchyMode.LifecycleHierarchy,
                RecursionMode.IgnoreConstraint, // handled via cost function
                CancelationMode.MimicResetArcs);
            tree2petrinet.setQueryCatchError(queryCatchError);
    
            PetrinetDecorated petrinet = tree2petrinet.transform(tree);
            
            DataPetriNet dataNet = DataPetriNet.Factory.viewAsDataPetriNet(petrinet);
            Marking source = petrinet.getInitialMarking();
            Marking[] sink = petrinet.getFinalMarkingsAsArray();
            
            // Perform alignments
            BalancedProcessorConfiguration alignConfig = BalancedProcessorConfiguration
                .newDefaultInstance(dataNet, source, sink, log, classifier, 1, 1, 0, 0);
            BalancedDataXAlignmentPlugin alignPlugin = new BalancedDataXAlignmentPlugin();
            XLog alignResult = alignPlugin.alignLog(progressHandler, dataNet, log, alignConfig);

            XAlignedLog alignLog = XAlignmentExtension.instance().extendLog(alignResult);
            
            // Precision
            PrecisionConfig precisionConfig = new PrecisionConfig(source,
                DataAwarePrecisionPlugin.convertMapping(alignConfig.getActivityMapping()), 
                classifier, Collections.<String, String> emptyMap());
            DataAwarePrecisionPlugin precisionPlugin = new DataAwarePrecisionPlugin();
            PrecisionResult precisionResult = precisionPlugin
                    .doMeasurePrecisionWithAlignment(dataNet, alignResult, alignLog, precisionConfig);

//            System.out.println("Place | Precision | Observed | Possible |-| Precision L. | Observed L. | Possible L.");
//            for (Place p : petrinet.getPlaces()) {
//                System.out.println(String.format("%s \t| %.3f \t| %d \t| %d \t|-| %.3f \t| %d \t| %d",
//                    p.getLabel(),
//                    precisionResult.getPrecision(p),
//                    precisionResult.getObservedContinuations(p),
//                    precisionResult.getPossibleContinuations(p),
//                    precisionResult.getLocalPrecision(p),
//                    precisionResult.getObservedLocalContinuations(p),
//                    precisionResult.getPossibleLocalContinuations(p)
//                ));
//            }
            
            // Return interpreted results
            XAlignedTreeLogImpl result = new XAlignedTreeLogImpl(
                alignLog, precisionResult.getPrecision(),
                tree, petrinet);
            
            // final inc (original BalancedDataXAlignmentPlugin doesn't reach max)
            progressHandler.inc();
            
            return result;
            
        } catch(ControlFlowAlignmentException | DataAlignmentException | PrecisionMeasureException | ProcessProjectionException e) {
            logger.error("Could not produce alignments", e);
            return null;
        }
    }
}
