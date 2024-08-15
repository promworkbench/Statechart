package org.processmining.utils.statechart.generic;

import static org.junit.Assert.*;
import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.processmining.utils.statechart.generic.SetUtil;

public class SetUtilTest {

    // Input:   split subsets by ; 
    //          split subset elements by ,
    private Set<Set<String>> construct(String in) {
        Set<Set<String>> input = new THashSet<Set<String>>();

        for (String set : in.split(";")) {
            Set<String> inSet = new THashSet<String>();
            for (String part : set.split(",")) {
                inSet.add(part);
            }
            input.add(inSet);
        }

        return input;
    }

    @Test
    public void testGenerateSubsetsErrors() {

        Set<String> input = new THashSet<String>();
        input.add("A");
        input.add("B");
        
        try {
            SetUtil.generateSubsets(input, -1, 0);
            fail("0 <= min");
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            SetUtil.generateSubsets(input, 0, 3);
            fail("max <= size");
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            SetUtil.generateSubsets(input, 2, 1);
            fail("min <= max");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test
    public void testGenerateSubsets() {

        Set<String> input = new THashSet<String>();
        input.add("A");
        input.add("B");
        input.add("C");
        input.add("D");

        // Test subsets size 0-0
        assertTrue("Subsets size 0-1", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 0, 0),
                construct(",")));
        
        // Test subsets size 0-1
        assertTrue("Subsets size 0-1", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 0, 1),
                construct("A;B;C;D" + ";" + ",")));
        // Test subsets size 1-1
        assertTrue("Subsets size 1-1", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 1, 1),
                construct("A;B;C;D")));

        // Test subsets size 0-2
        assertTrue("Subsets size 0-2", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 0, 2),
                construct("A;B;C;D" + ";" + "A,B;A,C;A,D;B,C;B,D;C,D" + ";" + ",")));
        // Test subsets size 1-2
        assertTrue("Subsets size 1-2", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 1, 2),
                construct("A;B;C;D" + ";" + "A,B;A,C;A,D;B,C;B,D;C,D")));
        // Test subsets size 2-2
        assertTrue("Subsets size 2-2", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 2, 2),
                construct("A,B;A,C;A,D;B,C;B,D;C,D")));

        // Test subsets size 0-3
        assertTrue("Subsets size 0-3", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 0, 3),
                construct("A;B;C;D" + ";" + "A,B;A,C;A,D;B,C;B,D;C,D" + ";" + ","
                        + ";" + "A,B,C;A,B,D;A,C,D;B,C,D")));
        // Test subsets size 1-3
        assertTrue("Subsets size 1-3", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 1, 3),
                construct("A;B;C;D" + ";" + "A,B;A,C;A,D;B,C;B,D;C,D"
                        + ";" + "A,B,C;A,B,D;A,C,D;B,C,D")));
        // Test subsets size 2-3
        assertTrue("Subsets size 2-3", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 2, 3),
                construct("A,B;A,C;A,D;B,C;B,D;C,D"
                        + ";" + "A,B,C;A,B,D;A,C,D;B,C,D")));
        // Test subsets size 3-3
        assertTrue("Subsets size 3-3", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 3, 3),
                construct("A,B,C;A,B,D;A,C,D;B,C,D")));

        // Test subsets size 0-4
        assertTrue("Subsets size 0-4", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 0, 4),
                construct("A;B;C;D" + ";" + "A,B;A,C;A,D;B,C;B,D;C,D" + ";" + ","
                        + ";" + "A,B,C;A,B,D;A,C,D;B,C,D" + ";" + "A,B,C,D")));
        // Test subsets size 1-4
        assertTrue("Subsets size 1-4", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 1, 4),
                construct("A;B;C;D" + ";" + "A,B;A,C;A,D;B,C;B,D;C,D"
                        + ";" + "A,B,C;A,B,D;A,C,D;B,C,D" + ";" + "A,B,C,D")));
        // Test subsets size 2-4
        assertTrue("Subsets size 2-4", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 2, 4),
                construct("A,B;A,C;A,D;B,C;B,D;C,D"
                        + ";" + "A,B,C;A,B,D;A,C,D;B,C,D" + ";" + "A,B,C,D")));
        // Test subsets size 3-4
        assertTrue("Subsets size 3-4", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 3, 4),
                construct("A,B,C;A,B,D;A,C,D;B,C,D" + ";" + "A,B,C,D")));
        // Test subsets size 4-4
        assertTrue("Subsets size 3-4", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input, 4, 4),
                construct("A,B,C,D")));

        // Test default = subsets size 4-4
        assertTrue("default = Subsets size 0-4", CollectionUtils.isEqualCollection(
                SetUtil.generateSubsets(input),
                construct("A;B;C;D" + ";" + "A,B;A,C;A,D;B,C;B,D;C,D" + ";" + ","
                        + ";" + "A,B,C;A,B,D;A,C,D;B,C,D" + ";" + "A,B,C,D")));
    }

}
