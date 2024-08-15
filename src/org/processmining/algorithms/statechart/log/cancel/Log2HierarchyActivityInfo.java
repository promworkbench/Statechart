package org.processmining.algorithms.statechart.log.cancel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.statechart.log.HierarchyActivityInfo;
import org.processmining.xes.statechart.classification.XEventListLabelClassifier;
import org.processmining.xes.statechart.extension.XListLabelExtension;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

import com.google.common.base.Function;

public class Log2HierarchyActivityInfo implements Function<XLog, HierarchyActivityInfo> {

    private static final XEventListLabelClassifier clsList = new XEventListLabelClassifier();
    
    private static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();
    private static final XConceptExtension extConceptname = XConceptExtension.instance();
    private static final XLifecycleExtension extLifecycle = XLifecycleExtension.instance();
    
    @Override
    public HierarchyActivityInfo apply(XLog input) {
        // TODO find more efficient implementation
        HierarchyActivityInfo result = new HierarchyActivityInfo();

        if (XListLabelExtension.isListLabelLog(input)) {
            _calculateListLabel(input, result);
        } else {
            _calculateSubtrace(input, result);
        }
        
        return result;
    }

    private void _calculateListLabel(XLog input, HierarchyActivityInfo result) {
        
        for (XTrace trace : input) {
            for (XEvent event : trace) {
                List<String> actList = clsList.getClassIdentityList(event);
//                List<String> instList = clsList.getInstanceList(event);
                for (int i = 0; i < actList.size(); i++) {
                    String activity = actList.get(i);
//                    String instance = null;
//                    if (i < instList.size()) {
//                        instance = instList.get(i);
//                    }
                    
                    result.addActivity(activity);//, i, instance);
                }
            }
        }
    }

    private void _calculateSubtrace(XLog input, HierarchyActivityInfo result) {
        Deque<XTrace> horizon = new ArrayDeque<XTrace>();
        for (XTrace trace : input) {
            horizon.add(trace);
        }
        
        while (!horizon.isEmpty()) {
            XTrace trace = horizon.removeLast();
            for (XEvent event : trace) {
                String activity = extConceptname.extractName(event);
                String trans = extLifecycle.extractTransition(event);
                XTrace subtrace = extSubtrace.extractSubtrace(event);
                
                if (trans.equals(StandardModel.COMPLETE.getEncoding())) {
                    result.addActivity(activity);
                } else if (subtrace != null) {
                    // TODO why are there subtraces on complete events?
                    // see Camunda - Dirk Fahland log
                    horizon.add(subtrace);
                }
            }
        }
        
//        for (XTrace trace : input) {
//            _calculateSubtrace(trace, 0, result);
//        }
    }

//    private void _calculateSubtrace(XTrace trace, int i, HierarchyActivityInfo result) {
//        
////        MultiSet<String> instanceCount = new HashMultiSet<>();
//        for (XEvent event : trace) {
//            String activity = extConceptname.extractName(event);
//            String trans = extLifecycle.extractTransition(event);
//            XTrace subtrace = extSubtrace.extractSubtrace(event);
//            
//            if (subtrace != null) {
//                _calculateSubtrace(subtrace, i + 1, result);
//            }
//            
//            if (trans.equals(StandardModel.COMPLETE.toString())) {
////                instanceCount.add(activity);
//                result.addActivity(activity);//, i, Integer.toString(instanceCount.getCount(activity)));
//            }
//        }
//    }
}
