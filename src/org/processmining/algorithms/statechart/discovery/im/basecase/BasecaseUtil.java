package org.processmining.algorithms.statechart.discovery.im.basecase;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.models.statechart.im.log.IMTraceReclass;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.xes.statechart.classification.XEventListLabelElementClassifier;

public class BasecaseUtil {

    public static boolean detectSingleActLoop(IMLogHierarchy hlog) {
        for (IMTrace trace : hlog) {
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
                for (XEvent event : trace) {
                    String currentInst = cls.getInstance(event);
                    if (runningInst == null) { 
                        runningInst = currentInst; 
                    } else if (!runningInst.equals(currentInst)) {
                        return true;
                    }
                }
                
            } else {
                // use repeating start events
                int instances = 0;
                for (XEvent event : trace) {
                    if (hlog.getLifeCycle(event) != XLifeCycleClassifier.Transition.complete) {
                        // start new instance
                        instances++;
                    }
                    if (instances >= 2) {
                        return true;
                    }
                }
            }
            
            
        }
        return false;
    }
}
