package org.processmining.algorithms.statechart.discovery.im.postprocessor;

import org.deckfour.xes.model.XEvent;
import org.processmining.models.statechart.decorate.swapp.SwAppDecoration;
import org.processmining.models.statechart.decorate.swapp.SwAppProperty;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.models.statechart.processtree.ISCCompositeOr;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;
import org.processmining.processtree.Node;
import org.processmining.processtree.Task.Manual;

import com.google.common.base.Optional;

public class SwAppDecoratorPostprocessor implements PostProcessor {

    @Override
    public Node postProcess(Node node, IMLog log, IMLogInfo logInfo,
            MinerState minerState) {
        if (node != null) {
            _annotate(node, log, logInfo);
        }

        return node;
    }

    private void _annotate(Node node, IMLog log, IMLogInfo logInfo) {
        try {
            IMLogHierarchy hlog = (IMLogHierarchy) log;
            
            if (node instanceof Manual || node instanceof ISCCompositeOr) {
                // find an example event, and use those values
                for (IMTrace trace : log) {
                    for (XEvent event : trace) {
                        if (log.classify(trace, event).getId()
                                .equals(node.getName())) {
                            Optional<SwAppDecoration> value = SwAppDecoration.deriveInstance(hlog, trace, event);
                            if (value.isPresent()) {
                                SwAppProperty.setValue(node, value.get());
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        }
    }

}
