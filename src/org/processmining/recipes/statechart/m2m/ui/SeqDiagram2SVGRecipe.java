package org.processmining.recipes.statechart.m2m.ui;

import java.util.Collections;
import java.util.Set;

import org.processmining.algorithms.statechart.m2m.ui.SeqDiagram2Svg;
import org.processmining.algorithms.statechart.m2m.ui.decorate.AlignMetricUiDecorator;
import org.processmining.algorithms.statechart.m2m.ui.decorate.IUiDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;
import org.processmining.models.statechart.decorate.ui.svg.ISvgDecorator;
import org.processmining.models.statechart.decorate.ui.svg.NullSvgDecorator;
import org.processmining.models.statechart.decorate.ui.svg.SvgMetricDecorator;
import org.processmining.models.statechart.msd.IActivation;
import org.processmining.models.statechart.msd.IMSDMessage;
import org.processmining.models.statechart.msd.ISeqDiagram;
import org.processmining.recipes.statechart.AbstractRecipe;
import org.processmining.recipes.statechart.align.AnalysisAlgorithm;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlay;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

public class SeqDiagram2SVGRecipe  extends
AbstractRecipe<ISeqDiagram, SVGDiagram, SeqDiagram2SVGRecipe.Parameters> {

    public static class Parameters {
        public Set<String> selectedNodes = Collections.emptySet();
        // TODO use two labelers
        //public ActivityLabeler activityLabeler;

        public ISvgDecorator<IActivation, IMSDMessage, ISeqDiagram> svgDecorator;
        
        public Parameters() {
            //activityLabeler = ActivityLabeler.Classifier;
            svgDecorator = new NullSvgDecorator<IActivation, IMSDMessage, ISeqDiagram>();
        }

        public void setupDecorator(
                AnalysisAlgorithm selectedAlignAlg,
                AnalysisAlignMetricOverlay selectedAlignMetric, StatMode statMode) {
            if (selectedAlignAlg == AnalysisAlgorithm.Approx) {
                svgDecorator = new NullSvgDecorator<IActivation, IMSDMessage, ISeqDiagram>();
            } else {
                svgDecorator = createAlignMetricUiDecorator(selectedAlignMetric, statMode);
            }
        }

        public static ISvgDecorator<IActivation, IMSDMessage, ISeqDiagram> createAlignMetricUiDecorator(
                AnalysisAlignMetricOverlay selectedAlignMetric, StatMode statMode) {
            IUiDecorator<IActivation, IMSDMessage> uiDecorator = new AlignMetricUiDecorator<>(
                selectedAlignMetric.getNodeMetrics(),
                selectedAlignMetric.getNodeColorMapProvider(),
                selectedAlignMetric.getEdgeMetrics(),
                selectedAlignMetric.getEdgeColorMapProvider(),
                statMode,
                SeqDiagram2Svg.EdgeMinStroke, SeqDiagram2Svg.EdgeMaxStroke,
                SeqDiagram2Svg.FncEdge2Node
            );
            return new SvgMetricDecorator<>(uiDecorator);
        }
    }

    public SeqDiagram2SVGRecipe() {
        super(new Parameters());
    }

    @Override
    protected SVGDiagram execute(ISeqDiagram input) {
        Parameters params = getParameters();
        
        try {
        SeqDiagram2Svg m2m = new SeqDiagram2Svg();
            return m2m.transform(input, 
                params.selectedNodes,
                params.svgDecorator
                //,
                );//params.activityLabeler.getLabeler());
        } catch (SVGException e) {
            e.printStackTrace();
            return null;
        }
    }

}
