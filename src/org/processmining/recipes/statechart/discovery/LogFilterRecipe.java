package org.processmining.recipes.statechart.discovery;

import java.util.Iterator;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.algorithms.statechart.l2l.L2LIMFilter;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.recipes.statechart.AbstractRecipe;

public class LogFilterRecipe extends
        AbstractRecipe<IMLog, IMLog, L2LIMFilter.Parameters> {

    public static final boolean AsyncDemoHack = false;
    
    public LogFilterRecipe() {
        super(new L2LIMFilter.Parameters());
    }

    @Override
    protected IMLog execute(IMLog input) {
        L2LIMFilter transform = new L2LIMFilter(getParameters());
        IMLog sublog = transform.transform(input);
        
        if (LogFilterRecipe.AsyncDemoHack) {
            // Hack:
            for (IMTrace trace : sublog) {
                for (Iterator<XEvent> it = trace.iterator(); it.hasNext();) {
                        XEventClass c = sublog.classify(trace, it.next());
                        if (c.getId().equals("Call")) {
                                it.remove();
                        }
                }
            }
        }
        
        return sublog;
    }

}
