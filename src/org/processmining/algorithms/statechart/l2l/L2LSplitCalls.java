package org.processmining.algorithms.statechart.l2l;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.xes.statechart.extension.XTraceType;
import org.processmining.xes.statechart.extension.XTraceType.TypeStandardModel;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class L2LSplitCalls implements Function<XLog, XLog> {

    public static class Parameters {
        public XEventClassifier clsLabel; // event activity label
        public XEventClassifier clsSR; // event Start-Return labeler
        public String startSymbol; // Start symbol to match
        public String returnSymbol; // End symbol to match
        
        public Pattern reTraceBaseName; // Base name for a trace (split around match)

        @SuppressWarnings("unchecked")
        public static final Pair<Pattern, String>[] ReSplitDefaults = new Pair[] {
            Pair.of(Pattern.compile(".*\\.test[^\\(]*\\(\\)"),
                    "Split on unit test methods"),
            Pair.of(Pattern.compile(".*\\.main\\(.*"),
                    "Split on main method")
        };
        
        public Parameters() {
            clsLabel = new XEventNameClassifier();
            clsSR = new XEventLifeTransClassifier();
            startSymbol = XLifecycleExtension.StandardModel.START.getEncoding();
            returnSymbol = XLifecycleExtension.StandardModel.COMPLETE.getEncoding();
            reTraceBaseName = ReSplitDefaults[0].getLeft();
        }
    }

    private final Parameters params;

    private static final XTraceType extTraceType = XTraceType.instance();
    private static final XConceptExtension extConcept = XConceptExtension.instance();
    
    public L2LSplitCalls() {
        this(new Parameters());
    }

    public L2LSplitCalls(Parameters params) {
        Preconditions.checkNotNull(params, "No parameters set");
        this.params = params;
    }

    @Override
    public XLog apply(XLog input) {
        return transform(input);
    }
    public List<XTrace> apply(XTrace input) {
        XFactory f = LogFactory.getFactory();
        return _transformTrace(f, input);
    }

    public XLog transform(XLog input) {
        Preconditions.checkNotNull(input);

        Preconditions.checkNotNull(params.clsLabel, "No label classifier set");
        Preconditions.checkNotNull(params.clsSR,
                "No start-return classifier set");
        Preconditions.checkNotNull(params.startSymbol, "No start symbol set");
        Preconditions.checkNotNull(params.returnSymbol, "No return symbol set");

        XFactory f = LogFactory.getFactory();
        XLog log = f.createLog();
        
        log.getExtensions().add(extTraceType);
        for (XExtension ext : input.getExtensions()) {
            log.getExtensions().add(ext);
        }
        log.getClassifiers().add(params.clsLabel);
        for (XEventClassifier cls : input.getClassifiers()) {
            log.getClassifiers().add(cls);
        }

        for (XTrace trace : input) {
            log.addAll(_transformTrace(f, trace));
        }

        return log;
    }


    private List<XTrace> _transformTrace(XFactory f, XTrace input) {
        List<XTrace> results = new ArrayList<>();
        XTrace currentTrace = null;
        
        for (XEvent event : input) {
            String eventLabel = params.clsLabel.getClassIdentity(event);
            String eventSR = params.clsSR.getClassIdentity(event);

            if (params.reTraceBaseName.matcher(eventLabel).matches()) {
                // boundary events for subtrace (boundary events not included)
                if (currentTrace != null) {
                    // end running trace
                    if (eventSR.equals(params.returnSymbol)) {
                        extTraceType.assignStandardTransition(
                                currentTrace, TypeStandardModel.NORMAL);
                    } else {
                        extTraceType.assignStandardTransition(
                                currentTrace, TypeStandardModel.INCOMPLETE);
                    }
                    results.add(currentTrace);
                    currentTrace = null;
                }
                
                if (eventSR.equals(params.startSymbol)) {
                    // start a new trace
                    currentTrace = f.createTrace();
                    extConcept.assignName(currentTrace, eventLabel);
                }
            } else if (currentTrace != null) {
                // copy events for current subtrace
                XEvent newEvent = f.createEvent();
                XAttributeMap newEventMap = newEvent.getAttributes();
                
                // copy basic attributes
                XAttributeMap eventMap = event.getAttributes();
                for (String key : eventMap.keySet()) {
                    newEventMap.put(key, eventMap.get(key));
                }
                
                currentTrace.add(newEvent);
            }
            
        }
        
        // finish any ongoing trace
        if (currentTrace != null) {
            extTraceType.assignStandardTransition(
                    currentTrace, TypeStandardModel.INCOMPLETE);
            results.add(currentTrace);
            currentTrace = null;
        }
        
        return results;
    }
}
