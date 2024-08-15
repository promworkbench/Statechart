package org.processmining.models.statechart.im.log;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveminer2.helperclasses.XLifeCycleClassifierIgnore;
import org.processmining.xes.statechart.classification.XEventListLabelClassifier;
import org.processmining.xes.statechart.classification.XEventListLabelElementClassifier;

public class IMLogHierarchyListImpl
        extends IMLogImplReclassLinkedList<XEventListLabelElementClassifier> {

    private static final XEventListLabelClassifier clsList = new XEventListLabelClassifier();

//    private Map<XTrace, XTrace> baseTraces;

    public IMLogHierarchyListImpl(XLog xlog, int classLevel) {
        super(xlog, new XEventListLabelElementClassifier(classLevel), new XLifeCycleClassifierIgnore());
//        baseTraces = new THashMap<>();
    }

    private IMLogHierarchyListImpl(IMLogHierarchyListImpl original) {
        super(original);
//        baseTraces = new THashMap<>(original.baseTraces);
    }

    @Override
    public IMLog clone() {
        return new IMLogHierarchyListImpl(this);
    }

    @Override
    public IMLogHierarchy deriveLowerlevel() {
        IMLogHierarchyListImpl derived = (IMLogHierarchyListImpl) this.clone();

        Iterator<TraceRecord<XEventListLabelElementClassifier>> it = derived.traces
                .iterator();
        while (it.hasNext()) {
            TraceRecord<XEventListLabelElementClassifier> record = it.next();

            // check for new "empty" traces to remove
            XTrace trace = derived.getTraceWithIndex(record.getXTraceIndex());
            if (trace.size() <= record.getOutEvents().cardinality()) {
                it.remove();
            } else {
                // check for illegal new classifier level traces to mark as empty events
                int newClsLevel = record.getClassifier().getLevel() + 1;
                List<String> list = null;
                BitSet outEvents = record.getOutEvents();
                int incb = outEvents.nextClearBit(0);
                if (incb < trace.size()) {
                    XEvent event = trace.get(incb);
                    list = clsList.getClassIdentityList(event);
                }
                if (list == null || newClsLevel >= list.size()) {
                    outEvents.set(0, trace.size());
                }
                
                record.setClassifier(new XEventListLabelElementClassifier(
                        newClsLevel));
            }
        }

        return derived;
    }

    @Override
    public IMLog decoupleFromXLog() {
        // TODO: loses information about trace specific level classifiers
        XLog xLog = toXLog();
        return new IMLogHierarchyListImpl(xLog, fallbackClassifier.getLevel());
    }

    @Override
    public int getTraceCaseId(IMTrace trace) {
        return trace.getXTraceIndex();
    }
}
