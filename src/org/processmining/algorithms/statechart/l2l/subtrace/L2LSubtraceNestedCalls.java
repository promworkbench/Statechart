package org.processmining.algorithms.statechart.l2l.subtrace;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.List;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.algorithms.statechart.l2l.HandleActivityUtil;
import org.processmining.algorithms.statechart.l2l.L2LNestedCalls;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class L2LSubtraceNestedCalls extends L2LNestedCalls {

    protected static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();
    protected static final XConceptExtension extConceptname = XConceptExtension.instance();
    protected static final XLifecycleExtension extLifecycle = XLifecycleExtension.instance();
    
    public L2LSubtraceNestedCalls() {
        this(new Parameters());
    }

    public L2LSubtraceNestedCalls(Parameters params) {
        super(params);
    }

    /**
     * Transform log like [ <a_s, b_s, c_s, c_c, b_c, d_s, d_c, e_s, e_c> ]
     * where _s, _c refer to start complete (e.g. via lifecycle info) into log
     * like [ < a.b.c, a.d, e > ] where a.b.c is an event with collection <a, b,
     * c> as label based on list-label extension and classifier
     * 
     * @param input
     * @return
     */
    @Override
    public XLog transform(XLog input) {
        Preconditions.checkNotNull(input);

        Preconditions.checkNotNull(params.clsLabel, "No label classifier set");
        Preconditions.checkNotNull(params.clsSR,
                "No start-return classifier set");
        Preconditions.checkNotNull(params.startSymbol, "No start symbol set");
        Preconditions.checkNotNull(params.returnSymbol, "No return symbol set");

        params.startSymbol = params.startSymbol.toLowerCase();
        params.returnSymbol = params.returnSymbol.toLowerCase();
        if (params.handleSymbol.isPresent()) {
            params.handleSymbol = Optional.of(params.handleSymbol.get().toLowerCase());
        }
        
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
    
    @Override
    protected XTrace _transformTrace(XFactory f, XTrace input) {
        XTrace result = f.createTrace();
        
        LogFactory.copy(input, result, f);

        // setup data structures for heuristic
        eventIndex = new TObjectIntHashMap<>();
        intervalStartEvent = new TIntObjectHashMap<>();
        intervalSubtrace = new TIntObjectHashMap<>();
        intervalParent = new TIntIntHashMap();
        openIntervals = new TIntArrayList();
        openLabels = new TIntObjectHashMap<>();
        
        // add root level to trackers at index 0
        intervalStartEvent.put(ROOT_INDEX, null);
        intervalSubtrace.put(ROOT_INDEX, result);
        intervalParent.put(ROOT_INDEX, ROOT_INDEX);
        openIntervals.add(ROOT_INDEX);
        openLabels.put(ROOT_INDEX, null);
        currentInterval = ROOT_INDEX;
        
        // process input
        for (XEvent event : input) {
            String eventLabel = params.clsLabel.getClassIdentity(event);
            String eventSR = params.clsSR.getClassIdentity(event).toLowerCase();
            
            if (params.handleSymbol.isPresent() && eventSR.equals(params.handleSymbol.get())) {
                // a handle exception happens in the middle of an interval
                // and should provide "1 extra level" with a special symbol annotation
                String handleLabel = HandleActivityUtil.createHandleActivity(eventLabel);
                completeInterval(f, event, handleLabel, false);
            } else if (eventSR.equals(params.startSymbol)) {
                // interval start
                startInterval(f, event, eventLabel);
                
            } else if (eventSR.equals(params.returnSymbol)) {
                // interval end
                completeInterval(f, event, eventLabel, false);
                
            }
        }
        
        // process unclosed intervals
        for (String openLabel : openLabels.values(new String[openLabels.size()])) {
            if (openLabel != null) { // avoid closing the root
                completeInterval(f, null, openLabel, true);
            }
        }
        
        // cleanup
        eventIndex = null;
        intervalStartEvent = null;
        intervalSubtrace = null;
        intervalParent = null;
        openIntervals = null;
        openLabels = null;
        
        // return result
        return result;
    }

    private static final int ROOT_INDEX = 0;
    private TObjectIntMap<XEvent> eventIndex;
    private TIntObjectMap<XEvent> intervalStartEvent;
    private TIntObjectMap<XTrace> intervalSubtrace;
    private TIntIntMap intervalParent;
    private TIntList openIntervals;
    private TIntObjectMap<String> openLabels;
    private int currentInterval;
    
    private int startInterval(XFactory f, XEvent inputEvent, String eventLabel) {
        currentInterval++;
        int iSelf = currentInterval;
        int iParent = openIntervals.get(openIntervals.size() - 1);
        
        XEvent eStart;
        if (inputEvent != null) {
            eStart = LogFactory.clone(inputEvent, f);
        } else {
            eStart = f.createEvent();
        }
        extConceptname.assignName(eStart, eventLabel);
        extLifecycle.assignStandardTransition(eStart, StandardModel.START);
        eventIndex.put(eStart, eventIndex.size());
        // delay add to trace, see completeInterval()
        
        intervalStartEvent.put(iSelf, eStart);
        // delay subtrace creation, see completeInterval()
        intervalParent.put(iSelf, iParent);
        openIntervals.add(iSelf);
        openLabels.put(iSelf, eventLabel);
        
        return iSelf;
    }
    
    private void completeInterval(XFactory f, XEvent inputEvent, String eventLabel, boolean addToClosedInterval) {
        // create close event 
        XEvent eComplete;
        if (inputEvent != null) {
            eComplete = LogFactory.clone(inputEvent, f);
        } else {
            eComplete = f.createEvent();
        }
        extConceptname.assignName(eComplete, eventLabel);
        extLifecycle.assignStandardTransition(eComplete, StandardModel.COMPLETE);
        int indexComplete = eventIndex.size();
        eventIndex.put(eComplete, eventIndex.size());
        
        // find current iself index based on open labels
        int iSelf = -1;
        for (int i = openIntervals.size() - 1; i >= 0 && !eventLabel.equals(openLabels.get(iSelf)); i--) {
            iSelf = openIntervals.get(i);
        }
        if (iSelf < 0 || !eventLabel.equals(openLabels.get(iSelf))) {
            // Tried to close an undefined interval.
            // Gracefully create missing start event for incomplete interval
            iSelf = startInterval(f, null, eventLabel);
        }
        if (iSelf == ROOT_INDEX) {
            throw new IllegalStateException("Tried to close root interval");
        }
        
        // close interval and retrieve start event and parent index
        openIntervals.remove(iSelf);
        openLabels.remove(iSelf);
        int iParent = intervalParent.get(iSelf);
        XEvent eStart = intervalStartEvent.get(iSelf);
        int indexStart = eventIndex.get(eStart);
        
        // check parent is open -> add to first open parent
        while(!addToClosedInterval 
            && !openLabels.containsKey(iParent) && iParent != ROOT_INDEX) {
            iParent = intervalParent.get(iParent);
        }
        
        // add to chosen parent subtrace
        XTrace subtrace = intervalSubtrace.get(iParent);
        if (subtrace == null) {
            subtrace = f.createTrace();
            extSubtrace.assignSubtrace(intervalStartEvent.get(iParent), subtrace);
            intervalSubtrace.put(iParent, subtrace);
        }
        
        // add start and complete at the right place in the subtrace
        for (int i = subtrace.size() - 1; i >= 0 && (eComplete != null || eStart != null); i--) {
            if (eComplete != null && eventIndex.get(subtrace.get(i)) < indexComplete) {
                subtrace.add(i + 1, eComplete);
                eComplete = null; // mark done
                i++;
            } else if (eStart != null && eventIndex.get(subtrace.get(i)) < indexStart) {
                subtrace.add(i + 1, eStart);
                eStart = null; // mark done
            }
        }
        if (eStart != null) {
            subtrace.add(eStart);
        }
        if (eComplete != null) {
            subtrace.add(eComplete);
        }
    }
    
}
