package org.processmining.algorithms.statechart.discovery.im.fallthrough;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier.Transition;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;

/**
 * 
 * @author mleemans
 *
 *         Fixed variant of FallThroughTauLoop. Uses an IMLogHierarchical for
 *         sublog construction, instead of filling an XLog and yielding a
 *         XLogImpl object.
 */
public class FallThroughTauLoopHierarchical implements FallThrough {

    private final boolean useLifeCycle;

    /**
     * 
     * @param useLifeCycle
     *            Denotes whether activity instances (i.e. combination of start
     *            & a complete event) should be kept together at all times. True
     *            = keep activity instances together; false = activity instances
     *            may be split.
     */
    public FallThroughTauLoopHierarchical(boolean useLifeCycle) {
        this.useLifeCycle = useLifeCycle;
    }

    public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree,
            MinerState minerState) {

        IMLogHierarchy hlog = (IMLogHierarchy) log;
        
        if (logInfo.getActivities().toSet().size() > 1) {
            // try to find a tau loop
            IMLogHierarchy sublog = (IMLogHierarchy) hlog.clone();

            for (IMTrace trace : sublog) {
                splitTrace(sublog, trace, logInfo.getDfg(), useLifeCycle);
            }
            
            if (sublog.size() > log.size()) {
                Miner.debug(" fall through: tau loop", minerState);
                // making a tau loop split makes sense
                Block loop = new XorLoop("");
                Miner.addNode(tree, loop);

                {
                    Node body = Miner.mineNode(sublog, tree, minerState);
                    Miner.addChild(loop, body, minerState);
                }

                {
                    Node redo = new Automatic("tau");
                    Miner.addNode(tree, redo);
                    Miner.addChild(loop, redo, minerState);
                }

                {
                    Node exit = new Automatic("tau");
                    Miner.addNode(tree, exit);
                    Miner.addChild(loop, exit, minerState);
                }

                return loop;
            }
        }

        return null;
    }

    private void splitTrace(IMLogHierarchy sublog, IMTrace trace, Dfg dfg, boolean useLifeCycle) {
        boolean first = true;

        MultiSet<XEventClass> openActivityInstances = new MultiSet<>();

        IMEventIterator it = trace.iterator();
        while (it.hasNext()) {
            XEvent event = it.next();
            XEventClass activity = sublog.classify(trace, event);

            if (!first  && dfg.isStartActivity(activity)) {
                // we discovered a transition body -> body
                // check whether there are no open activity instances
                if (!useLifeCycle || openActivityInstances.size() == 0) {
                    it.split();
                    first = true;
                }
            }

            if (useLifeCycle) {
                if (sublog.getLifeCycle(event) == Transition.complete) {
                    if (openActivityInstances.getCardinalityOf(activity) > 0) {
                        openActivityInstances.remove(activity, 1);
                    }
                } else if (sublog.getLifeCycle(event) == Transition.start) {
                    openActivityInstances.add(sublog.classify(trace, event));
                }
            }

            first = false;
        }
    }
    
    /* old
    public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree,
            MinerState minerState) {

        IMLogHierarchical hlog = (IMLogHierarchical) log;
        
        if (logInfo.getActivities().toSet().size() > 1) {

            // try to find a tau loop
            XFactory f = XFactoryRegistry.instance().currentDefault();
            IMLogHierarchical sublog = new IMLogHierarchical(f.createLog(), 0);
            // XLog sublog = new XLogImpl(new XAttributeMapImpl());

            for (IMTrace trace : log) {
                filterTrace(hlog, sublog, trace, logInfo.getDfg(),
                        useLifeCycle);
            }

            if (sublog.size() > log.size()) {
                Miner.debug(" fall through: tau loop", minerState);
                // making a tau loop split makes sense
                Block loop = new XorLoop("");
                Miner.addNode(tree, loop);

                {
                    Node body = Miner.mineNode(sublog, tree, minerState);
                    Miner.addChild(loop, body, minerState);
                }

                {
                    Node redo = new Automatic("tau");
                    Miner.addNode(tree, redo);
                    Miner.addChild(loop, redo, minerState);
                }

                {
                    Node exit = new Automatic("tau");
                    Miner.addNode(tree, exit);
                    Miner.addChild(loop, exit, minerState);
                }

                return loop;
            }
        }

        return null;
    }

    public static void filterTrace(IMLogHierarchical hlog, IMLogHierarchical sublog,
            IMTrace trace, Dfg dfg,
            boolean useLifeCycle) {
        boolean first = true;
        XTrace partialTrace = new XTraceImpl(new XAttributeMapImpl());

        MultiSet<XEventClass> openActivityInstances = new MultiSet<>();

        for (XEvent event : trace) {

            XEventClass activity = hlog.classify(trace, event);

            if (!first  && dfg.isStartActivity(activity)) {
                // we discovered a transition body -> body
                // check whether there are no open activity instances
                if (!useLifeCycle || openActivityInstances.size() == 0) {
                    sublog.addTrace(partialTrace, trace);//, hlog.getBaseTrace(trace));
                    partialTrace = new XTraceImpl(new XAttributeMapImpl());
                    first = true;
                }
            }

            if (useLifeCycle) {
                if (hlog.getLifeCycle(event) == Transition.complete) {
                    if (openActivityInstances.getCardinalityOf(activity) > 0) {
                        openActivityInstances.remove(activity, 1);
                    }
                } else if (hlog.getLifeCycle(event) == Transition.start) {
                    openActivityInstances.add(hlog.classify(trace, event));
                }
            }

            partialTrace.add(event);
            first = false;
        }
        sublog.addTrace(partialTrace, trace);//, hlog.getBaseTrace(trace));
    }
    */
}
