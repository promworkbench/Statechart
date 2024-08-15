package org.processmining.algorithms.statechart.m2m.ui.decorate;

import java.awt.Color;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.algorithms.statechart.align.metric.IMetric;
import org.processmining.algorithms.statechart.align.metric.MetricId;
import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.models.statechart.decorate.IDecorated;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricValue;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;
import org.processmining.models.statechart.decorate.align.metric.MetricsRefDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricsTreeDecorator;
import org.processmining.ui.statechart.color.IColorMap;
import org.processmining.ui.statechart.color.IColorMapProvider;
import org.processmining.utils.statechart.gfx.ColorUtil;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class AlignMetricUiDecorator<T, E> implements IUiDecorator<T, E> {

    private IMetricsDecorator<T> metricsDecorator;
    
    private IMetric[] nodeMetrics;
    private IColorMapProvider nodeColorMapProvider;
    private IMetric[] edgeMetrics;
    private IColorMapProvider edgeColorMapProvider;
    private StatMode statMode;
    
    private double minStroke;
    private double maxStroke;
    
    private IMetricValueConvertor nodePrimaryValueConvertor;
    private IMetricValueConvertor edgePrimaryValueConvertor;
    
    private IColorMap nodeColorMap;
    private IColorMap edgeColorMap;

    private Function<E, Pair<Set<T>, Set<T>>> fncEdge2node;
    
    public AlignMetricUiDecorator(
            IMetric[] nodeMetrics, 
            IColorMapProvider nodeColorMapProvider,
            IMetric[] edgeMetrics, 
            IColorMapProvider edgeColorMapProvider,
            StatMode statMode, 
            double minStroke, double maxStroke,
            Function<E, Pair<Set<T>, Set<T>>> fncEdge2node) {
        Preconditions.checkNotNull(nodeMetrics);
        Preconditions.checkNotNull(nodeColorMapProvider);
        Preconditions.checkNotNull(edgeMetrics);
        Preconditions.checkNotNull(edgeColorMapProvider);
        Preconditions.checkNotNull(statMode);
        
        Preconditions.checkArgument(nodeMetrics.length > 0);
        Preconditions.checkArgument(edgeMetrics.length > 0);
        
        this.nodeMetrics = nodeMetrics;
        this.nodeColorMapProvider = nodeColorMapProvider;
        
        this.edgeMetrics = edgeMetrics;
        this.edgeColorMapProvider = edgeColorMapProvider;
        
        this.statMode = statMode;
        
        this.minStroke = minStroke;
        this.maxStroke = maxStroke;
        
        this.fncEdge2node = fncEdge2node;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void visitModel(IDecorated<T> model) {
        metricsDecorator = (IMetricsDecorator<T>) model.getDecorations().getForType(MetricsRefDecorator.class);
        if (metricsDecorator == null) {
            metricsDecorator = (IMetricsDecorator<T>) model.getDecorations().getForType(MetricsTreeDecorator.class);
        }
        if (metricsDecorator == null) {
            throw new IllegalArgumentException("Model provided without a metrics decorator");
        }
        
        nodePrimaryValueConvertor = nodeMetrics[0].getValueConvertor(statMode);
        edgePrimaryValueConvertor = edgeMetrics[0].getValueConvertor(statMode);
        
        resetMetricValueScale(nodeMetrics);
        resetMetricValueScale(edgeMetrics);
    }

    private void resetMetricValueScale(IMetric[] metrics) {
        for (IMetric metric : metrics) {
            metric.getValueConvertor(statMode).resetMetricValueScale();
        }
    }

    @Override
    public void visitNode(T node) {
        for (IMetric metric : nodeMetrics) {
            MetricValue val = metricsDecorator.getMetric(node, metric.getId());
            if (val != null) {
                metric.getValueConvertor().updateValueScale(val);
            }
        }
    }

    @Override
    public void visitEdge(E edge) {
        Pair<Set<T>, Set<T>> edgeNodes = fncEdge2node.apply(edge);
        if (edgeNodes != null && edgeNodes.getRight() != null) {
            Set<T> from = edgeNodes.getLeft();
            Set<T> to = edgeNodes.getRight();
            for (IMetric metric : edgeMetrics) {
                MetricValue val = metricsDecorator.getMetric(from, to, metric.getId());
                if (val != null) {
                    metric.getValueConvertor().updateValueScale(val);
                }
            }
        }
    }
    
    @Override
    public void finishVisit() {
        nodeColorMap = createColorMap(
                nodePrimaryValueConvertor.getMetricValueScale(), 
                nodeColorMapProvider);
        edgeColorMap = createColorMap(
                edgePrimaryValueConvertor.getMetricValueScale(), 
                edgeColorMapProvider);
    }

    protected IColorMap createColorMap(MetricValueScale scale, IColorMapProvider provider) {
        IColorMap colorMap = provider.create();
        if (!scale.isEmpty()) {
            scale.resolveEmptyScale();
            colorMap.setValueRange(scale.getMin(), scale.getMax());
        }
        return colorMap;
    }

    @Override
    public UiDecoration getDecorationNode(final T node) {
        Function<MetricId, MetricValue> fncGetMetric = new Function<MetricId, MetricValue>() {
            @Override
            public MetricValue apply(MetricId id) {
                return metricsDecorator.getMetric(node, id);
            }
        };
        Function<MetricId, String> fncGetLabel = new Function<MetricId, String>() {
            @Override
            public String apply(MetricId id) {
                return metricsDecorator.getLabelName(node, id);
            }
        };
        
        return createDecoration(fncGetMetric, fncGetLabel, nodeMetrics, nodePrimaryValueConvertor, nodeColorMap);
    }
    
    @Override
    public UiDecoration getDecorationEdge(E edge) {
        Pair<Set<T>, Set<T>> edgeNodes = fncEdge2node.apply(edge);
        if (edgeNodes != null && edgeNodes.getRight() != null) {
            final Set<T> from = edgeNodes.getLeft();
            final Set<T> to = edgeNodes.getRight();
            
            Function<MetricId, MetricValue> fncGetMetric = new Function<MetricId, MetricValue>() {
                @Override
                public MetricValue apply(MetricId id) {
                    return metricsDecorator.getMetric(from, to, id);
                }
            };
            Function<MetricId, String> fncGetLabel = new Function<MetricId, String>() {
                @Override
                public String apply(MetricId id) {
                    return metricsDecorator.getLabelName(from, to, id);
                }
            };
            
            return createDecoration(fncGetMetric, fncGetLabel, edgeMetrics, edgePrimaryValueConvertor, edgeColorMap);
        } else {
            return null;
        }
    }

    private UiDecoration createDecoration(
            Function<MetricId, MetricValue> fncGetMetric, Function<MetricId, String> fncGetLabel, 
            IMetric[] metrics, IMetricValueConvertor primaryValueConvertor, IColorMap colorMap) {
            //T node, IMetric[] metrics, IMetricValueConvertor primaryValueConvertor, IColorMap colorMap) {
        IMetric primaryMetric = metrics[0];
        MetricValue val = fncGetMetric.apply(primaryMetric.getId()); //metricsDecorator.getMetric(node, primaryMetric.getId());
        if (val == null) {
            return null;
        }
        MetricValueScale scale = primaryValueConvertor.getMetricValueScale();
        
        double value = primaryValueConvertor.toValue(val);
        if (!Double.isNaN(value)) { // TODO is this the right choice at this level, or fallback to min value?
            // Determine basic decoration values
            double scaleFrac = scale.getPercentOnScale(value);
            Color colorBack = colorMap.getColor(value);
            Color colorStroke = colorBack.darker();
            Color colorText = ColorUtil.switchContrasting(colorBack, Color.white, Color.black);
            double stroke = minStroke + scaleFrac * (maxStroke - minStroke);
            
            // Build decoration label
            StringBuilder label = new StringBuilder();
            String sep = "";
            for (IMetric metric : metrics) {
                label.append(sep);
                MetricValue mval = fncGetMetric.apply(metric.getId()); //metricsDecorator.getMetric(node, metric.getId());
                if (mval != null) {
                    if (metrics.length > 1) {
                        label.append(fncGetLabel.apply(metric.getId())); //metricsDecorator.getLabelName(node, metric.getId()));
                        label.append(": ");
                    }
                    label.append(metric.getValueConvertor().toString(mval));
                    sep = "\n";
                }
            }
            
            // Return decoration settings
            return new UiDecoration(
                label.toString(),
                stroke,
                colorText,
                colorBack,
                colorStroke
            );
        } else {
            return null;
        }
    }

    @Override
    public IColorMap getNodeColorMap() {
        return nodeColorMap;
    }

    @Override
    public IMetricValueConvertor getNodeValueConvertor() {
        return nodePrimaryValueConvertor;
    }

    @Override
    public IColorMap getEdgeColorMap() {
        return edgeColorMap;
    }

    @Override
    public IMetricValueConvertor getEdgeValueConvertor() {
        return edgePrimaryValueConvertor;
    }

}
