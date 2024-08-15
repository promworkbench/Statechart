package org.processmining.algorithms.statechart.discovery.im.postprocessor;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.statechart.decorate.staticmetric.processtree.PropertyAbsoluteFrequency;
import org.processmining.models.statechart.decorate.staticmetric.processtree.PropertyCaseFrequency;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.models.statechart.processtree.ILoopCancel;
import org.processmining.models.statechart.processtree.ISCCompositeOr;
import org.processmining.models.statechart.processtree.ISCRecurrentOr;
import org.processmining.models.statechart.processtree.ISeqCancel;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier.Transition;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;
import org.processmining.processtree.Block;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Node;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.Task.Manual;

public class MetricsDecoratorPostprocessor implements PostProcessor {

    private Set<Node> _visitedNodes = new THashSet<>();

    private final boolean useLifeCycle;
    
    /**
     * 
     * @param useLifeCycle
     *            Denotes whether activity instances (i.e. combination of start
     *            & a complete event) should be kept together at all times. True
     *            = keep activity instances together; false = activity instances
     *            may be split.
     */
    public MetricsDecoratorPostprocessor(boolean useLifeCycle) {
        this.useLifeCycle = useLifeCycle;
    }
    
    @Override
    public Node postProcess(Node node, IMLog log, IMLogInfo logInfo,
            MinerState minerState) {
        if (node != null) {
            _annotate(node, log, logInfo);
        }

        return node;
    }

    private void _annotate(Node node, IMLog log, IMLogInfo logInfo) {
        try {
            IMLogHierarchy hlog = (IMLogHierarchy) log;

            // Administration update, node and property may have been cloned
            if (!_visitedNodes.contains(node) && PropertyAbsoluteFrequency.hasValue(node)) {
                _visitedNodes.add(node);
            }
            
            if (!_visitedNodes.contains(node)) {
                _visitedNodes.add(node);
                int absoluteFrequency = 0;
                TIntSet caseFrequency = new TIntHashSet();
                //Set<XTrace> caseFrequency = new THashSet<>();

                if (node instanceof Block) {
                    // some base cases yield subtrees instead of leaves (e.g.,
                    // semi-flower models) Since the children in these subtrees
                    // don't get a separate postprocess step, we need to visit
                    // them manually.
                    List<Node> children = ((Block) node).getChildren();
                    if (node instanceof XorLoop
                            && children.get(0) instanceof Automatic) {
                        _deriveSilentLoopDo(children.get(0), children.get(1),
                                hlog, logInfo);
                    } else if (node instanceof XorLoop
                            && children.get(1) instanceof Automatic) {
                        _deriveSilentLoopRedo(children.get(1), children.get(0),
                                hlog, logInfo);
                    }

                    int[] absFrequencies = new int[children.size()];
                    int i = 0;
                    for (Node child : children) {
                        _annotate(child, log, logInfo);
                        absFrequencies[i] = PropertyAbsoluteFrequency.getValue(child);
                        i++;
                    }
                    absoluteFrequency = _calcAbsFreqForBlock(node, absFrequencies, hlog);
                    
                    if (log != null) {
                        for (IMTrace trace : log) {
                            //caseFrequency.add(log.getTraceWithIndex(trace.getXTraceIndex()));
                            //caseFrequency.add(trace.getXTraceIndex());
                            caseFrequency.add(hlog.getTraceCaseId(trace));
                        }
                    }
                    
                } else if (node instanceof ISCRecurrentOr) {
                    // We assume that the consecutive events are one instance
                    for (IMTrace trace : log) {
                        boolean containsActivity = false;
                        for (XEvent event : trace) {
                            if (log.classify(trace, event).getId()
                                    .equals(node.getName())
                                && (!useLifeCycle || log.getLifeCycle(event) == Transition.start)) {
                                containsActivity = true;
                            }
                        }
                        if (containsActivity) {
                            absoluteFrequency++;
                            //caseFrequency.add(hlog.getBaseTrace(trace));
                            //caseFrequency.add(log.getTraceWithIndex(trace.getXTraceIndex()));
                            //caseFrequency.add(trace.getXTraceIndex());
                            caseFrequency.add(hlog.getTraceCaseId(trace));
                        }
                    }
                } else if (node instanceof Manual) {
                    // Activity could be semi-flow model, count activity
                    // occurrences
                    for (IMTrace trace : log) {
                        int activityCount = 0;
                        for (XEvent event : trace) {
                            if (log.classify(trace, event).getId()
                                    .equals(node.getName())
                                && (!useLifeCycle || log.getLifeCycle(event) == Transition.start)) {
                                activityCount++;
                            }
                        }
                        if (activityCount > 0) {
                            absoluteFrequency += activityCount;
                            //caseFrequency.add(hlog.getBaseTrace(trace));
                            //caseFrequency.add(log.getTraceWithIndex(trace.getXTraceIndex()));
                            //caseFrequency.add(trace.getXTraceIndex());
                            caseFrequency.add(hlog.getTraceCaseId(trace));
                        }
                    }
                } else if (node instanceof Automatic) {
                    // Tau is based on empty (epsilons) traces, so only count
                    // those
                    for (IMTrace trace : log) {
                        if (trace.isEmpty()) {
                            absoluteFrequency++;
                            //caseFrequency.add(hlog.getBaseTrace(trace));
                            //caseFrequency.add(log.getTraceWithIndex(trace.getXTraceIndex()));
                            //caseFrequency.add(trace.getXTraceIndex());
                            caseFrequency.add(hlog.getTraceCaseId(trace));
                        }
                    }
                }

                PropertyAbsoluteFrequency.setValue(node, absoluteFrequency);

                if (log != null) {
                    PropertyCaseFrequency.setValue(node, caseFrequency.size());
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        }
    }

    private int _calcAbsFreqForBlock(Node node, int[] absFrequencies,
            IMLogHierarchy log) throws IllegalAccessException,
            InstantiationException {
        if (node instanceof ISCCompositeOr) {
            if (((ISCCompositeOr) node).getChildren().isEmpty()) {
                // case of recursive discovery, count 'flat' occurrences for path up
                // Note: this node will be revisited for recursive call counts

                int absoluteFrequency = 0;
                if (log != null) {
                    // We assume that the consecutive events are one instance
                    for (IMTrace trace : log) {
                        boolean containsActivity = false;
                        for (XEvent event : trace) {
                            if (log.classify(trace, event).getId()
                                    .equals(node.getName())
                                && (!useLifeCycle || log.getLifeCycle(event) == Transition.start)) {
                                containsActivity = true;
                            }
                        }
                        if (containsActivity) {
                            absoluteFrequency++;
                        }
                    }

                    return absoluteFrequency;
                }
                return absoluteFrequency;

            } else {
                // case of normal discovery, count based on children
                int val = 0;
                for (int freq : absFrequencies) {
                    val = Math.max(val, freq);
                }
                return val;
            }
        }
        if (node instanceof Seq || node instanceof And) {
            int val = 0;
            for (int freq : absFrequencies) {
                val = Math.max(val, freq);
            }
            return val;
        }
        if (node instanceof Xor) {
            int val = 0;
            for (int freq : absFrequencies) {
                val += freq;
            }
            return val;
        }
        if ((node instanceof XorLoop 
            || node instanceof ILoopCancel) && absFrequencies.length >= 2) {
            return absFrequencies[0] - absFrequencies[1];
        }
        if ((node instanceof ISeqCancel) && absFrequencies.length >= 1) {
            return absFrequencies[0];
        }
        
        throw new IllegalStateException("Unexpected node type: " + node);
    }

    private void _deriveSilentLoopRedo(Node redo, Node body, IMLogHierarchy log,
            IMLogInfo logInfo) throws IllegalAccessException, InstantiationException {
        // TODO if there are two traces, each with the same list of events, we detect them as one case (same hash)
        
        if (!_visitedNodes.contains(redo)) {
            _visitedNodes.add(redo);

            _annotate(body, log, logInfo);

            int valAbsFreq = 0;
            int valCaseFreq = 0;
            
            if (body instanceof Block) {
                // abs frequency derived from body
                int bodyAbsFreq = PropertyAbsoluteFrequency.getValue(body);
                int bodyCaseFreq = PropertyCaseFrequency.getValue(body);
                valAbsFreq = bodyAbsFreq - bodyCaseFreq;
                
                // case frequency based on those xtraces that are in multiple imtraces
//                Dfg dfg = logInfo.getDfg();
                Set<String> startEvents = new THashSet<>();
                _deriveStartEvents(body, startEvents);
                
//                Multiset<XTrace> track = HashMultiset.create();
                TIntIntMap track = new TIntIntHashMap();
                for (IMTrace imtrace : log) {
                    
                    MultiSet<XEventClass> openActivityInstances = new MultiSet<>();
                    boolean first = true;
                    for (XEvent event : imtrace) {
                        XEventClass activity = log.classify(imtrace, event);
                        
                        if (!first  && startEvents.contains(activity.getId())) { //dfg.isStartActivity(activity)) {
                            // we discovered a transition body -> body
                            // check whether there are no open activity instances
                            if (!useLifeCycle || openActivityInstances.size() == 0) {
//                                track.add(log.getTraceWithIndex(imtrace.getXTraceIndex()));
                                track.adjustOrPutValue(log.getTraceCaseId(imtrace), 1, 1);
                                first = true;
                            }
                        }

                        if (useLifeCycle) {
                            if (log.getLifeCycle(event) == Transition.complete) {
                                if (openActivityInstances.getCardinalityOf(activity) > 0) {
                                    openActivityInstances.remove(activity, 1);
                                }
                            } else if (log.getLifeCycle(event) == Transition.start) {
                                openActivityInstances.add(log.classify(imtrace, event));
                            }
                        }
                        
                        first = false;
                    }
                    
                    //track.add(log.getTraceWithIndex(imtrace.getXTraceIndex()));
                    track.adjustOrPutValue(log.getTraceCaseId(imtrace), 1, 1);
                }
                
//                for (XTrace xtrace : track.elementSet()) {
//                    if (track.count(xtrace) > 1) {
//                        valCaseFreq++;
//                    }
//                }
                for (int caseKey : track.keys()) {
                    if (track.get(caseKey) > 1) {
                        valCaseFreq++;
                    }
                }
            } else if (body instanceof Manual) {
                // Activity could be semi-flow model, count activity
                // occurrences
//                Multiset<XTrace> absoluteFrequency = HashMultiset.create();
                TIntIntMap absoluteFrequency = new TIntIntHashMap();
//                Set<XTrace> caseFrequency = new THashSet<>();
                TIntSet caseFrequency = new TIntHashSet();

                for (IMTrace trace : log) {
//                    XTrace xtrace = log.getTraceWithIndex(trace
//                            .getXTraceIndex()); // log.getBaseTrace(trace);
                    for (XEvent event : trace) {
                        if (log.classify(trace, event).getId()
                                .equals(body.getName())
                            && (!useLifeCycle || log.getLifeCycle(event) == Transition.start)) {
//                            absoluteFrequency.add(xtrace);
                            absoluteFrequency.adjustOrPutValue(log.getTraceCaseId(trace), 1, 1);
                        }
                    }
                    // -1 due to initial iteration without redo
//                    if (absoluteFrequency.count(xtrace) > 1) {
                    if (absoluteFrequency.get(log.getTraceCaseId(trace)) > 1) {
//                        caseFrequency.add(xtrace);
                        caseFrequency.add(log.getTraceCaseId(trace));
                    }
                }

//                for (XTrace xtrace : absoluteFrequency.elementSet()) {
                for (int caseKey : absoluteFrequency.keys()) {
                    // -1 due to initial iteration without redo
//                    int absCnt = absoluteFrequency.count(xtrace);
                    int absCnt = absoluteFrequency.get(caseKey);
                    valAbsFreq += Math.max(0, absCnt - 1);
                }
                valCaseFreq = caseFrequency.size();
          }

            PropertyAbsoluteFrequency.setValue(redo, valAbsFreq);
            PropertyCaseFrequency.setValue(redo, valCaseFreq);
            
//            Multiset<XTrace> absoluteFrequency = HashMultiset.create();
//            Set<XTrace> caseFrequency = new THashSet<>();
//
//            if (body instanceof Block) {
//                // Block is based on traces
//                for (IMTrace trace : log) {
//                    XTrace xtrace = log.getTraceWithIndex(trace.getXTraceIndex()); //log.getBaseTrace(trace);
//                    absoluteFrequency.add(xtrace);
//                    // -1 due to initial iteration without redo
//                    if (absoluteFrequency.count(xtrace) > 1) {
//                        caseFrequency.add(xtrace);
//                    }
//                }
//            } else if (body instanceof Manual) {
//                // Activity could be semi-flow model, count activity
//                // occurrences
//                for (IMTrace trace : log) {
//                    XTrace xtrace = log.getTraceWithIndex(trace.getXTraceIndex()); //log.getBaseTrace(trace);
//                    for (XEvent event : trace) {
//                        if (log.classify(trace, event).getId()
//                                .equals(body.getName())) {
//                            absoluteFrequency.add(xtrace);
//                        }
//                    }
//                    // -1 due to initial iteration without redo
//                    if (absoluteFrequency.count(xtrace) > 1) {
//                        caseFrequency.add(xtrace);
//                    }
//                }
//            }
//
//            int valAbsFreq = 0;
//            for (XTrace xtrace : absoluteFrequency.elementSet()) {
//                // -1 due to initial iteration without redo
//                int absCnt = absoluteFrequency.count(xtrace);
//                valAbsFreq += Math.max(0, absCnt - 1);
//            }
//
//            PropertyAbsoluteFrequency.setValue(redo, valAbsFreq);
//            PropertyCaseFrequency.setValue(redo, caseFrequency.size());
        }
    }

    private void _deriveStartEvents(Node node, Set<String> startEvents) {
        if (node instanceof Block) {
            List<Node> children = ((Block) node).getChildren();
            if (node instanceof Xor || node instanceof And) {
                for (Node child : children) {
                    _deriveStartEvents(child, startEvents);
                }
            } else if (!children.isEmpty()) {
                _deriveStartEvents(children.get(0), startEvents);
            }
        } else if (node instanceof Manual || node instanceof ISCRecurrentOr) {
            startEvents.add(node.getName());
        }
    }

    private void _deriveSilentLoopDo(Node body, Node redo, IMLogHierarchy log,
            IMLogInfo logInfo) throws IllegalAccessException, InstantiationException {
        if (!_visitedNodes.contains(body)) {
            _visitedNodes.add(body);

            _annotate(redo, log, logInfo);
            int valAbsFreq = PropertyAbsoluteFrequency.getValue(redo);
            int valCaseFreq = PropertyCaseFrequency.getValue(redo);
            valAbsFreq += valCaseFreq;
            
            PropertyAbsoluteFrequency.setValue(body, valAbsFreq);
            PropertyCaseFrequency.setValue(body, valCaseFreq);
        }
    }

    public void revisitPathTo(Block block) {
        try {
            List<Block> revisits = new ArrayList<Block>();
            Deque<Block> horizon = new ArrayDeque<Block>();
            horizon.push(block);

            while (!horizon.isEmpty()) {
                Block front = horizon.pop();
                revisits.add(front);
                _visitedNodes.remove(front);
                PropertyAbsoluteFrequency.unsetValue(front);

                for (Block parent : front.getParents()) {
                    horizon.push(parent);
                }
            }

            for (Block node : revisits) {
                _annotate(node, null, null);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        }
    }

    public void revisitBlock(Block block) {
        try {
            _visitedNodes.remove(block);
            PropertyAbsoluteFrequency.unsetValue(block);
            _annotate(block, null, null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        }
    }
}
