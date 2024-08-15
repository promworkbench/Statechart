package org.processmining.algorithms.statechart.discovery.im;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.algorithms.statechart.discovery.im.basecase.AbstractFinderSCCompositeOr;
import org.processmining.algorithms.statechart.discovery.im.basecase.FinderCancellation;
import org.processmining.algorithms.statechart.discovery.im.basecase.FinderSCCompositeOrNaive;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.algorithms.statechart.discovery.im.cuts.CutFinderFrequentAdapter;
import org.processmining.algorithms.statechart.discovery.im.cuts.CutFinderIMSequenceCancelAware;
import org.processmining.algorithms.statechart.discovery.im.fallthrough.FallThroughFlowerWithEpsilonFixed;
import org.processmining.algorithms.statechart.discovery.im.fallthrough.FallThroughFlowerWithoutEpsilonFixed;
import org.processmining.algorithms.statechart.discovery.im.fallthrough.FallThroughTauLoopHierarchical;
import org.processmining.algorithms.statechart.discovery.im.log2logInfo.IMLog2IMLogInfoCancellationAdaptor;
import org.processmining.algorithms.statechart.discovery.im.logsplitter.LogSplitterParallelCancelAware;
import org.processmining.algorithms.statechart.discovery.im.logsplitter.LogSplitterSequenceFilteringCancelAware;
import org.processmining.algorithms.statechart.discovery.im.postprocessor.MetricsDecoratorPostprocessor;
import org.processmining.algorithms.statechart.discovery.im.postprocessor.SwAppDecoratorPostprocessor;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoLifeCycle;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIM;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMi;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMiEmptyLog;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMiEmptyTrace;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMConcurrent;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMConcurrentWithMinimumSelfDistance;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMExclusiveChoice;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMLoop;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMSequence;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMa.CutFinderIMaInterleaved;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMf.CutFinderIMf;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMlc.CutFinderIMlcConcurrent;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughActivityConcurrent;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughActivityOncePerTraceConcurrent;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughTauLoopStrict;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterCombination;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterLoop;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterMaybeInterleaved;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterOr;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterParallel;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterSequenceFiltering;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterXor;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycleClassifier;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessorInterleaved;

import com.google.common.base.Preconditions;

public class MiningParametersSC 
//    extends MiningParametersIMi {
extends MiningParameters {

    public MiningParametersSC(boolean useLifecycle, boolean useCancelation, IQueryCancelError queryCatchError) {
        this(new FinderSCCompositeOrNaive(), useLifecycle, useCancelation, queryCatchError);
    }
    
    //*
    public MiningParametersSC(AbstractFinderSCCompositeOr hbasecase,
            boolean useLifecycle, boolean useCancelation, IQueryCancelError queryCatchError) {
        Preconditions.checkNotNull(hbasecase);
        Preconditions.checkArgument(!useCancelation || queryCatchError != null);
        
        // Basic behavior
        IMLog2IMLogInfo logConverter;
        if (useLifecycle) {
            logConverter = new IMLog2IMLogInfoLifeCycle();
//            setRepairLifeCycle(true);
            setLifeCycleClassifier(new LifeCycleClassifier());
        } else {
            logConverter = new IMLog2IMLogInfoDefault();
        }
        ArrayList<BaseCaseFinder> baseCases = new ArrayList<BaseCaseFinder>(Arrays.asList(
            new BaseCaseFinderIMiEmptyLog(),
            new BaseCaseFinderIMiEmptyTrace(),
            hbasecase,
            new BaseCaseFinderIMi(),
            new BaseCaseFinderIM()
        ));
        ArrayList<CutFinder> cutFinders;
        if (useLifecycle) {
            cutFinders = new ArrayList<CutFinder>(Arrays.asList(
//                  new CutFinderIM(),
                  // ALT START
                  new CutFinderIMExclusiveChoice(),
                  new CutFinderIMSequence(),
                  new CutFinderIMlcConcurrent(),
                  new CutFinderIMaInterleaved(),
                  new CutFinderIMConcurrentWithMinimumSelfDistance(),
                  new CutFinderIMLoop(),
                  new CutFinderIMConcurrent(),
                  // ALT END
                  new CutFinderIMf()
            ));
        } else {
            cutFinders = new ArrayList<CutFinder>(Arrays.asList(
                new CutFinderIM(), 
                new CutFinderIMf()
            ));
        }
        LogSplitterCombination logSplitters = new LogSplitterCombination(
            new LogSplitterXor(), 
            new LogSplitterSequenceFiltering(), 
            new LogSplitterParallel(),
            new LogSplitterLoop(),
            new LogSplitterMaybeInterleaved(),
            new LogSplitterParallel(),
            new LogSplitterOr()
        );
        
        // Cancelation modifications
        if (useCancelation) {
            logConverter = new IMLog2IMLogInfoCancellationAdaptor(logConverter);
            baseCases.add(baseCases.indexOf(hbasecase) + 1, new FinderCancellation(queryCatchError));
            
            cutFinders.add(0, new CutFinderIMSequenceCancelAware(queryCatchError));
            cutFinders.add(1, new CutFinderFrequentAdapter(
                    new CutFinderIMSequenceCancelAware(queryCatchError)));
            
            logSplitters = new LogSplitterCombination(
                new LogSplitterXor(), 
                new LogSplitterSequenceFilteringCancelAware(), 
                new LogSplitterParallelCancelAware(),
                new LogSplitterLoop(),
                new LogSplitterMaybeInterleaved(),
                new LogSplitterParallelCancelAware(),
                new LogSplitterOr()
            );
        }
        
        // Setup
        setLogConverter(logConverter);
        setBaseCaseFinders(baseCases);
        setCutFinder(cutFinders);
        setLogSplitter(logSplitters);
        setFallThroughs(new ArrayList<FallThrough>(Arrays.asList(
            new FallThroughActivityOncePerTraceConcurrent(useLifecycle),
            new FallThroughActivityConcurrent(),
            new FallThroughTauLoopStrict(useLifecycle),
            new FallThroughTauLoopHierarchical(useLifecycle),
            new FallThroughFlowerWithoutEpsilonFixed(),
            new FallThroughFlowerWithEpsilonFixed()
        )));
        setPostProcessors(new ArrayList<PostProcessor>(Arrays.asList(
            new PostProcessorInterleaved(),
            new MetricsDecoratorPostprocessor(useLifecycle),
            new SwAppDecoratorPostprocessor()
        )));
        
        //set parameters
        setNoiseThreshold((float) 0.2);
        //setReduce(true);
//        setDebug(true);
    }
    //*/
}
