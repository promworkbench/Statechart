package org.processmining.algorithms.statechart.align.metric.logic;

import org.processmining.xesalignmentextension.XAlignmentExtension.MoveType;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

import com.google.common.base.Predicate;

public class NoModelMovePred implements Predicate<XAlignmentMove> {

    @Override
    public boolean apply(XAlignmentMove move) {
        return move != null && move.getType() != MoveType.MODEL;
    }

}
