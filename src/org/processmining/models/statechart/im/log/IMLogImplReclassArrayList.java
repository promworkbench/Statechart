package org.processmining.models.statechart.im.log;

import java.util.ArrayList;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;

public abstract class IMLogImplReclassArrayList<C extends XEventClassifier> extends
        IMLogImplReclassAbstract<C> {

    public IMLogImplReclassArrayList(XLog xlog, C classifier, XLifeCycleClassifier lifeCycleClassifier) {
        super(xlog, new ArrayList<TraceRecord<C>>(), new ArrayList<TraceRecord<C>>(), classifier, lifeCycleClassifier);
    }

    protected IMLogImplReclassArrayList(IMLogImplReclassArrayList<C> original) {
        super(original, new ArrayList<TraceRecord<C>>(), new ArrayList<TraceRecord<C>>());
    }
}
