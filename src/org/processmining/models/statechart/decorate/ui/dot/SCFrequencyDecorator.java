package org.processmining.models.statechart.decorate.ui.dot;

import java.awt.Color;

import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.algorithms.statechart.align.metric.value.MetricValueConvertorDefault;
import org.processmining.algorithms.statechart.align.metric.value.MetricValueConvertorDefault.StringPostFix;
import org.processmining.algorithms.statechart.m2m.ui.Statechart2DotSvg;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale;
import org.processmining.models.statechart.decorate.staticmetric.DerivedFreqMetricDecorator;
import org.processmining.models.statechart.decorate.staticmetric.FreqMetric;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.ISCTransition;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotCluster;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.ui.statechart.color.ColorSets;
import org.processmining.ui.statechart.color.IColorMap;
import org.processmining.utils.statechart.gfx.ColorUtil;

public class SCFrequencyDecorator 
    extends AbstractDotDecorator<ISCState, ISCTransition, Statechart> {

    private Decorations<ISCState> stateDecorations;
    private Decorations<ISCTransition> transitionDecorations;

    private DerivedFreqMetricDecorator<ISCState> stateFreqMetrics;
    private DerivedFreqMetricDecorator<ISCTransition> transitionFreqMetrics;

    // TODO use
    private MetricValueScale nodeScale, edgeScale;
    private IColorMap nodeColorMap = ColorSets.Bluescale.create();
    private IColorMap edgeColorMap = ColorSets.DarkGreyscale.create();
    private double minStroke = Statechart2DotSvg.EdgeMinStroke;
    private double maxStroke = Statechart2DotSvg.EdgeMaxStroke;
    
    @SuppressWarnings("unchecked")
    @Override
    public void visitModel(Statechart input, Dot dot) {
        stateDecorations = input.getStateDecorations();
        transitionDecorations = input.getTransitionDecorations();

        stateFreqMetrics = stateDecorations
                .getForType(DerivedFreqMetricDecorator.class);
        transitionFreqMetrics = transitionDecorations
                .getForType(DerivedFreqMetricDecorator.class);
        
//        StatisticalSummary stats = stateFreqMetrics.getStatsAbsolute();
//        if (stats.getN() > 0) {
//            colorMap.setValueRange(stats.getMin(), stats.getMax());
//        }
        nodeScale = new MetricValueScale();
        edgeScale = new MetricValueScale();
    }

    @Override
    public void visitNode(ISCState node) {
        FreqMetric metrics = stateFreqMetrics.getDecoration(node);
        if (metrics != null) {
            nodeScale.update(metrics.getFreqAbsolute());
        }
    }

    @Override
    public void visitEdge(ISCTransition edge) {
        FreqMetric metrics = transitionFreqMetrics.getDecoration(edge);
        if (metrics != null) {
            edgeScale.update(metrics.getFreqAbsolute());
        }
    }
    
    @Override
    public void finishVisit() {
        nodeScale.resolveEmptyScale();
        edgeScale.resolveEmptyScale();
        nodeColorMap.setValueRange(nodeScale.getMin(), nodeScale.getMax());
        edgeColorMap.setValueRange(edgeScale.getMin(), edgeScale.getMax());
        setApplied();
    }
    
    @Override
    public void decorateNode(ISCState state, DotNode node) {
//        Color color = Color.GRAY;
//        Color fillColor = Color.white;
//        Color textColor = Color.black;
//        String labelExtra = "";
//
//        if (!state.isPseudoState() && !(node instanceof DotCluster)) {
//            node.setOption("style", "rounded,filled");
//
//            FreqMetric metrics = stateFreqMetrics.getDecoration(state);
//            if (metrics != null) {
//                // compute state metrics
//                StatisticalSummary stats = stateFreqMetrics.getStatsAbsolute();
//                int freq = metrics.getFreqAbsolute();
//                double freqPercent = 1.0;
//                if (stats.getMax() > 0) {
//                    freqPercent = MathUtils.clamp01((double) freq / (double) stats.getMax());
//                }
//
//                // set state style
//                fillColor = colorMap.getColor(freq);
//                textColor = ColorUtil.switchContrasting(fillColor, Color.white,
//                        Color.black);
//                node.setOption("fillcolor", ColorUtil.rgbToHexString(color));
//                node.setOption("fontcolor", ColorUtil.rgbToHexString(textColor));
//                labelExtra = String.format("%d (%.1f%%)", freq,
//                        freqPercent * 100.0);
//            }
//            decorate(node, color, fillColor, textColor, labelExtra);
//        }
        if (!state.isPseudoState() && !(node instanceof DotCluster)) {
            FreqMetric metrics = stateFreqMetrics.getDecoration(state);
            if (metrics != null) {
                double value = metrics.getFreqAbsolute();
                
                double scaleFrac = nodeScale.getPercentOfMax(value);
                Color colorBack = nodeColorMap.getColor(value);
                Color colorStroke = colorBack.darker();
                Color colorText = ColorUtil.switchContrasting(colorBack, Color.white, Color.black);
//                double stroke = minStroke + scaleFrac * (maxStroke - minStroke);
                String labelExtra = String.format("%,d (%.1f%%)", (int) value, scaleFrac * 100.0);

                node.setOption("style", "rounded,filled");
                decorate(node, colorStroke, colorBack, colorText, labelExtra);
            }
        }
    }

//    private Map<ISCState, SummaryStatistics> stateOutState = new THashMap<>();

    @Override
    public void decorateEdge(ISCTransition transition, DotEdge edge) {
//        Color stroke = Color.BLACK;
//        double penwidth = 1.0;
//        String label = "";
//
//        FreqMetric metrics = transitionFreqMetrics.getDecoration(transition);
//        if (metrics != null) {
//            // compute transition metrics
//            ISCState fromState = transition.getFrom();
//            SummaryStatistics outStats = stateOutState.get(fromState);
//
//            // compute stats from oudgoing transitions
//            if (outStats == null) {
//                outStats = new SummaryStatistics();
//                for (ISCTransition t : fromState.getInvolvedTransitions()) {
//                    if (t.getFrom() == fromState) {
//                        FreqMetric Tmetrics = transitionFreqMetrics
//                                .getDecoration(t);
//                        if (Tmetrics != null) {
//                            outStats.addValue(Tmetrics.getFreqAbsolute());
//                        }
//                    }
//                }
//                stateOutState.put(fromState, outStats);
//            }
//
//            int freqLowerBound = (int) outStats.getMin();
//            int freqUpperBound = (int) outStats.getSum();
//            if (fromState.getStateType() == SCStateType.SplitPseudo) {
//                freqUpperBound = (int) outStats.getMax();
//            }
//
//            int freq = metrics.getFreqAbsolute();
//            double freqPercent = MathUtils.clamp01((double) freq / (double) freqUpperBound);
//            double freqScale = 1.0;
//            if (freqUpperBound != freqLowerBound) {
//                freqScale = MathUtils.clamp01((double) (freq - freqLowerBound)
//                        / (double) (freqUpperBound - freqLowerBound));
//            }
//
//            // set state style
//            stroke = ColorUtil.lerp(new Color(137, 137, 137), Color.black,
//                    freqScale);
//            penwidth = Statechart2DotSvg.EdgeMinStroke + freqScale 
//                    * (Statechart2DotSvg.EdgeMaxStroke - Statechart2DotSvg.EdgeMinStroke);
//            label = String.format("%d\n(%.1f%%)", freq, freqPercent * 100.0);
//        }
//
//        decorate(edge, stroke, penwidth, label);
        FreqMetric metrics = transitionFreqMetrics.getDecoration(transition);
        if (metrics != null) {
            double value = metrics.getFreqAbsolute();
            
            double scaleFrac = edgeScale.getPercentOfMax(value);
            Color colorBack = edgeColorMap.getColor(value);
//            Color colorStroke = colorBack.darker();
//            Color colorText = ColorUtil.switchContrasting(colorBack, Color.white, Color.black);
            double stroke = minStroke + scaleFrac * (maxStroke - minStroke);
            String labelExtra = String.format("%,d (%.1f%%)", (int) value, scaleFrac * 100.0);

            decorate(edge, colorBack, stroke, labelExtra);
        }
    }

    @Override
    public void decorateRecursionBackArrow(ISCState child, ISCState entryState, DotEdge e) {
        FreqMetric metrics = stateFreqMetrics.getDecoration(child);
        int freq = metrics.getFreqAbsolute();
        e.setLabel(Integer.toString(freq));
        // e.setOption("headlabel", Integer.toString(freq));
    }

    @Override
    public IColorMap getNodeColorMap() {
        return nodeColorMap;
    }

    @Override
    public IMetricValueConvertor getNodeValueConvertor() {
        return new MetricValueConvertorDefault(nodeScale, StringPostFix.Max);
    }

    @Override
    public IColorMap getEdgeColorMap() {
        return edgeColorMap;
    }

    @Override
    public IMetricValueConvertor getEdgeValueConvertor() {
        return new MetricValueConvertorDefault(edgeScale, StringPostFix.Max);
    }
}
