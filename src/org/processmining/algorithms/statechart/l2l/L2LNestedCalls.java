package org.processmining.algorithms.statechart.l2l;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.statechart.log.LogFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public abstract class L2LNestedCalls implements Function<XLog, XLog> {

    public static class Parameters {
        public XEventClassifier clsLabel; // event activity label
        public XEventClassifier clsSR; // event Start-Return labeler
        public String startSymbol; // Start symbol to match
        public String returnSymbol; // End symbol to match
        public Optional<String> handleSymbol; // Error handle symbol to match
        
        public Parameters() {
            clsLabel = new XEventNameClassifier();
            clsSR = new XEventLifeTransClassifier();
            startSymbol = XLifecycleExtension.StandardModel.START.getEncoding();
            returnSymbol = XLifecycleExtension.StandardModel.COMPLETE.getEncoding();
            handleSymbol = Optional.of(XLifecycleExtension.StandardModel.REASSIGN.getEncoding());
        }
    }

    protected final Parameters params;

    public L2LNestedCalls() {
        this(new Parameters());
    }

    public L2LNestedCalls(Parameters params) {
        Preconditions.checkNotNull(params, "No parameters set");
        this.params = params;
    }

    @Override
    public XLog apply(XLog input) {
        return transform(input);
    }

    public XTrace apply(XTrace input) {
        XFactory f = LogFactory.getFactory();
        return _transformTrace(f, input);
    }


    public abstract XLog transform(XLog input);

    protected abstract XTrace _transformTrace(XFactory f, XTrace input);
    
}
