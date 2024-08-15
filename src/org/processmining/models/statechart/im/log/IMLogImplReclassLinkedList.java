package org.processmining.models.statechart.im.log;

import java.util.LinkedList;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;

public abstract class IMLogImplReclassLinkedList<C extends XEventClassifier> extends
        IMLogImplReclassAbstract<C> {

    public IMLogImplReclassLinkedList(XLog xlog, C classifier, XLifeCycleClassifier lifeCycleClassifier) {
        super(xlog, new LinkedList<TraceRecord<C>>(),
                new LinkedList<TraceRecord<C>>(), classifier, lifeCycleClassifier);
    }

    protected IMLogImplReclassLinkedList(IMLogImplReclassAbstract<C> original) {
        super(original, new LinkedList<TraceRecord<C>>(),
                new LinkedList<TraceRecord<C>>());
    }

}
