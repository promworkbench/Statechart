package org.processmining.algorithms.statechart.align.metric;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.align.metric.impl.DurationMetric;
import org.processmining.algorithms.statechart.align.metric.impl.OwnDurationMetric;
import org.processmining.algorithms.statechart.align.metric.impl.WaitDurationMetric;
import org.processmining.algorithms.statechart.align.metric.time.Event2TimeAttribute;
import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricStat;
import org.processmining.models.statechart.decorate.align.metric.MetricsTreeDecorator;
import org.processmining.models.statechart.eptree.EPTreeCreateUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public class TimeMetricTest extends AbstractMetricTest {

    private static final String KeyTime = "Time";
    private static final IEvent2Time event2time = new Event2TimeAttribute(KeyTime, 1);
    
    protected static void setTime(XEvent event, double time) {
        event.getAttributes().put(KeyTime, new XAttributeContinuousImpl(KeyTime, time));
    }

    private static final String KeyTime2 = "Time2";
    private static final IEvent2Time event2time2 = new Event2TimeAttribute(KeyTime2, 1);
    
    protected static void setTime2(XEvent event, double time) {
        event.getAttributes().put(KeyTime2, new XAttributeContinuousImpl(KeyTime2, time));
    }

    @Test
    public void testNested1TotalTime() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "C_start", "C_complete", "A_complete"} 
        });
        setTime(log.get(0).get(0), 1); // A
        setTime(log.get(0).get(1), 3); // B
        setTime(log.get(0).get(2), 4);
        setTime(log.get(0).get(3), 7); // C
        setTime(log.get(0).get(4), 9);
        setTime(log.get(0).get(5), 14); // A
        
        IEPTree model = EPTreeCreateUtil.create("\\/=A(->(B,C))");

        DurationMetric metricDuration = new DurationMetric(event2time);
        _compute(log, model, metricDuration);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        // Total Duration
        Assert.assertEquals(new MetricStat(14 - 1), decAlign.getMetric(model.getNodeByLabel("A"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(4 - 3), decAlign.getMetric(model.getNodeByLabel("B"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(9 - 7), decAlign.getMetric(model.getNodeByLabel("C"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(9 - 3), decAlign.getMetric(model.getNodeByIndex(0), metricDuration.getId()));

    }
    
    @Test
    public void testNested1OwnTime1() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "C_start", "C_complete", "A_complete"} 
        });
        setTime(log.get(0).get(0), 1); // A
        setTime(log.get(0).get(1), 3); // B
        setTime(log.get(0).get(2), 4);
        setTime(log.get(0).get(3), 7); // C
        setTime(log.get(0).get(4), 9);
        setTime(log.get(0).get(5), 14); // A
        
        IEPTree model = EPTreeCreateUtil.create("\\/=A(->(B,C))");

        OwnDurationMetric metricOwnDuration = new OwnDurationMetric(event2time);
        _compute(log, model, metricOwnDuration);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        // Own Duration
        Assert.assertEquals(new MetricStat((14 - 1) - (4 - 3) - (9 - 7)), decAlign.getMetric(model.getNodeByLabel("A"), metricOwnDuration.getId()));
        Assert.assertEquals(new MetricStat(4 - 3), decAlign.getMetric(model.getNodeByLabel("B"), metricOwnDuration.getId()));
        Assert.assertEquals(new MetricStat(9 - 7), decAlign.getMetric(model.getNodeByLabel("C"), metricOwnDuration.getId()));
        Assert.assertEquals(new MetricStat((9 - 3) - (4 - 3) - (9 - 7)), decAlign.getMetric(model.getNodeByIndex(0), metricOwnDuration.getId()));
    }
    
    @Test
    public void testNested1OwnTime2() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "C_start", "C_complete", "A_complete"},
            {"A_start", "B_start", "B_complete", "C_start", "C_complete", "A_complete"},
        });
        setTime(log.get(0).get(0), 1); // A
        setTime(log.get(0).get(1), 3); // B
        setTime(log.get(0).get(2), 4);
        setTime(log.get(0).get(3), 7); // C
        setTime(log.get(0).get(4), 9);
        setTime(log.get(0).get(5), 14); // A

        setTime(log.get(1).get(0), 2); // A
        setTime(log.get(1).get(1), 2); // B
        setTime(log.get(1).get(2), 4);
        setTime(log.get(1).get(3), 8); // C
        setTime(log.get(1).get(4), 11);
        setTime(log.get(1).get(5), 19); // A
        
        IEPTree model = EPTreeCreateUtil.create("\\/=A(->(B,C))");

        OwnDurationMetric metricOwnDuration = new OwnDurationMetric(event2time);
        _compute(log, model, metricOwnDuration);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        // Own Duration
        Assert.assertEquals(new MetricStat(
            (14 - 1) - (4 - 3) - (9 - 7),
            (19 - 2) - (4 - 2) - (11 - 8)
        ), decAlign.getMetric(model.getNodeByLabel("A"), metricOwnDuration.getId()));
        Assert.assertEquals(new MetricStat(
            4 - 3, 
            4 - 2
        ), decAlign.getMetric(model.getNodeByLabel("B"), metricOwnDuration.getId()));
        Assert.assertEquals(new MetricStat(
            9 - 7, 
            11 - 8
        ), decAlign.getMetric(model.getNodeByLabel("C"), metricOwnDuration.getId()));
        Assert.assertEquals(new MetricStat(
            (9 - 3) - (4 - 3) - (9 - 7),
            (11 - 2) - (4 - 2) - (11 - 8)
        ), decAlign.getMetric(model.getNodeByIndex(0), metricOwnDuration.getId()));
    }

    @Test
    public void testNested1WaitTime() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "C_start", "C_complete", "A_complete"} 
        });
        setTime(log.get(0).get(0), 1); // A
        setTime(log.get(0).get(1), 3); // B
        setTime(log.get(0).get(2), 4);
        setTime(log.get(0).get(3), 7); // C
        setTime(log.get(0).get(4), 9);
        setTime(log.get(0).get(5), 14); // A
        
        IEPTree model = EPTreeCreateUtil.create("\\/=A(->(B,C))");

        WaitDurationMetric metricWaitDuration = new WaitDurationMetric(event2time);
        _compute(log, model, metricWaitDuration);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        // Total Duration
        Assert.assertEquals(new MetricStat(0), decAlign.getMetric(model.getNodeByLabel("A"), metricWaitDuration.getId()));
        Assert.assertEquals(new MetricStat(3 - 1), decAlign.getMetric(model.getNodeByLabel("B"), metricWaitDuration.getId()));
        Assert.assertEquals(new MetricStat(7 - 4), decAlign.getMetric(model.getNodeByLabel("C"), metricWaitDuration.getId()));
        Assert.assertEquals(new MetricStat(3 - 1), decAlign.getMetric(model.getNodeByIndex(0), metricWaitDuration.getId()));

    }

    @Test
    public void testNested1Reset() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "C_start", "C_complete", "A_complete"} 
        });
        setTime(log.get(0).get(0), 1); // A
        setTime(log.get(0).get(1), 3); // B
        setTime(log.get(0).get(2), 4);
        setTime(log.get(0).get(3), 7); // C
        setTime(log.get(0).get(4), 9);
        setTime(log.get(0).get(5), 14); // A

        setTime2(log.get(0).get(0), 2); // A
        setTime2(log.get(0).get(1), 2); // B
        setTime2(log.get(0).get(2), 4);
        setTime2(log.get(0).get(3), 8); // C
        setTime2(log.get(0).get(4), 9);
        setTime2(log.get(0).get(5), 19); // A
        
        IEPTree model = EPTreeCreateUtil.create("\\/=A(->(B,C))");

        DurationMetric metricDuration = new DurationMetric(event2time);
        _compute(log, model, metricDuration);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricStat(14 - 1), decAlign.getMetric(model.getNodeByLabel("A"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(4 - 3), decAlign.getMetric(model.getNodeByLabel("B"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(9 - 7), decAlign.getMetric(model.getNodeByLabel("C"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(9 - 3), decAlign.getMetric(model.getNodeByIndex(0), metricDuration.getId()));
        
        metricDuration.setEvent2Time(event2time2);

        Assert.assertEquals(new MetricStat(19 - 2), decAlign.getMetric(model.getNodeByLabel("A"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(4 - 2), decAlign.getMetric(model.getNodeByLabel("B"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(9 - 8), decAlign.getMetric(model.getNodeByLabel("C"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(9 - 2), decAlign.getMetric(model.getNodeByIndex(0), metricDuration.getId()));
    }
    
    
    @Test
    public void testRecurse1() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", 
                "A_start", "B_start", "B_complete", 
                    "A_start", "D_start", "D_complete", "A_complete", 
                "C_start", "C_complete", "A_complete",
            "C_start", "C_complete", "A_complete"} 
        });
        setTime(log.get(0).get(0), 1); // A 1
        setTime(log.get(0).get(1), 3); // B
        setTime(log.get(0).get(2), 4); 
        setTime(log.get(0).get(3), 6); // A 2
        setTime(log.get(0).get(4), 9); // B
        setTime(log.get(0).get(5), 12); 
        setTime(log.get(0).get(6), 14); // A 3
        setTime(log.get(0).get(7), 17); // D
        setTime(log.get(0).get(8), 22); 
        setTime(log.get(0).get(9), 26); // A 3
        setTime(log.get(0).get(10), 27); // C
        setTime(log.get(0).get(11), 28); // C
        setTime(log.get(0).get(12), 34); // A 2
        setTime(log.get(0).get(13), 37); // C
        setTime(log.get(0).get(14), 39); // C
        setTime(log.get(0).get(15), 42); // A 1
        
        IEPTree model = EPTreeCreateUtil.create("\\/=A(x(->(B, R\\/=A,C), D))");
        
        DurationMetric metricDuration = new DurationMetric(event2time);
        _compute(log, model, metricDuration);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricStat(42 - 1), decAlign.getMetric(model.getNodeByIndex(), metricDuration.getId())); // A def
        Assert.assertEquals(new MetricStat(34 - 6, 26 - 14), decAlign.getMetric(model.getNodeByIndex(0,0,1), metricDuration.getId())); // A recurse
        Assert.assertEquals(new MetricStat(4 - 3, 12 - 9), decAlign.getMetric(model.getNodeByLabel("B"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(28 - 27, 39 - 37), decAlign.getMetric(model.getNodeByLabel("C"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(22 - 17), decAlign.getMetric(model.getNodeByLabel("D"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(39 - 3, 28 - 9, 22 - 17), decAlign.getMetric(model.getNodeByIndex(0), metricDuration.getId())); // x
        Assert.assertEquals(new MetricStat(39 - 3, 28 - 9), decAlign.getMetric(model.getNodeByIndex(0,0), metricDuration.getId())); // ->
    }
    
    @Test
    public void testNormal1() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"}
        });
        setTime(log.get(0).get(0), 1); // A
        setTime(log.get(0).get(1), 3);
        setTime(log.get(0).get(2), 4); // B
        setTime(log.get(0).get(3), 7);
        
        IEPTree model = EPTreeCreateUtil.create("->(A, B)");

        DurationMetric metricDuration = new DurationMetric(event2time);
        _compute(log, model, metricDuration);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricStat(3 - 1), decAlign.getMetric(model.getNodeByLabel("A"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(7 - 4), decAlign.getMetric(model.getNodeByLabel("B"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(7 - 1), decAlign.getMetric(model.getNodeByIndex(), metricDuration.getId()));
    }
    
    @Test
    public void testNormal1Infreq() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"},
            {"A_start", "A_complete"}
        });
        setTime(log.get(0).get(0), 1); // A
        setTime(log.get(0).get(1), 3);
        setTime(log.get(0).get(2), 4); // B
        setTime(log.get(0).get(3), 7);
        setTime(log.get(1).get(0), 21); // A
        setTime(log.get(1).get(1), 25);
        
        IEPTree model = EPTreeCreateUtil.create("->(A, B)");

        DurationMetric metricDuration = new DurationMetric(event2time);
        _compute(log, model, metricDuration);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricStat(3 - 1, 25 - 21), decAlign.getMetric(model.getNodeByLabel("A"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(7 - 4), decAlign.getMetric(model.getNodeByLabel("B"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(7 - 1, 25 - 21), decAlign.getMetric(model.getNodeByIndex(), metricDuration.getId()));
    }
    
    @Test
    public void testNormal1Noise() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "A_start", "A_complete", "C_start", "C_complete"}
        });
        setTime(log.get(0).get(0), 1); // first move on log A
        setTime(log.get(0).get(1), 3);
        setTime(log.get(0).get(2), 4); // A
        setTime(log.get(0).get(3), 7);
        setTime(log.get(0).get(4), 11); // C
        setTime(log.get(0).get(5), 17);
        IEPTree model = EPTreeCreateUtil.create("->(A, B, C)");
        
        DurationMetric metricDuration = new DurationMetric(event2time);
        _compute(log, model, metricDuration);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricStat(7 - 4), decAlign.getMetric(model.getNodeByLabel("A"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(model.getNodeByLabel("B"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(17 - 11), decAlign.getMetric(model.getNodeByLabel("C"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(17 - 4), decAlign.getMetric(model.getNodeByIndex(), metricDuration.getId()));
    }

    @Test
    public void testPar1() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "C_start", "C_complete", 
                "B_start", "B_complete", "D_start", "D_complete"}
        });
        setTime(log.get(0).get(0), 1); // A
        setTime(log.get(0).get(1), 3);
        setTime(log.get(0).get(4), 4); // B
        setTime(log.get(0).get(5), 7);
        
        setTime(log.get(0).get(2), 2); // C
        setTime(log.get(0).get(3), 3);
        setTime(log.get(0).get(6), 11); // D
        setTime(log.get(0).get(7), 17);
        
        IEPTree model = EPTreeCreateUtil.create("/\\(->(A, B), ->(C, D))");
        
        DurationMetric metricDuration = new DurationMetric(event2time);
        _compute(log, model, metricDuration);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricStat(3 - 1), decAlign.getMetric(model.getNodeByLabel("A"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(7 - 4), decAlign.getMetric(model.getNodeByLabel("B"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(3 - 2), decAlign.getMetric(model.getNodeByLabel("C"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(17 - 11), decAlign.getMetric(model.getNodeByLabel("D"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(17 - 1), decAlign.getMetric(model.getNodeByIndex(), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(7 - 1), decAlign.getMetric(model.getNodeByIndex(0), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(17 - 2), decAlign.getMetric(model.getNodeByIndex(1), metricDuration.getId()));
    }

    @Test
    public void testPar2() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "C_start", "C_complete", 
                "B_start", "B_complete", "D_start", "D_complete"}
        });
        setTime(log.get(0).get(0), 1); // A
        setTime(log.get(0).get(1), 3);
        setTime(log.get(0).get(4), 4); // B
        setTime(log.get(0).get(5), 7);
        
        setTime(log.get(0).get(2), 2); // C
        setTime(log.get(0).get(3), 3);
        setTime(log.get(0).get(6), 4); // D
        setTime(log.get(0).get(7), 6);
        
        IEPTree model = EPTreeCreateUtil.create("/\\(->(A, B), ->(C, D))");
        
        DurationMetric metricDuration = new DurationMetric(event2time);
        _compute(log, model, metricDuration);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricStat(3 - 1), decAlign.getMetric(model.getNodeByLabel("A"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(7 - 4), decAlign.getMetric(model.getNodeByLabel("B"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(3 - 2), decAlign.getMetric(model.getNodeByLabel("C"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(6 - 4), decAlign.getMetric(model.getNodeByLabel("D"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(7 - 1), decAlign.getMetric(model.getNodeByIndex(), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(7 - 1), decAlign.getMetric(model.getNodeByIndex(0), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(6 - 2), decAlign.getMetric(model.getNodeByIndex(1), metricDuration.getId()));
    }

    @Test
    public void testLoop1() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", 
                "A_start", "A_complete", "B_start", "B_complete"}
        });
        setTime(log.get(0).get(0), 1); // A
        setTime(log.get(0).get(1), 3);
        setTime(log.get(0).get(2), 4); // B
        setTime(log.get(0).get(3), 5);
        setTime(log.get(0).get(4), 8); // A
        setTime(log.get(0).get(5), 12);
        setTime(log.get(0).get(6), 15); // B
        setTime(log.get(0).get(7), 19);
        
        IEPTree model = EPTreeCreateUtil.create("<->(->(A, B), tau)");

        DurationMetric metricDuration = new DurationMetric(event2time);
        _compute(log, model, metricDuration);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricStat(3 - 1, 12 - 8), decAlign.getMetric(model.getNodeByLabel("A"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(5 - 4, 19 - 15), decAlign.getMetric(model.getNodeByLabel("B"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(model.getNodeByLabel("tau"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(19 - 1), decAlign.getMetric(model.getNodeByIndex(), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(5 - 1, 19 - 8), decAlign.getMetric(model.getNodeByIndex(0), metricDuration.getId()));
    }

    
    @Test
    public void testLoop2() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "A_start", "A_complete"}
        });
        setTime(log.get(0).get(0), 1); // A
        setTime(log.get(0).get(1), 3);
        setTime(log.get(0).get(2), 7); // A
        setTime(log.get(0).get(3), 12);
        IEPTree model = EPTreeCreateUtil.create("<->(A, tau)");
        
        DurationMetric metricDuration = new DurationMetric(event2time);
        _compute(log, model, metricDuration);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricStat(3 - 1, 12 - 7), decAlign.getMetric(model.getNodeByLabel("A"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(model.getNodeByLabel("tau"), metricDuration.getId()));
        Assert.assertEquals(new MetricStat(12 - 1), decAlign.getMetric(model.getNodeByIndex(), metricDuration.getId()));
    }
}
