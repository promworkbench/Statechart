package org.processmining.models.statechart.decorate.error;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.processmining.models.statechart.decorate.AbstractDecorator;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.processtree.Node;

import com.google.common.base.Preconditions;

public class EPTreeErrorTriggerDecorator extends AbstractDecorator<IEPTreeNode, Set<String>> {

    @Override
    public AbstractDecorator<IEPTreeNode, Set<String>> newInstance() {
        return new EPTreeErrorTriggerDecorator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T2> IDecorator<T2, Set<String>> deriveDecorationInstance(Class<T2> type) {
        if (type.equals(IEPTreeNode.class)) {
            return (IDecorator<T2, Set<String>>) new EPTreeErrorTriggerDecorator();
        }
        return null;
    }

    @Override
    public void copyDecoration(IEPTreeNode target, IEPTreeNode oldTarget,
            IDecorator<IEPTreeNode, ?> oldDecorator) {
        @SuppressWarnings("unchecked")
        Set<String> old = (Set<String>) oldDecorator.getDecoration(oldTarget);
        if (old != null) {
            setDecoration(target, new THashSet<String>(old));
        }
    }

    @Override
    public void deriveDecoration(IEPTreeNode target, Object oldTarget,
            Decorations<?> oldDecorations) {
        Preconditions.checkNotNull(oldTarget);

        if (oldTarget instanceof Node) {
            Node node = (Node) oldTarget;
            try {
                Set<String> old = PropertyErrorTrigger.getValue(node);

                if (old != null) {
                    setDecoration(target, new THashSet<String>(old));
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
