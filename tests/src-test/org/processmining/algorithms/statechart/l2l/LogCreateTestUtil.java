package org.processmining.algorithms.statechart.l2l;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.xes.statechart.extension.XListLabelExtension;

import com.google.common.base.Optional;

public class LogCreateTestUtil {

    private static final XListLabelExtension extListLabel = XListLabelExtension.instance();
    
    public static XLog createLogFlat(String[][] input) {
        XConceptExtension extConceptName = XConceptExtension.instance();
        XLifecycleExtension extLifecycle = XLifecycleExtension.instance();
        XFactory f = LogFactory.getFactory();
        
        XLog result = f.createLog();
        for (String[] inTrace : input) {
            XTrace outTrace = f.createTrace();
            for (String inEvent : inTrace) {
                String[] inAttr = inEvent.split("_");
                
                XEvent outEvent = f.createEvent();
                extConceptName.assignName(outEvent, inAttr[0]);
                if (inAttr.length > 1) {
                    extLifecycle.assignTransition(outEvent, inAttr[1]);
                }
                outTrace.add(outEvent);
            }
            result.add(outTrace);
        }
        
        return result;
    }
    
    public static XTrace createTraceFlat(String[] input) {
        XConceptExtension extConceptName = XConceptExtension.instance();
        XLifecycleExtension extLifecycle = XLifecycleExtension.instance();
        XFactory f = LogFactory.getFactory();

        XTrace outTrace = f.createTrace();
        for (String inEvent : input) {
            String[] inAttr = inEvent.split("_");
            
            XEvent outEvent = f.createEvent();
            extConceptName.assignName(outEvent, inAttr[0]);
            if (inAttr.length > 1) {
                extLifecycle.assignTransition(outEvent, inAttr[1]);
            }
            outTrace.add(outEvent);
        }
        
        return outTrace;
    }

    public static XLog createLogFlat(String[][][] input) {
        XConceptExtension extConceptName = XConceptExtension.instance();
        XFactory f = LogFactory.getFactory();
        
        XLog result = f.createLog();
        for (String[][] inTrace : input) {
            XTrace outTrace = f.createTrace();
            for (String[] inEventList : inTrace) {
                String inEvent = flatten(inEventList, ",");
                
                XEvent outEvent = f.createEvent();
                extConceptName.assignName(outEvent, inEvent);
                outTrace.add(outEvent);
            }
            result.add(outTrace);
        }
        
        return result;
    }
    
    public static XLog createLogFlatStartComplete(String[][] input) {
        XConceptExtension extConceptName = XConceptExtension.instance();
        XLifecycleExtension extLifecycle = XLifecycleExtension.instance();
        XFactory f = LogFactory.getFactory();
        
        XLog result = f.createLog();
        for (String[] inTrace : input) {
            XTrace outTrace = f.createTrace();
            for (String inEvent : inTrace) {
                {
                    XEvent outEvent = f.createEvent();
                    extConceptName.assignName(outEvent, inEvent);
                    extLifecycle.assignTransition(outEvent, "start");
                    outTrace.add(outEvent);
                }
                {
                    XEvent outEvent = f.createEvent();
                    extConceptName.assignName(outEvent, inEvent);
                    extLifecycle.assignTransition(outEvent, "complete");
                    outTrace.add(outEvent);
                }
            }
            result.add(outTrace);
        }
        
        return result;
    }
    
    public static XLog createLogFlatStartComplete(String[][][] input) {
        XConceptExtension extConceptName = XConceptExtension.instance();
        XLifecycleExtension extLifecycle = XLifecycleExtension.instance();
        XFactory f = LogFactory.getFactory();
        
        XLog result = f.createLog();
        for (String[][] inTrace : input) {
            XTrace outTrace = f.createTrace();
            for (String[] inEventList : inTrace) {
                String inEvent = flatten(inEventList, ",");
                {
                    XEvent outEvent = f.createEvent();
                    extConceptName.assignName(outEvent, inEvent);
                    extLifecycle.assignTransition(outEvent, "start");
                    outTrace.add(outEvent);
                }
                {
                    XEvent outEvent = f.createEvent();
                    extConceptName.assignName(outEvent, inEvent);
                    extLifecycle.assignTransition(outEvent, "complete");
                    outTrace.add(outEvent);
                }
            }
            result.add(outTrace);
        }
        
        return result;
    }
    
    public static String flatten(String[] inEventList, String sepSymbol) {
        StringBuilder bld = new StringBuilder();
        String sep = "";
        for (String part : inEventList) {
            bld.append(sep);
            bld.append(part);
            sep = sepSymbol;
        }
        return bld.toString();
    }

    public static XLog createLogList(String[][][] input) {
        XListLabelExtension extListLabel = XListLabelExtension.instance();
        XFactory f = LogFactory.getFactory();
        
        XLog result = f.createLog();
        for (String[][] inTrace : input) {
            XTrace outTrace = f.createTrace();
            for (String[] inEvent : inTrace) {
                List<String> names = new ArrayList<>();
                List<String> insts = new ArrayList<>();
                for (String e : inEvent) {
                    String[] a = e.split("_");
                    names.add(a[0]);
                    if (a.length > 1) {
                        insts.add(a[1]);
                    } else {
                        insts.add("1");
                    }
                }
                
                XEvent outEvent = f.createEvent();
                extListLabel.assignName(outEvent, names);
                extListLabel.assignInstance(outEvent, insts);
                outTrace.add(outEvent);
            }
            result.add(outTrace);
        }
        
        return result;
    }
    
    public static void addMaps(XEvent xEvent, XEvent... events) {
        List<XAttributeMap> maps = new ArrayList<>();
        for (int i = 0; i < events.length; i++) {
            maps.add(events[i].getAttributes());
        }
        
        extListLabel.assignData(xEvent, maps);
    }
    
    public static void addMaps(XEvent xEvent, XAttributeMap... maps) {
        extListLabel.assignData(xEvent, Arrays.asList(maps));
    }
    
    public static void addDataFromMaps(XEvent xEvent, XEvent xEventMaps, int index, Set<String> keysToIgnore) {
        Optional<XAttributeMap> dataMap = extListLabel.getDataMap(xEventMaps, index);
        if (dataMap.isPresent()) {
            XAttributeMap map = dataMap.get();
            
            XAttributeMap targetMap = xEvent.getAttributes();
            for (String fromKey : map.keySet()) {
                if (!keysToIgnore.contains(fromKey)) {
                    targetMap.put(fromKey, map.get(fromKey));
                }
            }
        }
    }
    
    public static void addDataFromMap(XEvent toEvent, XEvent fromMap, Set<String> keysToIgnore) {
        XAttributeMap map = fromMap.getAttributes();
        
        XAttributeMap targetMap = toEvent.getAttributes();
        for (String fromKey : map.keySet()) {
            if (!keysToIgnore.contains(fromKey)) {
                targetMap.put(fromKey, map.get(fromKey));
            }
        }
    }
}
