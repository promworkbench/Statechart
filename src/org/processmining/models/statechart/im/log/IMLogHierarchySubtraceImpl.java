package org.processmining.models.statechart.im.log;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecorator;
import org.processmining.models.statechart.decorate.log.SubtraceDecorator;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycleClassifier;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

public class IMLogHierarchySubtraceImpl
    extends IMLogImplReclassLinkedList<XEventClassifier> {
    
    protected static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();

    protected final TIntIntMap trace2case;
    
    public IMLogHierarchySubtraceImpl(XLog input) {
        this(input, new XEventNameClassifier(), new LifeCycleClassifier(),
            new TIntIntHashMap(
                Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR,
                NO_ELEMENT, NO_ELEMENT));
    }
    
    private IMLogHierarchySubtraceImpl(XLog xLog, XEventClassifier activityClassifier, 
            XLifeCycleClassifier lifeCycleClassifier, TIntIntMap trace2case) {
        super(xLog, activityClassifier, lifeCycleClassifier);
        this.trace2case = trace2case;
    }
    
    public IMLogHierarchySubtraceImpl(IMLogHierarchySubtraceImpl log) {
        super(log);
        trace2case = log.trace2case;
    }

    @Override
    public IMLog clone() {
        return new IMLogHierarchySubtraceImpl(this);
    }

    @Override
    public IMLog decoupleFromXLog() {
        // TODO: loses information about trace specific level classifiers
        XLog xLog = toXLog();
        return new IMLogHierarchySubtraceImpl(xLog, fallbackClassifier, lifeCycleClassifier, trace2case);
    }
    
    @Override
    public IMLogHierarchy deriveLowerlevel() {
        XFactory f = LogFactory.getFactory();
        XLog sublog = f.createLog();
        
        TIntIntMap trace2case = new TIntIntHashMap(
                Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR,
                NO_ELEMENT, NO_ELEMENT);

        List<Decorations<XEvent>> decorations = new ArrayList<>();
        
        _resolveAddPending();
        int current = 0;
        for (TraceRecord<XEventClassifier> record : this.traces) {
            IMTrace trace = record.getIMTrace(current, this);
            current++;
            Decorations<XEvent> decs = record.getEventDecorations();
            
            int traceId = record.getXTraceIndex();
            int caseId = this.trace2case.get(traceId);
            if (caseId == NO_ELEMENT) {
                caseId = traceId;
            }
            
            IMEventIterator it = trace.iterator();
            while (it.hasNext()) {
                XEvent event = it.next();
                XTrace subtrace = extSubtrace.extractSubtrace(event);
                if (subtrace != null) {
                    // add referenced subtrace
                    XEvent eventS = event;
                    XEvent eventC = event;
                    if (it.hasNext()) {
                        eventC = it.next();
                    }
                    trace2case.put(sublog.size(), caseId);
                    sublog.add(subtrace);
                    decorations.add(_smartCloneDecorations(decs, eventS, eventC, subtrace));
                } else {
                    // add empty trace to represent absence of a lower level
                    sublog.add(f.createTrace());
                    decorations.add(null);
                }
            }
        }

        IMLogHierarchySubtraceImpl derived = new IMLogHierarchySubtraceImpl(sublog, 
                getClassifier(), getLifeCycleClassifier(), trace2case);
        
        Iterator<Decorations<XEvent>> it = decorations.iterator();
        for (TraceRecord<XEventClassifier> record : derived.traces) {
            Decorations<XEvent> dec = it.next();
            if (dec != null) {
                record.setEventDecorations(dec);
            }
        }
        
        return derived;
    }

    private Decorations<XEvent> _smartCloneDecorations(
            Decorations<XEvent> decs, XEvent eventS, XEvent eventC, XTrace subtrace) {
        // Intuition: If a super event has been annotated (e.g. with error triggers)
        // Then we also annotate all sub events in the corresponding subtrace
        Decorations<XEvent> resultingDecs = new Decorations<XEvent>();
        for (IDecorator<XEvent, ? extends Object> superDec : decs) {
            if (superDec instanceof SubtraceDecorator
                    && (superDec.hasDecoration(eventS) || superDec.hasDecoration(eventC))) {
                IDecorator<XEvent, ? extends Object> subDec = superDec.newInstance();
                ((SubtraceDecorator) subDec).deriveForSubtrace(
                        subtrace, new XEvent[] { eventS, eventC }, superDec);
                resultingDecs.registerDecorator(subDec);
            }
        }
        
        // TODO Auto-generated method stub
        return resultingDecs;
    }

    @Override
    protected void _newXTraceAdded(IMLogImplReclassAbstract<XEventClassifier> other, 
            int otherTraceIndex, XTrace trace, int traceIndex) {
        // Hook: update mapping new xtrace -> old xtrace
        int caseIdOther = ((IMLogHierarchySubtraceImpl) other).trace2case.get(otherTraceIndex);
        if (caseIdOther == NO_ELEMENT) {
            caseIdOther = otherTraceIndex;
        }
        trace2case.put(traceIndex, caseIdOther);
    }
    
    @Override
    public int getTraceCaseId(IMTrace trace) {
        // TODO: do something smart with subtraces originating from the same case
        // e.g.: map subtrace id to parenttrace id
        int traceId = trace.getXTraceIndex();
        int caseId = trace2case.get(traceId);
        if (caseId == NO_ELEMENT) {
            caseId = traceId;
        }
        return caseId;
    }
}
