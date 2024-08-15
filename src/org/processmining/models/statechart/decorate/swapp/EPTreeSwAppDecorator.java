package org.processmining.models.statechart.decorate.swapp;

import org.processmining.models.statechart.decorate.AbstractDecorator;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.processtree.Node;

import com.google.common.base.Preconditions;

public class EPTreeSwAppDecorator extends
        AbstractDecorator<IEPTreeNode, SwAppDecoration> {

    @Override
    public AbstractDecorator<IEPTreeNode, SwAppDecoration> newInstance() {
        return new EPTreeSwAppDecorator();
    }

    @Override
    public <T2> IDecorator<T2, SwAppDecoration> deriveDecorationInstance(Class<T2> type) {
        return null;
    }
    
    @Override
    public void copyDecoration(IEPTreeNode target, IEPTreeNode oldTarget,
            IDecorator<IEPTreeNode, ?> oldDecorator) {
        SwAppDecoration old = (SwAppDecoration) oldDecorator
                .getDecoration(oldTarget);
        if (old != null) {
            setDecoration(target, new SwAppDecoration(old));
        }
    }

    @Override
    public void deriveDecoration(IEPTreeNode target, Object oldTarget,
            Decorations<?> oldDecorations) {
        Preconditions.checkNotNull(oldTarget);

        if (oldTarget instanceof Node) {
            Node node = (Node) oldTarget;
            try {
                SwAppDecoration val = SwAppProperty.getValue(node);

                if (val != null) {
                    setDecoration(target, new SwAppDecoration(val));
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
