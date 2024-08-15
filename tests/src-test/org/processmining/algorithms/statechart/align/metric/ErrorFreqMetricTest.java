package org.processmining.algorithms.statechart.align.metric;

import java.util.Arrays;
import java.util.HashSet;

import org.deckfour.xes.model.XLog;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.align.metric.impl.AbsFreqMetric;
import org.processmining.algorithms.statechart.align.metric.impl.CaseFreqMetric;
import org.processmining.algorithms.statechart.align.metric.impl.ErrorFreqMetric;
import org.processmining.algorithms.statechart.discovery.im.cancellation.SetQueryCancelError;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricLong;
import org.processmining.models.statechart.decorate.align.metric.MetricsTreeDecorator;
import org.processmining.models.statechart.decorate.error.EPTreeErrorTriggerDecorator;
import org.processmining.models.statechart.eptree.EPTreeCreateUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public class ErrorFreqMetricTest extends AbstractMetricTest {

    @Test
    public void testNormal1() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete"},
            {"A_start", "A_complete", "B_start", "B_complete", "E_start", "E_complete", "F_start", "F_complete"},
            {"A_start", "A_complete", "E_start", "E_complete", "F_start", "F_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("SC(->(Er=A, Er=B, C), ->(E, F))");
        EPTreeErrorTriggerDecorator decorator = model.getDecorations().getForType(EPTreeErrorTriggerDecorator.class);
        if (decorator != null) {
            decorator = new EPTreeErrorTriggerDecorator();
            model.getDecorations().registerDecorator(decorator);
        }
        decorator.setDecoration(model.getNodeByLabel("A"), new HashSet<String>(Arrays.asList("E")));
        decorator.setDecoration(model.getNodeByLabel("B"), new HashSet<String>(Arrays.asList("E")));
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ErrorFreqMetric metricError = new ErrorFreqMetric();
        _compute(log, model, new SetQueryCancelError("E"), metricAbs, metricCase, metricError);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(3), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("E"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("F"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(3), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(3), decAlign.getMetric(model.getNodeByIndex(0), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(1), metricAbs.getId()));

        Assert.assertEquals(new MetricLong(3), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("E"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("F"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(3), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId()));
        Assert.assertEquals(new MetricLong(3), decAlign.getMetric(model.getNodeByIndex(0), metricCase.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(1), metricCase.getId()));

        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricError.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricError.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("C"), metricError.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("E"), metricError.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("F"), metricError.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricError.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0), metricError.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(1), metricError.getId()));
    }
}
