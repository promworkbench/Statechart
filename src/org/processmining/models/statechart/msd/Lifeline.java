package org.processmining.models.statechart.msd;

import java.util.ArrayList;
import java.util.Collection;

public class Lifeline implements ILifeline {

    private final String name;
    private final Collection<IActivation> activations = new ArrayList<>();
    private final LifelineType type;
    private int rank;

    public Lifeline(String name, LifelineType type) {
        this.name = name;
        this.type = type;
    }
    
    @Override
    public String getName() {
        return name;
    }

    public void addActivations(IActivation val) {
        activations.add(val);
    }
    
    @Override
    public Collection<IActivation> getActivations() {
        return activations;
    }

    public Activation createActivation() {
        Activation act = new Activation(this);
        addActivations(act);
        return act;
    }

    @Override
    public LifelineType getLifelineType() {
        return type;
    }

    @Override
    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public int getRank() {
        return rank;
    }

}
