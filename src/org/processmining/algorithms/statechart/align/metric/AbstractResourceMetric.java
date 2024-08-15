package org.processmining.algorithms.statechart.align.metric;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.processmining.algorithms.statechart.align.metric.logic.NoModelMovePred;
import org.processmining.algorithms.statechart.align.metric.time.MoveTimeCmp;
import org.processmining.algorithms.statechart.align.metric.value.MetricValueConvertorDefault;
import org.processmining.algorithms.statechart.align.metric.value.MetricValueConvertorDefault.StringPostFix;
import org.processmining.models.statechart.decorate.align.metric.IMetricsDecorator;
import org.processmining.models.statechart.decorate.align.metric.MetricValue;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public abstract class AbstractResourceMetric extends AbstractMetric implements IResourceMetric {

    protected String resourceAttribute;

    protected MoveTimeCmp moveCmp;
    protected NoModelMovePred predSync;

    private Set<IMetricsDecorator<IEPTreeNode>> decorators = new THashSet<>();

    public AbstractResourceMetric(String id, String name, String shortName) {
        this(XOrganizationalExtension.KEY_RESOURCE, id, name, shortName);
    }
    
    public AbstractResourceMetric(String resourceAttribute, String id, String name, String shortName) {
        super(id, name, shortName, new MetricValueConvertorDefault(StringPostFix.Scale));
        this.resourceAttribute = resourceAttribute;
    }

    @Override
    public void setResourceAttribute(String resourceAttribute) {
        this.resourceAttribute = resourceAttribute;
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
    public String getResourceAttribute() {
        return resourceAttribute;
    }

}
