package org.processmining.models.statechart.sc;

import java.util.List;

public interface ISCCompositeState extends ISCState {

    public void addRegion(ISCRegion region);

    public List<ISCRegion> getRegions();
}
