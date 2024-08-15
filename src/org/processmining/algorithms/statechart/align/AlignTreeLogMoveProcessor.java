package org.processmining.algorithms.statechart.align;

import gnu.trove.map.hash.THashMap;

import java.util.List;
import java.util.Map;

import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.align.ExecIntervals;
import org.processmining.models.statechart.decorate.align.ExecIntervalTreeDecorator;
import org.processmining.models.statechart.eptree.EPNodeType;
import org.processmining.models.statechart.eptree.EPTreeNode;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.recipes.statechart.discovery.LogFilterRecipe;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

import com.google.common.base.Preconditions;

public class AlignTreeLogMoveProcessor {

    @SuppressWarnings("unused")
    public void process(IEPTree tree) {
        ExecIntervalTreeDecorator execIntervals = tree.getDecorations()
            .getForType(ExecIntervalTreeDecorator.class);
        
        for (IEPTreeNode node : tree.iteratePreOrder()) {
            ExecIntervals ivals = execIntervals.getDecoration(node);
            if (ivals != null) {
                ExecIntervals pre = extractLogIntervals(ivals, false);
                ExecIntervals post = extractLogIntervals(ivals, true);
                
                // install SEQ wrapper between node and parent
                IEPTreeNode wrapper = null;
                if (!pre.isEmpty() || !post.isEmpty()) {
                    IEPTreeNode parent = node.getParent();
                    List<IEPTreeNode> parentChildren = parent.getChildren();
                    int index = parentChildren.indexOf(node);
                    Preconditions.checkArgument(index != -1);
                    
                    wrapper = new EPTreeNode(tree, parent, EPNodeType.Seq);
                    parentChildren.set(index, wrapper);
                    
                    wrapper.getChildren().add(node);
                    node.setParent(wrapper);
                }
                
                // Add log nodes
                if (!pre.isEmpty()) {
                    if (LogFilterRecipe.AsyncDemoHack && pre.size() == 1) {
                        // test code:
                        ExecInterval ival = pre.getLastInterval(pre.getTraceIndices()[0]);

                        IEPTreeNode logXor = new EPTreeNode(tree, wrapper, EPNodeType.Choice);
                        logXor.getChildren().add(new EPTreeNode(tree, logXor, EPNodeType.Silent));
                        
                        IEPTreeNode log = new EPTreeNode(tree, logXor, EPNodeType.Action, ival.getComplete().getEventClassId());
                        logXor.getChildren().add(log);
                        
                        wrapper.getChildren().add(0, logXor);
                        execIntervals.setDecoration(log, pre);
                    } else {
                        IEPTreeNode logXor = new EPTreeNode(tree, wrapper, EPNodeType.Choice);
                        logXor.getChildren().add(new EPTreeNode(tree, logXor, EPNodeType.Silent));
                        
                        IEPTreeNode log = new EPTreeNode(tree, logXor, EPNodeType.Log);
                        logXor.getChildren().add(log);
                        
                        wrapper.getChildren().add(0, logXor);
                        execIntervals.setDecoration(log, pre);
                    }
                }
                if (!post.isEmpty()) {

                    if (LogFilterRecipe.AsyncDemoHack && post.size() == 1) {
                        ExecInterval ival = post.getLastInterval(post.getTraceIndices()[0]);

                        IEPTreeNode logXor = new EPTreeNode(tree, wrapper, EPNodeType.Choice);
                        logXor.getChildren().add(new EPTreeNode(tree, logXor, EPNodeType.Silent));

                        IEPTreeNode log = new EPTreeNode(tree, logXor, EPNodeType.Action, ival.getComplete().getEventClassId());
                        logXor.getChildren().add(log);
                        
                        wrapper.getChildren().add(logXor);
                        execIntervals.setDecoration(log, post);
                    } else {
                        IEPTreeNode logXor = new EPTreeNode(tree, wrapper, EPNodeType.Choice);
                        logXor.getChildren().add(new EPTreeNode(tree, logXor, EPNodeType.Silent));
                        
                        IEPTreeNode log = new EPTreeNode(tree, logXor, EPNodeType.Log);
                        logXor.getChildren().add(log);
                        
                        wrapper.getChildren().add(logXor);
                        execIntervals.setDecoration(log, post);
                    }
                }
            }
        }
    }

    private ExecIntervals extractLogIntervals(ExecIntervals ivals, boolean usePost) {
        ExecIntervals logIntervals = new ExecIntervals();
        
        for (int traceIndex : ivals.getTraceIndices()) {
            for (ExecInterval ival : ivals.getIntervalsForTrace(traceIndex)) {
                // get list of log moves
                List<XAlignmentMove> moves;
                XAlignmentMove trigger = null;
                if (!usePost) {
                    moves = ival.getLogMovesPre();
                } else {
                    moves = ival.getLogMovesPost();
                    trigger = ival.getComplete();
                }
                
                // If moves are present
                if (!moves.isEmpty()) {
                    // convert to execution intervals
                    
                    Map<String, ExecInterval> incompleteIvals = new THashMap<>();
                    for (XAlignmentMove move : moves) {
                        if(isComplete(move)) {
                            ExecInterval ivalLog = incompleteIvals.get(move.getActivityId());
                            if (ivalLog == null) {
                                ivalLog = new ExecInterval();
                                if (trigger != null) {
                                    ivalLog.setEnabled(trigger);
                                    ivalLog.setCause(trigger);
                                    trigger = null;
                                }
                                ivalLog.setStart(move);
                                logIntervals.addInterval(traceIndex, ivalLog);
                            }
                            ivalLog.setComplete(move);
                        } else {
                            ExecInterval ivalLog = new ExecInterval();
                            if (trigger != null) {
                                ivalLog.setEnabled(trigger);
                                ivalLog.setCause(trigger);
                                trigger = null;
                            }
                            ivalLog.setStart(move);
                            incompleteIvals.put(move.getActivityId(), ivalLog);
                            logIntervals.addInterval(traceIndex, ivalLog);
                        }
                    }
                }
            }
        }
        
        return logIntervals;
    }

    private boolean isComplete(XAlignmentMove move) {
        return move.getLogMove().toUpperCase().endsWith("COMPLETE");
    }

}
