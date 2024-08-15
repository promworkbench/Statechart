package org.processmining.xes.statechart.classification;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.model.XEvent;

import com.google.common.base.Preconditions;

public class XEventListLabelElementClassifier extends XEventListLabelClassifier {

    private static final long serialVersionUID = -8420158029772496158L;
    private final int level;

    public XEventListLabelElementClassifier(int level) {
        super();
        Preconditions.checkArgument(level >= 0, "Level cannot be negative");
        this.level = level;
    }

    public String getClassIdentity(XEvent event) {
        List<String> identities = getClassIdentityList(event);

        if (level < identities.size()) {
            return identities.get(level);
        } else {
            return StringUtils.EMPTY;
        }
    }
    
    public String getInstance(XEvent event) {
        List<String> instances = getInstanceList(event);

        if (level < instances.size()) {
            return instances.get(level);
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public int compareTo(XEventAttributeClassifier o) {
        int r = super.compareTo(o);
        if (r == 0) {
            if (o instanceof XEventListLabelElementClassifier) {
                return Integer.compare(this.level,
                        ((XEventListLabelElementClassifier) o).level);
            }
        }

        return r;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return String.format("%s @ %d", super.toString(), level);
    }
    
    @Override
    public int hashCode() {
        return level;
    }
}
