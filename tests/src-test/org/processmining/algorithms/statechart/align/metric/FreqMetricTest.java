package org.processmining.algorithms.statechart.align.metric;

import java.util.Collections;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.align.metric.impl.AbsFreqMetric;
import org.processmining.algorithms.statechart.align.metric.impl.CaseFreqMetric;
import org.processmining.algorithms.statechart.align.metric.impl.ModelFreqMetric;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricLong;
import org.processmining.models.statechart.decorate.align.metric.MetricsTreeDecorator;
import org.processmining.models.statechart.eptree.EPTreeCreateUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public class FreqMetricTest extends AbstractMetricTest {

    @Test
    public void testNormal1() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, B)");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId()));
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId()));
        
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("A"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("B"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricModel.getId()));
    }
    
    @Test
    public void testNormal1Infreq() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"},
            {"A_start", "A_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, B)");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId()));
        
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId()));
        
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("A"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricModel.getId()));
    }

    @Test
    public void testNormal1Noise() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "A_start", "A_complete", "C_start", "C_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, B, C)");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        // Note: What about Log moves, where are they modeled?
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId()));

        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId()));

        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("A"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("C"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricModel.getId()));
    }
    
    @Test
    public void testPar1() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "C_start", "C_complete", 
                "B_start", "B_complete", "D_start", "D_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("/\\(->(A, B), ->(C, D))");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("D"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(1), metricAbs.getId()));
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("D"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(1), metricCase.getId()));
        
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("A"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("B"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("C"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("D"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(1), metricModel.getId()));
    }

    @Test
    public void testPar1Infreq() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("/\\(->(A, B), ->(C, D))");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("D"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(1), metricAbs.getId()));
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("C"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("D"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0), metricCase.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(1), metricCase.getId()));
        
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("A"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("B"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("D"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0), metricModel.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(1), metricModel.getId()));
    }
    
    @Test
    public void testPar1Infreq2() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", 
                "C_start", "C_complete", "D_start", "D_complete"},
            {"A_start", "A_complete", "B_start", "B_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("/\\(->(A, B), ->(C, D))");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("D"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(0), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(1), metricAbs.getId()));

        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("D"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(0), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(1), metricCase.getId()));

        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("A"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("B"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("D"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0), metricModel.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(1), metricModel.getId()));
    }
    
    @Test
    public void testLoop1() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", 
                "A_start", "A_complete", "B_start", "B_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("<->(->(A, B), tau)");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("tau"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(0), metricAbs.getId()));
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("tau"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0), metricCase.getId()));
        
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("A"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("B"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("tau"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0), metricModel.getId()));
    }
    
    @Test
    public void testLoop2() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "A_start", "A_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("<->(A, tau)");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("tau"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId()));

        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("tau"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId()));

        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("A"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("tau"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricModel.getId()));
    }
    
    
    @Test
    public void testMix1() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", 
                "C_start", "C_complete", "B_start", "B_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, <->(B, C))");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(1), metricAbs.getId()));
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(1), metricCase.getId()));
        
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("A"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("B"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("C"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(1), metricModel.getId()));
    }

    @Test
    public void testMix2() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "D_start", "D_complete", "B_start", "B_complete", 
                "C_start", "C_complete", "B_start", "B_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("/\\(->(A, <->(B, C)), D)");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("D"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0,1), metricAbs.getId()));
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("D"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0,1), metricCase.getId()));
        
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("A"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("B"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("C"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("D"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0,1), metricModel.getId()));
    }

    @Test
    public void testMix3() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", 
                "A_start", "A_complete", "B_start", "B_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("<->(->(A, B), C)");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(0), metricAbs.getId()));
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0), metricCase.getId()));
        
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("A"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("B"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("C"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0), metricModel.getId()));
    }

    @Test
    public void testMix4() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", 
                "A_start", "A_complete", "C_start", "C_complete", "B_start", "B_complete"}
        });
        IEPTree model = EPTreeCreateUtil.create("<->(/\\(->(A, B), C), tau)");
        
        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(0), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(0,0), metricAbs.getId()));
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0,0), metricCase.getId()));
        
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("A"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("B"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("C"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0,0), metricModel.getId()));
    }
    
    @Test
    public void testNested1() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "C_start", "C_complete", "A_complete"} 
        });
        IEPTree model = EPTreeCreateUtil.create("\\/=A(->(B,C))");

        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0), metricAbs.getId()));

        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("A"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0), metricCase.getId()));

        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("A"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("B"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("C"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0), metricModel.getId()));
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
        IEPTree model = EPTreeCreateUtil.create("\\/=A(x(->(B, R\\/=A,C), D))");

        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        ModelFreqMetric metricModel = new ModelFreqMetric();
        _compute(log, model, metricAbs, metricCase, metricModel);

        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId())); // A def
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(0,0,1), metricAbs.getId())); // A recurse
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("D"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(3), decAlign.getMetric(model.getNodeByIndex(0), metricAbs.getId())); // x
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByIndex(0,0), metricAbs.getId())); // ->

        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(), metricCase.getId())); // A def
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0,0,1), metricCase.getId())); // A recurse
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("B"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("D"), metricCase.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0), metricCase.getId())); // x
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByIndex(0,0), metricCase.getId())); // ->

        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(), metricModel.getId())); // A def
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0,0,1), metricModel.getId())); // A recurse
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("B"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("C"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByLabel("D"), metricModel.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0), metricModel.getId())); // x
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(model.getNodeByIndex(0,0), metricModel.getId())); // ->
    }
    
    @Test
    public void testArcs() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", "D_start", "D_complete" },
            {"A_start", "A_complete", "B_start", "B_complete", "D_start", "D_complete" },
            {"A_start", "A_complete", "C_start", "C_complete", "D_start", "D_complete" }
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, x(B, C), D)");

        AbsFreqMetric metricAbs = new AbsFreqMetric();
        CaseFreqMetric metricCase = new CaseFreqMetric();
        _compute(log, model, metricAbs, metricCase);
        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(model);
        Assert.assertNotNull(decAlign);
        
        // Nodes
        Assert.assertEquals(new MetricLong(3), decAlign.getMetric(model.getNodeByLabel("A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(model.getNodeByLabel("B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(model.getNodeByLabel("C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(3), decAlign.getMetric(model.getNodeByLabel("D"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(3), decAlign.getMetric(model.getNodeByIndex(), metricAbs.getId())); // ->
        Assert.assertEquals(new MetricLong(3), decAlign.getMetric(model.getNodeByIndex(0), metricAbs.getId())); // x

        // Existing arcs
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(_edgeN(model, "A"), _edgeN(model, "B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(_edgeN(model, "A"), _edgeN(model, "C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(2), decAlign.getMetric(_edgeN(model, "B"), _edgeN(model, "D"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(_edgeN(model, "C"), _edgeN(model, "D"), metricAbs.getId()));
        
        // Non-existing arcs
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(_edgeN(model, "B"), _edgeN(model, "C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(_edgeN(model, "C"), _edgeN(model, "B"), metricAbs.getId()));
        
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(_edgeN(model, "A"), _edgeN(model, "D"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(_edgeN(model, "D"), _edgeN(model, "A"), metricAbs.getId()));
        
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(_edgeN(model, "B"), _edgeN(model, "A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(_edgeN(model, "C"), _edgeN(model, "A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(_edgeN(model, "D"), _edgeN(model, "B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(_edgeN(model, "D"), _edgeN(model, "C"), metricAbs.getId()));
        
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(Collections.<IEPTreeNode>emptySet(), _edgeN(model, "A"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(Collections.<IEPTreeNode>emptySet(), _edgeN(model, "B"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(Collections.<IEPTreeNode>emptySet(), _edgeN(model, "C"), metricAbs.getId()));
        Assert.assertEquals(new MetricLong(0), decAlign.getMetric(Collections.<IEPTreeNode>emptySet(), _edgeN(model, "D"), metricAbs.getId()));
    }
    
    private Set<IEPTreeNode> _edgeN(IEPTree model, String key) {
        return Collections.singleton(model.getNodeByLabel(key));
    }
}
