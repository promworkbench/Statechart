package org.processmining.algorithms.statechart.discovery;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.List;

import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;

import com.google.common.base.Preconditions;

public class EPTreeCompareSame {

    public static boolean same(IEPTree expected, IEPTree actual) {
        Preconditions.checkNotNull(expected);
        Preconditions.checkNotNull(actual);
        
        if (expected.getRoot() == null && actual.getRoot() == null) {
            return true;
        }
        

        if (expected.getRoot() == null || actual.getRoot() == null) {
            return false;
        }
        
        return same(expected.getRoot(), actual.getRoot());
    }

    private static boolean same(IEPTreeNode expected, IEPTreeNode actual) {
        Preconditions.checkNotNull(expected);
        Preconditions.checkNotNull(actual);

        if (expected.getNodeType() != actual.getNodeType()) {
            return false;
        }

        if (!expected.getLabel().equals(actual.getLabel())) {
            return false;
        }
        
        List<IEPTreeNode> eChildren = expected.getChildren();
        List<IEPTreeNode> aChildren = actual.getChildren();
        
        if (eChildren.size() != aChildren.size()) {
            return false;
        }

        if (!actual.getNodeType().isOrderAware()) {
            TIntSet covered = new TIntHashSet();
            for (int i = 0; i < aChildren.size(); i++) {
                boolean matchFound = false;
                for (int j = 0; j < eChildren.size() && !matchFound; j++) {
                    if (!covered.contains(j) 
                        && same(aChildren.get(i), eChildren.get(j))) {
                        covered.add(j);
                        matchFound = true;
                    }
                }
                if (!matchFound) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < eChildren.size(); i++) {
                if (!same(eChildren.get(i), aChildren.get(i))) {
                    return false;
                }
            }
        }

        return true;
    }

}
