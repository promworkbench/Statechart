package org.processmining.algorithms.statechart.align;

import java.util.Iterator;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.align.ExecIntervals;
import org.processmining.models.statechart.align.XAlignedTreeLog;
import org.processmining.models.statechart.decorate.align.ExecIntervalTreeDecorator;
import org.processmining.models.statechart.eptree.EPTreeCreateUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.statechart.testutils.IntegrationTestUtil;
import org.processmining.xesalignmentextension.XAlignmentExtension.MoveType;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public class AlignTreeEventIntervalsTest {

    @BeforeClass
    public static void init() throws Throwable {
        IntegrationTestUtil.initializeProMWithRequiredPackages("LpSolve");
    }

    @Test
    public void testNormal1() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "C_start", "C_complete", "A_complete"} 
        });
        XTrace trace = log.get(0);
        for (int i = 0; i < trace.size(); i++) {
            trace.get(i).getAttributes().put("Instance", new XAttributeDiscreteImpl("Instance", i));
        }
        
        IEPTree model = EPTreeCreateUtil.create("\\/=A(->(B,C))");
        
        // align
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog align = aligner.performAlignment(log, model);
        
        // annotate tree
        AlignTreeEventIntervals annoter = new AlignTreeEventIntervals();
        annoter.annotate(align, model);
        
        // check annotations
        ExecIntervalTreeDecorator dec = model.getDecorations().getForType(ExecIntervalTreeDecorator.class);
        
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("A")), new XEvent[][] {
            {null, trace.get(0), trace.get(5)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("B")), new XEvent[][] {
            {trace.get(0), trace.get(1), trace.get(2)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("C")), new XEvent[][] {
            {trace.get(2), trace.get(3), trace.get(4)}
        });
    }

    @Test
    public void testNormal1Iterator() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "C_start", "C_complete", "B_complete", "D_start", "D_complete", "A_complete"} 
        });
        XTrace trace = log.get(0);
        for (int i = 0; i < trace.size(); i++) {
            trace.get(i).getAttributes().put("Instance", new XAttributeDiscreteImpl("Instance", i));
        }
        
        IEPTree model = EPTreeCreateUtil.create("\\/=A(->(\\/=B(C),D))");
        
        // align
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog align = aligner.performAlignment(log, model);
        
        // annotate tree
        AlignTreeEventIntervals annoter = new AlignTreeEventIntervals();
        annoter.annotate(align, model);
        
        // check iterators
        ExecIntervalTreeDecorator dec = model.getDecorations().getForType(ExecIntervalTreeDecorator.class);

        _assertIntervals(dec.aggregateDecorationsChildren(model.getNodeByLabel("A"), true, true), new XEvent[][] {
            {null, trace.get(0), trace.get(7)}
        });

        _assertIntervals(dec.aggregateDecorationsChildren(model.getNodeByLabel("A"), true, false), new XEvent[][] {
            {null, trace.get(0), trace.get(7)},
            {trace.get(0), trace.get(1), trace.get(4)},
            {trace.get(1), trace.get(2), trace.get(3)},
            {trace.get(4), trace.get(5), trace.get(6)}
        });

        _assertIntervals(dec.aggregateDecorationsChildren(model.getNodeByLabel("A"), false, true), new XEvent[][] {
            {trace.get(0), trace.get(1), trace.get(4)},
            {trace.get(4), trace.get(5), trace.get(6)}
        });

        _assertIntervals(dec.aggregateDecorationsChildren(model.getNodeByLabel("A"), false, false), new XEvent[][] {
            {trace.get(0), trace.get(1), trace.get(4)},
            {trace.get(1), trace.get(2), trace.get(3)},
            {trace.get(4), trace.get(5), trace.get(6)}
        });
    }

    @Test
    public void testPar1() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete"} 
        });
        XTrace trace = log.get(0);
        for (int i = 0; i < trace.size(); i++) {
            trace.get(i).getAttributes().put("Instance", new XAttributeDiscreteImpl("Instance", i));
        }
        
        IEPTree model = EPTreeCreateUtil.create("->(A, /\\(B, C), D)");

        // align
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog align = aligner.performAlignment(log, model);
        
        // annotate tree
        AlignTreeEventIntervals annoter = new AlignTreeEventIntervals();
        annoter.annotate(align, model);
        
        // check annotations
        ExecIntervalTreeDecorator dec = model.getDecorations().getForType(ExecIntervalTreeDecorator.class);
        
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("A")), new XEvent[][] {
            {null, trace.get(0), trace.get(1)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("B")), new XEvent[][] {
            {trace.get(1), trace.get(2), trace.get(3)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("C")), new XEvent[][] {
            {trace.get(1), trace.get(4), trace.get(5)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("D")), new XEvent[][] {
            {trace.get(5), trace.get(6), trace.get(7)}
        });
    }
    
    @Test
    public void testPar2() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "B_start", "B_complete", "C_start", "C_complete", "D_start", "D_complete"} 
        });
        XTrace trace = log.get(0);
        for (int i = 0; i < trace.size(); i++) {
            trace.get(i).getAttributes().put("Instance", new XAttributeDiscreteImpl("Instance", i));
        }
        
        IEPTree model = EPTreeCreateUtil.create("->(A, /\\(B, ->(tau, C)), D)");

        // align
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog align = aligner.performAlignment(log, model);
        
        // annotate tree
        AlignTreeEventIntervals annoter = new AlignTreeEventIntervals();
        annoter.annotate(align, model);
        
        // check annotations
        ExecIntervalTreeDecorator dec = model.getDecorations().getForType(ExecIntervalTreeDecorator.class);
        
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("A")), new XEvent[][] {
            {null, trace.get(0), trace.get(1)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("B")), new XEvent[][] {
            {trace.get(1), trace.get(2), trace.get(3)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("C")), new XEvent[][] {
            {trace.get(1), trace.get(4), trace.get(5)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("D")), new XEvent[][] {
            {trace.get(5), trace.get(6), trace.get(7)}
        });
    }

    @Test
    public void testPar3() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "C_start", "C_complete", "B_start", "B_complete", 
                "D_start", "D_complete", "E_start", "E_complete"} 
        });
        XTrace trace = log.get(0);
        for (int i = 0; i < trace.size(); i++) {
            trace.get(i).getAttributes().put("Instance", new XAttributeDiscreteImpl("Instance", i));
        }
        
        IEPTree model = EPTreeCreateUtil.create("->(A, /\\(B, ->(C, D)), E)");

        // align
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog align = aligner.performAlignment(log, model);
        
        // annotate tree
        AlignTreeEventIntervals annoter = new AlignTreeEventIntervals();
        annoter.annotate(align, model);
        
        // check annotations
        ExecIntervalTreeDecorator dec = model.getDecorations().getForType(ExecIntervalTreeDecorator.class);
        
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("A")), new XEvent[][] {
            {null, trace.get(0), trace.get(1)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("B")), new XEvent[][] {
            {trace.get(1), trace.get(4), trace.get(5)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("C")), new XEvent[][] {
            {trace.get(1), trace.get(2), trace.get(3)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("D")), new XEvent[][] {
            {trace.get(3), trace.get(6), trace.get(7)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("E")), new XEvent[][] {
            {trace.get(7), trace.get(8), trace.get(9)}
        });
    }

    @Test
    public void testLoop1() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "A_complete", "C_start", "C_complete", "C_start", "C_complete", "B_start", "B_complete"} ,
            {"A_start", "A_complete", "B_start", "B_complete"} 
        });
        XTrace trace0 = log.get(0);
        XTrace trace1 = log.get(1);
        for (int i = 0; i < trace0.size(); i++) {
            trace0.get(i).getAttributes().put("Instance", new XAttributeDiscreteImpl("Instance", i));
        }
        for (int i = 0; i < trace1.size(); i++) {
            trace1.get(i).getAttributes().put("Instance", new XAttributeDiscreteImpl("Instance", i + 100));
        }
        
        IEPTree model = EPTreeCreateUtil.create("->(A, <->(tau, C), B)");

        // align
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog align = aligner.performAlignment(log, model);
        
        // annotate tree
        AlignTreeEventIntervals annoter = new AlignTreeEventIntervals();
        annoter.annotate(align, model);
        
        // check annotations
        ExecIntervalTreeDecorator dec = model.getDecorations().getForType(ExecIntervalTreeDecorator.class);
        
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("A")), new XEvent[][] {
            {null, trace0.get(0), trace0.get(1)},
            {null, trace1.get(0), trace1.get(1)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("C")), new XEvent[][] {
            {trace0.get(1), trace0.get(2), trace0.get(3)},
            {trace0.get(3), trace0.get(4), trace0.get(5)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("B")), new XEvent[][] {
            {trace0.get(5), trace0.get(6), trace0.get(7)},
            {trace1.get(1), trace1.get(2), trace1.get(3)}
        });
    }

    @Test
    public void testNested1() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A_start", "B_start", "B_complete", "C_start", "C_complete", "A_complete"} 
        });
        XTrace trace0 = log.get(0);
        for (int i = 0; i < trace0.size(); i++) {
            trace0.get(i).getAttributes().put("Instance", new XAttributeDiscreteImpl("Instance", i));
        }
        IEPTree model = EPTreeCreateUtil.create("\\/=A(->(B,C))");

        // align
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog align = aligner.performAlignment(log, model);
        
        // annotate tree
        AlignTreeEventIntervals annoter = new AlignTreeEventIntervals();
        annoter.annotate(align, model);
        
        // check annotations
        ExecIntervalTreeDecorator dec = model.getDecorations().getForType(ExecIntervalTreeDecorator.class);
        
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("A")), new XEvent[][] {
            {null, trace0.get(0), trace0.get(5)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("B")), new XEvent[][] {
            {trace0.get(0), trace0.get(1), trace0.get(2)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("C")), new XEvent[][] {
            {trace0.get(2), trace0.get(3), trace0.get(4)}
        });
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
        XTrace trace0 = log.get(0);
        for (int i = 0; i < trace0.size(); i++) {
            trace0.get(i).getAttributes().put("Instance", new XAttributeDiscreteImpl("Instance", i));
        }
        IEPTree model = EPTreeCreateUtil.create("\\/=A(x(->(B, R\\/=A,C), D))");

        // align
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog align = aligner.performAlignment(log, model);
        
        // annotate tree
        AlignTreeEventIntervals annoter = new AlignTreeEventIntervals();
        annoter.annotate(align, model);
        
        // check annotations
        ExecIntervalTreeDecorator dec = model.getDecorations().getForType(ExecIntervalTreeDecorator.class);
        
        _assertIntervals(dec.getDecoration(model.getNodeByIndex()), new XEvent[][] { // A def
            {null, trace0.get(0), trace0.get(15)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByIndex(0,0,1)), new XEvent[][] { // A recurse
            {trace0.get(2), trace0.get(3), trace0.get(12)},
            {trace0.get(5), trace0.get(6), trace0.get(9)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("B")), new XEvent[][] {
            {trace0.get(0), trace0.get(1), trace0.get(2)},
            {trace0.get(3), trace0.get(4), trace0.get(5)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("C")), new XEvent[][] {
            {trace0.get(9), trace0.get(10), trace0.get(11)},
            {trace0.get(12), trace0.get(13), trace0.get(14)}
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("D")), new XEvent[][] {
            {trace0.get(6), trace0.get(7), trace0.get(8)}
        });
    }

    @Test
    public void testSeqLogAndSync() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {   "K_start", "K_complete", 
                "A_start", "A_complete", 
                "L_start", "L_complete", 
                "B_start", "B_complete",
                "M_start", "M_complete"
            } 
        });
        XTrace trace = log.get(0);
        for (int i = 0; i < trace.size(); i++) {
            trace.get(i).getAttributes().put("Instance", new XAttributeDiscreteImpl("Instance", i));
        }
        
        IEPTree model = EPTreeCreateUtil.create("->(A,B)");
        
        // align
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog align = aligner.performAlignment(log, model);
        
        // annotate tree
        AlignTreeEventIntervals annoter = new AlignTreeEventIntervals();
        annoter.annotate(align, model);
        
        // check annotations
        ExecIntervalTreeDecorator dec = model.getDecorations().getForType(ExecIntervalTreeDecorator.class);
        
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("A")), new XAlignmentMove[][] {
            {   _log(trace.get(0)), _log(trace.get(1)),
                _sync(null), _sync(trace.get(2)), _sync(trace.get(3)),
                _log(trace.get(4)), _log(trace.get(5))
            }
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("B")), new XAlignmentMove[][] {
            {
                _sync(trace.get(3)), _sync(trace.get(6)), _sync(trace.get(7)),
                _log(trace.get(8)), _log(trace.get(9))
            }
        });
    }

    @Test
    public void testSeqLogAndModel() {
        // setup input
        XLog log = LogCreateTestUtil.createLogFlat(new String[][] { 
            {   "K_start", "K_complete", 
                "A_start", "A_complete", 
                "L_start", "L_complete", 
                "C_start", "C_complete",
                "M_start", "M_complete"
            } 
        });
        XTrace trace = log.get(0);
        for (int i = 0; i < trace.size(); i++) {
            trace.get(i).getAttributes().put("Instance", new XAttributeDiscreteImpl("Instance", i));
        }
        
        IEPTree model = EPTreeCreateUtil.create("->(A,B,C)");
        
        // align
        AlignLog2Tree aligner = new AlignLog2Tree();
        XAlignedTreeLog align = aligner.performAlignment(log, model);
        
        // annotate tree
        AlignTreeEventIntervals annoter = new AlignTreeEventIntervals();
        annoter.annotate(align, model);
        
        // check annotations
        ExecIntervalTreeDecorator dec = model.getDecorations().getForType(ExecIntervalTreeDecorator.class);
        
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("A")), new XAlignmentMove[][] {
            {   _log(trace.get(0)), _log(trace.get(1)),
                _sync(null), _sync(trace.get(2)), _sync(trace.get(3)),
                _log(trace.get(4)), _log(trace.get(5))
            }
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("B")), new XAlignmentMove[][] {
            {   
                _sync(trace.get(3)), _model("B+start"), _model("B+complete")
            }
        });
        _assertIntervals(dec.getDecoration(model.getNodeByLabel("C")), new XAlignmentMove[][] {
            {
                _model("B+complete"), _sync(trace.get(6)), _sync(trace.get(7)),
                _log(trace.get(8)), _log(trace.get(9))
            }
        });
    }
    
    private XAlignmentMove _sync(XEvent xEvent) {
        if (xEvent == null) {
            return null;
        }
        
        XAttributeMap eMap = xEvent.getAttributes();
        String eConcept = eMap.get(XConceptExtension.KEY_NAME) + "+" + eMap.get(XLifecycleExtension.KEY_TRANSITION);
        return _move(MoveType.SYNCHRONOUS, xEvent, eConcept);
    }

    private XAlignmentMove _log(XEvent xEvent) {
        if (xEvent == null) {
            return null;
        }
        
        XAttributeMap eMap = xEvent.getAttributes();
        String eConcept = eMap.get(XConceptExtension.KEY_NAME) + "+" + eMap.get(XLifecycleExtension.KEY_TRANSITION);
        return _move(MoveType.LOG, xEvent, eConcept);
    }
    
    private XAlignmentMove _model(String label) {
        return _move(MoveType.MODEL, null, label);
    }
    
    private XAlignmentMove _move(final MoveType type, final XEvent event, final String label) {
        return new XAlignmentMove() {
            @Override
            public MoveType getType() {
                return type;
            }

            @Override
            public String getModelMove() {
                return label;
            }

            @Override
            public String getActivityId() {
                return label;
            }

            @Override
            public String getLogMove() {
                return label;
            }

            @Override
            public String getEventClassId() {
                return label;
            }

            @Override
            public boolean isObservable() {
                return true;
            }

            @Override
            public XEvent getEvent() {
                return event;
            }
            
        };
    }

    private void _assertIntervals(ExecIntervals annot, XEvent[][] xEvents) {
        _assertIntervals(annot.iterator(), xEvents);
    }

    private void _assertIntervals(Iterable<ExecInterval> itt, XEvent[][] xEvents) {
        _assertIntervals(itt.iterator(), xEvents);
    }
    
    private void _assertIntervals(Iterator<ExecInterval> it, XEvent[][] xEvents) {
        int i = 0;
        for (XEvent[] ivalExpected : xEvents) {
            Assert.assertTrue(it.hasNext());
            ExecInterval ival = it.next();
            
            Assert.assertTrue(ival.getLogMovesPre().isEmpty());
            
            _assertMoveEqualEvent("enabled[" + i + "]", _sync(ivalExpected[0]), ival.getEnabled());
            _assertMoveEqualEvent("start[" + i + "]", _sync(ivalExpected[1]), ival.getStart());
            _assertMoveEqualEvent("complete[" + i + "]", _sync(ivalExpected[2]), ival.getComplete());
            
            Assert.assertTrue(ival.getLogMovesPre().isEmpty());
            
            i++;
        }
        Assert.assertFalse(it.hasNext());
    }

    private void _assertIntervals(ExecIntervals annot, XAlignmentMove[][] xEvents) {
        _assertIntervals(annot.iterator(), xEvents);
    }

//    private void _assertIntervals(Iterable<ExecInterval> itt, XAlignmentMove[][] xEvents) {
//        _assertIntervals(itt.iterator(), xEvents);
//    }

    private void _assertIntervals(Iterator<ExecInterval> it, XAlignmentMove[][] xEvents) {
        int i = 0;
        for (XAlignmentMove[] ivalExpected : xEvents) {
            Assert.assertTrue(it.hasNext());
            ExecInterval ival = it.next();
            
            int intStatus = 0;
            ExecInterval ivalExp = new ExecInterval();
            for (XAlignmentMove move : ivalExpected) {
                if (move != null && move.getType() == MoveType.LOG) {
                    if (intStatus == 0) {
                        ivalExp.appendLogMovePre(move);
                    } else {
                        ivalExp.appendLogMovePost(move);
                    }
                } else {
                    if (intStatus == 0) {
                        ivalExp.setEnabled(move);
                    } else if (intStatus == 1) {
                        ivalExp.setStart(move);
                    } else if (intStatus == 2) {
                        ivalExp.setComplete(move);
                    }
                    intStatus++;
                }
            }
            
            {
                int j = 0;
                Iterator<XAlignmentMove> itLog = ival.getLogMovesPre().iterator();
                for (XAlignmentMove logExp : ivalExp.getLogMovesPre()) {
                    Assert.assertTrue(itLog.hasNext());
                    XAlignmentMove logAct = itLog.next();
                    _assertMoveEqualEvent("pre[" + i + "," + j + "]", logExp, logAct);
                    j++;
                }
                Assert.assertFalse(itLog.hasNext());
            }
            
            _assertMoveEqualEvent("enabled[" + i + "]", ivalExp.getEnabled(), ival.getEnabled());
            _assertMoveEqualEvent("start[" + i + "]", ivalExp.getStart(), ival.getStart());
            _assertMoveEqualEvent("complete[" + i + "]", ivalExp.getComplete(), ival.getComplete());

            {
                int j = 0;
                Iterator<XAlignmentMove> itLog = ival.getLogMovesPost().iterator();
                for (XAlignmentMove logExp : ivalExp.getLogMovesPost()) {
                    Assert.assertTrue(itLog.hasNext());
                    XAlignmentMove logAct = itLog.next();
                    _assertMoveEqualEvent("post[" + i + "," + j + "]", logExp, logAct);
                    j++;
                }
                Assert.assertFalse(itLog.hasNext());
            }
            
            i++;
        }
        Assert.assertFalse(it.hasNext());
    }
    
    private void _assertMoveEqualEvent(String msgType, XAlignmentMove expect, XAlignmentMove actual) {
        if (expect == null) {
            Assert.assertNull("ival " + msgType + " - expected no move", actual);
        } else {
//          String eConcept = eMap.get(XConceptExtension.KEY_NAME) + "+" + eMap.get(XLifecycleExtension.KEY_TRANSITION);
            String eConcept = expect.getActivityId();

            Assert.assertNotNull("ival " + msgType + " - expected a move like " + eConcept, actual);
            Assert.assertEquals("ival " + msgType + " - mismatch on move type '" + expect.getType() + "' in context " + eConcept, 
                    expect.getType(), actual.getType());
            
            if (expect.getEvent() != null) {
                Assert.assertNotNull("ival " + msgType + " - expected a xevent like " + eConcept, actual.getEvent());
                
                XAttributeMap eMap = expect.getEvent().getAttributes();
                XAttributeMap aMap = actual.getEvent().getAttributes();
                
                for (String key : eMap.keySet()) {
                    Assert.assertTrue("ival " + msgType + " - expected key '" + key + "' in context " + eConcept, 
                            aMap.containsKey(key));
                    if (key.equals(XConceptExtension.KEY_NAME) && expect.getType() != MoveType.LOG) {
                        Assert.assertEquals("ival " + msgType + " - mismatch on key '" + key + "' in context " + eConcept, 
                                eConcept, aMap.get(key).toString());
                    } else {
                        Assert.assertEquals("ival " + msgType + " - mismatch on key '" + key + "' in context " + eConcept, 
                                eMap.get(key), aMap.get(key));
                    }
                }
            } else {
//                Assert.assertNull("ival " + msgType + " - expected no xevent", actual.getEvent());
            }
        }
    }
}
