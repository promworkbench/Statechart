package org.processmining.xes.statechart.xport;

import org.deckfour.xes.classification.XEventAttributeClassifier;

public class XEventCallerPointClassifier extends XEventAttributeClassifier {
    
    private static final long serialVersionUID = 2649924898812833824L;

    public XEventCallerPointClassifier() {
        super("Caller Joinpoint", new String[] {
                XApplocExtension.KEY_CALLER_JOINPOINT,
                XApplocExtension.KEY_CALLER_LINENR,
                XApplocExtension.KEY_JOINPOINT
        });
    }

}
