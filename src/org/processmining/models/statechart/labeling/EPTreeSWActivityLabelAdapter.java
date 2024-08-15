package org.processmining.models.statechart.labeling;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.models.statechart.decorate.swapp.SwAppDecoration;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class EPTreeSWActivityLabelAdapter implements Function<Pair<IEPTree, IEPTreeNode>, String> {

    private IActivityLabeler labeler;

    public EPTreeSWActivityLabelAdapter(IActivityLabeler labeler) {
        this.labeler = labeler;
    }
    
    @Override
    public String apply(Pair<IEPTree, IEPTreeNode> input) {
        Optional<SwAppDecoration> sw = LabelingUtil.getSwAppDecoration(input.getLeft(), input.getRight());
        if (sw.isPresent()) {
            return labeler.getLabel(sw.get().getJoinpoint());
        }
        return labeler.getLabel(input.getRight().getLabel());
    }

}
