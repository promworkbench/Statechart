package org.processmining.recipes.statechart.l2l;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.recipes.statechart.AbstractRecipe;

public abstract class AbstractL2LRecipe<P> extends 
    AbstractRecipe<XLog, XLog, P> {

    public AbstractL2LRecipe(P parameters) {
        super(parameters);
    }

    public abstract XTrace apply(XTrace input);
}
