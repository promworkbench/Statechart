package org.processmining.ui.statechart.workbench.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.utils.statechart.generic.ListUtil;
import org.processmining.utils.statechart.generic.ToStringComparator;

public class WorkbenchLogUtil {

    private static final XEventClassifier[] DefaultClassifiers = new XEventClassifier[] {
            new XEventNameClassifier(), new XEventResourceClassifier(),
            new XEventLifeTransClassifier() };

    public static List<XEventClassifier> getAvailableClassifiers(XLog log) {
        return getAvailableClassifiers(log, true, true);
    }

    public static List<XEventClassifier> getAvailableClassifiers(XLog log,
            boolean includeDefault, boolean sortByName) {
        List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();

        if (log != null) {
            classifiers.addAll(log.getClassifiers());
        }

        if (includeDefault) {
            for (XEventClassifier c : DefaultClassifiers) {
                ListUtil.addIfNew(classifiers, c);
            }
        }

        if (sortByName) {
            Collections.sort(classifiers,
                    new ToStringComparator<XEventClassifier>());
        }

        return classifiers;
    }
}
