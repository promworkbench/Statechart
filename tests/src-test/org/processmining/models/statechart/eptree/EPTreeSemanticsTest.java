package org.processmining.models.statechart.eptree;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;
import org.processmining.models.statechart.decorate.error.EPTreeErrorTriggerDecorator;

import com.google.common.collect.ImmutableSet;

public class EPTreeSemanticsTest {

    @Test
    public void testSimpleSeq() {
        IEPTree tree = EPTreeCreateUtil.create("->(A, B, C)");

        // Presets
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("C")));

        // Postsets
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("C")));
    }

    @Test
    public void testChoice2() {
        IEPTree tree = EPTreeCreateUtil.create("->(x(A, B), x(C, D))");

        // Presets
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A"),
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A"),
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(0)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A"),
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1)));

        // Postsets
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
                tree.getNodeByLabel("C"),
                tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(0)));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1)));
    }

    @Test
    public void testLoop() {
        IEPTree tree = EPTreeCreateUtil.create("->(A, <->(B, C), D)");

        // Presets
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
                tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1)));

        // Postsets
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1)));
    }
    
    @Test
    public void testLoopTauBody() {
        IEPTree tree = EPTreeCreateUtil.create("->(A, <->(tau, C), D)");

        // Presets
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("tau")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1)));

        // Postsets
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("tau")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1)));
    }
    
    @Test
    public void testLoopTauRedo() {
        IEPTree tree = EPTreeCreateUtil.create("->(A, <->(B, tau), D)");

        // Presets
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B"),
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("tau")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1)));

        // Postsets
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B"),
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("tau")));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1)));
    }
    
    @Test
    public void testLoopTauTau() {
        IEPTree tree = EPTreeCreateUtil.create("->(A, <->(tau, tau), D)");

        // Presets
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1,0)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1,1)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1)));

        // Postsets
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1,0)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1,1)));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1)));
    }

    @Test
    public void testNestedSeq() {
        IEPTree tree = EPTreeCreateUtil.create("->(A, \\/=B(->(C, D)), E)");

        // Presets
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1,0)));

        // Postsets
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("E")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1,0)));
    }

    @Test
    public void testSeqCancel() {
        IEPTree tree = EPTreeCreateUtil.create("->(A, SC(->(B, Er=C, Er=D), x(E, F)), G)");
        EPTreeErrorTriggerDecorator decorator = tree.getDecorations().getForType(EPTreeErrorTriggerDecorator.class);
        if (decorator != null) {
            decorator = new EPTreeErrorTriggerDecorator();
            tree.getDecorations().registerDecorator(decorator);
        }
        decorator.setDecoration(tree.getNodeByLabel("C"), new HashSet<String>(ImmutableSet.of("E")));
        decorator.setDecoration(tree.getNodeByLabel("D"), new HashSet<String>(ImmutableSet.of("E", "F")));

        // Opening symbols
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("E")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("F")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("G")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByIndex(1)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByIndex(1,0)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("E"),
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByIndex(1,1)));

        // Closing symbols
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("E")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("F")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("G")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D"),
            tree.getNodeByLabel("E"),
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByIndex(1)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByIndex(1,0)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("E"),
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByIndex(1,1)));
        
        // Presets
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("F")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D"),
            tree.getNodeByLabel("E"),
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("G")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1,0)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1,1)));
        
        // Postsets
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D"),
            tree.getNodeByLabel("E")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G"),
            tree.getNodeByLabel("E"),
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("F")));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("G")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G"),
            tree.getNodeByLabel("E"),
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1,0)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1,1)));
    }

    @Test
    public void testSeqCancel2() {
        IEPTree tree = EPTreeCreateUtil.create("->(A, SC(->(B, Er=C, D), E), G)");
        EPTreeErrorTriggerDecorator decorator = tree.getDecorations().getForType(EPTreeErrorTriggerDecorator.class);
        if (decorator != null) {
            decorator = new EPTreeErrorTriggerDecorator();
            tree.getDecorations().registerDecorator(decorator);
        }
        decorator.setDecoration(tree.getNodeByLabel("C"), new HashSet<String>(ImmutableSet.of("E")));

        // Opening symbols
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("E")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("G")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByIndex(1)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByIndex(1,0)));

        // Closing symbols
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("E")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("G")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D"),
            tree.getNodeByLabel("E")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByIndex(1)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByIndex(1,0)));
        
        // Presets
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D"),
            tree.getNodeByLabel("E")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("G")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1,0)));
        
        // Postsets
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D"),
            tree.getNodeByLabel("E")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("G")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G"),
            tree.getNodeByLabel("E")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1,0)));
    }

    @Test
    public void testLoopCancel() {
        IEPTree tree = EPTreeCreateUtil.create("->(A, LC(->(B, Er=C, Er=D), x(E, F)), G)");
        EPTreeErrorTriggerDecorator decorator = tree.getDecorations().getForType(EPTreeErrorTriggerDecorator.class);
        if (decorator != null) {
            decorator = new EPTreeErrorTriggerDecorator();
            tree.getDecorations().registerDecorator(decorator);
        }
        decorator.setDecoration(tree.getNodeByLabel("C"), new HashSet<String>(ImmutableSet.of("E")));
        decorator.setDecoration(tree.getNodeByLabel("D"), new HashSet<String>(ImmutableSet.of("E", "F")));

        // Opening symbols
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("E")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("F")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByLabel("G")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByIndex(1)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByIndex(1,0)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("E"),
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodeOpeningSet(tree.getNodeByIndex(1,1)));

        // Closing symbols
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("E")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("F")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByLabel("G")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByIndex(1)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByIndex(1,0)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("E"),
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodeClosingSet(tree.getNodeByIndex(1,1)));
        
        // Presets
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("E"),
            tree.getNodeByLabel("F"),
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("F")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByLabel("G")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("E"),
            tree.getNodeByLabel("F"),
            tree.getNodeByLabel("A")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1,0)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C"),
            tree.getNodeByLabel("D")
        ), EPTreeSemantics.getNodePreset(tree.getNodeByIndex(1,1)));
        
        // Postsets
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("A")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("C")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("B")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("D"),
            tree.getNodeByLabel("E")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("C")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G"),
            tree.getNodeByLabel("E"),
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("D")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("E")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("F")));
        Assert.assertEquals(ImmutableSet.of(
        ), EPTreeSemantics.getNodePostset(tree.getNodeByLabel("G")));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("G"),
            tree.getNodeByLabel("E"),
            tree.getNodeByLabel("F")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1,0)));
        Assert.assertEquals(ImmutableSet.of(
            tree.getNodeByLabel("B")
        ), EPTreeSemantics.getNodePostset(tree.getNodeByIndex(1,1)));
    }
}
