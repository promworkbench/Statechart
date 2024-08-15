package org.processmining.models.statechart.decorate.staticmetric;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.processmining.models.statechart.decorate.AbstractDecorator;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.sc.ISCState;

import com.google.common.base.Preconditions;

public class DerivedFreqMetricDecorator<S> extends AbstractFreqMetricDecorator<S> {

    private static final Logger logger = LogManager
            .getLogger(DerivedFreqMetricDecorator.class.getName());
    
    @Override
    public AbstractDecorator<S, FreqMetric> newInstance() {
        return new DerivedFreqMetricDecorator<S>();
    }

    @Override
    public <T2> IDecorator<T2, FreqMetric> deriveDecorationInstance(Class<T2> type) {
        return null;
    }

    @Override
    public void copyDecoration(S target, S oldTarget,
            IDecorator<S, ?> oldDecorator) {
        FreqMetric old = (FreqMetric) oldDecorator.getDecoration(oldTarget);
        if (old != null) {
            setDecoration(target, new FreqMetric(old));
        }
    }

    @Override
    public void deriveDecoration(S target, Object oldTarget,
            Decorations<?> oldDecorations) {
        Preconditions.checkNotNull(oldTarget);
        Preconditions.checkNotNull(oldDecorations);

        if (oldTarget instanceof IEPTreeNode) {
            IEPTreeNode node = (IEPTreeNode) oldTarget;
            EPTreeFreqMetricDecorator oldDecorator = oldDecorations
                    .getForType(EPTreeFreqMetricDecorator.class);

            if (oldDecorator != null) {
                if (target instanceof ISCState
                        && ((ISCState) target).getStateType().isPseudostate()
                        && !node.getChildren().isEmpty()) {
                    _deriveForSubtreeMax(target, node, oldDecorator);
                } else {
                    _deriveForNode(target, node, oldDecorator);
                }
            }
        } else {
            throw new IllegalArgumentException("Cannot derive from "
                    + oldTarget.getClass());
        }
    }

    private void _deriveForSubtreeMax(S target, IEPTreeNode node,
            EPTreeFreqMetricDecorator oldDecorator) {
        int freqAbsolute = 0;
        int freqCase = 0;

        for (IEPTreeNode child : node.getChildren()) {
            FreqMetric original = oldDecorator.getDecoration(child);
            if (original == null) {
                logger.warn("SCFreqMetricDecorator::_deriveForSubtreeMax() - No original decoration, should not happen?");
            } else {
                freqAbsolute = Math.max(freqAbsolute, original.getFreqAbsolute());
                freqCase = Math.max(freqCase, original.getFreqCase());
            }
        }

        setDecoration(target, new FreqMetric(freqAbsolute, freqCase));
    }

    private void _deriveForNode(S target, IEPTreeNode node,
            EPTreeFreqMetricDecorator oldDecorator) {
        FreqMetric original = oldDecorator.getDecoration(node);
        if (original != null) {
            setDecoration(target, new FreqMetric(original));
        }
    }
}
