package org.processmining.algorithms.statechart.discovery.im.basecase;

import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.models.statechart.im.log.IMLogHierarchyListImpl;
import org.processmining.models.statechart.im.log.IMTraceReclass;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.xes.statechart.classification.XEventListLabelClassifier;
import org.processmining.xes.statechart.classification.XEventListLabelElementClassifier;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

public abstract class AbstractFinderSCCompositeOr implements BaseCaseFinder {

    protected static final XEventListLabelClassifier clsList = new XEventListLabelClassifier();
    protected static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();

    protected boolean isCompositeCase(IMLogHierarchy log, IMLogInfo logInfo,
            MinerState minerState) {
        // check if we only have one activity name (a base case)
        if (logInfo.getActivities().setSize() == 1) {
            if (log instanceof IMLogHierarchyListImpl) {
                return _isCompositeCaseListImpl(log);
            } else {
                return _isCompositeCaseSubtraceImpl(log);
            }
        } else {
            // multiple activities, skip
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean _isCompositeCaseListImpl(IMLogHierarchy log) {
        // some events may have a lower level, others may not (e.g., empty traces)
        boolean isComposite = false;
        Iterator<IMTrace> itTrace = log.iterator();
        while (itTrace.hasNext()) {
            IMTraceReclass<XEventListLabelElementClassifier> t = 
                (IMTraceReclass<XEventListLabelElementClassifier>) itTrace.next();
            if (!t.isEmpty()) {
                XEvent e = t.iterator().next();
                
                // check if there is one level deeper in the log
                List<String> list = clsList.getClassIdentityList(e);
                isComposite = isComposite || list.size() > t.getClassifier().getLevel() + 1;
            }
        }
        return isComposite;
    }

    private boolean _isCompositeCaseSubtraceImpl(IMLogHierarchy log) {
        // some events may have a lower level, others may not (e.g., empty traces)
        boolean isComposite = false;
        Iterator<IMTrace> itTrace = log.iterator();
        while (itTrace.hasNext()) {
            IMTrace t = itTrace.next();
            if (!t.isEmpty()) {
                XEvent e = t.iterator().next();
                isComposite = isComposite || extSubtrace.extractSubtrace(e) != null;
            }
        }
        return isComposite;
    }

    @SuppressWarnings("unchecked")
    protected <T extends MiningParameters> T getStateParams(
            MinerState minerState) {
        try {
            return (T) minerState.parameters;
        } catch (ClassCastException e) {
            throw new IllegalStateException(
                    "IM miner state paramerters illegal type", e);
        }
    }


    protected boolean _detectLoop(IMLogHierarchy hlog) {
        return BasecaseUtil.detectSingleActLoop(hlog);
    }

    protected void _splitLogLoops(IMLogHierarchy sublog) {
        for (IMTrace trace : sublog) {
            @SuppressWarnings("unchecked")
            IMTraceReclass<XEventClassifier> t = 
                (IMTraceReclass<XEventClassifier>) trace;
//          XEventListLabelElementClassifier classifier = t.getClassifier();
            XEventClassifier classifier = t.getClassifier();
            
            if (classifier instanceof XEventListLabelElementClassifier) {
                // use list label instance classifier
                final XEventListLabelElementClassifier cls =
                    (XEventListLabelElementClassifier) classifier;

                String runningInst = null;
                for (IMEventIterator itEvent = trace.iterator(); itEvent.hasNext();) {
                    XEvent event = itEvent.next();
                    String currentInst = cls.getInstance(event);
                    if (runningInst == null) { 
                        runningInst = currentInst; 
                    } else if (!runningInst.equals(currentInst)) {
                        runningInst = currentInst;
                        itEvent.split();
                    }
                }
            } else {
                // use repeating start events
                boolean first = true;
                for (IMEventIterator itEvent = trace.iterator(); itEvent.hasNext();) {
                    XEvent event = itEvent.next();
                    if (sublog.getLifeCycle(event) != XLifeCycleClassifier.Transition.complete) {
                        // start new instance
                        if (!first) {
                            itEvent.split();
                        } else {
                            first = false;
                        }
                    }
                }
            }
        }
    }

}
