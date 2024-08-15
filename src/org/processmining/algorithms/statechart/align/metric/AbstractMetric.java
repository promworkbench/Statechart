package org.processmining.algorithms.statechart.align.metric;

import java.util.Set;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.algorithms.statechart.align.metric.value.MetricValueConvertorDefault;
import org.processmining.algorithms.statechart.align.metric.value.MetricValueConvertorDefault.StringPostFix;
import org.processmining.models.statechart.decorate.align.AlignMappingTreeDecorator;
import org.processmining.models.statechart.decorate.align.ExecIntervalTreeDecorator;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricDouble;
import org.processmining.models.statechart.decorate.align.metric.MetricLong;
import org.processmining.models.statechart.decorate.align.metric.MetricStat;
import org.processmining.models.statechart.decorate.align.metric.MetricValue;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public abstract class AbstractMetric implements IMetric {

    private final MetricId id;
    private final String name, shortName;
    
    protected ExecIntervalTreeDecorator decInt;

    protected IMetricValueConvertor valueConvertor;
    private AlignMappingTreeDecorator decAlignMap;

    public AbstractMetric(String id, String name, String shortName) {
        this(id, name, shortName, new MetricValueConvertorDefault(StringPostFix.Max));
    }
    
    public AbstractMetric(String id, String name, String shortName, IMetricValueConvertor valueConvertor) {
        this.id = new MetricId(id);
        this.name = name;
        this.shortName = shortName;
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
    
    @Override
    public void setDecorators(ExecIntervalTreeDecorator decInt, 
            AlignMappingTreeDecorator decAlignMap) {
        this.decInt = decInt;
        this.decAlignMap = decAlignMap;
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

    protected boolean moveEqualsNode(XAlignmentMove move, IEPTreeNode node) {
        Set<String> dec = decAlignMap.getDecoration(node);
        return dec != null && dec.contains(move.getActivityId());
    }

    protected boolean moveInNodes(XAlignmentMove move, Set<IEPTreeNode> nodes) {
        if (move != null) {
            String act = move.getActivityId();
            for (IEPTreeNode node : nodes) {
                Set<String> dec = decAlignMap.getDecoration(node);
                if (dec != null && dec.contains(act)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // Node interface

    @Override
    public String getLabelName(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode context) {
        return shortName;
    }

    @Override
    public MetricValue getMetric(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        return decMetric.getMetric(target, getId());
    }

    @Override
    public double getMetricValue(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        return valueConvertor.toValue(getMetric(decMetric, target));
    }

    @Override
    public String getMetricValueString(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target) {
        return valueConvertor.toString(getMetric(decMetric, target));
    }

    protected void setMetric(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target, long value) {
        setMetric(decMetric, target, new MetricLong(getId(), value));
    }
    
    protected void setMetric(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target, double value) {
        setMetric(decMetric, target, new MetricDouble(getId(), value));
    }
    
    protected void setMetric(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target, StatisticalSummary value) {
        setMetric(decMetric, target, new MetricStat(getId(), value));
    }

    protected void setMetric(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target, MetricValue value) {
        decMetric.setMetric(target, getId(), value);
    }

    // Edge interface

    @Override
    public String getLabelName(IMetricsDecorator<IEPTreeNode> decMetric,
            Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        return shortName;
    }
    
    @Override
    public MetricValue getMetric(IMetricsDecorator<IEPTreeNode> decMetric, Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        return decMetric.getMetric(from, to, getId());
    }

    @Override
    public double getMetricValue(IMetricsDecorator<IEPTreeNode> decMetric, Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        return valueConvertor.toValue(getMetric(decMetric, from, to));
    }

    @Override
    public String getMetricValueString(IMetricsDecorator<IEPTreeNode> decMetric, Set<IEPTreeNode> from, Set<IEPTreeNode> to) {
        return valueConvertor.toString(getMetric(decMetric, from, to));
    }

    protected void setMetric(IMetricsDecorator<IEPTreeNode> decMetric, Set<IEPTreeNode> from, Set<IEPTreeNode> to, long value) {
        setMetric(decMetric, from, to, new MetricLong(getId(), value));
    }
    
    protected void setMetric(IMetricsDecorator<IEPTreeNode> decMetric, Set<IEPTreeNode> from, Set<IEPTreeNode> to, double value) {
        setMetric(decMetric, from, to, new MetricDouble(getId(), value));
    }
    
    protected void setMetric(IMetricsDecorator<IEPTreeNode> decMetric, Set<IEPTreeNode> from, Set<IEPTreeNode> to, StatisticalSummary value) {
        setMetric(decMetric, from, to, new MetricStat(getId(), value));
    }

    protected void setMetric(IMetricsDecorator<IEPTreeNode> decMetric, Set<IEPTreeNode> from, Set<IEPTreeNode> to, MetricValue value) {
        decMetric.setMetric(from, to, getId(), value);
    }

}
