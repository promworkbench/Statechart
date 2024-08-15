package org.processmining.algorithms.statechart.discovery.im.fallthrough;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter.LogSplitResult;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

/**
 * 
 * @author mleemans
 *
 *      Fixed variant of FallThroughFlowerWithoutEpsilon
 *      org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughFlowerWithoutEpsilon
 */
public class FallThroughFlowerWithoutEpsilonFixed implements FallThrough {

    public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {
            
            if (logInfo.getDfg().getNumberOfEmptyTraces() != 0) {
                    return null;
            }
            
            Miner.debug(" fall through: flower model", minerState);

            Block loopNode = new AbstractBlock.XorLoop("");
            Miner.addNode(tree, loopNode);

            //body: xor/activity
            Block xorNode;
            if (logInfo.getActivities().setSize() == 1) {
                    xorNode = loopNode;
            } else {
                    xorNode = new AbstractBlock.Xor("");
                    Miner.addNode(tree, xorNode);
                    Miner.addChild(loopNode, xorNode, minerState);
            }

            Collection<XEventClass> activities = logInfo.getActivities().sortByCardinality();
            for (XEventClass activity : logInfo.getActivities()) {
//                    Node child = new AbstractTask.Manual(activity.toString());
                    // Fix: use mine -> enable base cases like hierarchy or cancelation
                    Node child = Miner.mineNode(
                            _projectSublog(log, logInfo, tree, minerState, activity, activities), 
                            tree, minerState);
                    Miner.addNode(tree, child);
                    Miner.addChild(xorNode, child, minerState);
            }

            //redo: tau
            Node body = new AbstractTask.Automatic("tau");
            Miner.addNode(tree, body);
            Miner.addChild(loopNode, body, minerState);

            //exit: tau
            Node tau2 = new AbstractTask.Automatic("tau");
            Miner.addNode(tree, tau2);
            Miner.addChild(loopNode, tau2, minerState);

            return loopNode;
    }
    
    private IMLog _projectSublog(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState, 
            XEventClass activity, Collection<XEventClass> activities) {
        Set<XEventClass> sigma0 = new THashSet<>();
        sigma0.add(activity);
        Set<XEventClass> sigma1 = new THashSet<>(activities);
        sigma1.remove(activity);
        List<Set<XEventClass>> partition = new ArrayList<Set<XEventClass>>();
        partition.add(sigma0);
        partition.add(sigma1);
        Cut cut = new Cut(Operator.loop, partition);
        
        //split log
        LogSplitResult logSplitResult = minerState.parameters.getLogSplitter()
            .split(log, logInfo, cut, minerState);
        if (minerState.isCancelled()) {
                return null;
        }
        // remove empty traces
        IMLog newLog = logSplitResult.sublogs.get(0);
        Iterator<IMTrace> it = newLog.iterator();
        while (it.hasNext()) {
            IMTrace trace = it.next();
            if (trace.isEmpty()) {
                it.remove();
            }
        }
        return logSplitResult.sublogs.get(0);
    }
}
