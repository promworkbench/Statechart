package org.processmining.utils.statechart.petrinet;

import java.util.Iterator;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class PetrinetGraphConnectionIterator<T extends PetrinetNode> implements Iterator<T>, Iterable<T> {

    private Iterator<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges;
    private boolean inEdges;

    public PetrinetGraphConnectionIterator(PetrinetGraph net, PetrinetNode node, boolean inEdges) {
        this.inEdges = inEdges;
        if (inEdges) {
            this.edges = net.getInEdges(node).iterator();
        } else {
            this.edges = net.getOutEdges(node).iterator();
        }
    }
    
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return edges.hasNext();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T next() {
        PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge = edges.next();
        T current = null;
        if (inEdges) {
            current = (T) edge.getSource();
        } else {
            current = (T) edge.getTarget();
        }
        return current;
    }

    @Override
    public void remove() {
        throw new NotImplementedException();
    }

    
}
