package org.processmining.ui.statechart.workbench.integration;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.algorithms.statechart.align.metric.IMetric;
import org.processmining.algorithms.statechart.align.metric.MetricId;
import org.processmining.algorithms.statechart.align.metric.impl.DurationMetric;
import org.processmining.algorithms.statechart.align.metric.impl.WaitDurationMetric;
import org.processmining.algorithms.statechart.align.metric.time.Event2TimeNanotime;
import org.processmining.algorithms.statechart.align.metric.time.Event2TimeTimestamp;
import org.processmining.algorithms.statechart.discovery.EPTreeCompareSame;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.algorithms.statechart.m2m.ui.Statechart2DotSvg;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricLong;
import org.processmining.models.statechart.decorate.align.metric.MetricStat;
import org.processmining.models.statechart.decorate.align.metric.MetricValue;
import org.processmining.models.statechart.decorate.align.metric.MetricsRefDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricsTreeDecorator;
import org.processmining.models.statechart.eptree.EPTreeCreateUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.log.HierarchyActivityInfo;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.ISCTransition;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.recipes.statechart.AbstractRecipe;
import org.processmining.recipes.statechart.RecipeProcess.GetArtifactMode;
import org.processmining.recipes.statechart.SwitchArtifactRecipe;
import org.processmining.recipes.statechart.align.AlignLogFilterRecipe;
import org.processmining.recipes.statechart.align.AlignLogRecipe;
import org.processmining.recipes.statechart.align.AlignTreePostprocessingRecipe;
import org.processmining.recipes.statechart.align.AlignTreeRecipe;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlayManager;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlayManager.Overlays;
import org.processmining.recipes.statechart.align.Log2LogAlignRecipe;
import org.processmining.recipes.statechart.discovery.DiscoverEPTreeRecipe;
import org.processmining.recipes.statechart.discovery.Log2IMLogRecipe;
import org.processmining.recipes.statechart.discovery.LogFilterRecipe;
import org.processmining.recipes.statechart.l2l.AbstractL2LRecipe;
import org.processmining.recipes.statechart.l2l.L2LAttributeListRecipe;
import org.processmining.recipes.statechart.l2l.L2LNestedCallsRecipe;
import org.processmining.recipes.statechart.log.ActivityInfoRecipe;
import org.processmining.recipes.statechart.m2m.EPTree2StatechartRecipe;
import org.processmining.recipes.statechart.m2m.EPTreePostprocessRecipe;
import org.processmining.recipes.statechart.m2m.StatechartPostprocessRecipe;
import org.processmining.statechart.testutils.IntegrationTestUtil;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.xes.statechart.XesCompareSame;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

public class WorkbenchAlignIntegrationTest {

    protected static final XSubtraceExtension extSubtrace = XSubtraceExtension.instance();
    protected static final XTimeExtension time = XTimeExtension.instance();
    
    protected static void setTime(XEvent event, long timeMillis) {
        time.assignTimestamp(event, timeMillis);
    }
    
    @BeforeClass
    public static void init() throws Throwable {
        IntegrationTestUtil.initializeProMWithRequiredPackages("LpSolve");
    }
    
    @Test
    public void testSimple() {
        // test simple A, B, C log tree metrics using workbench recipes
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A", "B", "C"} 
        });
        setTime(input.get(0).get(0), 1); // A
        setTime(input.get(0).get(1), 3); // B
        setTime(input.get(0).get(2), 4); // C
        L2LAttributeListRecipe preprocessRecipe = new L2LAttributeListRecipe();
        preprocessRecipe.getParameters().clsList.add(new XEventNameClassifier());

        // compute
        AnalysisAlignMetricOverlayManager overlayManager = new AnalysisAlignMetricOverlayManager();
        WorkbenchModel model = integrationTestRun(input, preprocessRecipe, null, new DiscoverEPTreeRecipe(), overlayManager);
        
        // check log
        XLog expectedLog = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete"} 
        });
        setTime(expectedLog.get(0).get(0), 1);
        setTime(expectedLog.get(0).get(1), 1);
        setTime(expectedLog.get(0).get(2), 3);
        setTime(expectedLog.get(0).get(3), 3);
        setTime(expectedLog.get(0).get(4), 4);
        setTime(expectedLog.get(0).get(5), 4);
        XLog actualLog = model.getArtifact(WorkbenchArtifacts.LogPre, GetArtifactMode.GetOnly);
        Assert.assertTrue(XesCompareSame.same(expectedLog, actualLog));

        // check model
        IEPTree expectedModel = EPTreeCreateUtil.create("->(A, B, C)");
        IEPTree actualModel = model.getArtifact(WorkbenchArtifacts.EPTree, GetArtifactMode.GetOnly);
        Assert.assertNotNull(actualModel);
        Assert.assertTrue(EPTreeCompareSame.same(expectedModel, actualModel));

        actualModel = model.getArtifact(WorkbenchArtifacts.EPTreePost, GetArtifactMode.GetOnly);
        Assert.assertNotNull(actualModel);
        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(actualModel);
        Assert.assertNotNull(decAlign);
        
        // check freq metric
        IMetric metricAbsFreq = overlayManager.get(Overlays.AbsFreq).getPrimaryMetric();
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(actualModel.getNodeByLabel("A"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(actualModel.getNodeByLabel("B"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(actualModel.getNodeByLabel("C"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(actualModel.getNodeByIndex(), metricAbsFreq.getId()));
        
        // check time metric
        DurationMetric metricTime = (DurationMetric) overlayManager.get(Overlays.DutationTotal).getPrimaryMetric();
        metricTime.setEvent2Time(new Event2TimeTimestamp());
        Assert.assertEquals(new MetricStat(0), decAlign.getMetric(actualModel.getNodeByLabel("A"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(0), decAlign.getMetric(actualModel.getNodeByLabel("B"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(0), decAlign.getMetric(actualModel.getNodeByLabel("C"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(4 - 1), decAlign.getMetric(actualModel.getNodeByIndex(), metricTime.getId())); 
        
        // check nano time metric
        metricTime.setEvent2Time(new Event2TimeNanotime());
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(actualModel.getNodeByLabel("A"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(actualModel.getNodeByLabel("B"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(actualModel.getNodeByLabel("C"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(actualModel.getNodeByIndex(), metricTime.getId())); 
    }
    
    @Test
    public void testSimpleLC() {
        // test simple A, B, C start/complete log tree metrics using workbench recipes
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete"} 
        });
        setTime(input.get(0).get(0), 1); // A
        setTime(input.get(0).get(1), 3); 
        setTime(input.get(0).get(2), 4); // B
        setTime(input.get(0).get(3), 7); 
        setTime(input.get(0).get(4), 9); // C
        setTime(input.get(0).get(5), 14);
        L2LAttributeListRecipe preprocessRecipe = new L2LAttributeListRecipe();
        preprocessRecipe.getParameters().clsList.add(new XEventNameClassifier());

        // compute
        AnalysisAlignMetricOverlayManager overlayManager = new AnalysisAlignMetricOverlayManager();
        WorkbenchModel model = integrationTestRun(input, preprocessRecipe, null, new DiscoverEPTreeRecipe(), overlayManager);
        
        // check log
        XLog expectedLog = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete"} 
        });
        setTime(expectedLog.get(0).get(0), 1);
        setTime(expectedLog.get(0).get(1), 3);
        setTime(expectedLog.get(0).get(2), 4);
        setTime(expectedLog.get(0).get(3), 7);
        setTime(expectedLog.get(0).get(4), 9);
        setTime(expectedLog.get(0).get(5), 14);
        XLog actualLog = model.getArtifact(WorkbenchArtifacts.LogPre, GetArtifactMode.GetOnly);
        Assert.assertTrue(XesCompareSame.same(expectedLog, actualLog));

        // check model
        IEPTree expectedModel = EPTreeCreateUtil.create("->(A, B, C)");
        IEPTree actualModel = model.getArtifact(WorkbenchArtifacts.EPTree, GetArtifactMode.GetOnly);
        Assert.assertNotNull(actualModel);
        Assert.assertTrue(EPTreeCompareSame.same(expectedModel, actualModel));

        actualModel = model.getArtifact(WorkbenchArtifacts.EPTreePost, GetArtifactMode.GetOnly);
        Assert.assertNotNull(actualModel);
        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(actualModel);
        Assert.assertNotNull(decAlign);
        
        // check freq metric
        IMetric metricAbsFreq = overlayManager.get(Overlays.AbsFreq).getPrimaryMetric(); 
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(actualModel.getNodeByLabel("A"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(actualModel.getNodeByLabel("B"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(actualModel.getNodeByLabel("C"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(actualModel.getNodeByIndex(), metricAbsFreq.getId()));
        
        // check time metric
        DurationMetric metricTime = (DurationMetric) overlayManager.get(Overlays.DutationTotal).getPrimaryMetric();
        metricTime.setEvent2Time(new Event2TimeTimestamp());
        Assert.assertEquals(new MetricStat(3 - 1), decAlign.getMetric(actualModel.getNodeByLabel("A"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(7 - 4), decAlign.getMetric(actualModel.getNodeByLabel("B"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(14 - 9), decAlign.getMetric(actualModel.getNodeByLabel("C"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(14 - 1), decAlign.getMetric(actualModel.getNodeByIndex(), metricTime.getId())); 

        // check nano time metric
        metricTime.setEvent2Time(new Event2TimeNanotime());
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(actualModel.getNodeByLabel("A"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(actualModel.getNodeByLabel("B"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(actualModel.getNodeByLabel("C"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(actualModel.getNodeByIndex(), metricTime.getId())); 
    }
    
    @Test
    public void testNestedCalls() {
        // test simple sw log tree metrics using workbench recipes
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "C_start", "C_complete", "A_complete"} 
        });
        setTime(input.get(0).get(0), 1); // A
        setTime(input.get(0).get(1), 3); // B
        setTime(input.get(0).get(2), 4);
        setTime(input.get(0).get(3), 7); // C
        setTime(input.get(0).get(4), 9);
        setTime(input.get(0).get(5), 14); // A
        L2LNestedCallsRecipe preprocessRecipe = new L2LNestedCallsRecipe();
        
        // compute
        AnalysisAlignMetricOverlayManager overlayManager = new AnalysisAlignMetricOverlayManager();
        WorkbenchModel model = integrationTestRun(input, preprocessRecipe, null, new DiscoverEPTreeRecipe(), overlayManager);
        
        // check log
        XLog expectedLog = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete"} 
        });
        setTime(expectedLog.get(0).get(0), 1);
        setTime(expectedLog.get(0).get(1), 14);
        {
            XTrace trace = expectedLog.get(0);
            XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
                "B_start", "B_complete", "C_start", "C_complete"
            });
            setTime(sub1.get(0), 3); // B
            setTime(sub1.get(1), 4);
            setTime(sub1.get(2), 7); // C
            setTime(sub1.get(3), 9);
            extSubtrace.assignSubtrace(trace.get(0), sub1);
        }
        XLog actualLog = model.getArtifact(WorkbenchArtifacts.LogPre, GetArtifactMode.GetOnly);
        Assert.assertTrue(XesCompareSame.same(expectedLog, actualLog));

        // check model
        IEPTree expectedModel = EPTreeCreateUtil.create("\\/=A(->(B, C))");
        IEPTree actualModel = model.getArtifact(WorkbenchArtifacts.EPTree, GetArtifactMode.GetOnly);
        Assert.assertNotNull(actualModel);
        Assert.assertTrue(EPTreeCompareSame.same(expectedModel, actualModel));

        actualModel = model.getArtifact(WorkbenchArtifacts.EPTreePost, GetArtifactMode.GetOnly);
        Assert.assertNotNull(actualModel);
        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(actualModel);
        Assert.assertNotNull(decAlign);
        
        // check freq metric
        IMetric metricAbsFreq = overlayManager.get(Overlays.AbsFreq).getPrimaryMetric(); 
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(actualModel.getNodeByLabel("A"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(actualModel.getNodeByLabel("B"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(actualModel.getNodeByLabel("C"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(1), decAlign.getMetric(actualModel.getNodeByIndex(0), metricAbsFreq.getId()));
        
        // check time metric
        DurationMetric metricTime = (DurationMetric) overlayManager.get(Overlays.DutationTotal).getPrimaryMetric();
        metricTime.setEvent2Time(new Event2TimeTimestamp());
        Assert.assertEquals(new MetricStat(14 - 1), decAlign.getMetric(actualModel.getNodeByLabel("A"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(4 - 3), decAlign.getMetric(actualModel.getNodeByLabel("B"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(9 - 7), decAlign.getMetric(actualModel.getNodeByLabel("C"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(9 - 3), decAlign.getMetric(actualModel.getNodeByIndex(0), metricTime.getId())); 

        // check nano time metric
        metricTime.setEvent2Time(new Event2TimeNanotime());
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(actualModel.getNodeByLabel("A"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(actualModel.getNodeByLabel("B"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(actualModel.getNodeByLabel("C"), metricTime.getId()));
        Assert.assertEquals(new MetricStat(), decAlign.getMetric(actualModel.getNodeByIndex(0), metricTime.getId())); 
    }

    @Test
    public void testNestedCallsMany() {
        int NumTraces = 2;
        XFactory f = LogFactory.getFactory();
        XLog input = f.createLog();
        for (int i = 0; i < NumTraces; i++) {
            input.add(LogCreateTestUtil.createTraceFlat(new String[] 
                {"A_start", "B_start", "B_complete", "C_start", "C_complete", "A_complete"} 
            ));
            input.add(LogCreateTestUtil.createTraceFlat(new String[] 
                {"A_start", "B_start", "B_complete", "D_start", "D_complete", "A_complete"} 
            ));
            input.add(LogCreateTestUtil.createTraceFlat(new String[] 
                {"A_start", "E_start", "E_complete", "F_start", "F_complete", "A_complete"} 
            ));
            input.add(LogCreateTestUtil.createTraceFlat(new String[] 
                {"A_start", "E_start", "E_complete", "F_start", "F_complete", "G_start", "G_complete", "A_complete"} 
            ));
        }
        L2LNestedCallsRecipe preprocessRecipe = new L2LNestedCallsRecipe();
        
        // compute
        AnalysisAlignMetricOverlayManager overlayManager = new AnalysisAlignMetricOverlayManager();
        WorkbenchModel model = integrationTestRun(input, preprocessRecipe, null, new DiscoverEPTreeRecipe(), overlayManager);
        
        // check log

        XLog expectedLog = f.createLog();
        for (int i = 0; i < NumTraces; i++) {
            {
                XTrace trace = LogCreateTestUtil.createTraceFlat(new String[] { 
                    "A_start", "A_complete"
                });
                XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
                    "B_start", "B_complete", "C_start", "C_complete"
                });
                extSubtrace.assignSubtrace(trace.get(0), sub1);
                expectedLog.add(trace);
            }
            {
                XTrace trace = LogCreateTestUtil.createTraceFlat(new String[] { 
                    "A_start", "A_complete"
                });
                XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
                    "B_start", "B_complete", "D_start", "D_complete"
                });
                extSubtrace.assignSubtrace(trace.get(0), sub1);
                expectedLog.add(trace);
            }
            {
                XTrace trace = LogCreateTestUtil.createTraceFlat(new String[] { 
                    "A_start", "A_complete"
                });
                XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
                    "E_start", "E_complete", "F_start", "F_complete"
                });
                extSubtrace.assignSubtrace(trace.get(0), sub1);
                expectedLog.add(trace);
            }
            {
                XTrace trace = LogCreateTestUtil.createTraceFlat(new String[] { 
                    "A_start", "A_complete"
                });
                XTrace sub1 = LogCreateTestUtil.createTraceFlat(new String[] { 
                    "E_start", "E_complete", "F_start", "F_complete", "G_start", "G_complete"
                });
                extSubtrace.assignSubtrace(trace.get(0), sub1);
                expectedLog.add(trace);
            }
        }
        XLog actualLog = model.getArtifact(WorkbenchArtifacts.LogPre, GetArtifactMode.GetOnly);
        Assert.assertTrue(XesCompareSame.same(expectedLog, actualLog));

        // check model
        IEPTree expectedModel = EPTreeCreateUtil.create("\\/=A(x(->(B, x(C, D)), ->(E, F, x(tau, G))))");
        IEPTree actualModel = model.getArtifact(WorkbenchArtifacts.EPTree, GetArtifactMode.GetOnly);
        Assert.assertNotNull(actualModel);
        Assert.assertTrue("Expected: " + expectedModel.toString() + "\nActual:" + actualModel.toString(),
            EPTreeCompareSame.same(expectedModel, actualModel));

        actualModel = model.getArtifact(WorkbenchArtifacts.EPTreePost, GetArtifactMode.GetOnly);
        Assert.assertNotNull(actualModel);
        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(actualModel);
        Assert.assertNotNull(decAlign);
        
        // check freq metric
        IMetric metricAbsFreq = overlayManager.get(Overlays.AbsFreq).getPrimaryMetric(); 
        Assert.assertEquals(new MetricLong(NumTraces * 4), decAlign.getMetric(actualModel.getNodeByLabel("A"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(NumTraces * 2), decAlign.getMetric(actualModel.getNodeByLabel("B"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(NumTraces), decAlign.getMetric(actualModel.getNodeByLabel("C"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(NumTraces), decAlign.getMetric(actualModel.getNodeByLabel("D"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(NumTraces * 2), decAlign.getMetric(actualModel.getNodeByLabel("E"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(NumTraces * 2), decAlign.getMetric(actualModel.getNodeByLabel("F"), metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(NumTraces), decAlign.getMetric(actualModel.getNodeByLabel("G"), metricAbsFreq.getId()));
//        Assert.assertEquals(new MetricLong(NumTraces), decAlign.getMetric(actualModel.getNodeByIndex(0), metricAbsFreq.getId()));
    }
    

    @Test
    public void testMetricInTargetModel() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", "D_start", "D_complete"},
            {"A_start", "A_complete", "B_start", "B_complete", "E_start", "E_complete"},
            {"A_start", "A_complete", "C_start", "C_complete", "E_start", "E_complete"},
            
            {"A_start", "A_complete", "C_start", "C_complete", "D_start", "D_complete"},
            {"A_start", "A_complete", "C_start", "C_complete", "E_start", "E_complete"},
        });
        // Trace 1
        setTime(input.get(0).get(0), 1); // A
        setTime(input.get(0).get(1), 2); 
        setTime(input.get(0).get(2), 4); // B
        setTime(input.get(0).get(3), 6); 
        setTime(input.get(0).get(4), 9); // D
        setTime(input.get(0).get(5), 12);
        // Trace 2
        setTime(input.get(1).get(0), 1); // A
        setTime(input.get(1).get(1), 2); 
        setTime(input.get(1).get(2), 4); // B
        setTime(input.get(1).get(3), 6); 
        setTime(input.get(1).get(4), 10); // E
        setTime(input.get(1).get(5), 14);
        // Trace 3
        setTime(input.get(2).get(0), 1); // A
        setTime(input.get(2).get(1), 2); 
        setTime(input.get(2).get(2), 4); // C
        setTime(input.get(2).get(3), 6); 
        setTime(input.get(2).get(4), 10); // E
        setTime(input.get(2).get(5), 14);
        
        L2LAttributeListRecipe preprocessRecipe = new L2LAttributeListRecipe();
        preprocessRecipe.getParameters().clsList.add(new XEventNameClassifier());

        // compute
        AnalysisAlignMetricOverlayManager overlayManager = new AnalysisAlignMetricOverlayManager();
        WorkbenchModel model = integrationTestRun(input, preprocessRecipe, null, new DiscoverEPTreeRecipe(), overlayManager);

        // check log
        XLog expectedLog = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", "D_start", "D_complete"},
            {"A_start", "A_complete", "B_start", "B_complete", "E_start", "E_complete"},
            {"A_start", "A_complete", "C_start", "C_complete", "E_start", "E_complete"},
            
            {"A_start", "A_complete", "C_start", "C_complete", "D_start", "D_complete"},
            {"A_start", "A_complete", "C_start", "C_complete", "E_start", "E_complete"},
        });
        // Trace 1
        setTime(expectedLog.get(0).get(0), 1); // A
        setTime(expectedLog.get(0).get(1), 2); 
        setTime(expectedLog.get(0).get(2), 4); // B
        setTime(expectedLog.get(0).get(3), 6); 
        setTime(expectedLog.get(0).get(4), 9); // D
        setTime(expectedLog.get(0).get(5), 12);
        // Trace 2
        setTime(expectedLog.get(1).get(0), 1); // A
        setTime(expectedLog.get(1).get(1), 2); 
        setTime(expectedLog.get(1).get(2), 4); // B
        setTime(expectedLog.get(1).get(3), 6); 
        setTime(expectedLog.get(1).get(4), 10); // E
        setTime(expectedLog.get(1).get(5), 14);
        // Trace 3
        setTime(expectedLog.get(2).get(0), 1); // A
        setTime(expectedLog.get(2).get(1), 2); 
        setTime(expectedLog.get(2).get(2), 4); // C
        setTime(expectedLog.get(2).get(3), 6); 
        setTime(expectedLog.get(2).get(4), 10); // E
        setTime(expectedLog.get(2).get(5), 14);
        XLog actualLog = model.getArtifact(WorkbenchArtifacts.LogPre, GetArtifactMode.GetOnly);
        Assert.assertTrue(XesCompareSame.same(expectedLog, actualLog));

        // check model
        IEPTree expectedModel = EPTreeCreateUtil.create("->(A, x(B, C), x(D, E))");
        IEPTree actualModel = model.getArtifact(WorkbenchArtifacts.EPTree, GetArtifactMode.GetOnly);
        Assert.assertNotNull(actualModel);
        Assert.assertTrue(EPTreeCompareSame.same(expectedModel, actualModel));

        actualModel = model.getArtifact(WorkbenchArtifacts.EPTreePost, GetArtifactMode.GetOnly);
        Assert.assertNotNull(actualModel);
        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(actualModel);
        Assert.assertNotNull(decAlign);
        
        // check SC model
        model.setRecipe(WorkbenchArtifacts.Statechart, new EPTree2StatechartRecipe());
        model.setRecipe(WorkbenchArtifacts.StatechartPost, new StatechartPostprocessRecipe());
        Statechart statechart = model.getArtifact(WorkbenchArtifacts.StatechartPost);
        Assert.assertNotNull(statechart);

        @SuppressWarnings("unchecked")
        IMetricsDecorator<ISCState> decSCAlign = 
                (IMetricsDecorator<ISCState>) statechart.getDecorations().getForType(MetricsRefDecorator.class);
        Assert.assertNotNull(decSCAlign);
        
        List<ISCState> children = statechart.getChildren();
        Assert.assertNotNull(children);
        Assert.assertEquals(8, children.size());
        
        ISCState start = null, A = null, B = null, C = null, 
                point = null, D = null, E = null, end = null;
        for (ISCState state : children) {
            if (state.isInitialState()) {
                start = state;
            } else if (state.isEndState()) {
                end = state;
            } else if ("A".equals(state.getLabel())) {
                A = state;
            } else if ("B".equals(state.getLabel())) {
                B = state;
            } else if ("C".equals(state.getLabel())) {
                C = state;
            } else if ("D".equals(state.getLabel())) {
                D = state;
            } else if ("E".equals(state.getLabel())) {
                E = state;
            } else {
                point = state;
            }
        }
        Assert.assertNotNull(start);
        Assert.assertNotNull(A);
        Assert.assertNotNull(B);
        Assert.assertNotNull(C);
        Assert.assertNotNull(point);
        Assert.assertNotNull(D);
        Assert.assertNotNull(E);
        Assert.assertNotNull(end);
        
        // check freq metric
        IMetric metricAbsFreq = overlayManager.get(Overlays.AbsFreq).getPrimaryMetric();
        
        // check nodes
        Assert.assertEquals(null, decSCAlign.getMetric(start, metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(5), decSCAlign.getMetric(A, metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(2), decSCAlign.getMetric(B, metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(3), decSCAlign.getMetric(C, metricAbsFreq.getId()));
        Assert.assertEquals(null, decSCAlign.getMetric(point, metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(2), decSCAlign.getMetric(D, metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(3), decSCAlign.getMetric(E, metricAbsFreq.getId()));
        Assert.assertEquals(null, decSCAlign.getMetric(end, metricAbsFreq.getId()));

        // check edges
//        Assert.assertEquals(null, _scEdgeMetric(decSCAlign, start, A, metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(2), _scEdgeMetric(decSCAlign, A, B, metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(3), _scEdgeMetric(decSCAlign, A, C, metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(2), _scEdgeMetric(decSCAlign, B, point, metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(3), _scEdgeMetric(decSCAlign, C, point, metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(2), _scEdgeMetric(decSCAlign, point, D, metricAbsFreq.getId()));
        Assert.assertEquals(new MetricLong(3), _scEdgeMetric(decSCAlign, point, E, metricAbsFreq.getId()));
//        Assert.assertEquals(null, _scEdgeMetric(decSCAlign, D, end, metricAbsFreq.getId()));
//        Assert.assertEquals(null, _scEdgeMetric(decSCAlign, E, end, metricAbsFreq.getId()));
        
        // check time metric
        
        // check nodes
        DurationMetric metricTime = (DurationMetric) overlayManager.get(Overlays.DutationTotal).getPrimaryMetric();
        metricTime.setEvent2Time(new Event2TimeTimestamp());
        Assert.assertEquals(null, decSCAlign.getMetric(start, metricTime.getId()));
        Assert.assertEquals(new MetricStat(2 - 1, 2 - 1, 2 - 1), decSCAlign.getMetric(A, metricTime.getId()));
        Assert.assertEquals(new MetricStat(6 - 4, 6 - 4), decSCAlign.getMetric(B, metricTime.getId()));
        Assert.assertEquals(new MetricStat(6 - 4), decSCAlign.getMetric(C, metricTime.getId()));
        Assert.assertEquals(null, decSCAlign.getMetric(point, metricTime.getId()));
        Assert.assertEquals(new MetricStat(12 - 9), decSCAlign.getMetric(D, metricTime.getId()));
        Assert.assertEquals(new MetricStat(14 - 10, 14 - 10), decSCAlign.getMetric(E, metricTime.getId()));
        Assert.assertEquals(null, decSCAlign.getMetric(end, metricTime.getId()));

        // check edges
        WaitDurationMetric metricTimeE = (WaitDurationMetric) overlayManager.get(Overlays.DutationTotal).getEdgeMetrics()[0];
        metricTimeE.setEvent2Time(new Event2TimeTimestamp());
        
//        Assert.assertEquals(null, _scEdgeMetric(decSCAlign, start, A, metricTimeE.getId()));
        Assert.assertEquals(new MetricStat(4 - 2, 4 - 2), _scEdgeMetric(decSCAlign, A, B, metricTimeE.getId()));
        Assert.assertEquals(new MetricStat(4 - 2), _scEdgeMetric(decSCAlign, A, C, metricTimeE.getId()));
//        Assert.assertEquals(new MetricStat(9 - 6, 10 - 6), _scEdgeMetric(decSCAlign, B, point, metricTimeE.getId()));
//        Assert.assertEquals(new MetricStat(10 - 6), _scEdgeMetric(decSCAlign, C, point, metricTimeE.getId()));
//        Assert.assertEquals(null, _scEdgeMetric(decSCAlign, B, point, metricTimeE.getId()));
//        Assert.assertEquals(null, _scEdgeMetric(decSCAlign, C, point, metricTimeE.getId()));
        Assert.assertEquals(new MetricStat(9 - 6), _scEdgeMetric(decSCAlign, point, D, metricTimeE.getId()));
        Assert.assertEquals(new MetricStat(10 - 6, 10 - 6), _scEdgeMetric(decSCAlign, point, E, metricTimeE.getId()));
//        Assert.assertEquals(null, _scEdgeMetric(decSCAlign, D, end, metricTimeE.getId()));
//        Assert.assertEquals(null, _scEdgeMetric(decSCAlign, E, end, metricTimeE.getId()));
        
        
    }

    private MetricValue _scEdgeMetric(IMetricsDecorator<ISCState> decSCAlign,
            ISCState from, ISCState to, MetricId id) {
        ISCTransition edge = null;
        for (ISCTransition t : from.getInvolvedTransitions()) {
            if (t.getTo() == to) {
                edge = t;
                break;
            }
        }
        Assert.assertNotNull(edge);
        
        Pair<Set<ISCState>, Set<ISCState>> convertedEdge = Statechart2DotSvg.FncEdge2Node.apply(edge);
        Assert.assertNotNull(convertedEdge);
        Set<ISCState> edgeFrom = convertedEdge.getLeft();
        Set<ISCState> edgeTo = convertedEdge.getRight();
        
        Assert.assertNotNull(edgeFrom);
        Assert.assertFalse(edgeFrom.isEmpty());
        Assert.assertNotNull(edgeTo);
        Assert.assertFalse(edgeTo.isEmpty());
        
        return decSCAlign.getMetric(edgeFrom, edgeTo, id);
    }
    
    
//    @Test
//    public void testComputableJUnit() throws IOException {
//        _testComputable(
//            "./tests/testfiles/SWlog - JUnit - raw event log.xes.gz",
//            new L2LNestedCallsRecipe(),
//            new DiscoverEPTreeRecipe());
//    }

//    @Test
//    public void testBPIC2012A() throws IOException {
//        _testComputable(
//            "./tests/testfiles/A_subprocess_only.xes.gz",
//            new L2LNestedCallsRecipe(),
//            new DiscoverEPTreeRecipe());
//    }
    
//    @Test
//    public void testFailingLogNestedCallsMany() throws IOException {
//        DiscoverEPTreeRecipe discovery = new DiscoverEPTreeRecipe();
//        discovery.getParameters().setAlgorithm(DiscoveryAlgorithm.Recursion);
//        _testComputable(
//            "./tests/testfiles/testNestedCallsMany.xes",
//            new L2LNestedCallsRecipe(),
//            discovery);
//    }
    
//    private void _testComputable(String logfile, L2LNestedCallsRecipe preprocessRecipe, DiscoverEPTreeRecipe discoveryRecipe) throws IOException {
//        XESImport importer = new XESImport();
//        XLog input = importer.readLog(logfile);
//
//        // compute
//        AnalysisAlignMetricOverlayManager overlayManager = new AnalysisAlignMetricOverlayManager();
//        WorkbenchModel model = integrationTestRun(input, preprocessRecipe, null, discoveryRecipe, overlayManager);
//
//        // check log
//        Assert.assertNotNull(model.getArtifact(WorkbenchArtifacts.LogPre, GetArtifactMode.GetOnly));
//        
//        // check model
//        Assert.assertNotNull(model.getArtifact(WorkbenchArtifacts.EPTree, GetArtifactMode.GetOnly));
//
//        // check aligned model
//        IEPTree actualModel = model.getArtifact(WorkbenchArtifacts.EPTreePost, GetArtifactMode.GetOnly);
//        Assert.assertNotNull(actualModel);
//        IMetricsDecorator<IEPTreeNode> decAlign = MetricsTreeDecorator.getDecorator(actualModel);
//        Assert.assertNotNull(decAlign);
//        
//    }

    protected WorkbenchModel integrationTestRun(XLog input,
            AbstractL2LRecipe<?> preprocessRecipe,
            AbstractRecipe<Pair<XLog, HierarchyActivityInfo>, Set<String>, ?> cancelRecipe,
            DiscoverEPTreeRecipe discoverEPTreeRecipe, AnalysisAlignMetricOverlayManager overlayManager) {
        // Setup recipes
        WorkbenchModel model = _constructRecipeModel(
                preprocessRecipe, cancelRecipe, discoverEPTreeRecipe, overlayManager);
        
        // Setup input
        model.setArtifact(WorkbenchArtifacts.LogOriginal, input);

        // Calculate artifacts
        if (cancelRecipe != null) {
            model.computeArtifact(WorkbenchArtifacts.CancelOracleInput, true);
        }
        
        DiscoverEPTreeRecipe.Parameters params = discoverEPTreeRecipe.getParameters();
        
        Set<String> cancelOracle = model.getArtifact(WorkbenchArtifacts.CancelOracleInput, GetArtifactMode.GetOnly);
        params.setErrorClasses(cancelOracle);

        model.computeArtifact(WorkbenchArtifacts.EPTreePost, true);
        
        return model;
    }

    private WorkbenchModel _constructRecipeModel(
            AbstractL2LRecipe<?> preprocessRecipe,
            AbstractRecipe<Pair<XLog, HierarchyActivityInfo>, Set<String>, ?> cancelRecipe,
            DiscoverEPTreeRecipe discoverEPTreeRecipe, AnalysisAlignMetricOverlayManager overlayManager) {
        WorkbenchModel model = new WorkbenchModel();
        
        // Log preprocess
        model.setRecipe(WorkbenchArtifacts.ActivityInfo, new ActivityInfoRecipe());
        model.setRecipe(WorkbenchArtifacts.LogPre, preprocessRecipe, true);
        
        if (cancelRecipe != null) {
            model.setRecipe(WorkbenchArtifacts.CancelOracleInput, cancelRecipe, true);
        }
        
        // Tree Discovery
        EPTreePostprocessRecipe postDiscTree = new EPTreePostprocessRecipe();
        postDiscTree.getParameters().filter.setDepthFilter(0.0, 1.0);
        model.setRecipe(WorkbenchArtifacts.LogIM, new Log2IMLogRecipe());
        model.setRecipe(WorkbenchArtifacts.LogIMPre, new LogFilterRecipe());
        model.setRecipe(WorkbenchArtifacts.EPTree, discoverEPTreeRecipe);
        model.setRecipe(WorkbenchArtifacts.EPTreePost, postDiscTree);

        // Alignment
        model.setRecipe(WorkbenchArtifacts.LogAlign, new Log2LogAlignRecipe());
        model.setRecipe(WorkbenchArtifacts.AlignedLog, new AlignLogRecipe());
        model.setRecipe(WorkbenchArtifacts.AlignedTree, new AlignTreeRecipe());
        AlignTreePostprocessingRecipe postprocessingRecipe = new AlignTreePostprocessingRecipe();
        postprocessingRecipe.getParameters().overlayManager = overlayManager;
        model.setRecipe(WorkbenchArtifacts.AlignedLogPost, new AlignLogFilterRecipe());
        model.setRecipe(WorkbenchArtifacts.AlignedTreePost, postprocessingRecipe);

        SwitchArtifactRecipe<IEPTree> treeSwitch = new SwitchArtifactRecipe<IEPTree>();
        treeSwitch.setUseSecond(); // AnalysisAlgorithm.Align
        model.setRecipe(WorkbenchArtifacts.Tree2VisSwitch, treeSwitch);
        
        return model;
    }
}
