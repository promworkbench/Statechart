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

public class L2LStructuredActivity implements ISingleEventImplementation, Function<XLog, XLog> {

    public static class Parameters {
        public XEventClassifier clsLabel; // event labeler
        public Pattern reSelect; // part of label to consider
        public Pattern reSplit; // split selected part
        
        public XEventClassifier clsSC; // event Start-Return labeler
        public String startSymbol; // Start symbol to match
        public String completeSymbol; // End symbol to match
        
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

            clsSC = new XEventLifeTransClassifier();
            startSymbol = XLifecycleExtension.StandardModel.START.getEncoding();
            completeSymbol = XLifecycleExtension.StandardModel.COMPLETE.getEncoding();
        }
    }

    private final Parameters params;
    private ISingleEventDriver driver;
    
    public L2LStructuredActivity(Parameters params, ISingleEventDriver driver) {
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
        Preconditions.checkNotNull(params.reSelect, "No select regex set");
        Preconditions.checkNotNull(params.reSplit, "No split regex set");

        Preconditions.checkNotNull(params.startSymbol, "No start symbol set");
        Preconditions.checkNotNull(params.completeSymbol, "No complete symbol set");
        params.startSymbol = params.startSymbol.toLowerCase();
        params.completeSymbol = params.completeSymbol.toLowerCase();
    }

    @Override
    public String[] getEventLabelParts(XEvent event) {
        // try to match names
        String eventLabel = params.clsLabel.getClassIdentity(event);
        Matcher regexMatcher = params.reSelect.matcher(eventLabel);
        if (regexMatcher.find()) {
            // match found, apply split
            String label = regexMatcher.group();
            String[] labelParts = params.reSplit.split(label);
            labelParts[labelParts.length - 1] += eventLabel
                    .substring(regexMatcher.end());
            return labelParts;
        } else {
            // fallback, no split
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
