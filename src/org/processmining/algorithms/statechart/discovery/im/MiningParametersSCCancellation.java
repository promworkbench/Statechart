package org.processmining.algorithms.statechart.discovery.im;

import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;

public class MiningParametersSCCancellation 
//    extends MiningParametersIMi {
extends MiningParametersSC {

    public MiningParametersSCCancellation(boolean useLifecycle, IQueryCancelError queryCatchError) {
        super(useLifecycle, true, queryCatchError);
    }
    /*
    public MiningParametersSCCancellation(IQueryCancelError queryCatchError) {

        boolean useLifecycle = false;
        //setLogConverter(new IMLog2IMLogInfoLifeCycle());
        setLogConverter(new IMLog2IMLogInfoCancellation());
        
        setBaseCaseFinders(new ArrayList<BaseCaseFinder>(Arrays.asList(
            new BaseCaseFinderIMiEmptyLog(),
            new BaseCaseFinderIMiEmptyTrace(),
            new FinderSCCompositeOrNaive(),
            new FinderCancellation(queryCatchError),
            new BaseCaseFinderIMi(),
            new BaseCaseFinderIM()
        )));
        
        setCutFinder(new ArrayList<CutFinder>(Arrays.asList(
//            new CutFinderTrycatch(queryCatchError),
            

//            new CutFinderIMExclusiveChoice(),
//            new CutFinderIMSequence(), // TODO: include?
//            new CutFinderIMConcurrentWithMinimumSelfDistance(),
//            new CutFinderIMLoop(),
//            new CutFinderIMConcurrent()

            new CutFinderIMSequenceCancelAware(queryCatchError),
            new CutFinderIM(),
            new CutFinderFrequentAdapter(
                new CutFinderIMSequenceCancelAware(queryCatchError)
            ),
            new CutFinderIMf()
        )));
        
//        setLogSplitter(new LogSplitterCombinationExtended(
//            new LogSplitterTrycatch(),
//            new LogSplitterCombination(
//                new LogSplitterXor(), 
//                new LogSplitterSequenceFiltering(), 
//                new LogSplitterParallel(),
//                new LogSplitterLoop(),
//                new LogSplitterMaybeInterleaved(),
//                new LogSplitterParallel(),
//                new LogSplitterOr()
//            )
//        ));
        setLogSplitter(new LogSplitterCombination(
              new LogSplitterXor(), 
              new LogSplitterSequenceFilteringCancelAware(), 
              new LogSplitterParallelCancelAware(),
              new LogSplitterLoop(),
              new LogSplitterMaybeInterleaved(),
              new LogSplitterParallel(),
              new LogSplitterOr()
          ));
                
        /// TODO: setFallThroughs -- what is the actual difference here?
//        setFallThroughs(new ArrayList<FallThrough>(Arrays.asList(
//            new FallThroughActivityOncePerTraceConcurrent(false),
//            new FallThroughActivityConcurrent(),
//            new FallThroughTauLoopHierarchical(useLifecycle),
//            new FallThroughFlowerWithEpsilon()
//        )));
        setFallThroughs(new ArrayList<FallThrough>(Arrays.asList(
            new FallThroughIM()
        )));

        setPostProcessors(new ArrayList<PostProcessor>(Arrays.asList(
            new PostProcessorInterleaved()
//            new MetricsDecoratorPostprocessor(useLifecycle),
//            new SwAppDecoratorPostprocessor()
        )));
        
        //set parameters
        setNoiseThreshold((float) 0.2);
        //setReduce(true);
//        setDebug(true);
    }
    //*/
}
