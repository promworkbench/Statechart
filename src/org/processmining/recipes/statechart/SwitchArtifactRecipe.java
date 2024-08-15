package org.processmining.recipes.statechart;

import org.apache.commons.lang3.tuple.Pair;

public class SwitchArtifactRecipe<T> 
    extends AbstractRecipe<Pair<T, T>, T, SwitchArtifactRecipe.Parameters> {

    public static class Parameters {
        public boolean useFirst = true;
    }
    
    public SwitchArtifactRecipe() {
        super(new Parameters());
    }
    
    public void setUseFirst() {
        getParameters().useFirst = true;
    }
    
    public void setUseSecond() {
        getParameters().useFirst = false;
    }

    public boolean isUseFirst() {
        return getParameters().useFirst;
    }

    public boolean isUseSecond() {
        return !getParameters().useFirst;
    }
    
    @Override
    protected T execute(Pair<T, T> input) {
        if (getParameters().useFirst) {
            return input.getLeft();
        } else {
            return input.getRight();
        }
    }

}
