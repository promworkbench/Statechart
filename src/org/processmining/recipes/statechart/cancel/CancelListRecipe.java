package org.processmining.recipes.statechart.cancel;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XLog;
import org.processmining.models.statechart.log.HierarchyActivityInfo;
import org.processmining.recipes.statechart.AbstractRecipe;

public class CancelListRecipe extends
    AbstractRecipe<Pair<XLog, HierarchyActivityInfo>, Set<String>, CancelListRecipe.Parameters> {

    public static class Parameters {
        public Set<String> oracle = new THashSet<>();
    }
    
    public CancelListRecipe() {
        super(new Parameters());
    }

    @Override
    protected Set<String> execute(Pair<XLog, HierarchyActivityInfo> input) {
        return getParameters().oracle;
    }

}
