package org.processmining.ui.statechart.workbench.cancel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.RecipeProcess.GetArtifactMode;
import org.processmining.recipes.statechart.cancel.CancelListRecipe;
import org.processmining.recipes.statechart.discovery.DiscoverEPTreeRecipe;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Action2;
import org.processmining.utils.statechart.signals.Signal1;


public class CancelManualController
    extends CancelPreprocessController<CancelManualController.View, CancelListRecipe> {

    private static final Logger logger = LogManager
            .getLogger(CancelPreprocessController.class.getName());
    
    public static abstract class View extends CancelPreprocessController.View {
        public final Signal1<Set<String>> SignalInputErrors = new Signal1<>();

        public abstract void setInputOptions(Collection<String> values);
        public abstract void setInputSelected(Collection<String> selected);
    }

    private XLog currentLog;
    
    public CancelManualController(PluginContext context, WorkbenchModel model) {
        super(context, model, new CancelListRecipe());
    }

    @Override
    public void initialize() {
        view = new CancelManualView();
    }
    @Override
    public void activate() {
        super.activate();

        setLogUi(model.getArtifact(WorkbenchArtifacts.LogPre, GetArtifactMode.GetOnly));

        regrec.register(model.SignalDataChanged, new Action2<WorkbenchModel, RecipeArtifact<?>>() {
            @Override
            public void call(WorkbenchModel t, RecipeArtifact<?> u) {
                if (u == WorkbenchArtifacts.LogPre) {
                    setLogUi(model.getArtifact(WorkbenchArtifacts.LogPre, GetArtifactMode.GetOnly));
                } else if (u == WorkbenchArtifacts.EPTree) {
                    updateSelectedCancelFromDiscovery();
                }
            }

        });

        regrec.register(view.SignalInputErrors, new Action1<Set<String>>() {
            @Override
            public void call(Set<String> t) {
                preprocessRecipe.getParameters().oracle = t;
                updateUiTransform();
            }
        });
        
        updateSelectedCancelFromDiscovery();
    }
    
    private void updateSelectedCancelFromDiscovery() {
        Object discoverRecipe = model.getRecipe(WorkbenchArtifacts.EPTree);
        if (discoverRecipe != null && discoverRecipe instanceof DiscoverEPTreeRecipe) {
            DiscoverEPTreeRecipe discoverEPTreeRecipe = 
                (DiscoverEPTreeRecipe) discoverRecipe;
            Set<String> errors = discoverEPTreeRecipe.getParameters().getErrorClasses();
            if (errors != null) {
                view.setInputSelected(errors);
            }
        }
    }

    private void setLogUi(XLog log) {
        if (log != currentLog) {
            currentLog = log;
            
            
            if (currentLog != null) {
                runBackground(new SwingWorker<List<String>, Void>() {
                    @Override
                    protected List<String> doInBackground() throws Exception {
                        Set<String> values = model
                                .getArtifact(WorkbenchArtifacts.ActivityInfo)
                                .getActivities();
                        
                        List<String> valuesSorted = new ArrayList<String>(values);
                        Collections.sort(valuesSorted);
                        
                        return valuesSorted;
                    }

                    @Override
                    protected void done() {
                        try {
                            List<String> valuesSorted = get();
                            
                            view.setInputOptions(valuesSorted);
                            
                        } catch (InterruptedException | ExecutionException e) {
                            logger.error("Error in LogMultiAttrib - setLogUi", e);
                        }
                    }
                    
                });
            }
            
        }
    }
}
