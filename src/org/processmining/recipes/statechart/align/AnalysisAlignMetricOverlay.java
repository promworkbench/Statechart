package org.processmining.recipes.statechart.align;

import org.apache.commons.lang3.ArrayUtils;
import org.processmining.algorithms.statechart.align.metric.IMetric;
import org.processmining.ui.statechart.color.IColorMapProvider;
import org.processmining.ui.statechart.gfx.GfxIcons;

/**
 * Defines a Metric-based overlays to be used.
 * @author mleemans
 *
 */
public class AnalysisAlignMetricOverlay {
    private final String name, tooltip;
    private GfxIcons icon;
    private final IMetric[] nodeMetrics, edgeMetrics;
    private final IColorMapProvider nodeColorMapProvider, edgeColorMapProvider;

    public AnalysisAlignMetricOverlay(String name, GfxIcons icon, String tooltip,
            IMetric[] nodeMetrics, IColorMapProvider nodeColorMapProvider,  
            IMetric[] edgeMetrics, IColorMapProvider edgeColorMapProvider) {
        this.name = name;
        this.icon = icon;
        this.tooltip = tooltip;
        this.nodeMetrics = nodeMetrics;
        this.edgeMetrics = edgeMetrics;
        this.nodeColorMapProvider = nodeColorMapProvider;
        this.edgeColorMapProvider = edgeColorMapProvider;
    }

    public String getName() {
        return name;
    }

    public GfxIcons getIcon() {
        return icon;
    }

    public IMetric getPrimaryMetric() {
        return nodeMetrics[0];
    }
    
    public IMetric[] getNodeMetrics() {
        return nodeMetrics;
    }

    public IMetric[] getEdgeMetrics() {
        return edgeMetrics;
    }

    public IMetric[] getAllMetrics() {
        return (IMetric[])ArrayUtils.addAll(nodeMetrics, edgeMetrics);
    }
    
    public IColorMapProvider getNodeColorMapProvider() {
        return nodeColorMapProvider;
    }

    public IColorMapProvider getEdgeColorMapProvider() {
        return edgeColorMapProvider;
    }

    public String getTooltip() {
        return tooltip;
    }
}
