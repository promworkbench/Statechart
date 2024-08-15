package org.processmining.models.statechart.labeling;

import org.processmining.models.statechart.decorate.swapp.EPTreeSwAppDecorator;
import org.processmining.models.statechart.decorate.swapp.SwAppDecoration;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

import com.google.common.base.Optional;

public class LabelingUtil {

    public static Optional<SwAppDecoration> getSwAppDecoration(IEPTree tree, IEPTreeNode node) {
        EPTreeSwAppDecorator decorator = tree.getDecorations()
                .getForType(EPTreeSwAppDecorator.class);
        if (decorator != null) {
            return Optional.fromNullable(decorator.getDecoration(node));
        }
        return Optional.absent();
    }
    
}
