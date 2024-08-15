package org.processmining.algorithms.statechart.discovery.im.basecase;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.algorithms.statechart.discovery.im.cuts.CutFinderFrequentAdapter;
import org.processmining.algorithms.statechart.discovery.im.cuts.CutFinderLoopCancel;
import org.processmining.algorithms.statechart.discovery.im.cuts.CutFinderSeqCancel;
import org.processmining.algorithms.statechart.discovery.im.logsplitter.LogSplitterLoopCancel;
import org.processmining.algorithms.statechart.discovery.im.logsplitter.LogSplitterSeqCancel;
import org.processmining.models.statechart.decorate.error.IMErrorTriggerDecorator;
import org.processmining.models.statechart.decorate.error.PropertyErrorTrigger;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.models.statechart.processtree.ErrorTrigger;
import org.processmining.models.statechart.processtree.LoopCancel;
import org.processmining.models.statechart.processtree.SeqCancel;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter.LogSplitResult;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractTask;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class FinderCancellation implements BaseCaseFinder {

    private Function<String, Block>[] operators;    
    private CutFinder cutFinders[];
    private LogSplitter cutSplitters[];

    @SuppressWarnings("unchecked")
    public FinderCancellation(IQueryCancelError queryCatchError) {
        Function<String, Block> opSeqCancel = new Function<String, Block>() {
            @Override
            public Block apply(String arg) {
                return new SeqCancel(arg);
            }
        };
        Function<String, Block> opLoopCancel = new Function<String, Block>() {
            @Override
            public Block apply(String arg) {
                return new LoopCancel(arg);
            }
        };
        
        this.operators = (Function<String, Block>[]) new Function[] {
            opSeqCancel,
            opLoopCancel,
            opSeqCancel,
            opLoopCancel
        };
        this.cutFinders = new CutFinder[] {
            new CutFinderSeqCancel(queryCatchError),
            new CutFinderLoopCancel(queryCatchError),
            new CutFinderFrequentAdapter(new CutFinderSeqCancel(queryCatchError)),
            new CutFinderFrequentAdapter(new CutFinderLoopCancel(queryCatchError)),
        };
        this.cutSplitters = new LogSplitter[] {
            new LogSplitterSeqCancel(queryCatchError),
            new LogSplitterLoopCancel(queryCatchError),
            new LogSplitterSeqCancel(queryCatchError),
            new LogSplitterLoopCancel(queryCatchError),
        };
    }

    @Override
    public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree,
            MinerState minerState) {
        // check base case
        if (logInfo.getActivities().setSize() == 1) {
            if (_hasErrorTrigger(log)) {
                try {
                    return _baseCaseErrorTrigger(log, logInfo, tree, minerState);
                } catch (IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }

        // check cuts
        for (int k = 0; k< operators.length; k++) {
            Function<String, Block> opFactory = operators[k];
            CutFinder cutFinder = cutFinders[k];
            LogSplitter cutSplitter = cutSplitters[k];
            
            // check cut
            Cut cut = cutFinder.findCut(log, logInfo, minerState);

            if (cut != null && cut.isValid()) {
                LogSplitResult splitResult = cutSplitter.split(log, logInfo, cut, minerState);
                
                //Block newNode = new SeqCancel("");
                Block newNode = opFactory.apply("");
                Miner.addNode(tree, newNode);

                Node childBody = Miner.mineNode(splitResult.sublogs.get(0), tree, minerState);
                Miner.addChild(newNode, childBody, minerState);
                
                if (splitResult.sublogs.size() == 2) {
                    Node child = Miner.mineNode(splitResult.sublogs.get(1), tree, minerState);
                    Miner.addChild(newNode, child, minerState);
                } else {
                    Block errorXor = new Xor("");
                    Miner.addNode(tree, errorXor);
                    Miner.addChild(newNode, errorXor, minerState);
                    
                    for (int i = 1; i < splitResult.sublogs.size(); i++) {
                        Node child = Miner.mineNode(splitResult.sublogs.get(i), tree, minerState);
                        Miner.addChild(errorXor, child, minerState);
                    }
                }
                
                return newNode;
            }
        }
        
        // No base case match
        return null;
    }

    private Node _baseCaseErrorTrigger(IMLog log, IMLogInfo logInfo,
            ProcessTree tree, MinerState minerState) throws IllegalAccessException, InstantiationException {
        XEventClass activity = logInfo.getActivities().iterator().next();
        
        Set<String> errorsTriggered = _extractErrorsTriggered(log);
        
        if (logInfo.getDfg().getNumberOfEmptyTraces() == 0
                && !BasecaseUtil.detectSingleActLoop((IMLogHierarchy)log)) {
            // single activity
            ErrorTrigger newNode = new ErrorTrigger(activity.toString());
            PropertyErrorTrigger.setValue(newNode, errorsTriggered);
            Miner.addNode(tree, newNode);
            return newNode;
        } else if (logInfo.getDfg().getNumberOfEmptyTraces() == 0) {
            //single activity in semi-flower model
            Block loopNode = new AbstractBlock.XorLoop("");
            Miner.addNode(tree, loopNode);

            //body: activity
            Node body = new ErrorTrigger(activity.toString());
            PropertyErrorTrigger.setValue(body, errorsTriggered);
            Miner.addNode(tree, body);
            loopNode.addChild(body);

            //redo: tau
            Node redo = new AbstractTask.Automatic("tau");
            Miner.addNode(tree, redo);
            loopNode.addChild(redo);

            //exit: tau
            Node exit = new AbstractTask.Automatic("tau");
            Miner.addNode(tree, exit);
            loopNode.addChild(exit);

            return loopNode;
        } else {
            //single activity in semi-flower model
            Block loopNode = new AbstractBlock.XorLoop("");
            Miner.addNode(tree, loopNode);

            //body: tau
            Node body = new AbstractTask.Automatic("tau");
            Miner.addNode(tree, body);
            loopNode.addChild(body);

            //redo: activity
            Node redo = new ErrorTrigger(activity.toString());
            PropertyErrorTrigger.setValue(redo, errorsTriggered);
            Miner.addNode(tree, redo);
            loopNode.addChild(redo);

            //exit: tau
            Node exit = new AbstractTask.Automatic("tau");
            Miner.addNode(tree, exit);
            loopNode.addChild(exit);

            return loopNode;
        }
    }

    private Set<String> _extractErrorsTriggered(IMLog log) {
        Set<String> errors = new THashSet<String>();
        
        for (IMTrace trace : log) {
            Optional<IMErrorTriggerDecorator> optDecorator = IMErrorTriggerDecorator.getDecorator(trace);
            if (optDecorator.isPresent()) {
                IMErrorTriggerDecorator decorator = optDecorator.get();
                for (XEvent event : trace) {
                    if (decorator.hasDecoration(event)) {
                        errors.addAll(decorator.getDecoration(event));
                    }
                }
            }
        }
        
        return errors;
    }

    private boolean _hasErrorTrigger(IMLog log) {
        boolean hasErrorTrigger = false;
        for (IMTrace trace : log) {
            hasErrorTrigger = hasErrorTrigger || _hasErrorTrigger(trace);
        }
        return hasErrorTrigger;
    }

    private boolean _hasErrorTrigger(IMTrace trace) {
        Optional<IMErrorTriggerDecorator> optDecorator = IMErrorTriggerDecorator.getDecorator(trace);
        if (!trace.isEmpty()) {
            boolean hasAnyErrorTrigger = false;
            for (XEvent event : trace) {
                hasAnyErrorTrigger = hasAnyErrorTrigger 
                    || IMErrorTriggerDecorator.hasAnyErrorTrigger(optDecorator, event);
            }
            return hasAnyErrorTrigger;
        } else {
            return false;
        }
    }
}
