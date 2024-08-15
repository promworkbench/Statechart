package org.processmining.recipes.statechart.cancel;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.l2l.HandleActivityUtil;
import org.processmining.models.statechart.log.HierarchyActivityInfo;
import org.processmining.recipes.statechart.AbstractRecipe;

public class CancelNestedHandleRecipe extends
    AbstractRecipe<Pair<XLog, HierarchyActivityInfo>, Set<String>, Void> {

    public CancelNestedHandleRecipe() {
        super(null);
    }

    @Override
    protected Set<String> execute(Pair<XLog, HierarchyActivityInfo> input) {
        Set<String> values = input.getRight().getActivities();
        
        Set<String> errors = new THashSet<String>();
        for (String value : values) {
            if (HandleActivityUtil.isHandleActivity(value)) {
                errors.add(value);
            }
        }
        
        return errors;
    }

}
