package org.processmining.utils.statechart.petrinet;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.utils.statechart.generic.SetUtil;
import org.processmining.utils.statechart.petrinet.PetrinetMarkingIterator;

public class PetrinetMarkingIteratorTest {

    @Test
    public void testSequence() {
        Petrinet net = new PetrinetImpl("");
        Place p0 = net.addPlace("");
        Place p1 = net.addPlace("");
        Place p2 = net.addPlace("");
        
        Transition t0 = net.addTransition("");
        Transition t1 = net.addTransition("");
        
        net.addArc(p0, t0);
        net.addArc(t0, p1);
        net.addArc(p1, t1);
        net.addArc(t1, p2);
        
        PetrinetMarkingIterator it = new PetrinetMarkingIterator(net, p0);
        
        assertEquals(SetUtil.createSet(it), SetUtil.createSet(new Set[] {
            SetUtil.createSet(new Place[]{ p0 }),
            SetUtil.createSet(new Place[]{ p1 }),
            SetUtil.createSet(new Place[]{ p2 }),
        }));
    }
    
    @Test
    public void testSequencePartial1() {
        Petrinet net = new PetrinetImpl("");
        Place p0 = net.addPlace("");
        Place p1 = net.addPlace("");
        Place p2 = net.addPlace("");
        
        Transition t0 = net.addTransition("");
        Transition t1 = net.addTransition("");
        
        net.addArc(p0, t0);
        net.addArc(t0, p1);
        net.addArc(p1, t1);
        net.addArc(t1, p2);
        
        PetrinetMarkingIterator it = new PetrinetMarkingIterator(net, p0, p1, false);

        assertEquals(SetUtil.createSet(it), SetUtil.createSet(new Set[] {
            SetUtil.createSet(new Place[]{ p0 }),
        }));
        
        it = new PetrinetMarkingIterator(net, p0, p1, true);

        assertEquals(SetUtil.createSet(it), SetUtil.createSet(new Set[] {
            SetUtil.createSet(new Place[]{ p0 }),
            SetUtil.createSet(new Place[]{ p1 }),
        }));
    }

    @Test
    public void testSequencePartial2() {
        Petrinet net = new PetrinetImpl("");
        Place p0 = net.addPlace("");
        Place p1 = net.addPlace("");
        Place p2 = net.addPlace("");
        
        Transition t0 = net.addTransition("");
        Transition t1 = net.addTransition("");
        
        net.addArc(p0, t0);
        net.addArc(t0, p1);
        net.addArc(p1, t1);
        net.addArc(t1, p2);
        
        PetrinetMarkingIterator it = new PetrinetMarkingIterator(net, p0, p2, false);

        assertEquals(SetUtil.createSet(it), SetUtil.createSet(new Set[] {
            SetUtil.createSet(new Place[]{ p0 }),
            SetUtil.createSet(new Place[]{ p1 }),
        }));
        
        it = new PetrinetMarkingIterator(net, p0, p2, true);

        assertEquals(SetUtil.createSet(it), SetUtil.createSet(new Set[] {
            SetUtil.createSet(new Place[]{ p0 }),
            SetUtil.createSet(new Place[]{ p1 }),
            SetUtil.createSet(new Place[]{ p2 }),
        }));
    }
    
    @Test
    public void testChoice() {
        Petrinet net = new PetrinetImpl("");
        Place p0 = net.addPlace("");
        Place p1 = net.addPlace("");
        Place p2 = net.addPlace("");
        Place p3 = net.addPlace("");
        
        Transition t0 = net.addTransition("");
        Transition t1 = net.addTransition("");
        Transition t2 = net.addTransition("");
        Transition t3 = net.addTransition("");
        
        net.addArc(p0, t0);
        net.addArc(t0, p1);
        net.addArc(p1, t1);
        net.addArc(t1, p3);
        
        net.addArc(p0, t2);
        net.addArc(t2, p2);
        net.addArc(p2, t3);
        net.addArc(t3, p3);
        
        PetrinetMarkingIterator it = new PetrinetMarkingIterator(net, p0);

        assertEquals(SetUtil.createSet(it), SetUtil.createSet(new Set[] {
            SetUtil.createSet(new Place[]{ p0 }),
            SetUtil.createSet(new Place[]{ p1 }),
            SetUtil.createSet(new Place[]{ p2 }),
            SetUtil.createSet(new Place[]{ p3 }),
        }));
    }
    
    @Test
    public void testAnd() {
        Petrinet net = new PetrinetImpl("");
        Place p0 = net.addPlace("");
        Place p1 = net.addPlace("");
        Place p2 = net.addPlace("");
        Place p3 = net.addPlace("");
        Place p4 = net.addPlace("");
        Place p5 = net.addPlace("");
        
        Transition t0 = net.addTransition("");
        Transition t1 = net.addTransition("");
        Transition t2 = net.addTransition("");
        Transition t3 = net.addTransition("");
        
        net.addArc(p0, t0);
        net.addArc(t0, p1);
        net.addArc(p1, t1);
        net.addArc(t1, p2);
        net.addArc(p2, t3);
        net.addArc(t3, p5);
        
        net.addArc(t0, p3);
        net.addArc(p3, t2);
        net.addArc(t2, p4);
        net.addArc(p4, t3);
        
        PetrinetMarkingIterator it = new PetrinetMarkingIterator(net, p0);

        assertEquals(SetUtil.createSet(it), SetUtil.createSet(new Set[] {
            SetUtil.createSet(new Place[]{ p0 }),
            SetUtil.createSet(new Place[]{ p1, p3 }),
            SetUtil.createSet(new Place[]{ p1, p4 }),
            SetUtil.createSet(new Place[]{ p2, p3 }),
            SetUtil.createSet(new Place[]{ p2, p4 }),
            SetUtil.createSet(new Place[]{ p5 }),
        }));
    }
    
    @Test
    public void testLoop() {
        Petrinet net = new PetrinetImpl("");
        Place p0 = net.addPlace("");
        Place p1 = net.addPlace("");
        Place p2 = net.addPlace("");
        Place p3 = net.addPlace("");
        
        Transition t0 = net.addTransition("");
        Transition t1 = net.addTransition("");
        Transition t2 = net.addTransition("");
        Transition t3 = net.addTransition("");
        
        net.addArc(p0, t0);
        net.addArc(t0, p1);
        net.addArc(p1, t1);
        net.addArc(t1, p3);
        
        net.addArc(p3, t3);
        net.addArc(t3, p2);
        net.addArc(p2, t2);
        net.addArc(t2, p0);
        
        PetrinetMarkingIterator it = new PetrinetMarkingIterator(net, p0);

        assertEquals(SetUtil.createSet(it), SetUtil.createSet(new Set[] {
            SetUtil.createSet(new Place[]{ p0 }),
            SetUtil.createSet(new Place[]{ p1 }),
            SetUtil.createSet(new Place[]{ p2 }),
            SetUtil.createSet(new Place[]{ p3 }),
        }));
    }
}
