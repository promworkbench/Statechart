package org.processmining.algorithms.statechart.align.metric;

import org.deckfour.xes.model.XLog;
import org.junit.BeforeClass;
import org.processmining.algorithms.statechart.align.AlignLog2Tree;
import org.processmining.algorithms.statechart.align.AlignTreeEventIntervals;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.models.statechart.align.XAlignedTreeLog;
import org.processmining.models.statechart.decorate.align.AlignMappingTreeDecorator;
import org.processmining.models.statechart.decorate.align.ExecIntervalTreeDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricsTreeDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.statechart.testutils.IntegrationTestUtil;

public class AbstractMetricTest {

    @BeforeClass
    public static void init() throws Throwable {
        IntegrationTestUtil.initializeProMWithRequiredPackages("LpSolve");
    }

    protected void _compute(XLog log, IEPTree model, IMetric... metrics) {
        _compute(log, model, null, metrics);
    }
    
    protected void _compute(XLog log, IEPTree model, IQueryCancelError queryCatchError, IMetric... metrics) {
        // align
        AlignLog2Tree aligner = new AlignLog2Tree();
        if (queryCatchError != null) {
            aligner.setQueryCatchError(queryCatchError);
        }
        XAlignedTreeLog align = aligner.performAlignment(log, model);

        // annotate tree
        AlignTreeEventIntervals annoter = new AlignTreeEventIntervals();
        annoter.annotate(align, model);
        
        // setup metrics
        MetricsTreeDecorator metricsDec = new MetricsTreeDecorator(
            model.getDecorations().getForType(ExecIntervalTreeDecorator.class),
            model.getDecorations().getForType(AlignMappingTreeDecorator.class)
        );
        model.getDecorations().registerDecorator(metricsDec);
        for (IMetric metric : metrics) {
            metricsDec.registerMetric(metric);
        }
    }
}
