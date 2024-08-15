package org.processmining.utils.statechart.petrinet;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class PetrinetPlaceIterator implements Iterator<Place>, Iterable<Place>  {

    private final Deque<Place> stack;
    private final Set<Place> processed;
    private PetrinetGraph net;
    
    private boolean includeEnd;
    private Deque<Place> end;
    
    public PetrinetPlaceIterator(PetrinetGraph net, Place start) {
        this(net, Collections.singleton(start), Collections.<Place>emptySet(), false);
    }
    
    public PetrinetPlaceIterator(PetrinetGraph net, Place start, Place end, boolean includeEnd) {
        this(net, Collections.singleton(start), Collections.singleton(end), includeEnd);
    }
    
    public PetrinetPlaceIterator(PetrinetGraph net, Set<Place> start, Set<Place> end, boolean includeEnd) {
        stack = new ArrayDeque<>();
        processed = new THashSet<>();
        
        this.net = net;
        stack.addAll(start);
        processed.addAll(start);
        processed.addAll(end);

        this.includeEnd = includeEnd;
        this.end = new ArrayDeque<>(end);
    }
    
    @Override
    public Iterator<Place> iterator() {
        return this;
    }
    
    @Override
    public void remove() {
        throw new NotImplementedException();
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty() || includeEnd;
    }

    @Override
    public Place next() {
        if (stack.isEmpty() && includeEnd) {
            Place current = end.pop();
            includeEnd = !end.isEmpty();
            return current;
        }
        Place current = stack.pop();
        
//        for (Place p : current) {
            for (Transition t : PetrinetGraphUtils.getOut(net, current)) {
//                Set<Place> inP = PetrinetGraphUtils.getInSet(net, t);
//                if (inP. current.containsAll(inP)) {
//                    Set<Place> next = new HashSet<>(current);
//                    next.removeAll(inP);
//                    next.addAll(PetrinetGraphUtils.getOutSet(net, t));
                for (Place p : PetrinetGraphUtils.getOutSet(net, t)) {
                    if (!processed.contains(p)) {
                        stack.push(p);
                        processed.add(p);
                    }
                }
            }
//        }
        
        return current;
    }
}
