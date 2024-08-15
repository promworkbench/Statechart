package org.processmining.algorithms.statechart.align;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.align.metric.logic.NoModelMovePred;
import org.processmining.algorithms.statechart.align.metric.time.Event2TimeAttribute;
import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;
import org.processmining.algorithms.statechart.align.metric.time.MoveTimeCmp;
import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.xesalignmentextension.XAlignmentExtension.MoveType;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

import com.google.common.base.Predicate;

public class ExecIntervalTest {

    private static final String KeyTime = "Time";

    private static final IEvent2Time event2time = new Event2TimeAttribute(KeyTime, 1);
    private static final Predicate<XAlignmentMove> predSync = new NoModelMovePred();
    private static final Comparator<XAlignmentMove> moveCmp = new MoveTimeCmp(event2time);

    private static final double Delta = 0.01;

    private static double getTime(XAlignmentMove move) {
        return event2time.apply(move);
    }
    
    private static ExecInterval createTimeInterval(double start, double complete) {
        ExecInterval ival = new ExecInterval();
        ival.setStart(createTimedMove(start));
        ival.setComplete(createTimedMove(complete));
        return ival;
    }
    
    private static XAlignmentMove createTimedMove(double time) {
        XFactory f = LogFactory.getFactory();
        final XEvent event = f.createEvent();
        event.getAttributes().put(KeyTime, new XAttributeContinuousImpl(KeyTime, time));
        
        return new XAlignmentMove() {

            @Override
            public MoveType getType() {
                return MoveType.SYNCHRONOUS;
            }

            @Override
            public String getModelMove() {
                return null;
            }

            @Override
            public String getActivityId() {
                return null;
            }

            @Override
            public String getLogMove() {
                return null;
            }

            @Override
            public String getEventClassId() {
                return null;
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

    private void assertEqualIntervals(List<ExecInterval> expected,
            List<ExecInterval> actual) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.size(), actual.size());
        
        for (int i = 0; i < expected.size(); i++) {
            ExecInterval e = expected.get(i);
            ExecInterval a = actual.get(i);
            
            Assert.assertEquals(getTime(e.getStart()), getTime(a.getStart()), Delta);
            Assert.assertEquals(getTime(e.getComplete()), getTime(a.getComplete()), Delta);
        }
    }

    @Test
    public void testMerge1() {
        List<ExecInterval> input = new ArrayList<ExecInterval>();
        input.add(createTimeInterval(2, 4));
        input.add(createTimeInterval(5, 7));
        input.add(createTimeInterval(6, 8));
        
        List<ExecInterval> expected = new ArrayList<ExecInterval>();
        expected.add(createTimeInterval(2, 4));
        expected.add(createTimeInterval(5, 8));
        
        List<ExecInterval> actual = ExecInterval.condenseOverlappingIntervals(input, predSync, moveCmp);
        assertEqualIntervals(expected, actual);
    }

    @Test
    public void testMerge2() {
        List<ExecInterval> input = new ArrayList<ExecInterval>();
        input.add(createTimeInterval(6, 8));
        input.add(createTimeInterval(5, 7));
        input.add(createTimeInterval(2, 4));
        
        List<ExecInterval> expected = new ArrayList<ExecInterval>();
        expected.add(createTimeInterval(5, 8));
        expected.add(createTimeInterval(2, 4));
        
        List<ExecInterval> actual = ExecInterval.condenseOverlappingIntervals(input, predSync, moveCmp);
        assertEqualIntervals(expected, actual);
    }
    
    @Test
    public void testMerge3() {
        List<ExecInterval> input = new ArrayList<ExecInterval>();
        input.add(createTimeInterval(6, 8));
        input.add(createTimeInterval(2, 4));
        input.add(createTimeInterval(5, 7));
        
        List<ExecInterval> expected = new ArrayList<ExecInterval>();
        expected.add(createTimeInterval(5, 8));
        expected.add(createTimeInterval(2, 4));
        
        List<ExecInterval> actual = ExecInterval.condenseOverlappingIntervals(input, predSync, moveCmp);
        assertEqualIntervals(expected, actual);
    }
    
    @Test
    public void testMerge4() {
        List<ExecInterval> input = new ArrayList<ExecInterval>();
        input.add(createTimeInterval(2, 5));
        input.add(createTimeInterval(7, 12));
        input.add(createTimeInterval(4, 6));
        input.add(createTimeInterval(3, 8));
        
        List<ExecInterval> expected = new ArrayList<ExecInterval>();
        expected.add(createTimeInterval(2, 12));
        
        List<ExecInterval> actual = ExecInterval.condenseOverlappingIntervals(input, predSync, moveCmp);
        assertEqualIntervals(expected, actual);
    }
}
