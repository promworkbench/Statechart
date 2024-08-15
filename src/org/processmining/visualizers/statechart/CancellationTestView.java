package org.processmining.visualizers.statechart;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.ui.statechart.cancellation.CancellationController;
import org.processmining.ui.statechart.cancellation.model.CancellationArtifacts;

public class CancellationTestView {

    @Plugin(
            name = "Cancellation test (Statechart derivative)", 
            returnLabels = { "Cancellation test (Statechart derivative) - XLog" }, 
            returnTypes = { JComponent.class }, 
            parameterLabels = { "Event Log" }, 
            userAccessible = true
    )
    @Visualizer
    public JComponent visualize(PluginContext context, XLog log) {
        CancellationController ctrl = new CancellationController(context);
        ctrl.getModel().setArtifact(CancellationArtifacts.LogOriginal, log);
        ctrl.initialize();
        ctrl.activate();
        return ctrl.getView().getComponent();
    }
}
