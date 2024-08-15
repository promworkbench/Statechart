package org.processmining.algorithms.statechart.align.metric.impl;

import java.util.Set;

import org.processmining.algorithms.statechart.align.metric.IMetric;
import org.processmining.algorithms.statechart.align.metric.MetricId;
import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.algorithms.statechart.align.metric.value.MetricValueConvertorDefault;
import org.processmining.algorithms.statechart.align.metric.value.MetricValueConvertorDefault.StringPostFix;
import org.processmining.algorithms.statechart.align.metric.value.MetricValueConvertorSwitchZero;
import org.processmining.models.statechart.decorate.align.AlignMappingTreeDecorator;
import org.processmining.models.statechart.decorate.align.ExecIntervalTreeDecorator;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricValue;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;
import org.processmining.models.statechart.eptree.IEPTreeNode;

import com.google.common.base.Predicate;

public class SwitchMetric implements IMetric {

    public  static final Predicate<MetricValue> SwitchOnEmptyValue = new Predicate<MetricValue>() {
        @Override
        public boolean apply(MetricValue value) {
            if (value == null) {
                return true;
            } else if (value.isDouble()) {
                double val = value.getDouble();
                return val == 0 || Double.isNaN(val);
            } else if (value.isLong()) {
                long val = value.getLong();
                return val == 0;
            } else if (value.isStat()) {
                return value.getStat().getN() == 0;
            }
            throw new IllegalArgumentException();
        }
    };
    
    private MetricId id;
    private String name;
    private String shortName;
    private IMetric firstMetric;
    private IMetric secondMetric;
    
    private Predicate<MetricValue> switchToSecondMetric;
    protected IMetricValueConvertor valueConvertor;

    public SwitchMetric(String id, String name, String shortName,
            IMetric firstMetric, IMetric secondMetric) {
        this(id, name, shortName, 
            firstMetric, secondMetric, 
            SwitchOnEmptyValue, 
            new MetricValueConvertorSwitchZero(
                firstMetric.getId(),
                new MetricValueConvertorDefault(StringPostFix.Max)
            ));
    }
    
    public SwitchMetric(String id, String name, String shortName,
            IMetric firstMetric, IMetric secondMetric,
            Predicate<MetricValue> switchToSecondMetric, 
            IMetricValueConvertor valueConvertor) {
        this.id = new MetricId(id);
        this.name = name;
        this.shortName = shortName;
        this.firstMetric = firstMetric;
        this.secondMetric = secondMetric;
        this.switchToSecondMetric = switchToSecondMetric;
        this.valueConvertor = valueConvertor;
    }
    
    @Override
    public MetricId getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public IMetric getFirstMetric() {
        return firstMetric;
    }
    
    public IMetric getSecondMetric() {
        return secondMetric;
    }
    
    @Override
    public void setDecorators(ExecIntervalTreeDecorator decInt, 
            AlignMappingTreeDecorator decAlignMap) {
        firstMetric.setDecorators(decInt, decAlignMap);
        secondMetric.setDecorators(decInt, decAlignMap);
    }
    
    // Node interface

    @Override
    public String getLabelName(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode context) {
        MetricValue value = firstMetric.getMetric(decMetric, context);
        if (switchToSecondMetric.apply(value)) {
            value = secondMetric.getMetric(decMetric, context);
            if (switchToSecondMetric.apply(value)) {
                return shortName;
            } else {
                return secondMetric.getLabelName(decMetric, context);
            }
        } else {
            return firstMetric.getLabelName(decMetric, context);
        }
    }

    @Override
    public boolean computeForNode(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        return firstMetric.computeForNode(decMetric, target)
                || secondMetric.computeForNode(decMetric, target);
        
    }

    @Override
    public MetricValue getMetric(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        MetricValue value = firstMetric.getMetric(decMetric, target);
        if (switchToSecondMetric.apply(value)) {
            value = secondMetric.getMetric(decMetric, target);
        }
        return value;
    }

    @Override
    public double getMetricValue(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        return valueConvertor.toValue(getMetric(decMetric, target));
    }

    @Override
    public String getMetricValueString(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        return valueConvertor.toString(getMetric(decMetric, target));
    }

    @Override
    public IMetricValueConvertor getValueConvertor() {
        return valueConvertor;
    }

    @Override
    public IMetricValueConvertor getValueConvertor(StatMode statMode) {
        valueConvertor.setStatMode(statMode);
        return valueConvertor;
    }

    // Edge interface
    
    @Override
    public String getLabelName(IMetricsDecorator<IEPTreeNode> decMetric,
            Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        MetricValue value = firstMetric.getMetric(decMetric, from, to);
        if (switchToSecondMetric.apply(value)) {
            value = secondMetric.getMetric(decMetric, from, to);
            if (switchToSecondMetric.apply(value)) {
                return shortName;
            } else {
                return secondMetric.getLabelName(decMetric, from, to);
            }
        } else {
            return firstMetric.getLabelName(decMetric, from, to);
        }
    }

    @Override
    public boolean computeForEdge(IMetricsDecorator<IEPTreeNode> decMetric,
            Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        return firstMetric.computeForEdge(decMetric, from, to)
            || secondMetric.computeForEdge(decMetric, from, to);
    }

    @Override
    public MetricValue getMetric(IMetricsDecorator<IEPTreeNode> decMetric,
            Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        MetricValue value = firstMetric.getMetric(decMetric, from, to);
        if (switchToSecondMetric.apply(value)) {
            value = secondMetric.getMetric(decMetric, from, to);
        }
        return value;
    }

    @Override
    public double getMetricValue(IMetricsDecorator<IEPTreeNode> decMetric,
            Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        return valueConvertor.toValue(getMetric(decMetric, from, to));
    }

    @Override
    public String getMetricValueString(IMetricsDecorator<IEPTreeNode> decMetric, 
            Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        return valueConvertor.toString(getMetric(decMetric, from, to));
    }

}
