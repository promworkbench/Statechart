package org.processmining.algorithms.statechart.l2l;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.statechart.log.LogFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class L2LShortenNames implements Function<XLog, XLog> {

    public static class Parameters {
        public XEventClassifier clsLabel; // event labeler
        public Pattern reSelect; // part of label to consider
        public Pattern reSplit; // split selected part

        @SuppressWarnings("unchecked")
        public static final Pair<Pattern, String>[] ReSelectDefaults = new Pair[] {
                Pair.of(Pattern.compile("([\\w$]+(?:\\.[\\w$]+)*)"),
                        "Select the pattern: \"package.class.method\""),
                Pair.of(Pattern.compile(".*"), "Select everything") };

        @SuppressWarnings("unchecked")
        public static final Pair<Pattern, String>[] ReSplitDefaults = new Pair[] {
                Pair.of(Pattern.compile("\\."),
                        "Split on dot in \"package.class.method\""),
                Pair.of(Pattern.compile("\\+"),
                        "Split on plus in \"name1+name2+name3\""),
                        Pair.of(Pattern.compile("_"),
                                "Split on underscore in \"name1_name2_name3\"") };

        public Parameters() {
            // Default: split on dot in package.subpackage.class like labels,
            // where we select only the part before parenthesis to split on.
            clsLabel = new XEventNameClassifier();
            reSelect = ReSelectDefaults[0].getLeft();
            reSplit = ReSplitDefaults[0].getLeft();
        }
    }
    
    private final Parameters params;
    
    private XConceptExtension extConcept = XConceptExtension.instance();

    public L2LShortenNames() {
        this(new Parameters());
    }

    public L2LShortenNames(Parameters params) {
        Preconditions.checkNotNull(params, "No parameters set");
        this.params = params;
    }

    @Override
    public XLog apply(XLog input) {
        return transform(input);
    }

    public XTrace apply(XTrace input) {
        XFactory f = LogFactory.getFactory();
        Map<String, String> cache = new HashMap<>();
        return _transformTrace(f, input, cache);
    }

    public XLog transform(XLog input) {
        Preconditions.checkNotNull(input);

        Preconditions.checkNotNull(params.clsLabel, "No label classifier set");
        Preconditions.checkNotNull(params.reSelect, "No select regex set");
        Preconditions.checkNotNull(params.reSplit, "No split regex set");

        XFactory f = LogFactory.getFactory();
        XLog log = f.createLog();
        log.getExtensions().add(XConceptExtension.instance());
        log.getClassifiers().add(new XEventNameClassifier());

        Map<String, String> cache = new HashMap<>();
        for (XTrace trace : input) {
            log.add(_transformTrace(f, trace, cache));
        }

        return log;
    }
    
    private XTrace _transformTrace(XFactory f, XTrace input, Map<String, String> cache) {
        XTrace result = f.createTrace();
        for (XEvent event : input) {
            result.add(_createEvent(f, event, cache));
        }
        return result;
    }

    private XEvent _createEvent(XFactory f, XEvent oldEvent, Map<String, String> cache) {
        XEvent event = f.createEvent();
        XAttributeMap eMap = event.getAttributes();

        // copy basic attributes
        XAttributeMap returnMap = oldEvent.getAttributes();
        for (String key : returnMap.keySet()) {
            if (!key.equals(XConceptExtension.KEY_NAME)) {
                eMap.put(key, returnMap.get(key));
            }
        }

        // override label
        String eventLabel = params.clsLabel.getClassIdentity(oldEvent);
        Matcher regexMatcher = params.reSelect.matcher(eventLabel);
        if (regexMatcher.find()) {
            String label = regexMatcher.group();
            String[] labelParts = params.reSplit.split(label);

            String newLabel = cache.get(eventLabel);
            if (newLabel == null) {
                newLabel = "";
                for (int i = 0; i < labelParts.length; i++) {
                    if (i > 0) {
                        newLabel += ".";
                    }
                    if (i < labelParts.length - 2) {
                        newLabel += labelParts[i].substring(0, 1);
                    } else {
                        newLabel += labelParts[i];
                    }
                }
                newLabel += "_" + cache.size();
                
                cache.put(eventLabel, newLabel);
            }
            
            // override label
            extConcept.assignName(event, newLabel);
        } else {
            extConcept.assignName(event, eventLabel);
        }

        return event;
    }
}
