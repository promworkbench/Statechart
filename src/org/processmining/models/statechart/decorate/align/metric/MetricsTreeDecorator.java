package org.processmining.models.statechart.decorate.align.metric;

import gnu.trove.map.hash.THashMap;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.processmining.algorithms.statechart.align.metric.IMetric;
import org.processmining.algorithms.statechart.align.metric.MetricId;
import org.processmining.algorithms.statechart.align.metric.impl.SwitchMetric;
import org.processmining.models.statechart.decorate.AbstractDecorator;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecorator;
import org.processmining.models.statechart.decorate.align.AlignMappingTreeDecorator;
import org.processmining.models.statechart.decorate.align.ExecIntervalTreeDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

import com.google.common.base.Preconditions;

public class MetricsTreeDecorator 
    extends AbstractDecorator<IEPTreeNode, Map<MetricId, MetricValue>>
    implements IMetricsDecorator<IEPTreeNode> {

    protected final Map<Pair<Set<IEPTreeNode>, Set<IEPTreeNode>>, Map<MetricId, MetricValue>> 
        edgeDecorations = new THashMap<>();

    @SuppressWarnings("unchecked")
    public static IMetricsDecorator<IEPTreeNode> getDecorator(IEPTree tree) {
        IMetricsDecorator<IEPTreeNode> decAlign = tree.getDecorations().getForType(MetricsTreeDecorator.class);
        if (decAlign == null) {
            decAlign = tree.getDecorations().getForType(MetricsRefDecorator.class);
        }
        return decAlign;
    }
    
    private ExecIntervalTreeDecorator decInt;
    private AlignMappingTreeDecorator decAlignMap;
    
    private Map<MetricId, IMetric> metricInsts = new THashMap<>();
    
    public MetricsTreeDecorator(ExecIntervalTreeDecorator decInt,
            AlignMappingTreeDecorator decAlignMap) {
        this.decInt = decInt;
        this.decAlignMap = decAlignMap;
    }
    
    public void registerMetric(IMetric metric) {
        if (metric instanceof SwitchMetric) {
            SwitchMetric switchMetric = (SwitchMetric) metric;
            registerMetric(switchMetric.getFirstMetric());
            registerMetric(switchMetric.getSecondMetric());
        }
        
        metricInsts.put(metric.getId(), metric);
        metric.setDecorators(decInt, decAlignMap);

    }
    
    // Node interface
    
    @Override
    public void setMetric(IEPTreeNode target, MetricId key, MetricValue value) {
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        
        Map<MetricId, MetricValue> map = getDecoration(target);
        if (map == null) {
            map = new THashMap<>();
            setDecoration(target, map);
        }
        
        map.put(key, value);
    }
    
    @Override
    public MetricValue getMetric(IEPTreeNode target, MetricId key) {
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(key);

        // retrieve stored value
        Map<MetricId, MetricValue> map = getDecoration(target);
        if (map == null) {
            map = new THashMap<>();
            setDecoration(target, map);
        }
        
        MetricValue value = map.get(key);
        if (value == null) {
            // delayed computation, do it now
            IMetric metric = metricInsts.get(key);
            if (metric.computeForNode(this, target)) {
                //value = map.get(key);
                value = metric.getMetric(this, target); 
            }
        }
        
        return value;
    }

    @Override
    public double getMetricValue(IEPTreeNode target, MetricId key) {
        return metricInsts.get(key).getMetricValue(this, target);
    }

    @Override
    public String getMetricValueString(IEPTreeNode target, MetricId key) {
        return metricInsts.get(key).getMetricValueString(this, target);
    }

    @Override
    public String getLabelName(IEPTreeNode target, MetricId key) {
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(key);

        IMetric metric = metricInsts.get(key);
        return metric.getLabelName(this, target);
    }

    @Override
    public void resetMetric(MetricId id) {
        for (Map<MetricId, MetricValue> map : decorations.values()) {
            map.remove(id);
        }
        for (Map<MetricId, MetricValue> map : edgeDecorations.values()) {
            map.remove(id);
        }
    }
    
    // Edge interface

    @Override
    public String getLabelName(Set<IEPTreeNode> from,Set<IEPTreeNode> to,
            MetricId key) {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(key);

        IMetric metric = metricInsts.get(key);
        return metric.getLabelName(this, from, to);
    }
    
    @Override
    public MetricValue getMetric(Set<IEPTreeNode> from, Set<IEPTreeNode> to,
            MetricId key) {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(key);

        // retrieve stored value
        Map<MetricId, MetricValue> map = getEdgeDecoration(from, to);
        if (map == null) {
            map = new THashMap<>();
            setEdgeDecoration(from, to, map);
        }
        
        MetricValue value = map.get(key);
        if (value == null) {
            // delayed computation, do it now
            IMetric metric = metricInsts.get(key);
            if (metric.computeForEdge(this, from, to)) {
                //value = map.get(key);
                value = metric.getMetric(this, from, to); 
            }
        }
        
        return value;
    }

    @Override
    public double getMetricValue(Set<IEPTreeNode> from, Set<IEPTreeNode> to,
            MetricId key) {
        return metricInsts.get(key).getMetricValue(this, from, to);
    }

    @Override
    public String getMetricValueString(Set<IEPTreeNode> from, Set<IEPTreeNode> to,
            MetricId key) {
        return metricInsts.get(key).getMetricValueString(this, from, to);
    }

    @Override
    public void setMetric(Set<IEPTreeNode> from, Set<IEPTreeNode> to,
            MetricId key, MetricValue value) {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        
        Map<MetricId, MetricValue> map = getEdgeDecoration(from, to);
        if (map == null) {
            map = new THashMap<>();
            setEdgeDecoration(from, to, map);
        }
        
        map.put(key, value);
    }

    public boolean hasEdgeDecoration(Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        return edgeDecorations.containsKey(Pair.of(from, to));
    }

    public Map<MetricId, MetricValue> getEdgeDecoration(Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        return edgeDecorations.get(Pair.of(from, to));
    }

    public void setEdgeDecoration(Set<IEPTreeNode> from, Set<IEPTreeNode> to, Map<MetricId, MetricValue> decoration) {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(decoration);
        
        edgeDecorations.put(Pair.of(from, to), decoration);
    }

    public void removeEdgeDecoration(IEPTreeNode enabled, IEPTreeNode target) {
        edgeDecorations.remove(Pair.of(enabled, target));
    }

    // Start: For cloning decorations
    
    @Override
    public IDecorator<IEPTreeNode, Map<MetricId, MetricValue>> newInstance() {
        return new MetricsRefDecorator<IEPTreeNode>(this);
    }

    @Override
    public <T2> IDecorator<T2, Map<MetricId, MetricValue>> deriveDecorationInstance(Class<T2> type) {
        return new MetricsRefDecorator<T2>(this);
    }
    
    @Override
    public void copyDecoration(IEPTreeNode target, IEPTreeNode oldTarget,
            IDecorator<IEPTreeNode, ?> oldDecorator) {
        // nop, see reference in newInstance()
        throw new NotImplementedException("MetricsTreeDecorator should be 'copied' via MetricsRefDecorator");
    }

    // Start: For deriving decorations
    
    @Override
    public void deriveDecoration(IEPTreeNode target, Object oldTarget,
            Decorations<?> oldDecorations) {
        // nop, we should never derive decorations for this setup
        throw new NotImplementedException("MetricsTreeDecorator should be 'derived' via MetricsRefDecorator");
    }

}
