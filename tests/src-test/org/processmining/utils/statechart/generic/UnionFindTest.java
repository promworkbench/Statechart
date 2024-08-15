package org.processmining.utils.statechart.generic;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author mleemans
 *
 * @see http://www.java2s.com/Code/Java/Collections-Data-Structure/Implementationofdisjointsetdatastructure.htm
 */
public class UnionFindTest {

    @Test
    public void testAdd() {
        UnionFind<String> union = new UnionFind<>();
        union.add("A");
        
        assertContents(new String[][] {
            {"A"}
        }, union);
    }
    
    @Test
    public void testFind() {
        UnionFind<String> union = new UnionFind<>();
        union.add("A");
        
        Assert.assertSame("A", union.find("A"));
    }

    @Test
    public void testUnionWithLessRank() {
        UnionFind<String> union = new UnionFind<>();
        union.add("A");
        union.add("B");
        union.add("C");
        assertContents(new String[][] {
            {"A"},
            {"B"},
            {"C"}
        }, union);

        union.union("B", "C");
        assertContents(new String[][] {
            {"A"},
            {"B", "C"}
        }, union);

        String pSmall = "A";
        String pBig = "B";
        union.union(pSmall, pBig);
        
        //the small is attached to the big
        Assert.assertSame(union.find(pSmall), pBig);
        Assert.assertSame(union.find(pBig), pBig);
    }

    @Test
    public void testUnionWithMoreRank() {
        UnionFind<String> union = new UnionFind<>();
        union.add("A");
        union.add("B");
        union.add("C");
        assertContents(new String[][] {
            {"A"},
            {"B"},
            {"C"}
        }, union);

        union.union("B", "C");
        assertContents(new String[][] {
            {"A"},
            {"B", "C"}
        }, union);

        String pSmall = "A";
        String pBig = "B";
        union.union(pBig, pSmall);
        
        //the small is attached to the big
        Assert.assertSame(union.find(pSmall), pBig);
        Assert.assertSame(union.find(pBig), pBig);
    }

    @Test
    public void testUnionIndirect() {
        UnionFind<Integer> union = new UnionFind<>();
        for (int i = 2; i <= 5; i++) {
            union.add(i);
        }
        assertContents(new Integer[][] {
            {2},
            {3},
            {4},
            {5}
        }, union);

        union.union(2, 3);
        assertContents(new Integer[][] {
            {2, 3},
            {4},
            {5}
        }, union);
        
        union.union(4, 5);
        assertContents(new Integer[][] {
            {2, 3},
            {4, 5}
        }, union);
        
        union.union(3, 4);
        assertContents(new Integer[][] {
            {2, 3, 4, 5}
        }, union);
    }
    
    private <E> void assertContents(E[][] expectedContents, UnionFind<E> union) {
        Assert.assertEquals(expectedContents.length, union.count());
        
        Collection<Collection<E>> components = union.getComponents();
        Assert.assertEquals(expectedContents.length, components.size());
        
        List<Collection<E>> entries = new ArrayList<>(components);
        
        for (E[] expectedContent : expectedContents) {
            // find corresponding actual entry
            boolean foundMatch = false;
            Iterator<Collection<E>> it = entries.iterator();
            while (it.hasNext() && !foundMatch) {
                Collection<E> actualContent = it.next();
                boolean match = expectedContent.length == actualContent.size();
                for (E expected : expectedContent) {
                    match = match && actualContent.contains(expected);
                }
                if (match) {
                    it.remove();
                    foundMatch = true;
                }
            }
            Assert.assertTrue(foundMatch);
        }
        Assert.assertTrue(entries.isEmpty());
    }
}
