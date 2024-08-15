package org.processmining.models.statechart.decorate.align.metric;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Map;
import java.util.Set;

import org.processmining.algorithms.statechart.align.metric.MetricId;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;

import com.google.common.base.Preconditions;

public class MetricsRefDecorator<T> implements 
    IDecorator<T, Map<MetricId, MetricValue>>, IMetricsDecorator<T> {

    private MetricsTreeDecorator baseDecorator;
    
    private Map<T, IEPTreeNode> baseMapper = new THashMap<>();
    
    public MetricsRefDecorator(MetricsTreeDecorator baseDecorator) {
        this.baseDecorator = baseDecorator;
    }

    @Override
    public boolean hasDecoration(T target) {
        return baseDecorator.hasDecoration(baseMapper.get(target));
    }

    @Override
    public Map<MetricId, MetricValue> getDecoration(T target) {
        return baseDecorator.getDecoration(baseMapper.get(target));
    }

    @Override
    public void setDecoration(T target, Map<MetricId, MetricValue> decoration) {
        baseDecorator.setDecoration(baseMapper.get(target), decoration);
    }

    @Override
    public void removeDecoration(T target) {
        baseDecorator.removeDecoration(baseMapper.get(target));
    }

    @Override
    public IDecorator<T, Map<MetricId, MetricValue>> clone() {
        MetricsRefDecorator<T> newInst = (MetricsRefDecorator<T>) newInstance();
        newInst.baseMapper = new THashMap<>(this.baseMapper);
        return newInst;
    }

    // Node interface
    
    @Override
    public MetricValue getMetric(T target, MetricId key) {
        IEPTreeNode node = baseMapper.get(target);
        if (node != null) {
            return baseDecorator.getMetric(node, key);
        } else {
            return null;
        }
    }

    @Override
    public String getLabelName(T target, MetricId key) {
        IEPTreeNode node = baseMapper.get(target);
        if (node != null) {
            return baseDecorator.getLabelName(node, key);
        } else {
            return null;
        }
    }

    @Override
    public double getMetricValue(T target, MetricId key) {
        IEPTreeNode node = baseMapper.get(target);
        if (node != null) {
            return baseDecorator.getMetricValue(node, key);
        } else {
            return Double.NaN;
        }
    }

    @Override
    public String getMetricValueString(T target, MetricId key) {
        IEPTreeNode node = baseMapper.get(target);
        if (node != null) {
            return baseDecorator.getMetricValueString(node, key);
        } else {
            return null;
        }
    }

    @Override
    public void setMetric(IEPTreeNode target, MetricId key, MetricValue value) {
        IEPTreeNode node = baseMapper.get(target);
        if (node != null) {
            baseDecorator.setMetric(node, key, value);
        }
    }

    @Override
    public void resetMetric(MetricId id) {
        baseDecorator.resetMetric(id);
    }
    
    // Edge interface
    
    private <Tsub> Set<IEPTreeNode> convert(Set<Tsub> from) {
        Set<IEPTreeNode> result = new THashSet<>(from.size());
        for (Tsub entry : from) {
            IEPTreeNode res = baseMapper.get(entry);
            if (res != null) {
                result.add(res);
            }
        }
        return result;
    }

    @Override
    public String getLabelName(Set<T> from, Set<T> to, MetricId key) {
        Set<IEPTreeNode> nodeFrom = convert(from);
        Set<IEPTreeNode> nodeTo = convert(to);
        if (nodeFrom != null && !nodeFrom.isEmpty() 
                && nodeTo != null && !nodeTo.isEmpty()) {
            return baseDecorator.getLabelName(nodeFrom, nodeTo, key);
        } else {
            return null;
        }
    }

    @Override
    public MetricValue getMetric(Set<T> from, Set<T> to, MetricId key) {
        Set<IEPTreeNode> nodeFrom = convert(from);
        Set<IEPTreeNode> nodeTo = convert(to);
        if (nodeFrom != null && !nodeFrom.isEmpty() 
                && nodeTo != null && !nodeTo.isEmpty()) {
            return baseDecorator.getMetric(nodeFrom, nodeTo, key);
        } else {
            return null;
        }
    }

    @Override
    public double getMetricValue(Set<T> from, Set<T> to, MetricId key) {
        Set<IEPTreeNode> nodeFrom = convert(from);
        Set<IEPTreeNode> nodeTo = convert(to);
        if (nodeFrom != null && !nodeFrom.isEmpty() 
                && nodeTo != null && !nodeTo.isEmpty()) {
            return baseDecorator.getMetricValue(nodeFrom, nodeTo, key);
        } else {
            return Double.NaN;
        }
    }

    @Override
    public String getMetricValueString(Set<T> from, Set<T> to, MetricId key) {
        Set<IEPTreeNode> nodeFrom = convert(from);
        Set<IEPTreeNode> nodeTo = convert(to);
        if (nodeFrom != null && !nodeFrom.isEmpty() 
                && nodeTo != null && !nodeTo.isEmpty()) {
            return baseDecorator.getMetricValueString(nodeFrom, nodeTo, key);
        } else {
            return null;
        }
    }

    @Override
    public void setMetric(Set<IEPTreeNode> from, Set<IEPTreeNode> to,
            MetricId key, MetricValue value) {
        Set<IEPTreeNode> nodeFrom = convert(from);
        Set<IEPTreeNode> nodeTo = convert(to);
        if (nodeFrom != null && !nodeFrom.isEmpty() 
                && nodeTo != null && !nodeTo.isEmpty()) {
            baseDecorator.setMetric(nodeFrom, nodeTo, key, value);
        }
    }

    @Override
    public IDecorator<T, Map<MetricId, MetricValue>> newInstance() {
        return new MetricsRefDecorator<T>(baseDecorator);
    }

    @Override
    public <T2> IDecorator<T2, Map<MetricId, MetricValue>> deriveDecorationInstance(Class<T2> type) {
        return new MetricsRefDecorator<T2>(baseDecorator);
    }
    
    @Override
    public void copyDecoration(T target, T oldTarget, Decorations<?> oldDecorations) {
        deriveDecoration(target, oldTarget, oldDecorations);
    }

    @Override
    public void copyDecoration(T target, T oldTarget, IDecorator<T, ?> oldDecorator) {
        if (oldDecorator instanceof MetricsTreeDecorator) {
            MetricsTreeDecorator oldTreeDecorator = (MetricsTreeDecorator) oldDecorator;
            
            // use the actual old tree decorator for building references
            this.baseDecorator = oldTreeDecorator;
            
            // create reference
            baseMapper.put(target, (IEPTreeNode) oldTarget);
        } else if (oldDecorator instanceof MetricsRefDecorator) {
            @SuppressWarnings("unchecked")
            MetricsRefDecorator<Object> oldRefDecorator = (MetricsRefDecorator<Object>) oldDecorator;

            // extract the referenced tree decorator for building references
            this.baseDecorator = oldRefDecorator.baseDecorator;
            
            // create reference
            baseMapper.put(target, oldRefDecorator.baseMapper.get(oldTarget));
        } else {
            throw new IllegalStateException(
                    "Could not copy decoration, no matching decorator provided");
        }
    }

    @Override
    public void deriveDecoration(T target, Object oldTarget,
            Decorations<?> oldDecorations) {
        Preconditions.checkNotNull(oldTarget);
        Preconditions.checkNotNull(oldDecorations);
        
        // lookup base decorator
        MetricsTreeDecorator oldTreeDecorator = oldDecorations
                .getForType(MetricsTreeDecorator.class);
        if (oldTreeDecorator != null && oldTarget instanceof IEPTreeNode) {
            // use the actual old tree decorator for building references
            this.baseDecorator = oldTreeDecorator;
            
            // create reference
            baseMapper.put(target, (IEPTreeNode) oldTarget);
        } else {
            // extract the referenced tree decorator for building references
            @SuppressWarnings("unchecked")
            MetricsRefDecorator<Object> oldRefDecorator = oldDecorations
                    .getForType(MetricsRefDecorator.class);
            if (oldRefDecorator != null) {
                this.baseDecorator = oldRefDecorator.baseDecorator;
                
                // create reference
                baseMapper.put(target, oldRefDecorator.baseMapper.get(oldTarget));
            } else {
                throw new IllegalStateException(
                        "Could not copy decoration, no matching decorator found in old");
            }
        }
    }

}
