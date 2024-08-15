package org.processmining.models.statechart.align;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

import com.google.common.base.Predicate;

public class ExecInterval {

    private XAlignmentMove enabled, start, complete;
    private Object cause;
    private XAlignmentMove completeEnabled;

    private List<XAlignmentMove> logMovesPre, logMovesPost;
    
    /**
     * Condense list of intervals by merging overlapping intervals
     * @param intervals
     * @param predAcceptMove
     * @param cmp
     * @return
     */
    public static List<ExecInterval> condenseOverlappingIntervals(
            List<ExecInterval> intervals, 
            Predicate<XAlignmentMove> predAcceptMove, 
            Comparator<XAlignmentMove> cmp) {
        // Deep clone list
        List<ExecInterval> result = new ArrayList<ExecInterval>();
        for (ExecInterval ival : intervals) {
            result.add(new ExecInterval(ival));
        }
        
        // Merge
        for (int i = result.size() - 1; i >= 0; i--) {
            ExecInterval c = result.get(i);
            boolean processed = false;
            for (int j = 0; j < i && !processed; j++) {
                ExecInterval d = result.get(j);
                if (d.overlapInterval(c, predAcceptMove, cmp)) {
                    d.extendInterval(c, predAcceptMove, cmp);
                    result.remove(i);
                    processed = true;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Empty interval
     */
    public ExecInterval() {
        cause = null;
        enabled = null;
        start = null;
        complete = null;
    }
    
    /**
     * Clone ExecInterval
     * @param exec
     */
    public ExecInterval(ExecInterval exec) {
        cause = exec.cause;
        enabled = exec.enabled;
        completeEnabled = exec.completeEnabled;
        start = exec.start;
        complete = exec.complete;
        
        if (exec.logMovesPre != null) {
            logMovesPre = new ArrayList<>(exec.logMovesPre);
        }
        if (exec.logMovesPost != null) {
            logMovesPost = new ArrayList<>(exec.logMovesPost);
        }
    }
    
    /**
     * Create minimal containing ExecInterval for the cluster, 
     * using the given comparator to determine the boundary moves
     * @param cluster
     * @param predAcceptMove 
     * @param cmp
     */
    public ExecInterval(List<ExecInterval> cluster, 
            Predicate<XAlignmentMove> predAcceptMove, 
            Comparator<XAlignmentMove> cmp) {
        cause = null;
        enabled = null;
        completeEnabled = null;
        start = null;
        complete = null;
        for (ExecInterval ival : cluster) {
            extendInterval(ival, predAcceptMove, cmp);
        }
    }
    
    public boolean overlapInterval(ExecInterval ival, 
            Predicate<XAlignmentMove> predAcceptMove, 
            Comparator<XAlignmentMove> cmp) {
        return start != null && complete != null
            && predAcceptMove.apply(ival.getStart()) 
            && predAcceptMove.apply(ival.getComplete())
            && cmp.compare(ival.getStart(), complete) <= 0
            && cmp.compare(ival.getComplete(), start) >= 0;
    }
    
    public void extendInterval(ExecInterval ival, 
            Predicate<XAlignmentMove> predAcceptMove, 
            Comparator<XAlignmentMove> cmp) {
        boolean acceptEnabled = predAcceptMove.apply(ival.getEnabled());
        boolean acceptStart = predAcceptMove.apply(ival.getStart());
        boolean acceptComplete = predAcceptMove.apply(ival.getComplete());
        
        if (cause == null || !(ival.getCause() instanceof XAlignmentMove)
            || (cause instanceof XAlignmentMove
                && predAcceptMove.apply((XAlignmentMove) ival.getCause()) 
                && cmp.compare((XAlignmentMove) ival.getCause(), (XAlignmentMove) enabled) < 0
            )) {
            cause = ival.getCause();
        }
        if (acceptEnabled && (enabled == null || cmp.compare(ival.getEnabled(), enabled) < 0)) {
            enabled = ival.getEnabled();
        }
        if (acceptStart && (enabled == null || cmp.compare(ival.getStart(), enabled) < 0)) {
            enabled = ival.getStart();
        }
        if (acceptStart && (start == null || cmp.compare(ival.getStart(), start) < 0)) {
            start = ival.getStart();
        }
        if (acceptComplete && (complete == null || cmp.compare(ival.getComplete(), complete) > 0)) {
            complete = ival.getComplete();
            if (predAcceptMove.apply(ival.getCompleteEnabled())
                && (completeEnabled == null || cmp.compare(ival.getCompleteEnabled(), completeEnabled) > 0)) {
                completeEnabled = ival.getCompleteEnabled();
            }
        }
        
        if (acceptStart || acceptComplete) {
            for (XAlignmentMove move : ival.getLogMovesPre()) {
                appendLogMovePre(move);
            }
            for (XAlignmentMove move : ival.getLogMovesPost()) {
                appendLogMovePost(move);
            }
        }
    }

    public void setEnabled(XAlignmentMove enabled) {
        this.enabled = enabled;
    }

    public void setCompleteEnabled(XAlignmentMove completeEnabled) {
        this.completeEnabled = completeEnabled;
    }

    public void setCause(Object object) {
        this.cause = object;
    }
    
    public void setStart(XAlignmentMove start) {
        this.start = start;
    }
    
    public void setComplete(XAlignmentMove complete) {
        this.complete = complete;
    }

    public XAlignmentMove getEnabled() {
        return enabled;
    }

    public XAlignmentMove getCompleteEnabled() {
        return completeEnabled;
    }

    public Object getCause() {
        return cause;
    }
    
    public XAlignmentMove getStart() {
        return start;
    }

    public XAlignmentMove getComplete() {
        return complete;
    }
    
    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        bld.append("<");
        bld.append(enabled);
        bld.append(">[");
        bld.append(start);
        bld.append(", ");
        bld.append(complete);
        bld.append("]");
        return bld.toString();
    }
    
    public void appendLogMovePre(XAlignmentMove move) {
        if (logMovesPre == null) {
            logMovesPre = new ArrayList<>();
        }
        logMovesPre.add(move);
    }

    public void appendLogMovePost(XAlignmentMove move) {
        if (logMovesPost == null) {
            logMovesPost = new ArrayList<>();
        }
        logMovesPost.add(move);
    }
    
    public List<XAlignmentMove> getLogMovesPre() {
        if (logMovesPre == null) {
            return Collections.emptyList();
        } else {
            return logMovesPre;
        }
    }
    
    public List<XAlignmentMove> getLogMovesPost() {
        if (logMovesPost == null) {
            return Collections.emptyList();
        } else {
            return logMovesPost;
        }
    }
}
