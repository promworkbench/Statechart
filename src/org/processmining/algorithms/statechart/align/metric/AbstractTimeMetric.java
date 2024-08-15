package org.processmining.algorithms.statechart.align.metric;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.processmining.algorithms.statechart.align.metric.logic.NoModelMovePred;
import org.processmining.algorithms.statechart.align.metric.time.Event2TimeTimestamp;
import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;
import org.processmining.algorithms.statechart.align.metric.time.MoveTimeCmp;
import org.processmining.algorithms.statechart.align.metric.value.MetricValueConvertorDefault.StringPostFix;
import org.processmining.algorithms.statechart.align.metric.value.MetricValueConvertorTime;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricValue;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public abstract class AbstractTimeMetric extends AbstractMetric implements ITimeMetric {

    protected IEvent2Time event2time;

    protected MoveTimeCmp moveCmp;
    protected NoModelMovePred predNoModelMove;

    private Set<IMetricsDecorator<IEPTreeNode>> decorators = new THashSet<>();

    public AbstractTimeMetric(String id, String name, String shortName) {
        this(new Event2TimeTimestamp(), id, name, shortName);
    }
    
    public AbstractTimeMetric(IEvent2Time event2time, String id, String name, String shortName) {
        super(id, name, shortName, new MetricValueConvertorTime(StringPostFix.Scale));
        this.event2time = event2time;
        this.predNoModelMove = new NoModelMovePred();
        this.moveCmp = new MoveTimeCmp(event2time);
    }


    @Override
    public void setEvent2Time(IEvent2Time event2time) {
        this.event2time = event2time;
        moveCmp.setEvent2Time(event2time);
        for (IMetricsDecorator<IEPTreeNode> decMetric : decorators) {
            decMetric.resetMetric(getId());
        }
        decorators.clear();
    }

    @Override
    protected void setMetric(IMetricsDecorator<IEPTreeNode> decMetric, IEPTreeNode target, MetricValue value) {
        super.setMetric(decMetric, target, value);
        decorators.add(decMetric);
    }
    
    @Override
    protected void setMetric(IMetricsDecorator<IEPTreeNode> decMetric, Set<IEPTreeNode> from, Set<IEPTreeNode> to, MetricValue value) {
        decMetric.setMetric(from, to, getId(), value);
        decorators.add(decMetric);
    }
    
    @Override
    public IEvent2Time getEvent2Time() {
        return event2time;
    }
}
