package org.processmining.models.statechart.decorate.align;

import gnu.trove.set.hash.THashSet;

import java.util.Map;
import java.util.Set;

import org.processmining.models.statechart.align.XAlignedTreeLog;
import org.processmining.models.statechart.decorate.AbstractDecorator;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public class AlignMappingTreeDecorator extends AbstractDecorator<IEPTreeNode, Set<String>> {

    @Override
    public IDecorator<IEPTreeNode, Set<String>> newInstance() {
        return new AlignMappingTreeDecorator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T2> IDecorator<T2, Set<String>> deriveDecorationInstance(
            Class<T2> type) {
        if (type.equals(IEPTreeNode.class)) {
            return (IDecorator<T2, Set<String>>) new AlignMappingTreeDecorator();
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
        throw new IllegalArgumentException("Cannot derive from "
                + oldTarget.getClass());
    }

    public void extractFrom(XAlignedTreeLog align) {
        Map<String, IEPTreeNode> map = align.getMapTreeNodes();
        for (String actId : map.keySet()) {
            IEPTreeNode node = map.get(actId);
            Set<String> decs = decorations.get(node);
            if (decs == null) {
                decs = new THashSet<String>();
                decorations.put(node, decs);
            }
            decs.add(actId);
        }
    }

}
