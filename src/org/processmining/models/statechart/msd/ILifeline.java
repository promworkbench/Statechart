package org.processmining.models.statechart.msd;

import java.util.Collection;

public interface ILifeline {

    public String getName();
    
    public Collection<IActivation> getActivations();
    
    public LifelineType getLifelineType();

    public void setRank(int rank);
    
    public int getRank();
}
