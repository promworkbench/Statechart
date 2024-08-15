package org.processmining.algorithms.statechart.discovery;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ContextPath {

    private final List<String> contextPath;
    private final Set<String> contextSymbols;

    public ContextPath() {
        contextPath = new ArrayList<>();
        contextSymbols = new THashSet<>();
    }

    private ContextPath(ContextPath original) {
        contextPath = new ArrayList<>(original.contextPath);
        contextSymbols = new THashSet<>(original.contextSymbols);
    }

    public boolean isInContext(String symbol) {
        return contextSymbols.contains(symbol);
    }

    public ContextPath getSubcontext(String symbol) {
        ContextPath newContext = new ContextPath(this);
        boolean delete = false;
        for (Iterator<String> it = newContext.contextPath.iterator(); it
                .hasNext();) {
            String current = it.next();
            if (!delete && current.equals(symbol)) {
                delete = true;
            } else if (delete) {
                newContext.contextSymbols.remove(current);
                it.remove();
            }
        }
        return newContext;
    }

    public ContextPath deriveContext(String symbol) {
        ContextPath newContext = new ContextPath(this);
        newContext.contextPath.add(symbol);
        newContext.contextSymbols.add(symbol);
        return newContext;
    }

    @Override
    public int hashCode() {
        return contextPath.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ContextPath) {
            return contextPath.equals(((ContextPath) o).contextPath);
        }
        return false;
    }

    @Override
    public String toString() {
        return contextPath.toString();
    }
}
