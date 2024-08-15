package org.processmining.xes.statechart.xport;

import org.deckfour.xes.classification.XEventAttributeClassifier;

public class XEventCalleePointClassifier extends XEventAttributeClassifier {
    
    private static final long serialVersionUID = -2889992216593660847L;

    public XEventCalleePointClassifier() {
        super("Callee Joinpoint", new String[] {
                XApplocExtension.KEY_JOINPOINT,
                XApplocExtension.KEY_LINENR
        });
    }

}
