package org.processmining.recipes.statechart.m2m.ui;

import java.util.Collections;
import java.util.Set;

import org.processmining.algorithms.statechart.m2m.ui.Statechart2DotSvg;
import org.processmining.algorithms.statechart.m2m.ui.decorate.AlignMetricUiDecorator;
import org.processmining.algorithms.statechart.m2m.ui.decorate.IUiDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;
import org.processmining.models.statechart.decorate.ui.dot.IDotDecorator;
import org.processmining.models.statechart.decorate.ui.dot.SCFrequencyDecorator;
import org.processmining.models.statechart.decorate.ui.dot.SCMetricDecorator;
import org.processmining.models.statechart.labeling.ActivityLabeler;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.ISCTransition;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.recipes.statechart.AbstractRecipe;
import org.processmining.recipes.statechart.align.AnalysisAlgorithm;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlay;

import com.kitfox.svg.SVGDiagram;

public class Statechart2DotSVGRecipe extends
        AbstractRecipe<Statechart, SVGDiagram, Statechart2DotSVGRecipe.Parameters> {

    public static class Parameters {
        public Dot.GraphDirection graphDir;
        public Set<String> selectedNodes = Collections.emptySet();

        public boolean recursionBackArrow;
        
        public ActivityLabeler activityLabeler;
        
        public IDotDecorator<ISCState, ISCTransition, Statechart> dotDecorator;
        
        public Parameters() {
            graphDir = GraphDirection.topDown;
            recursionBackArrow = true;
            activityLabeler = ActivityLabeler.Classifier;
            dotDecorator = new SCFrequencyDecorator();
        }

        public void setupDecorator(AnalysisAlgorithm selectedAlignAlg,
                AnalysisAlignMetricOverlay selectedAlignMetric, StatMode statMode) {
            if (selectedAlignAlg == AnalysisAlgorithm.Approx) {
                dotDecorator = new SCFrequencyDecorator();
            } else {
                dotDecorator = createAlignMetricUiDecorator(selectedAlignMetric, statMode);
            }
        }

        public static IDotDecorator<ISCState, ISCTransition, Statechart> createAlignMetricUiDecorator(
                AnalysisAlignMetricOverlay selectedAlignMetric, StatMode statMode) {
            IUiDecorator<ISCState, ISCTransition> uiDecorator = new AlignMetricUiDecorator<>(
                selectedAlignMetric.getNodeMetrics(),
                selectedAlignMetric.getNodeColorMapProvider(),
                selectedAlignMetric.getEdgeMetrics(),
                selectedAlignMetric.getEdgeColorMapProvider(),
                statMode,
                Statechart2DotSvg.EdgeMinStroke, Statechart2DotSvg.EdgeMaxStroke,
                Statechart2DotSvg.FncEdge2Node
            );
            return new SCMetricDecorator(uiDecorator);
        }
    }

    public Statechart2DotSVGRecipe() {
        super(new Parameters());
    }

    @Override
    protected SVGDiagram execute(Statechart input) {
        Parameters params = getParameters();
        
        Statechart2DotSvg sc2svg = new Statechart2DotSvg();
        SVGDiagram svg = sc2svg.transform(input, params.graphDir,
                params.selectedNodes, params.recursionBackArrow,
                params.activityLabeler.getLabeler(),
                params.dotDecorator);
        
        return svg;
    }

}
