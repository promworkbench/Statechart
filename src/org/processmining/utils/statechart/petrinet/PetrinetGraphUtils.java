package org.processmining.utils.statechart.petrinet;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.utils.statechart.generic.SetUtil;

public class PetrinetGraphUtils {

    public static Iterable<Place> getIn(PetrinetGraph net, Transition element) {
        return new PetrinetGraphConnectionIterator<Place>(net, element, true);
    }
    
    public static Iterable<Transition> getIn(PetrinetGraph net, Place element) {
        return new PetrinetGraphConnectionIterator<Transition>(net, element, true);
    }
    
    public static Iterable<Place> getOut(PetrinetGraph net, Transition element) {
        return new PetrinetGraphConnectionIterator<Place>(net, element, false);
    }
    
    public static Iterable<Transition> getOut(PetrinetGraph net, Place element) {
        return new PetrinetGraphConnectionIterator<Transition>(net, element, false);
    }

    public static Set<Place> getInSet(PetrinetGraph net, Transition element) {
        return SetUtil.createSet(getIn(net, element));
    }
    
    public static Set<Transition> getInSet(PetrinetGraph net, Place element) {
        return SetUtil.createSet(getIn(net, element));
    }
    
    public static Set<Place> getOutSet(PetrinetGraph net, Transition element) {
        return SetUtil.createSet(getOut(net, element));
    }
    
    public static Set<Transition> getOutSet(PetrinetGraph net, Place element) {
        return SetUtil.createSet(getOut(net, element));
    }
    
    public static Set<Place> getFinalPlaceSet(PetrinetGraph net) {
        Place minOut = null;
        int minOutDegree = Integer.MAX_VALUE;
        
        Set<Place> result = new THashSet<Place>();
        for (Place p : net.getPlaces()) {
            Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges = net.getOutEdges(p);
            if (outEdges.size() < minOutDegree) {
                minOut = p;
                minOutDegree = outEdges.size();
            }
            if (outEdges.isEmpty()) {
                result.add(p);
            }
        }
        if (result.isEmpty() && minOut != null) {
            result.add(minOut);
        }
        return result;
    }
}
