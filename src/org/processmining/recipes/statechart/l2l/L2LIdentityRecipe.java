package org.processmining.recipes.statechart.l2l;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class L2LIdentityRecipe extends AbstractL2LRecipe<Void> {

    public L2LIdentityRecipe() {
        super(null);
    }

    @Override
    protected XLog execute(XLog input) {
        return input;
    }

    @Override
    public XTrace apply(XTrace input) {
        return input;
    }
}
