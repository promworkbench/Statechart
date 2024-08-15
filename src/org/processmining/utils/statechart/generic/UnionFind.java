package org.processmining.utils.statechart.generic;

import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectByteHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Union Find data structure
 * @author mleemans
 *
 * @param <E> type of elements in set
 * @see http://algs4.cs.princeton.edu/15uf/UF.java.html
 * @see https://www.cs.princeton.edu/~rs/AlgsDS07/01UnionFind.pdf
 */
public class UnionFind<E> {

    private static final byte ByteZero = 0;
    private static final byte ByteOne = 1;

    private Map<E, E> parent;  // parent[i] = parent of i
    private TObjectByteMap<E> rank;   // rank[i] = rank of subtree rooted at i (never more than 31)
    private int count;     // number of components
    
    public UnionFind() {
        parent = new THashMap<>();
        rank = new TObjectByteHashMap<>();
        count = 0;
    }
    
    /**
     * Add element p to the dataset
     * @param p
     */
    public void add(E p) {
        parent.put(p, p);
        rank.put(p, ByteZero);
        count++;
    }
    
    /**
     * Get all components
     * @return
     */
    public Collection<Collection<E>> getComponents() {
        Map<E, Collection<E>> components = new THashMap<>();
        
        for (E element : parent.keySet()) {
            E root = find(element);
            Collection<E> list = components.get(root);
            if (list == null) {
                list = new ArrayList<>();
                components.put(root, list);
            }
            list.add(element);
        }
        
        return components.values();
    }
    
    /**
     * Returns the component identifier for the component containing site {@code p}.
     *
     * @param  p the integer representing one site
     * @return the component identifier for the component containing site {@code p}
     * @throws IllegalArgumentException unless {@code 0 <= p < n}
     */
    public E find(E p) {
        validate(p);
        while (p != parent.get(p)) {
            parent.put(p, parent.get(parent.get(p)));    // path compression by halving
            p = parent.get(p);
        }
        return p;
    }
    
    /**
     * Returns the number of components.
     *
     * @return the number of components (between {@code 1} and {@code n})
     */
    public int count() {
        return count;
    }
    
    /**
     * Returns true if the the two sites are in the same component.
     *
     * @param  p the integer representing one site
     * @param  q the integer representing the other site
     * @return {@code true} if the two sites {@code p} and {@code q} are in the same component;
     *         {@code false} otherwise
     * @throws IllegalArgumentException unless
     *         both {@code 0 <= p < n} and {@code 0 <= q < n}
     */
    public boolean connected(E p, E q) {
        return find(p) == find(q);
    }
    
    /**
     * Merges the component containing site {@code p} with the 
     * the component containing site {@code q}.
     *
     * @param  p the integer representing one site
     * @param  q the integer representing the other site
     * @throws IllegalArgumentException unless
     *         both {@code 0 <= p < n} and {@code 0 <= q < n}
     */
    public void union(E p, E q) {
        E rootP = find(p);
        E rootQ = find(q);
        if (rootP == rootQ) return;

        // make root of smaller rank point to root of larger rank
        if      (rank.get(rootP) < rank.get(rootQ)) { parent.put(rootP, rootQ); }
        else if (rank.get(rootP) > rank.get(rootQ)) { parent.put(rootQ, rootP); }
        else {
            parent.put(rootQ, rootP);
            rank.adjustValue(rootP, ByteOne);
        }
        count--;
    }

    // validate that p is a valid index
    private void validate(E p) {
        if (!parent.containsKey(p)) {
            throw new IllegalArgumentException("element " + p + " is not in the UnionFind datastructure");  
        }
    }
}
