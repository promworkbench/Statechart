package org.processmining.models.statechart.im.log;

import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;

import com.google.common.base.Preconditions;

public abstract class IMLogImplReclassAbstract<C extends XEventClassifier>
    implements IMLog, Cloneable, IMLogHierarchy {

    private static boolean CacheIMTrace = true;

    protected static class TraceRecord<C extends XEventClassifier> implements Cloneable {
        private final int xtraceIndex;
        private C classifier;
        private final BitSet outEvents;
        private Decorations<XEvent> eventDecorations;

        private IMTrace imTrace;

        protected TraceRecord(int xtraceIndex, C classifier, Decorations<XEvent> eventDecorations, BitSet outEvents) {
            this.xtraceIndex = xtraceIndex;
            this.classifier = classifier;
            this.outEvents = outEvents;
            this.eventDecorations = eventDecorations;
        }

        public int getXTraceIndex() {
            return xtraceIndex;
        }

        public C getClassifier() {
            return classifier;
        }

        public void setClassifier(C newCls) {
            classifier = newCls;
            imTrace = null;
        }

        public BitSet getOutEvents() {
            return outEvents;
        }

        public IMTrace getIMTrace(int IMTraceIndex, IMLog imlog) {
            if (CacheIMTrace) {
                if (imTrace == null) {
                    imTrace = new IMTraceReclass<C>(xtraceIndex, IMTraceIndex,
                            outEvents, classifier, eventDecorations, imlog);
                }
                return imTrace;
            } else {
                return new IMTraceReclass<C>(xtraceIndex, IMTraceIndex,
                        outEvents, classifier, eventDecorations, imlog);
            }
        }

        @Override
        public TraceRecord<C> clone() {
            return new TraceRecord<C>(xtraceIndex, classifier, 
                    eventDecorations.clone(),
                    (BitSet) outEvents.clone());
        }

        public Decorations<XEvent> getEventDecorations() {
            return eventDecorations;
        }
        
        protected void setEventDecorations(Decorations<XEvent> decorations) {
            eventDecorations = decorations;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object other) {
            if (other instanceof TraceRecord<?>) {
                TraceRecord<C> rec = (TraceRecord<C>) other;
                return xtraceIndex == rec.xtraceIndex
                        && classifier.equals(rec.classifier)
                        && outEvents.equals(rec.outEvents);
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("(@%d, %s, %s)", xtraceIndex,
                    classifier.toString(), outEvents.toString());
        }

        /**
         * Beware: this hash is not strictly stable. Only to be used in cases
         * where you don't change the classifier or outEvents in the meantime
         * 
         * @return
         */
        @Override
        public int hashCode() {
            return Objects.hash(xtraceIndex, classifier, outEvents);
        }
    }

    protected final XLog xLog;

    protected final List<TraceRecord<C>> traces;
    protected final List<TraceRecord<C>> tracesAddPending;

    protected final Map<String, XEventClass> classMap;
    protected final C fallbackClassifier;
    protected XLifeCycleClassifier lifeCycleClassifier;

    protected static final int NO_ELEMENT = -1;

    private final List<XTrace> newTraces;
    private final TObjectIntMap<XTrace> newTraceIndices;

    /**
     * Initialize IMLogHierarchical backed by given log. All traces & events are
     * in by default, and for each trace the classifier is initialized at level
     * zero
     * 
     * @param xlog
     */
    protected IMLogImplReclassAbstract(XLog xLog,
            List<TraceRecord<C>> emptyList1, List<TraceRecord<C>> emptyList2,
            C classifier, XLifeCycleClassifier lifeCycleClassifier) {
        this.xLog = xLog;

        traces = emptyList1;
        tracesAddPending = emptyList2;
        for (int i = 0; i < xLog.size(); i++) {
            traces.add(new TraceRecord<C>(i, classifier, new Decorations<XEvent>(), new BitSet()));
        }

        classMap = new THashMap<String, XEventClass>();
        fallbackClassifier = classifier;
        this.lifeCycleClassifier = lifeCycleClassifier;
        
        newTraces = new ArrayList<XTrace>();
        newTraceIndices = new TObjectIntHashMap<XTrace>(
                Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR,
                NO_ELEMENT);
    }

    /**
     * Copy constructor
     * 
     * @param original
     * @param emptyList1
     * @param emptyList2
     */
    protected IMLogImplReclassAbstract(IMLogImplReclassAbstract<C> original,
            List<TraceRecord<C>> emptyList1, List<TraceRecord<C>> emptyList2) {
        this.xLog = original.xLog;

        traces = emptyList1;
        tracesAddPending = emptyList2;
        original._resolveAddPending();
        for (TraceRecord<C> record : original.traces) {
            traces.add(record.clone());
        }

        classMap = original.classMap; // TODO copy?
        fallbackClassifier = original.fallbackClassifier;
        lifeCycleClassifier = original.lifeCycleClassifier;
        
        newTraces = new ArrayList<XTrace>(original.newTraces);
        newTraceIndices = new TObjectIntHashMap<XTrace>(
                original.newTraceIndices);
    }

    @Override
    public abstract IMLog clone();

    @Override
    public abstract IMLog decoupleFromXLog();

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {
        if (other instanceof IMLogImplReclassAbstract<?>) {
            IMLogImplReclassAbstract<C> ot = (IMLogImplReclassAbstract<C>) other;
            _resolveAddPending();
            ot._resolveAddPending();
            return traces.equals(ot.traces);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (IMTrace trace : this) {
            result.append(trace.toString());
            result.append("\n");
        }
        return result.toString();
    }

    protected class It implements Iterator<IMTrace> {

        private int current;
        private int lastReturned;

        private final Iterator<TraceRecord<C>> it;

        protected It() {
            current = 0;
            lastReturned = -1;

            it = traces.iterator();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public IMTrace next() {
            lastReturned = current;
            current++;

            TraceRecord<C> record = it.next();
            return record.getIMTrace(lastReturned,
                    IMLogImplReclassAbstract.this);
        }

        @Override
        public void remove() {
            if (lastReturned < 0)
                throw new IllegalStateException();

            it.remove();

            current = lastReturned;
            lastReturned = -1;
        }

    }

    @Override
    public Iterator<IMTrace> iterator() {
        _resolveAddPending();
        return new It();
    }

    @Override
    public XEventClass classify(IMTrace IMTrace, XEvent event) {

        C classifier = resolveClassifier(IMTrace);
        String classId = classifier.getClassIdentity(event);

        XEventClass eventClass = classMap.get(classId);
        if (eventClass == null) {
            eventClass = new XEventClass(classId, classMap.size());
            classMap.put(classId, eventClass);
        }

        return eventClass;
    }

    @Override
    public void setClassifier(XEventClassifier classifier) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public IMTrace copyTrace(IMTrace trace) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public XEventClassifier getClassifier() {
        return fallbackClassifier;
    }

    @Override
    public XLifeCycleClassifier.Transition getLifeCycle(XEvent event) {
        return getLifeCycleClassifier().getLifeCycleTransition(event);
    }

    @Override
    public XLifeCycleClassifier getLifeCycleClassifier() {
        return lifeCycleClassifier;
    }

    @Override
    public void setLifeCycleClassifier(XLifeCycleClassifier lifeCycleClassifier) {
        this.lifeCycleClassifier = lifeCycleClassifier;
    }

//    @Override
//    public XTrace getTraceWithIndex(int traceIndex) {
//        return xLog.get(traceIndex);
//    }

    @Override
    public int size() {
        _resolveAddPending();
        return traces.size();
    }

    @Override
    public IMTrace copyTrace(IMTrace trace, BitSet outEvents) {
        C classifier = resolveClassifier(trace);
        return copyTrace(trace, outEvents, classifier);
    }

    public IMTrace copyTrace(IMTrace trace, BitSet outEvents, C classifier) {
        int xTraceIndex = trace.getXTraceIndex();

        BitSet newOutEvents = (BitSet) outEvents.clone();
        Decorations<XEvent> newEventDecorations;
        if (trace instanceof IMTraceReclass<?>) {
            newEventDecorations = ((IMTraceReclass<?>) trace).getEventDecorations().clone();
        } else {
            newEventDecorations = new Decorations<XEvent>();
        }
        TraceRecord<C> newTraceRecord = new TraceRecord<C>(xTraceIndex, 
                classifier, newEventDecorations, newOutEvents);
        tracesAddPending.add(newTraceRecord);

        int imTraceIndex = traces.size() + tracesAddPending.size();

        return newTraceRecord.getIMTrace(imTraceIndex, IMLogImplReclassAbstract.this);
    }

    public List<XAttribute> getGlobalTraceAttributes() {
        if (xLog != null) {
            return xLog.getGlobalTraceAttributes();
        }
        return null;
    }
    
    public List<XAttribute> getGlobalEventAttributes() {
        if (xLog != null) {
            return xLog.getGlobalEventAttributes();
        }
        return null;
    }
    
    @Override
    public XLog toXLog() {
        XFactory f = LogFactory.getFactory();
        XAttributeMap map = f.createAttributeMap();
        XLog result = f.createLog(map);
        for (IMTrace trace : this) {
            if (trace.isEmpty()) {
                result.add(f.createTrace(map));
            } else {
                XTrace xTrace = f.createTrace(map);
                for (XEvent e : trace) {
                    xTrace.add(e);
                }
                result.add(xTrace);
            }
        }

        return result;
    }

    @Override
    public XTrace getTraceWithIndex(int traceIndex) {
        Preconditions.checkElementIndex(traceIndex,
                xLog.size() + newTraces.size());

        if (traceIndex < xLog.size()) {
            return xLog.get(traceIndex);
        } else {
            return newTraces.get(traceIndex - xLog.size());
        }

    }

//  public XTrace getBaseTrace(IMTrace derivedTrace) {
//      return getBaseTrace(getTraceWithIndex(derivedTrace.getXTraceIndex()));
//  }
//  
//  public XTrace getBaseTrace(XTrace derivedTrace) {
//      XTrace baseTrace = derivedTrace;
//      XTrace result = baseTrace;
//      while (baseTrace != null) {
//          // TODO issue: hashed by contents (ArrayList<XEvent>)
//          baseTrace = baseTraces.get(derivedTrace);
//          if (baseTrace != null) {
//              result = baseTrace; 
//          }
//      }
//      return result;
//  }

//  public void addTrace(XTrace trace, IMTrace source) {//, XTrace basedOnTrace) {
//      int traceIndex = _newTraceIndex(trace);
//
//      C classifier = resolveClassifier(source);
//      traces.add(new TraceRecord<C>(
//              traceIndex, classifier, new Decorations<XEvent>(), new BitSet()));
////      baseTraces.put(trace, basedOnTrace);
//  }

    private int _newTraceIndex(IMLogImplReclassAbstract<C> other, int otherTraceIndex, XTrace trace) {
        for (int i = 0; i < xLog.size(); i++) {
            if (xLog.get(i) == trace) {
                return i;
            }
        }

        int traceIndex = newTraceIndices.get(trace);
        if (traceIndex == NO_ELEMENT) {
            traceIndex = xLog.size() + newTraces.size();
            newTraces.add(trace);
            newTraceIndices.put(trace, traceIndex);
            _newXTraceAdded(other, otherTraceIndex, trace, traceIndex);
        }
        return traceIndex;
    }

    protected void _newXTraceAdded(IMLogImplReclassAbstract<C> other, 
        int otherTraceIndex, XTrace trace, int traceIndex) {
        // Hook
    }

    protected void _resolveAddPending() {
        for (TraceRecord<C> record : tracesAddPending) {
            traces.add(record);
        }
        tracesAddPending.clear();
    }

    @SuppressWarnings("unchecked")
    public C resolveClassifier(IMTrace IMTrace) {
        // int imTraceIndex = IMTrace.getIMTraceIndex();

        if (IMTrace instanceof IMTraceReclass<?>) {
            return ((IMTraceReclass<C>) IMTrace).getClassifier();
        } else {
            throw new IllegalStateException();
        }
        // } else if (imTraceIndex >= traces.size()) {
        // return tracesAddPending.get(imTraceIndex - traces.size()).classifier;
        // } else {
        // return traces.get(imTraceIndex).classifier;
        // }
    }
    
    @Override
    public boolean addLog(IMLogHierarchy otherU) {
        if (!(otherU instanceof IMLogImplReclassAbstract)) {
            throw new IllegalArgumentException("Dont mix implementations for IMLogHierarchical");
        }
        @SuppressWarnings({ "rawtypes", "unchecked" })
        IMLogImplReclassAbstract<C> other = (IMLogImplReclassAbstract) otherU;
        _resolveAddPending();
        other._resolveAddPending();

        boolean changed = false;
        Set<TraceRecord<C>> records = 
                new THashSet<>(traces);

        for (TraceRecord<C> record : other.traces) {
            TraceRecord<C> newRecord = record;

            if ((other.xLog != this.xLog)
                    || (!other.newTraces.isEmpty() && record.getXTraceIndex() >= other.xLog
                            .size())) {
                XTrace trace = other.getTraceWithIndex(record.getXTraceIndex());
                int newXTraceIndex = _newTraceIndex(other, record.getXTraceIndex(), trace);
                
                // TODO: check if we need cloning
                newRecord = new TraceRecord<C>(
                        newXTraceIndex, record.getClassifier(),
                        record.getEventDecorations(),
                        record.getOutEvents());
            }

            if (!records.contains(newRecord)) {
                traces.add(newRecord);
                records.add(newRecord);
                changed = true;
            }
        }

        return changed;
    }
}
