package org.processmining.algorithms.statechart.l2l.subtrace;

import gnu.trove.map.hash.THashMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.algorithms.statechart.l2l.ISingleEventDriver;
import org.processmining.algorithms.statechart.l2l.ISingleEventImplementation;
import org.processmining.algorithms.statechart.l2l.ISingleEventImplementation.Lifecycle;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.utils.statechart.tree.impl.ObjectTreeNode;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

import com.google.common.base.Preconditions;

public class L2LSubtraceSingleEventDriver implements ISingleEventDriver {

    protected static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();
    protected static final XConceptExtension extConceptname = XConceptExtension.instance();
    protected static final XLifecycleExtension extLifecycle = XLifecycleExtension.instance();

    private ISingleEventImplementation implementation;
    
    @Override
    public void setImplement(ISingleEventImplementation implementation) {
        this.implementation = implementation;
    }
    
    @Override
    public XLog apply(XLog input) {
        return transform(input);
    }

    @Override
    public XTrace apply(XTrace input) {
        XFactory f = LogFactory.getFactory();
        return _transformTrace(f, input);
    }

    public XLog transform(XLog input) {
        Preconditions.checkNotNull(implementation);
        Preconditions.checkNotNull(input);
        implementation.checkInput();

        XFactory f = LogFactory.getFactory();
        XLog log = f.createLog();
        log.getExtensions().add(extSubtrace);
        log.getExtensions().add(XConceptExtension.instance());
        log.getClassifiers().add(new XEventAndClassifier(
            new XEventNameClassifier(), new XEventLifeTransClassifier()));
        log.getClassifiers().add(new XEventNameClassifier());
        log.getClassifiers().add(new XEventLifeTransClassifier());

        List<XAttribute> ltAttr = log.getGlobalTraceAttributes();
        for (XAttribute attr : input.getGlobalTraceAttributes()) {
            ltAttr.add(LogFactory.clone(attr, f));
        }
        List<XAttribute> leAttr = log.getGlobalEventAttributes();
        for (XAttribute attr : input.getGlobalEventAttributes()) {
            leAttr.add(LogFactory.clone(attr, f));
        }
        
        for (XTrace trace : input) {
            log.add(_transformTrace(f, trace));
        }

        return log;
    }

    private Map<ObjectTreeNode<String>, XTrace> prefix2trace;
    private Map<ObjectTreeNode<String>, XEvent> prefix2startEvent;
    private Map<ObjectTreeNode<String>, XEvent> prefix2completeEvent;
    private ObjectTreeNode<String> prefixRoot;
    
    private XTrace _transformTrace(XFactory f, XTrace input) {
        XTrace result = f.createTrace();

        XAttributeMap attrMap = result.getAttributes();
        XAttributeMap inputTraceAttrMap = input.getAttributes();
        for (String attrKey : inputTraceAttrMap.keySet()) {
            attrMap.put(attrKey, LogFactory.clone(inputTraceAttrMap.get(attrKey), f));
        }
        
        // setup data structures for heuristic
        prefix2trace = new THashMap<>();
        prefix2startEvent = new THashMap<>();
        prefix2completeEvent = new THashMap<>();

        // setup root
        prefixRoot = new ObjectTreeNode<>(null);
        prefix2trace.put(prefixRoot, result);

        // process input
        for (XEvent event : input) {
            String[] labelParts = implementation.getEventLabelParts(event);
            Lifecycle eventSC = implementation.getEventStartComplete(event);
            
            if (eventSC == Lifecycle.Start) {
                // interval start
                startInterval(f, event, labelParts);
            } else if (eventSC == Lifecycle.Complete) {
                // interval end
                completeInterval(f, event, labelParts);
            }
        }
        
        // process unclosed intervals
        for (ObjectTreeNode<String> node : prefixRoot.iteratePreOrder()) {
            if (node.getObject() != null && !prefix2completeEvent.containsKey(node)) {
                XEvent start = prefix2startEvent.get(node);
                if (start != null) {
                    createCompleteEvent(f, start, node.getObject(), node.getParent(), node);
                }
            }
        }
        
        // return result
        return result;
    }

    private void startInterval(XFactory f, XEvent inputEvent, String[] labelParts) {
        ObjectTreeNode<String> parent = prefixRoot;
        for (int i = 0; i < labelParts.length; i++) {
            String labelpart = labelParts[i];
            
            ObjectTreeNode<String> match = null;
            for (ObjectTreeNode<String> child : parent.getChildren()) {
                if (labelpart.equals(child.getObject())) {
                    // no need to create a new start event for this level
                    match = child;
                    break;
                }
            }
            
            if (match != null) {
                // match -> check closed interval
                if (i < labelParts.length - 1) {
                    // if at the lowest level: don't close, we're looping
                    uncloseInterval(match);
                } else {
                    // at the lowest level, ensure previous match was closed, 
                    // and start anew for looping behavior
                    if (!prefix2completeEvent.containsKey(match)) {
                        XEvent start = prefix2startEvent.get(match);
                        if (start != null) {
                            createCompleteEvent(f, start, labelpart, parent, match);
                        }
                    }
                    match = createStartEvent(f, inputEvent, labelpart, parent);
                }
                
                // advance
                parent = match;
            } else {
                // no match -> create new start event for this level
                ObjectTreeNode<String> newPrefix = 
                    createStartEvent(f, inputEvent, labelpart, parent);
                
                // advance
                parent = newPrefix;
            }
        }
    }

    private void uncloseInterval(ObjectTreeNode<String> match) {
        // check data
        ObjectTreeNode<String> parent = match.getParent();
        XTrace subtrace = prefix2trace.get(parent);
        XEvent complete = prefix2completeEvent.get(match);
        
        // unclose this interval, remove the complete event
        if (subtrace != null && complete != null) {
            // do custom backwards search -> the event is probably
            // at the end of the subtrace, hence this is a faster search
            boolean removed = false;
            for (int j = subtrace.size() - 1; j >= 0 && !removed; j--) {
                if (subtrace.get(j) == complete) {
                    subtrace.remove(j);
                    prefix2completeEvent.remove(match);
                    removed = true;
                }
            }
        }
    }

    private ObjectTreeNode<String> createStartEvent(XFactory f, XEvent inputEvent,
            String labelpart, ObjectTreeNode<String> parent) {
        // create new start event for this level
        XEvent eStart;
        if (inputEvent != null) {
            eStart = LogFactory.clone(inputEvent, f);
        } else {
            eStart = f.createEvent();
        }
        extConceptname.assignName(eStart, labelpart);
        extLifecycle.assignStandardTransition(eStart, StandardModel.START);

        // add to parent subtrace
        addToTrace(f, eStart, parent);
        
        // prune tree for closed siblings
        Iterator<ObjectTreeNode<String>> it = parent.getChildren().iterator();
        while (it.hasNext()) {
            ObjectTreeNode<String> child = it.next();
            if (prefix2completeEvent.containsKey(child)) {
                it.remove();
            }
        }
        
        // register in prefix tree
        ObjectTreeNode<String> newPrefix = new ObjectTreeNode<>(parent);
        newPrefix.setObject(labelpart);
        parent.getChildren().add(newPrefix);
        
        prefix2startEvent.put(newPrefix, eStart);
        
        return newPrefix;
    }

    private void addToTrace(XFactory f, XEvent event, ObjectTreeNode<String> parent) {
        XTrace subtrace = prefix2trace.get(parent);
        if (subtrace == null) {
            subtrace = f.createTrace();
            extSubtrace.assignSubtrace(prefix2startEvent.get(parent), subtrace);
            prefix2trace.put(parent, subtrace);
        }
        subtrace.add(event);
    }

    private void completeInterval(XFactory f, XEvent inputEvent, String[] labelParts) {
        ObjectTreeNode<String> parent = prefixRoot;
        for (int i = 0; i < labelParts.length; i++) {
            String labelpart = labelParts[i];
            
            ObjectTreeNode<String> match = null;
            for (ObjectTreeNode<String> child : parent.getChildren()) {
                if (labelpart.equals(child.getObject())) {
                    // no need to create a new start event for this level
                    match = child;
                    break;
                }
            }

            if (match == null) {
                // no match -> open interval first
                match = createStartEvent(f, inputEvent, labelpart, parent);
            } else {
                // match -> perhaps unclose interval if it was closed too soon
                uncloseInterval(match);
            }
            
            // match guaranteed -> close interval
            createCompleteEvent(f, inputEvent, labelpart, parent, match);
            
            // advance
            parent = match;
        }
    }

    private void createCompleteEvent(XFactory f, XEvent inputEvent,
            String labelpart, ObjectTreeNode<String> parent, ObjectTreeNode<String> match) {
        // create new complete event for this level
        XEvent eComplete;
        if (inputEvent != null) {
            eComplete = LogFactory.clone(inputEvent, f);
        } else {
            eComplete = f.createEvent();
        }
        extConceptname.assignName(eComplete, labelpart);
        extLifecycle.assignStandardTransition(eComplete, StandardModel.COMPLETE);

        // add to parent subtrace
        addToTrace(f, eComplete, parent);
        
        // register for tree
        prefix2completeEvent.put(match, eComplete);
    }
        
    /*
    private Deque<XTrace> prefixTrace;
    private XTrace currentTrace;
    private List<XEvent> prefixSymbol;
    private XEvent symbol;
    private List<XEvent> prefixInputSymbol;
    private XEvent inputSymbol;
    
    private XTrace _transformTrace(XFactory f, XTrace input) {
        XTrace result = f.createTrace();

        XAttributeMap attrMap = result.getAttributes();
        XAttributeMap inputTraceAttrMap = input.getAttributes();
        for (String attrKey : inputTraceAttrMap.keySet()) {
            attrMap.put(attrKey, LogFactory.clone(inputTraceAttrMap.get(attrKey), f));
        }
        
        List<String> currentContext = new ArrayList<>();
        prefixTrace = new ArrayDeque<>();
        currentTrace = result;
        prefixSymbol = new ArrayList<>();
        symbol = null;
        prefixInputSymbol = new ArrayList<>();
        inputSymbol = null;
        
        for (XEvent event : input) {
            // determine label
            String[] labelParts = implementation.getEventLabelParts(event);

            // find level where labels differ
            int startFrom = 0;
            while(startFrom < currentContext.size()
                && startFrom < labelParts.length) {
                String labelOld = currentContext.get(startFrom);
                String labelNew = labelParts[startFrom];
                if (!labelOld.equals(labelNew)) {
                    break; // found point where old and new differ
                }
                startFrom++;
            }
            
            // for part with same labels, adjust closing input events
            // Note: this automatically handles the lifecycle start-complete case
            for (int i = 0; i < startFrom; i++) {
                if (i < prefixInputSymbol.size()) {
                    prefixInputSymbol.set(i, event);
                } else {
                    inputSymbol = event;
                }
            }
            
            // close current context deeper than startFrom
            for (int i = currentContext.size() - 1; i >= startFrom; i--) {
                _closeEvent(f, currentContext.remove(i));
            }

            // open new context at startFrom
            for (int i = startFrom; i < labelParts.length; i++) {
                currentContext.add(labelParts[i]);
                _addEventStart(f, event, labelParts[i]);
            }
        }
        
        // close open context
        for (int i = currentContext.size() - 1; i >= 0; i--) {
            _closeEvent(f, currentContext.remove(i));
        }
        
        return result;
    }

    private void _addEventStart(XFactory f, XEvent inputEvent, String eventLabel) {
        if (currentTrace == null) {
            currentTrace = f.createTrace();
            extSubtrace.assignSubtrace(symbol, currentTrace);
        }
        
        XEvent event;
        if (inputEvent != null) {
            event = LogFactory.clone(inputEvent, f);
        } else {
            event = f.createEvent();
        }
        extConceptname.assignName(event, eventLabel);
        extLifecycle.assignStandardTransition(event, StandardModel.START);
        
        currentTrace.add(event);
        prefixTrace.add(currentTrace);
        currentTrace = null;
        
        if (inputSymbol != null) {
            prefixInputSymbol.add(inputSymbol);
            prefixSymbol.add(symbol);
        }
        inputSymbol = inputEvent;
        symbol = event;
    }
    
    private void _closeEvent(XFactory f, String eventLabel) {
        XEvent inputEvent = inputSymbol; 
        
        XEvent event;
        if (inputEvent != null) {
            event = LogFactory.clone(inputEvent, f);
        } else {
            event = f.createEvent();
        }

        extConceptname.assignName(event, eventLabel);
        extLifecycle.assignStandardTransition(event, StandardModel.COMPLETE);

        if (!prefixTrace.isEmpty()) {
            currentTrace = prefixTrace.removeLast();
        }
        currentTrace.add(event);

        if (prefixInputSymbol.isEmpty()) {
            inputSymbol = null;
            symbol = null;
        } else {
            inputSymbol = prefixInputSymbol.remove(prefixInputSymbol.size() - 1);
            symbol = prefixSymbol.remove(prefixSymbol.size() - 1);
        }
    }*/
    
}

