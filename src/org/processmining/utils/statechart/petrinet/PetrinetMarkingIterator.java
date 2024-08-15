package org.processmining.utils.statechart.petrinet;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * 
 * @author mleemans
 *
 * Iterates over all reachable markings (set behavior, 1 token per place)
 */
public class PetrinetMarkingIterator implements Iterator<Set<Place>>, Iterable<Set<Place>>  {

    private final Deque<Set<Place>> stack;
    private final Set<Set<Place>> processed;
    private PetrinetGraph net;
    
    private boolean includeEnd;
    private Set<Place> end;
    
    public PetrinetMarkingIterator(PetrinetGraph net, Place start) {
        this(net, Collections.singleton(start), Collections.<Place>emptySet(), false);
    }
    
    public PetrinetMarkingIterator(PetrinetGraph net, Place start, Place end, boolean includeEnd) {
        this(net, Collections.singleton(start), Collections.singleton(end), includeEnd);
    }
    
    public PetrinetMarkingIterator(PetrinetGraph net, Set<Place> start, Set<Place> end, boolean includeEnd) {
        stack = new ArrayDeque<>();
        processed = new THashSet<>();
        
        this.net = net;
        stack.add(start);
        processed.add(start);
        processed.add(end);
        
        this.includeEnd = includeEnd;
        this.end = end;
    }
    
    @Override
    public Iterator<Set<Place>> iterator() {
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
    public Set<Place> next() {
        if (stack.isEmpty() && includeEnd) {
            includeEnd = false;
            return end;
        }
        Set<Place> current = stack.pop();
        
        for (Place p : current) {
            for (Transition t : PetrinetGraphUtils.getOut(net, p)) {
                Set<Place> inP = PetrinetGraphUtils.getInSet(net, t);
                if (current.containsAll(inP)) {
                    Set<Place> next = new HashSet<>(current);
                    next.removeAll(inP);
                    next.addAll(PetrinetGraphUtils.getOutSet(net, t));
                    if (!processed.contains(next)) {
                        stack.push(next);
                        processed.add(next);
                    }
                }
            }
        }
        
        return current;
    }

}
