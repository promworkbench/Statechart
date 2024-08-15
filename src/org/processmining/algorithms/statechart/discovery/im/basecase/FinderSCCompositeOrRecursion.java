package org.processmining.algorithms.statechart.discovery.im.basecase;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.algorithms.statechart.discovery.ContextPath;
import org.processmining.algorithms.statechart.discovery.im.MiningParametersSCRecursion;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.models.statechart.processtree.SCCompositeOr;
import org.processmining.models.statechart.processtree.SCRecurrentOr;
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

public class FinderSCCompositeOrRecursion extends AbstractFinderSCCompositeOr {

    @Override
    public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree,
            MinerState minerState) {
        Preconditions
            .checkArgument(log instanceof IMLogHierarchy,
                "Error in FinderSCCompositeOrRecursion: illegal IMLog instance");
        IMLogHierarchy hlog = (IMLogHierarchy) log;

        MiningParametersSCRecursion stateParams = getStateParams(minerState);
        ContextPath currentContextPath = stateParams.getCurrentContext();
        
        XEventClass label = logInfo.getActivities().iterator().next();
        String contextLabel = label.toString();

        boolean isLoop = false;
        Node newNode = null;
        
        // Check recursion
        if (logInfo.getActivities().setSize() == 1 && currentContextPath.isInContext(contextLabel)) {
            // In current context, mark as recursive node

            // check for loop at this level
            isLoop = _detectLoop(hlog);
            
            // In current context, mark as recursive node
            newNode = new SCRecurrentOr(contextLabel);
            Miner.addNode(tree, newNode);

            // Set the level projection one level deeper
            IMLogHierarchy sublog = hlog;
            if (isLoop) {
                sublog = (IMLogHierarchy) hlog.clone();
                _splitLogLoops(sublog);
            }
            sublog = sublog.deriveLowerlevel();
            
            // Record sublog for next discovery iteration
            //IMLogHierarchical sublog = hlog.deriveLowerlevel();
            //stateParams.recordForDiscovery(currentContextPath, sublog, null);
            ContextPath subContextPath = currentContextPath.getSubcontext(contextLabel);
            stateParams.recordForDiscovery(subContextPath, sublog, null);
            
        } 
        // Check if this is a composite (base) case
        else if (isCompositeCase(hlog, logInfo, minerState)) {
            // check for loop at this level
            isLoop = _detectLoop(hlog);
            
            // Not in current context:
            // Create the composite state
            Block newBlock = new SCCompositeOr(contextLabel);
            newNode = newBlock;
            Miner.addNode(tree, newNode);

            // Set the level projection one level deeper
            IMLogHierarchy sublog = hlog;
            if (isLoop) {
                sublog = (IMLogHierarchy) hlog.clone();
                _splitLogLoops(sublog);
            }
            sublog = sublog.deriveLowerlevel();
            
            // Record sublog for next discovery iteration
            //IMLogHierarchical sublog = hlog.deriveLowerlevel();
            ContextPath newContextPath = currentContextPath.deriveContext(contextLabel);
            stateParams.recordForDiscovery(newContextPath, sublog, newBlock);
        }

        // Create Xor loop in case of loops
        if (newNode != null && isLoop) {
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
        
        return newNode;
    }
    
    /*
    @Override
    public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree,
            MinerState minerState) {
        Preconditions
                .checkArgument(log instanceof IMLogHierarchy,
                        "Error in FinderSCCompositeOrRecursion: illegal IMLog instance");
        IMLogHierarchy hlog = (IMLogHierarchy) log;

        // Check if this is a composite (base) case
        if (isCompositeCase(hlog, logInfo, minerState)) {
            MiningParametersSCRecursion stateParams = getStateParams(minerState);
            ContextPath currentContextPath = stateParams.getCurrentContext();
            
            XEventClass label = logInfo.getActivities().iterator().next();
            String contextLabel = label.toString();
            Node newNode;
            
            // check for loop at this level
            boolean isLoop = _detectLoop(hlog);
            
            if (currentContextPath.isInContext(contextLabel)) {
                // In current context, mark as recursive node
                newNode = new SCRecurrentOr(contextLabel);
                Miner.addNode(tree, newNode);

                // Set the level projection one level deeper
                IMLogHierarchy sublog = hlog;
                if (isLoop) {
                    sublog = (IMLogHierarchy) hlog.clone();
                    _splitLogLoops(sublog);
                }
                sublog = sublog.deriveLowerlevel();
                
                // Record sublog for next discovery iteration
                //IMLogHierarchical sublog = hlog.deriveLowerlevel();
                //stateParams.recordForDiscovery(currentContextPath, sublog, null);
                ContextPath subContextPath = currentContextPath.getSubcontext(contextLabel);
                stateParams.recordForDiscovery(subContextPath, sublog, null);

            } else {
                // Not in current context:
                // Create the composite state
                Block newBlock = new SCCompositeOr(contextLabel);
                newNode = newBlock;
                Miner.addNode(tree, newNode);

                // Set the level projection one level deeper
                IMLogHierarchy sublog = hlog;
                if (isLoop) {
                    sublog = (IMLogHierarchy) hlog.clone();
                    _splitLogLoops(sublog);
                }
                sublog = sublog.deriveLowerlevel();
                
                // Record sublog for next discovery iteration
                //IMLogHierarchical sublog = hlog.deriveLowerlevel();
                ContextPath newContextPath = currentContextPath.deriveContext(contextLabel);
                stateParams.recordForDiscovery(newContextPath, sublog, newBlock);
            }


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
        } else {
            // Empty recursion
            // TODO fully figure this out
            
            MiningParametersSCRecursion stateParams = getStateParams(minerState);
            ContextPath currentContextPath = stateParams.getCurrentContext();
            
            XEventClass label = logInfo.getActivities().iterator().next();
            String contextLabel = label.toString();
            Node newNode;
            
            // check for loop at this level
            boolean isLoop = _detectLoop(hlog);
            
            if (currentContextPath.isInContext(contextLabel)) {
                // In current context, mark as recursive node
                newNode = new SCRecurrentOr(contextLabel);
                Miner.addNode(tree, newNode);

                // Set the level projection one level deeper
                IMLogHierarchy sublog = hlog;
                if (isLoop) {
                    sublog = (IMLogHierarchy) hlog.clone();
                    _splitLogLoops(sublog);
                }
                sublog = sublog.deriveLowerlevel();
                
                // Record sublog for next discovery iteration
                //IMLogHierarchical sublog = hlog.deriveLowerlevel();
                //stateParams.recordForDiscovery(currentContextPath, sublog, null);
                ContextPath subContextPath = currentContextPath.getSubcontext(contextLabel);
                stateParams.recordForDiscovery(subContextPath, sublog, null);

                return newNode;
            }
        }

        // No base case match
        return null;
    }
     */
}
