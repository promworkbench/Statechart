package org.processmining.algorithms.statechart.l2l.list;

import java.util.Arrays;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.algorithms.statechart.l2l.ISingleEventDriver;
import org.processmining.algorithms.statechart.l2l.ISingleEventImplementation;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.xes.statechart.classification.XEventListLabelClassifier;
import org.processmining.xes.statechart.extension.XListLabelExtension;

import com.google.common.base.Preconditions;

public class L2LListSingleEventDriver implements ISingleEventDriver {

    protected static final XListLabelExtension extListLabel = XListLabelExtension.instance();
    
    private ISingleEventImplementation implementation;

    @Override
    public void setImplement(ISingleEventImplementation implementation) {
        this.implementation = implementation;
    }
    
    @Override
    public XLog apply(XLog input) {
        return transform(input);
    }

    @Override
    public XTrace apply(XTrace input) {
        XFactory f = LogFactory.getFactory();
        return _transformTrace(f, input);
    }

    public XLog transform(XLog input) {
        Preconditions.checkNotNull(implementation);
        Preconditions.checkNotNull(input);
        implementation.checkInput();

        XFactory f = LogFactory.getFactory();
        XLog log = f.createLog();
        log.getExtensions().add(extListLabel);
        log.getExtensions().add(XConceptExtension.instance());
        log.getClassifiers().add(new XEventNameClassifier());
        log.getClassifiers().add(new XEventListLabelClassifier());

        for (XTrace trace : input) {
            log.add(_transformTrace(f, trace));
        }

        return log;
    }
    
    protected XTrace _transformTrace(XFactory f, XTrace input) {
        XTrace result = f.createTrace();
        
        for (XEvent oldEvent : input) {
            String[] labelParts = implementation.getEventLabelParts(oldEvent);
            // TODO smart instance detection?
            String[] instanceParts = new String[labelParts.length];
            for (int i = 0; i < labelParts.length; i++) {
                instanceParts[i] = "1";
            }
            
            // Create list label event
            XEvent newEvent = f.createEvent();
            XAttributeMap eMap = newEvent.getAttributes();

            // copy basic attributes
            XAttributeMap returnMap = oldEvent.getAttributes();
            for (String key : returnMap.keySet()) {
                if (!key.equals(XConceptExtension.KEY_NAME)) {
                    eMap.put(key, returnMap.get(key));
                }
            }
            
            // override label
            extListLabel.assignName(newEvent, Arrays.asList(labelParts));
            extListLabel.assignInstance(newEvent, Arrays.asList(instanceParts));
            result.add(newEvent);
        }
        
        return result;
    }
}
