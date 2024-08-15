package org.processmining.algorithms.statechart.align.metric.impl;

import gnu.trove.set.hash.THashSet;

import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.processmining.algorithms.statechart.align.metric.AbstractResourceMetric;
import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.decorate.align.IntervalClusters;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public class ResourceCountMetric extends AbstractResourceMetric {

    public static final String Id = "ResCount";
    public static final String Name = "Resource Count";
    public static final String ShortName = "# Res.";
    public static final String DescriptionShort = "How many resources were used to perform the activity?";
    
    public ResourceCountMetric() {
        super(Id, Name, ShortName);
    }

    @Override
    public boolean computeForNode(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        SummaryStatistics stat = new SummaryStatistics();

        IntervalClusters clusters = decInt.getIntervalClusters(target, true, false);
        for (List<ExecInterval> cluster : clusters.getClusters()) {
            Set<String> values = new THashSet<>();
            for (ExecInterval ival : cluster) {
                String val = move2value(ival.getStart());
                if (val != null) {
                    values.add(val);
                }
                val = move2value(ival.getComplete());
                if (val != null) {
                    values.add(val);
                }
            }
            stat.addValue(values.size());
        }
        setMetric(decMetric, target, stat);
        return true;
    }

    private String move2value(XAlignmentMove move) {
        if (move != null) {
            XEvent event = move.getEvent();
            if (event != null) {
                XAttribute value = event.getAttributes().get(resourceAttribute);
                if (value != null) {
                    return value.toString();
                }
            }
        }
        return null;
    }
    
    @Override
    public boolean computeForEdge(IMetricsDecorator<IEPTreeNode> decMetric,
            Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        throw new UnsupportedOperationException(Name + " metric is not defined on edges");
    }
}
