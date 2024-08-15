package org.processmining.recipes.statechart.align;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.processmining.algorithms.statechart.align.metric.IMetric;
import org.processmining.algorithms.statechart.align.metric.IResourceMetric;
import org.processmining.algorithms.statechart.align.metric.ITimeMetric;
import org.processmining.algorithms.statechart.align.metric.impl.AbsFreqMetric;
import org.processmining.algorithms.statechart.align.metric.impl.CaseFreqMetric;
import org.processmining.algorithms.statechart.align.metric.impl.DurationEfficiencyMetric;
import org.processmining.algorithms.statechart.align.metric.impl.DurationMetric;
import org.processmining.algorithms.statechart.align.metric.impl.ErrorFreqMetric;
import org.processmining.algorithms.statechart.align.metric.impl.LogFreqMetric;
import org.processmining.algorithms.statechart.align.metric.impl.ModelFreqMetric;
import org.processmining.algorithms.statechart.align.metric.impl.OwnDurationMetric;
import org.processmining.algorithms.statechart.align.metric.impl.ResourceCountMetric;
import org.processmining.algorithms.statechart.align.metric.impl.SwitchMetric;
import org.processmining.algorithms.statechart.align.metric.impl.WaitDurationMetric;
import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;
import org.processmining.ui.statechart.color.ColorSets;
import org.processmining.ui.statechart.gfx.GfxIcons;

/**
 * Defines the Metric-based overlays to be used.
 * @author mleemans
 *
 */
public class AnalysisAlignMetricOverlayManager {
    
    public static enum Overlays {
        AbsFreq,
        CaseFreq,
        ModelLogFreq,
        ErrorFreq,
        DutationTotal,
        DutationOwn,
        DutationEff,
        ThreadCount;
    }
    
    private LinkedHashMap<Overlays, AnalysisAlignMetricOverlay> overlays;

    public AnalysisAlignMetricOverlayManager() {
        overlays = new LinkedHashMap<>();

        // Available metrics:
        overlays.put(Overlays.AbsFreq, new AnalysisAlignMetricOverlay(
            AbsFreqMetric.Name, GfxIcons.IconOptStat, AbsFreqMetric.DescriptionShort,
            new IMetric[] { new AbsFreqMetric(), new CaseFreqMetric() },
            ColorSets.Bluescale, 
            new IMetric[] { new AbsFreqMetric() },
            ColorSets.DarkGreyscale
        ));
        overlays.put(Overlays.CaseFreq, new AnalysisAlignMetricOverlay(
            CaseFreqMetric.Name, GfxIcons.IconOptStat, CaseFreqMetric.DescriptionShort,
            new IMetric[] { new CaseFreqMetric(), new AbsFreqMetric() },
            ColorSets.Bluescale, 
            new IMetric[] { new CaseFreqMetric() },
            ColorSets.DarkGreyscale
        ));
        
        SwitchMetric logModelMetric = new SwitchMetric(
            "MLFreq", "Log / Model Frequency", "Log/Model",
            new LogFreqMetric(), new ModelFreqMetric()
        );
        overlays.put(Overlays.ModelLogFreq, new AnalysisAlignMetricOverlay(
            "Log / Model Frequency", GfxIcons.IconOptExclamation,
            "How many deviations from the discovered model are present in the log?",
            new IMetric[] { logModelMetric, new AbsFreqMetric() },
            ColorSets.LogModelMoveScale.deriveWithValueChangepoints(0),
//            new IMetric[] { logModelMetric },
            new IMetric[] { new AbsFreqMetric() },
            ColorSets.DarkGreyscale
        ));
        overlays.put(Overlays.ErrorFreq, new AnalysisAlignMetricOverlay(
            ErrorFreqMetric.Name, GfxIcons.IconOptError, ErrorFreqMetric.DescriptionShort,
            new IMetric[] { new ErrorFreqMetric(), new AbsFreqMetric() },
            ColorSets.Redscale,
            new IMetric[] { new AbsFreqMetric() },
            ColorSets.DarkGreyscale
        ));
        overlays.put(Overlays.DutationTotal, new AnalysisAlignMetricOverlay(
            DurationMetric.Name, GfxIcons.IconOptTime, DurationMetric.DescriptionShort,
            new IMetric[] { new DurationMetric() },
            ColorSets.Redscale, 
            new IMetric[] { new WaitDurationMetric() },
            ColorSets.DarkRedscale
        ));
        overlays.put(Overlays.DutationOwn, new AnalysisAlignMetricOverlay(
            OwnDurationMetric.Name, GfxIcons.IconOptTime, OwnDurationMetric.DescriptionShort,
            new IMetric[] { new OwnDurationMetric(), new DurationMetric() }, 
            ColorSets.Redscale, 
            new IMetric[] { new WaitDurationMetric() },
            ColorSets.DarkRedscale
        ));
        overlays.put(Overlays.DutationEff, new AnalysisAlignMetricOverlay(
            DurationEfficiencyMetric.Name, GfxIcons.IconOptPercent, DurationEfficiencyMetric.DescriptionShort,
            new IMetric[] { new DurationEfficiencyMetric(), new OwnDurationMetric(), new DurationMetric() }, 
            ColorSets.RedNeutralGreenscale.deriveWithValueChangepoints(1),
            new IMetric[] { new WaitDurationMetric() },
            ColorSets.DarkRedscale
        ));
        overlays.put(Overlays.ThreadCount, new AnalysisAlignMetricOverlay(
            ResourceCountMetric.Name, GfxIcons.IconOptStat, ResourceCountMetric.DescriptionShort,
            new IMetric[] { new ResourceCountMetric(), new AbsFreqMetric() }, 
            ColorSets.Bluescale,
            new IMetric[] { new AbsFreqMetric() },
            ColorSets.DarkGreyscale
        ));
        
    }

    public AnalysisAlignMetricOverlay getApproxMetric() {
        return overlays.get(Overlays.AbsFreq);
    }
    
    public AnalysisAlignMetricOverlay get(Overlays overlay) {
        return overlays.get(overlay);
    }
    
    public Collection<AnalysisAlignMetricOverlay> getOverlays() {
        return overlays.values();
    }
    
    /**
     * Set the event 2 time conversion for all overlays
     * @param event2time
     */
    public void setEvent2Time(IEvent2Time event2time) {
        for (AnalysisAlignMetricOverlay metricType : getOverlays()) {
            for (IMetric metric : metricType.getAllMetrics()) {
                if (metric instanceof ITimeMetric) {
                    ((ITimeMetric) metric).setEvent2Time(event2time);
                }
            }
        }
    }
    
    /**
     * Set the resource attribute for all overlays
     * @param event2time
     */
    public void setResourceAttribute(String resourceAttribute) {
        for (AnalysisAlignMetricOverlay metricType : getOverlays()) {
            for (IMetric metric : metricType.getAllMetrics()) {
                if (metric instanceof IResourceMetric) {
                    ((IResourceMetric) metric).setResourceAttribute(resourceAttribute);
                }
            }
        }
    }
}
