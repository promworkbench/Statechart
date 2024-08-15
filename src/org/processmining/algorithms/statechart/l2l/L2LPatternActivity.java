package org.processmining.algorithms.statechart.l2l;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class L2LPatternActivity implements ISingleEventImplementation, Function<XLog, XLog> {

    public static class Parameters {
        public XEventClassifier clsLabel; // event labeler
        public Pattern reParts; // parts capture regex
        
        public XEventClassifier clsSC; // event Start-Return labeler
        public String startSymbol; // Start symbol to match
        public String completeSymbol; // End symbol to match
        
        @SuppressWarnings("unchecked")
        public static final Pair<Pattern, String>[] RePartsDefaults = new Pair[] {
                Pair.of(Pattern.compile("([^+]*)\\+(.*)"),
                        "Parts with plus \"part1+part2\""),
                Pair.of(Pattern.compile("([^_]*)_(.*)"),
                        "Parts with underscore \"part1_part2\"") };

        public Parameters() {
            // Default: split on dot in package.subpackage.class like labels,
            // where we select only the part before parenthesis to split on.
            clsLabel = new XEventNameClassifier();
            reParts = RePartsDefaults[0].getLeft();

            clsSC = new XEventLifeTransClassifier();
            startSymbol = XLifecycleExtension.StandardModel.START.getEncoding();
            completeSymbol = XLifecycleExtension.StandardModel.COMPLETE.getEncoding();
        }
    }

    private final Parameters params;
    private ISingleEventDriver driver;

    public L2LPatternActivity(Parameters params, ISingleEventDriver driver) {
        Preconditions.checkNotNull(params, "No parameters set");
        this.params = params;
        
        this.driver = driver;
        driver.setImplement(this);
    }

    @Override
    public XLog apply(XLog input) {
        return driver.apply(input);
    }
    
    public XTrace apply(XTrace input) {
        return driver.apply(input);
    }

    @Override
    public void checkInput() {
        Preconditions.checkNotNull(params.clsLabel, "No label classifier set");
        Preconditions.checkNotNull(params.reParts, "No parts capture regex set");

        Preconditions.checkNotNull(params.startSymbol, "No start symbol set");
        Preconditions.checkNotNull(params.completeSymbol, "No complete symbol set");
        params.startSymbol = params.startSymbol.toLowerCase();
        params.completeSymbol = params.completeSymbol.toLowerCase();
    }

    @Override
    public String[] getEventLabelParts(XEvent event) {
        String eventLabel = params.clsLabel.getClassIdentity(event);
        Matcher regexMatcher = params.reParts.matcher(eventLabel);
        if (regexMatcher.find()) {
            // Collect parts
            final int numGroups = regexMatcher.groupCount();
            String[] labelParts = new String[numGroups];
            for (int i = 0; i < numGroups; i++) {
                labelParts[i] = regexMatcher.group(i + 1);
            }
            return labelParts;
        } else {
            return new String[] {
                eventLabel
            };
        }
    }

    @Override
    public Lifecycle getEventStartComplete(XEvent event) {
        String eventSR = params.clsSC.getClassIdentity(event).toLowerCase();
        if (eventSR == null || eventSR.isEmpty() 
                || eventSR.equals(params.completeSymbol)) {
            return Lifecycle.Complete;
        } else if (eventSR.equals(params.startSymbol)) {
            return Lifecycle.Start;
        } else  {
            return Lifecycle.Other;
        }
    }
    
}
