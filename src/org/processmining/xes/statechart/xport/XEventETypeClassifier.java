package org.processmining.xes.statechart.xport;

import org.deckfour.xes.classification.XEventAttributeClassifier;

public class XEventETypeClassifier extends XEventAttributeClassifier {
    
    private static final long serialVersionUID = 3393244711285475783L;

    public XEventETypeClassifier() {
        super("Software Event Type", new String[] {
                XApplocExtension.KEY_ETYPE
        });
    }

}
