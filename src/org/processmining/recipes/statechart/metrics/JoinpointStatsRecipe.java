package org.processmining.recipes.statechart.metrics;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.protocols.statechart.saw.api.data.Joinpoint;
import org.processmining.protocols.statechart.saw.api.data.JoinpointStat;
import org.processmining.recipes.statechart.AbstractRecipe;
import org.processmining.xes.statechart.XUtil;
import org.processmining.xes.statechart.extension.XListLabelExtension;
import org.processmining.xes.statechart.extension.XSubtraceExtension;
import org.processmining.xes.statechart.xport.XApplocExtension;

public class JoinpointStatsRecipe extends
    AbstractRecipe<XLog, Collection<JoinpointStat>, Void> {

    private static final XListLabelExtension extListLabel = XListLabelExtension.instance();

    private static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();
//    private static final XConceptExtension extConceptname = XConceptExtension.instance();
    private static final XLifecycleExtension extLifecycle = XLifecycleExtension.instance();
    
    public JoinpointStatsRecipe() {
        super(null);
    }

    @Override
    protected Collection<JoinpointStat> execute(XLog input) {
        if (XListLabelExtension.isListLabelLog(input)) {
            return _processListLabel(input);
        } else {
            return _processSubtrace(input);
        }
    }

    private Collection<JoinpointStat> _processListLabel(XLog input) {
        Map<String, JoinpointStat> result = new HashMap<>();
        
        for (XTrace trace : input) {
            for (XEvent event : trace) {
                
                List<XAttributeMap> attrMapList = extListLabel.extractData(event);
                if (attrMapList.isEmpty()) {
                    // Data unavailable, graceful fallback
                    return result.values();
                } else {
                    final int length = attrMapList.size();
                    for (int i = 0; i < length; i++) {
                        _processMap(result, attrMapList.get(i), i == (length - 1));
                    }
                }
            }
        }
        
        return result.values();
    }

    private void _processMap(Map<String, JoinpointStat> result, XAttributeMap map, boolean leaf) {
        Joinpoint jp = new Joinpoint(
                XUtil.extractLiteral(map, XApplocExtension.KEY_JOINPOINT),
                XUtil.extractLiteral(map, XApplocExtension.KEY_FILENAME),
                XUtil.extractInt(map, XApplocExtension.KEY_LINENR));
        
        JoinpointStat stat = result.get(jp.getJoinpoint());
        if (stat == null) {
            stat = new JoinpointStat(jp, 1);
            result.put(jp.getJoinpoint(), stat);
        } else if (leaf) {
            stat.increaseFrequency();
        }
    }


    private Collection<JoinpointStat> _processSubtrace(XLog input) {
        Map<String, JoinpointStat> result = new HashMap<>();
        
        Deque<XTrace> horizon = new ArrayDeque<XTrace>();
        for (XTrace trace : input) {
            horizon.add(trace);
        }
        
        while (!horizon.isEmpty()) {
            XTrace trace = horizon.removeLast();
            for (XEvent event : trace) {
//                String activity = extConceptname.extractName(event);
                String trans = extLifecycle.extractTransition(event);
                XTrace subtrace = extSubtrace.extractSubtrace(event);
                
                if (trans.equals(StandardModel.COMPLETE.getEncoding())) {
                    XAttributeMap map = event.getAttributes();
                    Joinpoint jp = new Joinpoint(
                            XUtil.extractLiteral(map, XApplocExtension.KEY_JOINPOINT),
                            XUtil.extractLiteral(map, XApplocExtension.KEY_FILENAME),
                            XUtil.extractInt(map, XApplocExtension.KEY_LINENR));
                    
                    JoinpointStat stat = result.get(jp.getJoinpoint());
                    if (stat == null) {
                        stat = new JoinpointStat(jp, 1);
                        result.put(jp.getJoinpoint(), stat);
                    } else if (subtrace == null) {
                        stat.increaseFrequency();
                    }
                } else if (subtrace != null) {
                    // TODO why are there subtraces on complete events?
                    // see Camunda - Dirk Fahland log
                    horizon.add(subtrace);
                }
            }
        }
        
        return result.values();
    }
    
}
