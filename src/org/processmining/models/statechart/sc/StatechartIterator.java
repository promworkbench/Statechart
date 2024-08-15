package org.processmining.models.statechart.sc;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class StatechartIterator implements Iterator<ISCRegion>, Iterable<ISCRegion> {

    private Deque<ISCRegion> horizon = new ArrayDeque<>();

    public StatechartIterator(ISCRegion root) {
        horizon.add(root);
    }
    
    public StatechartIterator(ISCCompositeState root) {
        horizon.addAll(root.getRegions());
    }
    
    @Override
    public Iterator<ISCRegion> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return !horizon.isEmpty();
    }

    @Override
    public ISCRegion next() {
        ISCRegion element = horizon.removeFirst();
        
        for (ISCState state : element.getStates()) {
            if (state instanceof ISCCompositeState) {
                horizon.addAll(((ISCCompositeState) state).getRegions());
            }
        }
        
        return element;
    }

    @Override
    public void remove() {
        throw new NotImplementedException();
    }

}
