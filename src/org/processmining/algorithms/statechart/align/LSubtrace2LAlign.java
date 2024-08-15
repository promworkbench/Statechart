package org.processmining.algorithms.statechart.align;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.statechart.im.log.IMLogImplReclassAbstract;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class LSubtrace2LAlign implements Function<IMLog, XLog> {

    private static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();

    @Override
    public XLog apply(IMLog input) {
        return transform(input);
    }
    
    public XLog transform(IMLog input) {
        Preconditions.checkNotNull(input);

        XFactory f = LogFactory.getFactory();
        XLog log = f.createLog();
        log.getClassifiers().add(new XEventAndClassifier(
            new XEventNameClassifier(), new XEventLifeTransClassifier()));
        log.getClassifiers().add(new XEventNameClassifier());
        log.getClassifiers().add(new XEventLifeTransClassifier());

        if (input instanceof IMLogImplReclassAbstract) {
            IMLogImplReclassAbstract<?> abstrInput = (IMLogImplReclassAbstract<?>) input;
            
            if (abstrInput.getGlobalTraceAttributes() != null) {
                List<XAttribute> ltAttr = log.getGlobalTraceAttributes();
                for (XAttribute attr : abstrInput.getGlobalTraceAttributes()) {
                    ltAttr.add(LogFactory.clone(attr, f));
                }
            }
            if (abstrInput.getGlobalEventAttributes() != null) {
                List<XAttribute> leAttr = log.getGlobalEventAttributes();
                for (XAttribute attr : abstrInput.getGlobalEventAttributes()) {
                    leAttr.add(LogFactory.clone(attr, f));
                }
            }
        }
        
        for (IMTrace trace : input) {
            log.add(_transformTrace(f, input.getTraceWithIndex(trace.getXTraceIndex())));
        }

        return log;
    }

    private XTrace _transformTrace(XFactory f, XTrace traceIn) {
        XTrace result = f.createTrace();

        XAttributeMap attrMap = result.getAttributes();
        XAttributeMap inputTraceAttrMap = traceIn.getAttributes();
        for (String attrKey : inputTraceAttrMap.keySet()) {
            attrMap.put(attrKey, LogFactory.clone(inputTraceAttrMap.get(attrKey), f));
        }
        
        Iterator<XEvent> currentIt = traceIn.iterator();
        Deque<Iterator<XEvent>> openIts = new ArrayDeque<>();
        
        while(currentIt.hasNext()) {
            XEvent inputEvent = currentIt.next();
            XTrace subtrace = extSubtrace.extractSubtrace(inputEvent);
            if (subtrace != null) {
                openIts.addLast(currentIt);
                currentIt = subtrace.iterator();
            }
            
            XEvent event = LogFactory.clone(inputEvent, f);
            event.getAttributes().remove(XSubtraceExtension.KEY_SUBTRACE);
            result.add(event);
            
            while (!currentIt.hasNext() && !openIts.isEmpty()) {
                currentIt = openIts.removeLast();
            }
        }
        
        return result;
    }

}
