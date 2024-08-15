package org.processmining.utils.statechart.svg;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;

public class SVGIterator implements Iterator<SVGElement>, Iterable<SVGElement> {

    private List<?> retriever = new ArrayList<>();
    private Deque<SVGElement> horizon = new ArrayDeque<>();
    
    public SVGIterator(SVGDiagram svg) {
        this(svg.getRoot());
    }

    public SVGIterator(SVGElement root) {
        horizon.add(root);
    }

    @Override
    public Iterator<SVGElement> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return !horizon.isEmpty();
    }

    @Override
    public SVGElement next() {
        SVGElement element = horizon.removeFirst();
        
        retriever.clear();
        element.getChildren(retriever);
        for (int i = 0; i < retriever.size(); i++) {
            horizon.addLast((SVGElement) retriever.get(i));
        }
        
        return element;
    }

    @Override
    public void remove() {
        throw new NotImplementedException();
    }

}
