package org.processmining.ui.statechart.workbench;

import java.util.HashSet;
import java.util.Set;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.log.ActivityInfoRecipe;
import org.processmining.recipes.statechart.metrics.JoinpointStatsRecipe;
import org.processmining.ui.statechart.workbench.cancel.CancelWorkbenchController;
import org.processmining.ui.statechart.workbench.discovery.DiscoveryWorkbenchController;
import org.processmining.ui.statechart.workbench.log.LogWorkbenchController;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.signals.Action0;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Action2;
import org.processmining.utils.statechart.ui.ctrlview.AbstractContainerController;

public class WorkbenchController
        extends
        AbstractContainerController<WorkbenchController.ViewState, WorkbenchController.View> {

    public static enum ViewState {
        LogHierarchy("Log Hierarchy"), 
        CancellationSetup("Cancellation Setup"), 
        Discovery("Discovery & Analysis");

        private final String label;

        private ViewState(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static abstract class View extends
            AbstractContainerController.View<WorkbenchController.ViewState> {

    }

    private WorkbenchModel model;

    private LogWorkbenchController ctrlLogHierarchy;
    private CancelWorkbenchController ctrlCancellationSetup;
    private DiscoveryWorkbenchController ctrlDisc;

    public WorkbenchController(PluginContext context) {
        super(context);
    }

    @Override
    public void initialize() {
        model = new WorkbenchModel();
        view = new WorkbenchView();

        // Generic Log derivatives recipes
        model.setRecipe(WorkbenchArtifacts.JoinpointStats, new JoinpointStatsRecipe());
        model.setRecipe(WorkbenchArtifacts.ActivityInfo, new ActivityInfoRecipe());

        // Prepare all child controllers
        ctrlLogHierarchy = new LogWorkbenchController(context, model);
        ctrlCancellationSetup = new CancelWorkbenchController(context, model);
        ctrlDisc = new DiscoveryWorkbenchController(context, model);
        registerChildController(ViewState.LogHierarchy, ctrlLogHierarchy);
        registerChildController(ViewState.CancellationSetup, ctrlCancellationSetup);
        registerChildController(ViewState.Discovery, ctrlDisc);

        initializeChildren();
    }

    @Override
    public void activate() {
        super.activate();

        // register listeners
        regrec.register(model.SignalDataChanged,
                new Action2<WorkbenchModel, RecipeArtifact<?>>() {
                    @Override
                    public void call(WorkbenchModel t, RecipeArtifact<?> u) {
                        runUi(new Runnable() {
                            @Override
                            public void run() {
                                updateViewStatesEnabled();
                            }
                        });
                    }
                });

        regrec.register(ctrlLogHierarchy.SignalLogReady, new Action1<WorkbenchController.ViewState>() {
            @Override
            public void call(final WorkbenchController.ViewState t) {
                runUi(new Runnable() {
                    @Override
                    public void run() {
                        updateViewState(t);
                        if (t == ViewState.Discovery) {
                            ctrlDisc.updateDiagram(true);
                        }
                        // TODO: unnecessary dependency, use model data change?
                    }
                });
            }
        });
        
        regrec.register(ctrlCancellationSetup.SignalCancelReady, new Action0() {
            @Override
            public void call() {
                updateViewState(ViewState.Discovery);
                ctrlDisc.updateDiagram(true);
            }
        });
    }

    public WorkbenchModel getModel() {
        return model;
    }

    public LogWorkbenchController getLogWorkbenchController() {
        return ctrlLogHierarchy;
    }
    
    public CancelWorkbenchController getCancelWorkbenchController() {
        return ctrlCancellationSetup;
    }
    
    public DiscoveryWorkbenchController getDiscoveryWorkbenchController() {
        return ctrlDisc;
    }
    
    protected void updateViewStatesEnabled() {
        Set<ViewState> enabledViews = new HashSet<>();
        if (model.hasArtifact(WorkbenchArtifacts.LogOriginal)) {
            enabledViews.add(ViewState.LogHierarchy);
        }
        if (model.hasArtifact(WorkbenchArtifacts.LogPre)) {
            enabledViews.add(ViewState.CancellationSetup);
        }
        if (model.hasArtifact(WorkbenchArtifacts.LogPre)
            || model.hasArtifact(WorkbenchArtifacts.EPTree)
            || model.hasArtifact(WorkbenchArtifacts.Statechart)) {
            enabledViews.add(ViewState.Discovery);
        }
        
        updateViewStatesEnabled(ViewState.values(), enabledViews);
    }

}
