package org.processmining.algorithms.statechart.l2l;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class L2LAttributeList implements ISingleEventImplementation, Function<XLog, XLog> {

    public static class Parameters {
        public List<XEventClassifier> clsList; // list of attributes and/or
                                               // classifier to use
        public XEventClassifier clsSC; // event Start-Return labeler
        public String startSymbol; // Start symbol to match
        public String completeSymbol; // End symbol to match
        
        public Parameters() {
            clsList = new ArrayList<>();
            clsSC = new XEventLifeTransClassifier();
            startSymbol = XLifecycleExtension.StandardModel.START.getEncoding();
            completeSymbol = XLifecycleExtension.StandardModel.COMPLETE.getEncoding();
        }
    }

    private final Parameters params;
    private ISingleEventDriver driver;

    public L2LAttributeList(Parameters params, ISingleEventDriver driver) {
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
        Preconditions.checkNotNull(params.clsList, "No classifier list set");
        Preconditions.checkArgument(!params.clsList.isEmpty(),
                "Empty classifier list set");

        Preconditions.checkNotNull(params.startSymbol, "No start symbol set");
        Preconditions.checkNotNull(params.completeSymbol, "No complete symbol set");
        params.startSymbol = params.startSymbol.toLowerCase();
        params.completeSymbol = params.completeSymbol.toLowerCase();
    }
    
    @Override
    public String[] getEventLabelParts(XEvent event) {
        String[] labels = new String[params.clsList.size()];
        for (int i = 0; i < params.clsList.size(); i++) {
            labels[i] = params.clsList.get(i).getClassIdentity(event);
        }
        return labels;
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
