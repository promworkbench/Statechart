package org.processmining.algorithms.statechart.log.cancel;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.align.FitnessPrecision;
import org.processmining.algorithms.statechart.discovery.DiscoverEPTreeNaive;
import org.processmining.algorithms.statechart.discovery.im.cancellation.SetQueryCancelError;
import org.processmining.algorithms.statechart.m2m.EPTree2Ptree;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.log.HierarchyActivityInfo;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.ProcessTree2EfficientTree;
import org.processmining.processtree.ProcessTree;
import org.processmining.projectedrecallandprecision.framework.CompareLog2ProcessTree;
import org.processmining.projectedrecallandprecision.framework.CompareParameters;
import org.processmining.projectedrecallandprecision.helperclasses.AutomatonFailedException;
import org.processmining.projectedrecallandprecision.helperclasses.EfficientLog;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult.ProjectedMeasuresFailedException;

import com.google.common.base.Function;

public class DiscoverErrorOraclePrecision implements Function<Pair<XLog, HierarchyActivityInfo>, Set<String>> {

    private static final Logger logger = LogManager
            .getLogger(DiscoverErrorOraclePrecision.class.getName());

    private ProMCanceller mockupCanceller = new ProMCanceller() {
        @Override
        public boolean isCancelled() {
            return false;
        }
        
    };
    
    private double pathThreshold = 1.0; // percentage of paths to keep (0.0 - 1.0)
                                        // (inverse of noise threshold)

    @Override
    public Set<String> apply(Pair<XLog, HierarchyActivityInfo> input) {
        return apply(input.getLeft(), input.getRight(), mockupCanceller);
    }

    public Set<String> apply(XLog log, HierarchyActivityInfo info, ProMCanceller promCanceller) {
        FitnessPrecision baseScore = measure(log, Collections.<String>emptySet(), promCanceller);
        if (baseScore == null) {
            logger.error("Could not calculate base score");
        }
        
        // TODO Auto-generated method stub
        return null;
    }

    private FitnessPrecision measure(XLog log, Set<String> errorHypothesis, ProMCanceller promCanceller) {
        DiscoverEPTreeNaive.Parameters params = new DiscoverEPTreeNaive.Parameters();
        params.pathThreshold = pathThreshold;
        params.useCancelation = true;
        params.queryCatchError = new SetQueryCancelError(errorHypothesis); 
        
        DiscoverEPTreeNaive disc = new DiscoverEPTreeNaive(params);
        IEPTree tree = disc.discover(log);
        
        return measure(log, tree, promCanceller);
    }

    private FitnessPrecision measure(XLog log, IEPTree tree, ProMCanceller promCanceller) {
        EPTree2Ptree transform = new EPTree2Ptree(true);
        ProcessTree model = transform.transform(tree);

        XEventAttributeClassifier classifier = new XEventAndClassifier(
                new XEventNameClassifier(), new XEventLifeTransClassifier());
        EfficientTree efficientTree = ProcessTree2EfficientTree.convert(model);
        EfficientLog efficientLog = new EfficientLog(log, classifier);

        CompareParameters params = new CompareParameters(2);
        CompareLog2ProcessTree m = new CompareLog2ProcessTree();
        try {
            return new FitnessPrecision(m.apply(efficientTree, efficientLog, params, promCanceller));
        } catch (ProjectedMeasuresFailedException | AutomatonFailedException
                | InterruptedException e) {
            logger.error(e);
            return null;
        }
    }

}
