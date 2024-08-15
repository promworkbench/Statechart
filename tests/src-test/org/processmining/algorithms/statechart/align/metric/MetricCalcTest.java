package org.processmining.algorithms.statechart.align.metric;

import org.deckfour.xes.model.XLog;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.align.metric.impl.AbsFreqMetric;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.algorithms.statechart.m2m.EPTree2StatechartStates;
import org.processmining.models.statechart.decorate.align.metric.MetricLong;
import org.processmining.models.statechart.decorate.align.metric.MetricsRefDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricsTreeDecorator;
import org.processmining.models.statechart.eptree.EPTreeCreateUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.Statechart;

public class MetricCalcTest extends AbstractMetricTest {

    @Test
    public void testDelayedComputation() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, B)");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        
        _compute(log, model, metricAbs);
        MetricsTreeDecorator metrics = model.getDecorations().getForType(MetricsTreeDecorator.class);

        Assert.assertNotNull(metrics);
        Assert.assertNull(metrics.getDecoration(model.getNodeByLabel("A")));
        Assert.assertNull(metrics.getDecoration(model.getNodeByLabel("B")));
        Assert.assertNull(metrics.getDecoration(model.getNodeByIndex()));
        
        Assert.assertEquals(new MetricLong(1), metrics.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertNotNull(metrics.getDecoration(model.getNodeByLabel("A")));
        Assert.assertEquals(1, metrics.getDecoration(model.getNodeByLabel("A")).size());
        Assert.assertNull(metrics.getDecoration(model.getNodeByLabel("B")));
        Assert.assertNull(metrics.getDecoration(model.getNodeByIndex()));
        
        Assert.assertEquals(new MetricLong(1), metrics.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertNotNull(metrics.getDecoration(model.getNodeByLabel("A")));
        Assert.assertEquals(1, metrics.getDecoration(model.getNodeByLabel("A")).size());
        Assert.assertNotNull(metrics.getDecoration(model.getNodeByLabel("B")));
        Assert.assertEquals(1, metrics.getDecoration(model.getNodeByLabel("B")).size());
        Assert.assertNull(metrics.getDecoration(model.getNodeByIndex()));
        
        Assert.assertEquals(new MetricLong(1), metrics.getMetric(model.getNodeByIndex(), metricAbs.getId()));
        Assert.assertNotNull(metrics.getDecoration(model.getNodeByLabel("A")));
        Assert.assertEquals(1, metrics.getDecoration(model.getNodeByLabel("A")).size());
        Assert.assertNotNull(metrics.getDecoration(model.getNodeByLabel("B")));
        Assert.assertEquals(1, metrics.getDecoration(model.getNodeByLabel("B")).size());
        Assert.assertNotNull(metrics.getDecoration(model.getNodeByIndex()));
        Assert.assertEquals(1, metrics.getDecoration(model.getNodeByIndex()).size());
    }
    
    @Test
    public void testClonedComputation() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, B)");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        
        _compute(log, model, metricAbs);
        MetricsTreeDecorator metrics = model.getDecorations().getForType(MetricsTreeDecorator.class);
        
        IEPTree modelCopy = model.createCopy();
        Assert.assertNull(modelCopy.getDecorations().getForType(MetricsTreeDecorator.class));
        Assert.assertNotNull(modelCopy.getDecorations().getForType(MetricsRefDecorator.class));
        @SuppressWarnings("unchecked")
        MetricsRefDecorator<IEPTreeNode> metricsCopy = (MetricsRefDecorator<IEPTreeNode>) modelCopy.getDecorations().getForType(MetricsRefDecorator.class);

        Assert.assertNull(metrics.getDecoration(model.getNodeByLabel("A")));
        Assert.assertNull(metrics.getDecoration(model.getNodeByLabel("B")));
        Assert.assertNull(metrics.getDecoration(model.getNodeByIndex()));
        Assert.assertNull(metricsCopy.getDecoration(modelCopy.getNodeByLabel("A")));
        Assert.assertNull(metricsCopy.getDecoration(modelCopy.getNodeByLabel("B")));
        Assert.assertNull(metricsCopy.getDecoration(modelCopy.getNodeByIndex()));

        Assert.assertEquals(new MetricLong(1), metricsCopy.getMetric(modelCopy.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertNotNull(metrics.getDecoration(model.getNodeByLabel("A")));
        Assert.assertEquals(1, metrics.getDecoration(model.getNodeByLabel("A")).size());
        
//        Assert.assertNull(metricsCopy.getDecoration(model.getNodeByLabel("A"))); // model and modelCopy nodes have same id => same hash
        Assert.assertNotNull(metricsCopy.getDecoration(modelCopy.getNodeByLabel("A")));
        Assert.assertEquals(metrics.getDecoration(model.getNodeByLabel("A")), metricsCopy.getDecoration(modelCopy.getNodeByLabel("A")));
        
        Assert.assertNull(metrics.getDecoration(model.getNodeByLabel("B")));
        Assert.assertNull(metrics.getDecoration(model.getNodeByIndex()));
        Assert.assertNull(metricsCopy.getDecoration(modelCopy.getNodeByLabel("B")));
        Assert.assertNull(metricsCopy.getDecoration(modelCopy.getNodeByIndex()));
    }
    
    @Test
    public void testDerivedComputation1() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, B)");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        
        _compute(log, model, metricAbs);
        MetricsTreeDecorator metrics = model.getDecorations().getForType(MetricsTreeDecorator.class);

        // compute SC
        EPTree2StatechartStates m2m = new EPTree2StatechartStates();
        Statechart sc = m2m.apply(model);

        Assert.assertNull(sc.getStateDecorations().getForType(MetricsTreeDecorator.class));
        Assert.assertNotNull(sc.getStateDecorations().getForType(MetricsRefDecorator.class));

        @SuppressWarnings("unchecked")
        MetricsRefDecorator<ISCState> metricsRef = (MetricsRefDecorator<ISCState>) sc.getStateDecorations().getForType(MetricsRefDecorator.class);
        
        // check metric over SC nodes via ref.
        Assert.assertNull(metrics.getDecoration(model.getNodeByLabel("A")));
        Assert.assertNull(metrics.getDecoration(model.getNodeByLabel("B")));
        Assert.assertNull(metrics.getDecoration(model.getNodeByIndex()));
        Assert.assertNull(metricsRef.getDecoration(sc.getStateByLabel("A")));
        Assert.assertNull(metricsRef.getDecoration(sc.getStateByLabel("B")));

        Assert.assertEquals(new MetricLong(1), metricsRef.getMetric(sc.getStateByLabel("A"), metricAbs.getId()));
        Assert.assertNotNull(metrics.getDecoration(model.getNodeByLabel("A")));
        Assert.assertEquals(1, metrics.getDecoration(model.getNodeByLabel("A")).size());
        
        Assert.assertNotNull(metricsRef.getDecoration(sc.getStateByLabel("A")));
        Assert.assertEquals(metrics.getDecoration(model.getNodeByLabel("A")), metricsRef.getDecoration(sc.getStateByLabel("A")));
        
        Assert.assertNull(metrics.getDecoration(model.getNodeByLabel("B")));
        Assert.assertNull(metrics.getDecoration(model.getNodeByIndex()));
        Assert.assertNull(metricsRef.getDecoration(sc.getStateByLabel("B")));
    }
    
    @Test
    public void testDerivedComputation2() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
                {"A_start", "A_complete", "B_start", "B_complete"}
            });
            IEPTree model = EPTreeCreateUtil.create("->(A, B)");
            
            AbsFreqMetric metricAbs = new AbsFreqMetric();
            
            _compute(log, model, metricAbs);
            MetricsTreeDecorator metrics = model.getDecorations().getForType(MetricsTreeDecorator.class);

            // compute SC
            EPTree2StatechartStates m2m = new EPTree2StatechartStates();
            Statechart sc = m2m.apply(model).createCopy(); // NOTE: we have one more indirection, via a copy

            Assert.assertNull(sc.getStateDecorations().getForType(MetricsTreeDecorator.class));
            Assert.assertNotNull(sc.getStateDecorations().getForType(MetricsRefDecorator.class));

            @SuppressWarnings("unchecked")
            MetricsRefDecorator<ISCState> metricsRef = (MetricsRefDecorator<ISCState>) sc.getStateDecorations().getForType(MetricsRefDecorator.class);
            
            // check metric over SC nodes via ref.
            Assert.assertNull(metrics.getDecoration(model.getNodeByLabel("A")));
            Assert.assertNull(metrics.getDecoration(model.getNodeByLabel("B")));
            Assert.assertNull(metrics.getDecoration(model.getNodeByIndex()));
            Assert.assertNull(metricsRef.getDecoration(sc.getStateByLabel("A")));
            Assert.assertNull(metricsRef.getDecoration(sc.getStateByLabel("B")));

            Assert.assertEquals(new MetricLong(1), metricsRef.getMetric(sc.getStateByLabel("A"), metricAbs.getId()));
            Assert.assertNotNull(metrics.getDecoration(model.getNodeByLabel("A")));
            Assert.assertEquals(1, metrics.getDecoration(model.getNodeByLabel("A")).size());
            
            Assert.assertNotNull(metricsRef.getDecoration(sc.getStateByLabel("A")));
            Assert.assertEquals(metrics.getDecoration(model.getNodeByLabel("A")), metricsRef.getDecoration(sc.getStateByLabel("A")));
            
            Assert.assertNull(metrics.getDecoration(model.getNodeByLabel("B")));
            Assert.assertNull(metrics.getDecoration(model.getNodeByIndex()));
            Assert.assertNull(metricsRef.getDecoration(sc.getStateByLabel("B")));
    }
}
