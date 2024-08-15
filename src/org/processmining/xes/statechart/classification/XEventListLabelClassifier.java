package org.processmining.xes.statechart.classification;

import java.util.List;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.model.XEvent;
import org.processmining.xes.statechart.extension.XListLabelExtension;

public class XEventListLabelClassifier extends XEventAttributeClassifier {

    private static final long serialVersionUID = -6154245835383294522L;
    private static final XListLabelExtension extListLabel = XListLabelExtension.instance();

    public XEventListLabelClassifier() {
        super("Event List Label ", new String[] { "listlabel:name" });
    }
    
    public List<String> getClassIdentityList(XEvent event) {
        return extListLabel.extractName(event);
    }
    
    public List<String> getInstanceList(XEvent event) {
        return extListLabel.extractInstance(event);
    }
}
