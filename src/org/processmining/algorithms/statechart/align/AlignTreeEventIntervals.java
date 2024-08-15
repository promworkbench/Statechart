package org.processmining.algorithms.statechart.align;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.impl.EfficientPetrinetSemanticsImpl;
import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.align.ExecIntervals;
import org.processmining.models.statechart.align.XAlignedTreeLog;
import org.processmining.models.statechart.decorate.align.AlignMappingTreeDecorator;
import org.processmining.models.statechart.decorate.align.ExecIntervalTreeDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.xesalignmentextension.XAlignmentExtension.MoveType;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignment;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

import com.google.common.base.Function;

public class AlignTreeEventIntervals implements Function<Pair<XAlignedTreeLog, IEPTree>, IEPTree> {
    
    private int concurrentThreads = Runtime.getRuntime().availableProcessors();

    public int getConcurrentThreads() {
        return concurrentThreads;
    }
    
    public void setConcurrentThreads(int concurrentThreads) {
        this.concurrentThreads = concurrentThreads;
    }
    
    @Override
    public IEPTree apply(Pair<XAlignedTreeLog, IEPTree> input) {
        annotate(input.getLeft(), input.getRight());
        return input.getRight();
    }

    public void annotate(final XAlignedTreeLog align, IEPTree tree) {
        final ExecIntervalTreeDecorator decIval = new ExecIntervalTreeDecorator();
        tree.getDecorations().registerDecorator(decIval);

        // Copy align mapping
        AlignMappingTreeDecorator decAlignMap = new AlignMappingTreeDecorator();
        tree.getDecorations().registerDecorator(decAlignMap);
        decAlignMap.extractFrom(align);
        
        // build base semantics model, for sharing Petri net data
        PetrinetDecorated petrinet = align.getPerinet();
        final EfficientPetrinetSemanticsImpl baseSemantics = 
            new EfficientPetrinetSemanticsImpl(petrinet, petrinet.getInitialMarking());
        
        // annotate for all traces in parallel
        ExecutorService pool = Executors.newFixedThreadPool(getConcurrentThreads());
        
        int traceIndex = 0;
        for (final XAlignment trace : align) {
            final int myTraceIndex = traceIndex;
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    annotateForTrace(myTraceIndex, trace, align, baseSemantics, decIval);
                }
                
            });
            traceIndex++;
        }

        pool.shutdown();
        try {
            pool.awaitTermination(30, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void annotateForTrace(int traceIndex, XAlignment trace,
            XAlignedTreeLog align, EfficientPetrinetSemanticsImpl baseSemantics, 
            ExecIntervalTreeDecorator decIval) {
        
        // replay semantics needed to detect when nodes are enabled
        // make a copy of the base semantics (efficiency)
        EfficientPetrinetSemanticsImpl semantics = new EfficientPetrinetSemanticsImpl(baseSemantics);
        Set<Transition> enabledTransitions = new THashSet<>();
        PetrinetDecorated net = align.getPerinet();
        
        Map<Transition, XAlignmentMove> enableObjectCause = new THashMap<>();
        Map<Transition, XAlignmentMove> enableCause = new THashMap<>();
        
        List<XAlignmentMove> logMovesPre = new ArrayList<>();
        ExecInterval lastIval = null;
        
        // visit all moves and build intervals for this trace
        for (XAlignmentMove move : trace) {
            // record log moves at the right place
            if (move.getType() == MoveType.LOG) {
                if (lastIval != null) {
                    lastIval.appendLogMovePost(move);
                } else {
                    logMovesPre.add(move);
                }
            }
            
            // handle replay semantics and determine enabled-events
            Transition tMove = align.getTransition(move);
            if (tMove != null) {
                semantics.directExecuteExecutableTransition(tMove);
                
                for (Transition t : net.getTransitions()) {
                    if (semantics.isEnabled(t)) {
                        if (!enabledTransitions.contains(t)) {
                            enabledTransitions.add(t);
                            
                            // figure out the enable object cause for a transition enable
                            {
                                XAlignmentMove moveCause = move;
                                while (moveCause != null && align.getNode(moveCause) == null) {
                                    Transition tCause = align.getTransition(moveCause);
                                    if (tCause != null) {
                                        moveCause = enableObjectCause.get(tCause);
                                    } else {
                                        moveCause = null;
                                    }
                                }
                                if (moveCause != null) {
                                    enableObjectCause.put(t, moveCause);
                                }
                            }
                            // figure out the enable visible move cause for a transition enable
                            {
                                XAlignmentMove moveCause = move;
                                while (moveCause != null && !moveCause.isObservable()) {
                                    Transition tCause = align.getTransition(moveCause);
                                    if (tCause != null) {
                                        moveCause = enableCause.get(tCause);
                                    } else {
                                        moveCause = null;
                                    }
                                }
                                if (moveCause != null) {
                                    enableCause.put(t, moveCause);
                                }
                            }
                        }
                    } else {
                        enabledTransitions.remove(t);
                    }
                }
            }
            
            // handle actual node event intervals
            IEPTreeNode treeNode = align.getNode(move);
            if (treeNode != null) {
                // we have a move for a specific node --> add to intervals
                ExecIntervals intervals;
                synchronized (decIval) {
                    intervals = decIval.getDecoration(treeNode);
                    if (intervals == null) {
                        intervals = new ExecIntervals();
                        decIval.setDecoration(treeNode, intervals);
                    }
                }
                
                // add move on the right interval
                ExecInterval ival;
                if (!move.isObservable()) {
                    // a modeled tau move
                    ival = new ExecInterval(); 
                    ival.setStart(move);
                    ival.setComplete(move);
                    
                    if (tMove != null) {
                        XAlignmentMove enablingMove = enableObjectCause.get(tMove);
                        if (enablingMove != null) {
                            ival.setCause(enablingMove);
                        } else {
                            ival.setCause(trace);
                        }
                        XAlignmentMove enablingObservableMove = enableCause.get(tMove);
                        if (enablingObservableMove != null) {
                            ival.setEnabled(enablingObservableMove);
                        }
                    }
                    
                    synchronized (intervals) {
                        intervals.addInterval(traceIndex, ival);
                    }
                } else if(isComplete(move)) {
                    // a complete move for an existing action
                    ival = intervals.getLastOpenInterval(traceIndex);
                    ival.setComplete(move);
                    
                    // what caused this complete?
                    if (tMove != null) {
                        XAlignmentMove enablingObservableMove = enableCause.get(tMove);
                        if (enablingObservableMove != null) {
                            ival.setCompleteEnabled(enablingObservableMove);
                        }
                    }
                } else {
                    // a start move for a new action
                    ival = new ExecInterval(); 
                    ival.setStart(move);
                    
                    // what caused this start?
                    if (tMove != null) {
                        XAlignmentMove enablingMove = enableObjectCause.get(tMove);
                        if (enablingMove != null) {
                            ival.setCause(enablingMove);
                        } else {
                            ival.setCause(trace);
                        }
                        XAlignmentMove enablingObservableMove = enableCause.get(tMove);
                        if (enablingObservableMove != null) {
                            ival.setEnabled(enablingObservableMove);
                        }
                    }

                    synchronized (intervals) {
                        intervals.addInterval(traceIndex, ival);
                    }
                }

                lastIval = ival;
                if (!logMovesPre.isEmpty()) {
                    for (XAlignmentMove log : logMovesPre) {
                        lastIval.appendLogMovePre(log);
                    }
                    logMovesPre.clear();
                }
            }
        }
    }
/*
    private void annotateForTrace_old(int traceIndex, XAlignment trace,
            XAlignedTreeLog align, ExecIntervalTreeDecorator dec) {
        
        // replay semantics needed to detect when nodes are enabled
        EfficientPetrinetSemantics semantics = 
            new EfficientPetrinetSemanticsImpl(align.getPerinet(), align.getInitialMarking());
        Set<Transition> enabledTransitions = new THashSet<>();
        Petrinet net = align.getPerinet();
        
        Map<Transition, XAlignmentMove> enableCause = new THashMap<>();
        
        // visit all moves and build intervals for this trace
        for (XAlignmentMove move : trace) {
            // handle replay semantics and determine enabled-events
            Transition tMove = align.getTransition(move);
            if (tMove != null) {
                semantics.directExecuteExecutableTransition(tMove);
                
                for (Transition t : net.getTransitions()) {
                    if (semantics.isEnabled(t)) {
                        if (!enabledTransitions.contains(t)) {
                            enabledTransitions.add(t);
                            
                            // lookup observable move cause for a transition enable
                            XAlignmentMove moveCause = move;
                            while (moveCause != null && !moveCause.isObservable()) {
                                Transition tCause = align.getTransition(moveCause);
                                if (tCause != null) {
                                    moveCause = enableCause.get(tCause);
                                } else {
                                    moveCause = null;
                                }
                            }
                            if (moveCause != null) {
                                enableCause.put(t, moveCause);
                            }
                        }
                    } else {
                        enabledTransitions.remove(t);
                    }
                }
            }
            
            // handle actual node event intervals
            if (move.isObservable()) {
                IEPTreeNode treeNode = align.getNode(move);
                if (treeNode != null) {
                    // we have a move for a specific node --> add to intervals
                    ExecIntervals intervals = dec.getDecoration(treeNode);
                    if (intervals == null) {
                        intervals = new ExecIntervals();
                        dec.setDecoration(treeNode, intervals);
                    }
                    
                    // add move on the right interval
                    if(isComplete(move)) {
                        ExecInterval ival = intervals.getLastInterval(traceIndex);
                        ival.setComplete(move);
                    } else {
                        ExecInterval ival = new ExecInterval(); 
                        ival.setStart(move);
                        
                        if (tMove != null) {
                            XAlignmentMove enablingObservableMove = enableCause.get(tMove);
                            if (enablingObservableMove != null) {
                                ival.setEnabled(enablingObservableMove); // :/
                            } else {
                                ival.setEnabledObject(trace); // :/
                            }
                        }
                        
                        intervals.addInterval(traceIndex, ival);
                    }
                }
            }
        }
    }
     */
    private boolean isComplete(XAlignmentMove move) {
        return move.getModelMove().toUpperCase().endsWith("COMPLETE");
    }
}
