package org.processmining.recipes.statechart.l2l;

import java.util.regex.Pattern;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.algorithms.statechart.l2l.L2LNestedCalls;
import org.processmining.algorithms.statechart.l2l.L2LSplitCalls;
import org.processmining.algorithms.statechart.l2l.subtrace.L2LSubtraceNestedCalls;

import com.google.common.base.Optional;

public class L2LNestedCallsRecipe extends AbstractL2LRecipe<L2LNestedCallsRecipe.Parameters> {

    public static class Parameters {
        public L2LNestedCalls.Parameters paramsNestedCalls;
        
        public boolean doSplitCalls;
        public L2LSplitCalls.Parameters paramsSplitCalls;

        public Parameters() {
            doSplitCalls = false;
//            paramsNestedCalls = new L2LListNestedCalls.Parameters();
            paramsNestedCalls = new L2LNestedCalls.Parameters();
            paramsSplitCalls = new L2LSplitCalls.Parameters();
        }
        
        public void setClsLabel(XEventClassifier clsLabel) {
            paramsNestedCalls.clsLabel = clsLabel;
            paramsSplitCalls.clsLabel = clsLabel;
        }
        
        public void setClsInst(XEventClassifier clsInst) {
//            paramsNestedCalls.clsInst = clsInst;
        }
        
        public void setClsSR(XEventClassifier clsSR) {
            paramsNestedCalls.clsSR = clsSR;
            paramsSplitCalls.clsSR = clsSR;
        }
        
        public void setStartSymbol(String startSymbol) {
            paramsNestedCalls.startSymbol = startSymbol;
            paramsSplitCalls.startSymbol = startSymbol;
        }
        
        public void setReturnSymbol(String returnSymbol) {
            paramsNestedCalls.returnSymbol = returnSymbol;
            paramsSplitCalls.returnSymbol = returnSymbol;
        }
        
        public void setHandleSymbol(Optional<String> handleSymbol) {
            paramsNestedCalls.handleSymbol = handleSymbol;
        }
        
        public void setReTraceBaseName(Pattern reTraceBaseName) {
            paramsSplitCalls.reTraceBaseName = reTraceBaseName;
        }
        
        public void setDoSplitCalls(boolean doSplitCalls) {
            this.doSplitCalls = doSplitCalls;
        }
    }

    public L2LNestedCallsRecipe() {
        super(new L2LNestedCallsRecipe.Parameters());
    }

    @Override
    protected XLog execute(XLog input) {
        input = splitLog(input);
        // TODO upgrade to real subtrace implementation
//        L2LListNestedCalls transform = new L2LListNestedCalls(getParameters().paramsNestedCalls);
        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls(getParameters().paramsNestedCalls);
        return transform.transform(input);
    }

    @Override
    public XTrace apply(XTrace input) {
//        L2LListNestedCalls transform = new L2LListNestedCalls(getParameters().paramsNestedCalls);
        L2LSubtraceNestedCalls transform = new L2LSubtraceNestedCalls(getParameters().paramsNestedCalls);
        return transform.apply(input);
    }

    public XLog splitLog(XLog input) {
        L2LNestedCallsRecipe.Parameters params = getParameters();
        if (params.doSplitCalls) {
            L2LSplitCalls transform = new L2LSplitCalls(params.paramsSplitCalls);
            return transform.transform(input);
        } else {
            return input;
        }
    }
}
