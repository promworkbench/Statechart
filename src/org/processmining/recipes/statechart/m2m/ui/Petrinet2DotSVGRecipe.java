package org.processmining.recipes.statechart.m2m.ui;

import java.util.Collections;
import java.util.Set;

import org.processmining.algorithms.statechart.m2m.ui.Petrinet2DotSvg;
import org.processmining.algorithms.statechart.m2m.ui.decorate.AlignMetricUiDecorator;
import org.processmining.algorithms.statechart.m2m.ui.decorate.IUiDecorator;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;
import org.processmining.models.statechart.decorate.ui.dot.IDotDecorator;
import org.processmining.models.statechart.decorate.ui.dot.NullDotDecorator;
import org.processmining.models.statechart.decorate.ui.dot.PTnetMetricDecorator;
import org.processmining.models.statechart.labeling.ActivityLabeler;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.recipes.statechart.AbstractRecipe;
import org.processmining.recipes.statechart.align.AnalysisAlgorithm;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlay;

import com.kitfox.svg.SVGDiagram;

public class Petrinet2DotSVGRecipe extends
        AbstractRecipe<PetrinetDecorated, SVGDiagram, Petrinet2DotSVGRecipe.Parameters> {

    public static class Parameters {
        public Dot.GraphDirection graphDir;
        public Set<String> selectedNodes = Collections.emptySet();

//        public boolean recursionBackArrow;
        
        public ActivityLabeler activityLabeler;

        public IDotDecorator<Transition, Arc, PetrinetDecorated> dotDecorator;
        
        public Parameters() {
            graphDir = GraphDirection.topDown;
//            recursionBackArrow = true;
            activityLabeler = ActivityLabeler.Classifier;
            dotDecorator = new NullDotDecorator<Transition, Arc, PetrinetDecorated>();
        }
        
        public void setupDecorator(AnalysisAlgorithm selectedAlignAlg,
                AnalysisAlignMetricOverlay selectedAlignMetric, StatMode statMode) {
            if (selectedAlignAlg == AnalysisAlgorithm.Approx) {
                dotDecorator = new NullDotDecorator<Transition, Arc, PetrinetDecorated>();
            } else {
                dotDecorator = createAlignMetricUiDecorator(selectedAlignMetric, statMode);
            }
        }

        public static IDotDecorator<Transition, Arc, PetrinetDecorated> createAlignMetricUiDecorator(
                AnalysisAlignMetricOverlay selectedAlignMetric, StatMode statMode) {
            PTnetMetricDecorator.ModelAwareEdge2Node edge2node = Petrinet2DotSvg.FncEdge2Node;
            IUiDecorator<Transition, Arc> uiDecorator = new AlignMetricUiDecorator<>(
                selectedAlignMetric.getNodeMetrics(),
                selectedAlignMetric.getNodeColorMapProvider(),
                selectedAlignMetric.getEdgeMetrics(),
                selectedAlignMetric.getEdgeColorMapProvider(),
                statMode,
                Petrinet2DotSvg.EdgeMinStroke, Petrinet2DotSvg.EdgeMaxStroke,
                edge2node
            );
            return new PTnetMetricDecorator(uiDecorator, edge2node);
        }
    }

    public Petrinet2DotSVGRecipe() {
        super(new Parameters());
    }

    @Override
    protected SVGDiagram execute(PetrinetDecorated input) {
        Parameters params = getParameters();
        
        Petrinet2DotSvg sc2svg = new Petrinet2DotSvg();
        SVGDiagram svg = sc2svg.transform(input, params.graphDir,
                params.selectedNodes, //params.recursionBackArrow,
                params.activityLabeler.getLabeler(),
                params.dotDecorator);
        
        return svg;
    }

}
