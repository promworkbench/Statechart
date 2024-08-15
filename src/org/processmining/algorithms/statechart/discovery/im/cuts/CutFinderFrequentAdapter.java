package org.processmining.algorithms.statechart.discovery.im.cuts;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMf.CutFinderIMf;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderFrequentAdapter implements CutFinder {

    private CutFinder baseFinder;

    public CutFinderFrequentAdapter(CutFinder baseFinder) {
        this.baseFinder = baseFinder;
    }
    
    public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
        //filter logInfo
        IMLogInfo logInfoFiltered = CutFinderIMf.filterNoise(logInfo, minerState.parameters.getNoiseThreshold());

        //call IM cut detection
        Cut cut = baseFinder.findCut(null, logInfoFiltered, minerState);

        return cut;
    }
}
