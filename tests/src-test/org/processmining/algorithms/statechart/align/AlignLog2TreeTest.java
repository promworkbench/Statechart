package org.processmining.algorithms.statechart.align;

import org.deckfour.xes.model.XLog;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.statechart.align.XAlignedTreeLog;
import org.processmining.models.statechart.eptree.EPTreeCreateUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.statechart.testutils.IntegrationTestUtil;
import org.processmining.xesalignmentextension.XAlignmentExtension.MoveType;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public class AlignLog2TreeTest {

    @BeforeClass
    public static void init() throws Throwable {
        IntegrationTestUtil.initializeProMWithRequiredPackages("LpSolve");
    }
    
    @Test
    public void testNormal1() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete"} 
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, B)");
        
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog alignment = aligner.performAlignment(log, model);

        Assert.assertEquals(1, alignment.size());
        Assert.assertEquals(4, alignment.get(0).size());
        
        for (XAlignmentMove move : alignment.get(0)) {
            Assert.assertEquals(MoveType.SYNCHRONOUS, move.getType());
            Assert.assertTrue(move.isObservable());
        }
        
        Assert.assertEquals(model.getNodeByLabel("A"), alignment.getNode(alignment.get(0).get(0)));
        Assert.assertEquals(model.getNodeByLabel("A"), alignment.getNode(alignment.get(0).get(1)));
        Assert.assertEquals(model.getNodeByLabel("B"), alignment.getNode(alignment.get(0).get(2)));
        Assert.assertEquals(model.getNodeByLabel("B"), alignment.getNode(alignment.get(0).get(3)));
    }

    @Test
    public void testNormal2() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "A_complete"} 
        });
        IEPTree model = EPTreeCreateUtil.create("\\/=A(B)");
        
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog alignment = aligner.performAlignment(log, model);

        Assert.assertEquals(1, alignment.size());
        Assert.assertEquals(4, alignment.get(0).size());
        
        for (XAlignmentMove move : alignment.get(0)) {
            Assert.assertEquals(MoveType.SYNCHRONOUS, move.getType());
            Assert.assertTrue(move.isObservable());
        }
        
        Assert.assertEquals(model.getNodeByLabel("A"), alignment.getNode(alignment.get(0).get(0)));
        Assert.assertEquals(model.getNodeByLabel("B"), alignment.getNode(alignment.get(0).get(1)));
        Assert.assertEquals(model.getNodeByLabel("B"), alignment.getNode(alignment.get(0).get(2)));
        Assert.assertEquals(model.getNodeByLabel("A"), alignment.getNode(alignment.get(0).get(3)));
    }
    
    @Test
    public void testNormal3() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "C_start", "C_complete", "B_complete", 
                "D_start", "D_complete",
            "A_complete", "E_start", "E_complete"} 
        });
        IEPTree model = EPTreeCreateUtil.create("->(\\/=A(->(\\/=B(C), D)), E)");
        
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog alignment = aligner.performAlignment(log, model);

        Assert.assertEquals(1, alignment.size());
        Assert.assertEquals(10, alignment.get(0).size());
        
        for (XAlignmentMove move : alignment.get(0)) {
            Assert.assertEquals(MoveType.SYNCHRONOUS, move.getType());
            Assert.assertTrue(move.isObservable());
        }
        
        Assert.assertEquals(model.getNodeByLabel("A"), alignment.getNode(alignment.get(0).get(0)));
        Assert.assertEquals(model.getNodeByLabel("B"), alignment.getNode(alignment.get(0).get(1)));
        Assert.assertEquals(model.getNodeByLabel("C"), alignment.getNode(alignment.get(0).get(2)));
        Assert.assertEquals(model.getNodeByLabel("C"), alignment.getNode(alignment.get(0).get(3)));
        Assert.assertEquals(model.getNodeByLabel("B"), alignment.getNode(alignment.get(0).get(4)));
        Assert.assertEquals(model.getNodeByLabel("D"), alignment.getNode(alignment.get(0).get(5)));
        Assert.assertEquals(model.getNodeByLabel("D"), alignment.getNode(alignment.get(0).get(6)));
        Assert.assertEquals(model.getNodeByLabel("A"), alignment.getNode(alignment.get(0).get(7)));
        Assert.assertEquals(model.getNodeByLabel("E"), alignment.getNode(alignment.get(0).get(8)));
        Assert.assertEquals(model.getNodeByLabel("E"), alignment.getNode(alignment.get(0).get(9)));
    }

    @Test
    public void testNormal4Tau() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "C_start", "C_complete"} 
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, x(tau, B), C)");
        
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog alignment = aligner.performAlignment(log, model);

//        debugPrintAlignment(alignment, 0);
        
        Assert.assertEquals(1, alignment.size());
        Assert.assertEquals(5, alignment.get(0).size());

        int i = 0;
        for (XAlignmentMove move : alignment.get(0)) {
            if (i == 2) {
                Assert.assertEquals(MoveType.MODEL, move.getType());
                Assert.assertFalse(move.isObservable());
            } else {
                Assert.assertEquals(MoveType.SYNCHRONOUS, move.getType());
                Assert.assertTrue(move.isObservable());
            }
            i++;
        }
        
        Assert.assertEquals(model.getNodeByLabel("A"), alignment.getNode(alignment.get(0).get(0)));
        Assert.assertEquals(model.getNodeByLabel("A"), alignment.getNode(alignment.get(0).get(1)));
        Assert.assertEquals(model.getNodeByLabel("tau"), alignment.getNode(alignment.get(0).get(2)));
        Assert.assertEquals(model.getNodeByLabel("C"), alignment.getNode(alignment.get(0).get(3)));
        Assert.assertEquals(model.getNodeByLabel("C"), alignment.getNode(alignment.get(0).get(4)));
    }

    @Test
    public void testNormal5Par() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "C_start", "B_complete", "C_complete", "D_start", "D_complete"} 
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, /\\(B, C), D)");
        
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog alignment = aligner.performAlignment(log, model);

//        debugPrintAlignment(alignment, 0);
        
        Assert.assertEquals(1, alignment.size());
        Assert.assertEquals(10, alignment.get(0).size());

        int i = 0;
        for (XAlignmentMove move : alignment.get(0)) {
            if (i == 2 || i == 7) {
                Assert.assertEquals(MoveType.MODEL, move.getType());
                Assert.assertFalse(move.isObservable());
            } else {
                Assert.assertEquals(MoveType.SYNCHRONOUS, move.getType());
                Assert.assertTrue(move.isObservable());
            }
            i++;
        }
        
        Assert.assertEquals(model.getNodeByLabel("A"), alignment.getNode(alignment.get(0).get(0)));
        Assert.assertEquals(model.getNodeByLabel("A"), alignment.getNode(alignment.get(0).get(1)));
        Assert.assertNull(alignment.getNode(alignment.get(0).get(2)));
        Assert.assertNotNull(alignment.getTransition(alignment.get(0).get(2)));
        Assert.assertEquals(model.getNodeByLabel("B"), alignment.getNode(alignment.get(0).get(3)));
        Assert.assertEquals(model.getNodeByLabel("C"), alignment.getNode(alignment.get(0).get(4)));
        Assert.assertEquals(model.getNodeByLabel("B"), alignment.getNode(alignment.get(0).get(5)));
        Assert.assertEquals(model.getNodeByLabel("C"), alignment.getNode(alignment.get(0).get(6)));
        Assert.assertNull(alignment.getNode(alignment.get(0).get(7)));
        Assert.assertNotNull(alignment.getTransition(alignment.get(0).get(7)));
        Assert.assertEquals(model.getNodeByLabel("D"), alignment.getNode(alignment.get(0).get(8)));
        Assert.assertEquals(model.getNodeByLabel("D"), alignment.getNode(alignment.get(0).get(9)));
    }
    
    @Test
    public void testSimpleNestingSeq() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", 
                "X_start", "X_complete", "B_start","Y_start", "Y_complete",  "B_complete",
                "B_complete", "C_start", "C_complete" } 
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, \\/=B(->(X, \\/=B(Y))), C)");

        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog alignment = aligner.performAlignment(log, model);

        Assert.assertEquals(1, alignment.size());
        Assert.assertEquals(12, alignment.get(0).size());
        
        for (XAlignmentMove move : alignment.get(0)) {
            Assert.assertEquals(MoveType.SYNCHRONOUS, move.getType());
            Assert.assertTrue(move.isObservable());
        }

        Assert.assertEquals(model.getNodeByIndex(0), alignment.getNode(alignment.get(0).get(0)));
        Assert.assertEquals(model.getNodeByIndex(0), alignment.getNode(alignment.get(0).get(1)));
        Assert.assertEquals(model.getNodeByIndex(1), alignment.getNode(alignment.get(0).get(2)));
        Assert.assertEquals(model.getNodeByIndex(1,0,0), alignment.getNode(alignment.get(0).get(3)));
        Assert.assertEquals(model.getNodeByIndex(1,0,0), alignment.getNode(alignment.get(0).get(4)));
        Assert.assertEquals(model.getNodeByIndex(1,0,1), alignment.getNode(alignment.get(0).get(5)));
        Assert.assertEquals(model.getNodeByIndex(1,0,1,0), alignment.getNode(alignment.get(0).get(6)));
        Assert.assertEquals(model.getNodeByIndex(1,0,1,0), alignment.getNode(alignment.get(0).get(7)));
        Assert.assertEquals(model.getNodeByIndex(1,0,1), alignment.getNode(alignment.get(0).get(8)));
        Assert.assertEquals(model.getNodeByIndex(1), alignment.getNode(alignment.get(0).get(9)));
        Assert.assertEquals(model.getNodeByIndex(2), alignment.getNode(alignment.get(0).get(10)));
        Assert.assertEquals(model.getNodeByIndex(2), alignment.getNode(alignment.get(0).get(11)));
    }
    
    @Test
    public void testSimpleNestingSeqRecursion() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", 
                "X_start", "X_complete", "B_start","Y_start", "Y_complete",  "B_complete",
                "B_complete", "C_start", "C_complete" } 
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, \\/=B(x(->(X, R\\/=B), Y)), C)");

        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog alignment = aligner.performAlignment(log, model);

        Assert.assertEquals(1, alignment.size());
        Assert.assertEquals(12, alignment.get(0).size());
        
        for (XAlignmentMove move : alignment.get(0)) {
            Assert.assertEquals(MoveType.SYNCHRONOUS, move.getType());
            Assert.assertTrue(move.isObservable());
        }

        Assert.assertEquals(model.getNodeByIndex(0), alignment.getNode(alignment.get(0).get(0)));
        Assert.assertEquals(model.getNodeByIndex(0), alignment.getNode(alignment.get(0).get(1)));
        Assert.assertEquals(model.getNodeByIndex(1), alignment.getNode(alignment.get(0).get(2)));
        Assert.assertEquals(model.getNodeByIndex(1,0,0,0), alignment.getNode(alignment.get(0).get(3)));
        Assert.assertEquals(model.getNodeByIndex(1,0,0,0), alignment.getNode(alignment.get(0).get(4)));
        Assert.assertEquals(model.getNodeByIndex(1,0,0,1), alignment.getNode(alignment.get(0).get(5)));
        Assert.assertEquals(model.getNodeByIndex(1,0,1), alignment.getNode(alignment.get(0).get(6)));
        Assert.assertEquals(model.getNodeByIndex(1,0,1), alignment.getNode(alignment.get(0).get(7)));
        Assert.assertEquals(model.getNodeByIndex(1,0,0,1), alignment.getNode(alignment.get(0).get(8)));
        Assert.assertEquals(model.getNodeByIndex(1), alignment.getNode(alignment.get(0).get(9)));
        Assert.assertEquals(model.getNodeByIndex(2), alignment.getNode(alignment.get(0).get(10)));
        Assert.assertEquals(model.getNodeByIndex(2), alignment.getNode(alignment.get(0).get(11)));
    }

    @Test
    public void testRecursionWithLogMove() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", 
                "X_start", "X_complete", /*"B_start",*/ "Y_start", "Y_complete",  /*"B_complete",*/
                "B_complete", "C_start", "C_complete" } 
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, \\/=B(x(->(X, R\\/=B), Y)), C)");

        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog alignment = aligner.performAlignment(log, model);
        
        Assert.assertEquals(1, alignment.size());
        Assert.assertEquals(10, alignment.get(0).size());
        
        for (XAlignmentMove move : alignment.get(0)) {
            if (move.getEventClassId().startsWith("X")) {
                Assert.assertEquals(MoveType.LOG, move.getType());
            } else {
                Assert.assertEquals(MoveType.SYNCHRONOUS, move.getType());
            }
            Assert.assertTrue(move.isObservable());
        }

        Assert.assertEquals(model.getNodeByIndex(0), alignment.getNode(alignment.get(0).get(0)));
        Assert.assertEquals(model.getNodeByIndex(0), alignment.getNode(alignment.get(0).get(1)));
        Assert.assertEquals(model.getNodeByIndex(1), alignment.getNode(alignment.get(0).get(2)));
        Assert.assertEquals(null, alignment.getNode(alignment.get(0).get(3)));
        Assert.assertEquals(null, alignment.getNode(alignment.get(0).get(4)));
        Assert.assertEquals(model.getNodeByIndex(1,0,1), alignment.getNode(alignment.get(0).get(5)));
        Assert.assertEquals(model.getNodeByIndex(1,0,1), alignment.getNode(alignment.get(0).get(6)));
        Assert.assertEquals(model.getNodeByIndex(1), alignment.getNode(alignment.get(0).get(7)));
        Assert.assertEquals(model.getNodeByIndex(2), alignment.getNode(alignment.get(0).get(8)));
        Assert.assertEquals(model.getNodeByIndex(2), alignment.getNode(alignment.get(0).get(9)));
    }
    
    @Test
    public void testRecursionWithModelMove() {
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", 
                "X_start", "X_complete", /*"B_start",*/ "Y_start", "Y_complete",  "B_complete",
                "B_complete", "C_start", "C_complete" } 
        });
        IEPTree model = EPTreeCreateUtil.create("->(A, \\/=B(x(->(X, R\\/=B), Y)), C)");

        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog alignment = aligner.performAlignment(log, model);
        
        Assert.assertEquals(1, alignment.size());
        Assert.assertEquals(12, alignment.get(0).size());
        
        int i = 0;
        for (XAlignmentMove move : alignment.get(0)) {
            if (i == 5) {
                Assert.assertEquals(MoveType.MODEL, move.getType());
            } else {
                Assert.assertEquals(MoveType.SYNCHRONOUS, move.getType());
            }
            Assert.assertTrue(move.isObservable());
            i++;
        }

        Assert.assertEquals(model.getNodeByIndex(0), alignment.getNode(alignment.get(0).get(0)));
        Assert.assertEquals(model.getNodeByIndex(0), alignment.getNode(alignment.get(0).get(1)));
        Assert.assertEquals(model.getNodeByIndex(1), alignment.getNode(alignment.get(0).get(2)));
        Assert.assertEquals(model.getNodeByIndex(1,0,0,0), alignment.getNode(alignment.get(0).get(3)));
        Assert.assertEquals(model.getNodeByIndex(1,0,0,0), alignment.getNode(alignment.get(0).get(4)));
        Assert.assertEquals(model.getNodeByIndex(1,0,0,1), alignment.getNode(alignment.get(0).get(5)));
        Assert.assertEquals(model.getNodeByIndex(1,0,1), alignment.getNode(alignment.get(0).get(6)));
        Assert.assertEquals(model.getNodeByIndex(1,0,1), alignment.getNode(alignment.get(0).get(7)));
        Assert.assertEquals(model.getNodeByIndex(1,0,0,1), alignment.getNode(alignment.get(0).get(8)));
        Assert.assertEquals(model.getNodeByIndex(1), alignment.getNode(alignment.get(0).get(9)));
        Assert.assertEquals(model.getNodeByIndex(2), alignment.getNode(alignment.get(0).get(10)));
        Assert.assertEquals(model.getNodeByIndex(2), alignment.getNode(alignment.get(0).get(11)));
    }

    @Test
    public void testRecurse1() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", 
                "A_start", "B_start", "B_complete", 
                    "A_start", "D_start", "D_complete", "A_complete", 
                "C_start", "C_complete", "A_complete",
            "C_start", "C_complete", "A_complete"} 
        });
        IEPTree model = EPTreeCreateUtil.create("\\/=A(x(->(B, R\\/=A,C), D))");
        
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog alignment = aligner.performAlignment(log, model);
        
        Assert.assertEquals(1, alignment.size());
        Assert.assertEquals(16, alignment.get(0).size());
        
        for (XAlignmentMove move : alignment.get(0)) {
            Assert.assertEquals(MoveType.SYNCHRONOUS, move.getType());
            Assert.assertTrue(move.isObservable());
        }

        Assert.assertEquals(model.getNodeByIndex(), alignment.getNode(alignment.get(0).get(0)));
        Assert.assertEquals(model.getNodeByIndex(0,0,0), alignment.getNode(alignment.get(0).get(1)));
        Assert.assertEquals(model.getNodeByIndex(0,0,0), alignment.getNode(alignment.get(0).get(2)));
        Assert.assertEquals(model.getNodeByIndex(0,0,1), alignment.getNode(alignment.get(0).get(3)));
        Assert.assertEquals(model.getNodeByIndex(0,0,0), alignment.getNode(alignment.get(0).get(4)));
        Assert.assertEquals(model.getNodeByIndex(0,0,0), alignment.getNode(alignment.get(0).get(5)));
        Assert.assertEquals(model.getNodeByIndex(0,0,1), alignment.getNode(alignment.get(0).get(6)));
        Assert.assertEquals(model.getNodeByIndex(0,1), alignment.getNode(alignment.get(0).get(7)));
        Assert.assertEquals(model.getNodeByIndex(0,1), alignment.getNode(alignment.get(0).get(8)));
        Assert.assertEquals(model.getNodeByIndex(0,0,1), alignment.getNode(alignment.get(0).get(9)));
        Assert.assertEquals(model.getNodeByIndex(0,0,2), alignment.getNode(alignment.get(0).get(10)));
        Assert.assertEquals(model.getNodeByIndex(0,0,2), alignment.getNode(alignment.get(0).get(11)));
        Assert.assertEquals(model.getNodeByIndex(0,0,1), alignment.getNode(alignment.get(0).get(12)));
        Assert.assertEquals(model.getNodeByIndex(0,0,2), alignment.getNode(alignment.get(0).get(13)));
        Assert.assertEquals(model.getNodeByIndex(0,0,2), alignment.getNode(alignment.get(0).get(14)));
        Assert.assertEquals(model.getNodeByIndex(), alignment.getNode(alignment.get(0).get(15)));
        
    }
    
    @SuppressWarnings("unused")
    private static void debugPrintAlignment(XAlignedTreeLog alignment, int trace) {
        for (XAlignmentMove move : alignment.get(trace)) {
            System.out.print(move.getType().toString().charAt(0));
            IEPTreeNode node = alignment.getNode(move);
            if (node != null) {
                System.out.print("-PT-" + node);
            } else {
                Transition t = alignment.getTransition(move);
                if (t != null) {
                    System.out.print("-PN-" + t);
                } else {
                    System.out.print("='" + move.getEventClassId() + "'");
                }
            }
            System.out.println(", ");
        }
    }
}
