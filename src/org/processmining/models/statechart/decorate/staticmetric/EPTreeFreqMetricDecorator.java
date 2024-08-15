package org.processmining.models.statechart.decorate.staticmetric;

import org.processmining.models.statechart.decorate.AbstractDecorator;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecorator;
import org.processmining.models.statechart.decorate.staticmetric.processtree.PropertyAbsoluteFrequency;
import org.processmining.models.statechart.decorate.staticmetric.processtree.PropertyCaseFrequency;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.processtree.Node;

import com.google.common.base.Preconditions;

public class EPTreeFreqMetricDecorator extends
        AbstractFreqMetricDecorator<IEPTreeNode> {

    @Override
    public AbstractDecorator<IEPTreeNode, FreqMetric> newInstance() {
        return new EPTreeFreqMetricDecorator();
    }

    @Override
    public <T2> IDecorator<T2, FreqMetric> deriveDecorationInstance(Class<T2> type) {
        return new DerivedFreqMetricDecorator<T2>();
    }

    @Override
    public void copyDecoration(IEPTreeNode target, IEPTreeNode oldTarget,
            IDecorator<IEPTreeNode, ?> oldDecorator) {
        FreqMetric old = (FreqMetric) oldDecorator.getDecoration(oldTarget);
        if (old != null) {
            setDecoration(target, new FreqMetric(old));
        }
    }

    @Override
    public void deriveDecoration(IEPTreeNode target, Object oldTarget,
            Decorations<?> oldDecorations) {
        Preconditions.checkNotNull(oldTarget);

        if (oldTarget instanceof Node) {
            Node node = (Node) oldTarget;
            try {
                int freqAbsolute = PropertyAbsoluteFrequency.getValue(node);
                int freqCase = PropertyCaseFrequency.getValue(node);

                if (freqAbsolute != Integer.MIN_VALUE
                        && freqCase != Integer.MIN_VALUE) {
                    setDecoration(target,
                            new FreqMetric(freqAbsolute, freqCase));
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            } catch (InstantiationException e) {
                throw new IllegalStateException(e);
            }
        } else {
            throw new IllegalArgumentException("Cannot derive from "
                    + oldTarget.getClass());
        }
    }

}
