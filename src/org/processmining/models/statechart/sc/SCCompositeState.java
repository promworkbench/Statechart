package org.processmining.models.statechart.sc;

import java.util.ArrayList;
import java.util.List;

public class SCCompositeState extends SCState implements ISCCompositeState {

    private final List<ISCRegion> regions;
    
    public SCCompositeState(ISCRegion parentRegion) {
        this(parentRegion, SCStateType.OrComposite);
    }

    public SCCompositeState(ISCRegion parentRegion, SCStateType type) {
        this(parentRegion, type, "");
    }

    public SCCompositeState(ISCRegion parentRegion, SCStateType type, String label) {
        this(parentRegion, type, label, genNewId());
    }

    public SCCompositeState(ISCRegion parentRegion, SCStateType type, String label, String id) {
        super(parentRegion, type, label, id);

        regions = new ArrayList<ISCRegion>();
    }

    @Override
    public void addRegion(ISCRegion region) {
        regions.add(region);
    }

    @Override
    public List<ISCRegion> getRegions() {
        return regions;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        
        buf.append("(");
        String sep = "";
        for (ISCRegion child : getRegions()) {
            buf.append(sep);
            buf.append(child.toString());
            sep = ", ";
        }
        buf.append(")");
        
        return buf.toString();
    }

    @Override
    public List<ISCState> getChildren() {
        int size = 0;
        for (ISCRegion region : regions) {
            size += region.getStates().size();
        }
        List<ISCState> children = new ArrayList<>(size);
        for (ISCRegion region : regions) {
            children.addAll(region.getStates());
        }
        return children;
    }

}
