package org.processmining.recipes.statechart.align;

import org.processmining.algorithms.statechart.align.AlignTreeLogMoveProcessor;
import org.processmining.algorithms.statechart.align.metric.IMetric;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.align.AlignMappingTreeDecorator;
import org.processmining.models.statechart.decorate.align.ExecIntervalTreeDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricsTreeDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.recipes.statechart.AbstractRecipe;

import com.google.common.base.Preconditions;

public class AlignTreePostprocessingRecipe extends
        AbstractRecipe<IEPTree, IEPTree, AlignTreePostprocessingRecipe.Parameters> {

    public static class Parameters {
        public AnalysisAlignMetricOverlayManager overlayManager;
        
        public boolean expandLogMoves = true;
    }

    public AlignTreePostprocessingRecipe() {
        super(new Parameters());
    }

    @Override
    protected IEPTree execute(IEPTree input) {
        Parameters params = getParameters();
        Preconditions.checkNotNull(params.overlayManager);
        
        // clone tree
        IEPTree result = input.createCopy();

        // compute tree modifications: log moves
        if (params.expandLogMoves) {
            AlignTreeLogMoveProcessor proc = new AlignTreeLogMoveProcessor();
            proc.process(result);
        }
        
        // setup metrics (delayed computations)
        Decorations<IEPTreeNode> decs = result.getDecorations();
        MetricsTreeDecorator metricsDec = new MetricsTreeDecorator(
            decs.getForType(ExecIntervalTreeDecorator.class),
            decs.getForType(AlignMappingTreeDecorator.class));
        result.getDecorations().registerDecorator(metricsDec);
        for (AnalysisAlignMetricOverlay metricOverlay : params.overlayManager.getOverlays()) {
            for (IMetric metric : metricOverlay.getAllMetrics()) {
                metricsDec.registerMetric(metric);
            }
        }
        
        return result;
    }

}
