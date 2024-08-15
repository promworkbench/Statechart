package org.processmining.models.statechart.msd;


public class Activation implements IActivation {

    private final ILifeline lifeline;

    public Activation(ILifeline lifeline) {
        this.lifeline = lifeline;
    }

    @Override
    public ILifeline getLifeline() {
        return lifeline;
    }

}
