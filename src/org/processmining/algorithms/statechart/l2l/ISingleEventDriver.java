package org.processmining.algorithms.statechart.l2l;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import com.google.common.base.Function;

public interface ISingleEventDriver extends Function<XLog, XLog> {

    public void setImplement(ISingleEventImplementation implementation);

    @Override
    public XLog apply(XLog input);

    public XTrace apply(XTrace input);
}
