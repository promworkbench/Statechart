package org.processmining.utils.statechart.generic;

import gnu.trove.set.hash.THashSet;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;

public class SetUtil {

    public static <T> Set<T> createSet(T[] it) {
        Set<T> set = new HashSet<T>();
        for (T elm : it) {
            set.add(elm);
        }
        return set;
    }
    
    public static <T> Set<T> createSet(Iterable<T> it) {
        Set<T> set = new HashSet<T>();
        for (T elm : it) {
            set.add(elm);
        }
        return set;
    }
    
    public static <E> Set<Set<E>> generateSubsets(Set<E> input) {
        return generateSubsets(input, 0, input.size());
    }
    
    public static <E> Set<Set<E>> generateSubsets(Set<E> input, int minSubsetSize, int maxSubsetSize) {
        // check
        Preconditions.checkArgument(0 <= minSubsetSize, "Required: 0 <= minSubsetSize");
        Preconditions.checkArgument(minSubsetSize <= maxSubsetSize, "Required: minSubsetSize <= maxSubsetSize");
        Preconditions.checkArgument(maxSubsetSize <= input.size(), "Required: maxSubsetSize <= input.size()");
        
        // construct
        Set<Set<E>> result = new THashSet<Set<E>>();

        // construct emptyset
        Set<E> emptyset = new THashSet<E>();
        if (minSubsetSize == 0) {
            result.add(emptyset);
        }
        
        Set<Set<E>> prevSubs = new THashSet<Set<E>>();
        prevSubs.add(emptyset);
        
        // generate larger subsets
        for (int i = 1; i <= maxSubsetSize; i++) {
            // generate sets of size i from sets of size i - 1
            Set<Set<E>> newSubs = new THashSet<Set<E>>();
            for (Set<E> subset : prevSubs) {
                for (E elm : input) {
                    if (!subset.contains(elm)) {
                        Set<E> newSubset = new THashSet<E>(subset.size() + 1);
                        newSubset.addAll(subset);
                        newSubset.add(elm);
                        
                        newSubs.add(newSubset);
                    }
                }
            }
            if (minSubsetSize <= i) {
                result.addAll(newSubs);
            }
            prevSubs = newSubs;
        }
        
        return result;
    }

    public static <T> boolean hasIntersection(Set<T> A, Set<T> B) {
        if (A == null || B == null) {
            return false;
        }
        for (T a : A) {
            if (B.contains(a)) {
                return true;
            }
        }
        return false;
    }

}
