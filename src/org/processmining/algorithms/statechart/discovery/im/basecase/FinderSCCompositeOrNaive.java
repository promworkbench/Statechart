package org.processmining.algorithms.statechart.discovery.im.basecase;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.models.statechart.processtree.SCCompositeOr;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

import com.google.common.base.Preconditions;

public class FinderSCCompositeOrNaive extends AbstractFinderSCCompositeOr {
    
    @Override
    public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree,
            MinerState minerState) {
        Preconditions
                .checkArgument(log instanceof IMLogHierarchy,
                        "Error in FinderSCCompositeOrNaive: illegal IMLog instance");
        IMLogHierarchy hlog = (IMLogHierarchy) log;

        // Check if this is a composite (base) case
        if (isCompositeCase(hlog, logInfo, minerState)) {
            // check for loop at this level
            boolean isLoop = _detectLoop(hlog);
            
            // Create the composite state
            XEventClass label = logInfo.getActivities().iterator().next();
            Block newNode = new SCCompositeOr(label.toString());
            Miner.addNode(tree, newNode);
            
            // Set the level projection one level deeper
            IMLogHierarchy sublog = hlog;
            if (isLoop) {
                sublog = (IMLogHierarchy) hlog.clone();
                _splitLogLoops(sublog);
            }
            sublog = sublog.deriveLowerlevel();

            // Mine the sub model (recurse)
            Node child = Miner.mineNode(sublog, tree, minerState);
            Miner.addChild(newNode, child, minerState);

            // Create Xor loop in case of loops
            if (isLoop) {
                Block loopNode = new AbstractBlock.XorLoop("");
                Miner.addNode(tree, loopNode);

                //body: activity
                loopNode.addChild(newNode);

                //redo: tau
                Node redo = new AbstractTask.Automatic("tau");
                Miner.addNode(tree, redo);
                loopNode.addChild(redo);

                //exit: tau
                Node exit = new AbstractTask.Automatic("tau");
                Miner.addNode(tree, exit);
                loopNode.addChild(exit);
                
                // set the returned node to the loop node
                newNode = loopNode;
            }
            
            // Return the new composite state
            return newNode;
        }

        // No base case match
        return null;
    }

}
