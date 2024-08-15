package org.processmining.ui.statechart.workbench.cancel;

import gnu.trove.set.hash.THashSet;

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
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Action2;
import org.processmining.utils.statechart.signals.Signal1;


public class CancelQualityController
    extends CancelPreprocessController<CancelQualityController.View, CancelListRecipe> {

    private static final Logger logger = LogManager
            .getLogger(CancelPreprocessController.class.getName());
    
    public static abstract class View extends CancelPreprocessController.View {
        public final Signal1<Set<String>> SignalInputErrors = new Signal1<>();

        public abstract void setInputOptions(Collection<String> values, Collection<String> defaultVals);
    }

    private XLog currentLog;
    
    public CancelQualityController(PluginContext context, WorkbenchModel model) {
        super(context, model, new CancelListRecipe()); // Do something smarter here
    }

    @Override
    public void initialize() {
        view = new CancelQualityView();
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
                }
            }

        });

        regrec.register(view.SignalInputErrors, new Action1<Set<String>>() {
            @Override
            public void call(Set<String> t) {
                model.setArtifact(WorkbenchArtifacts.CancelOracleInput, t);
            }
        });
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
                            Set<String> defaultVals = new THashSet<String>();
                            
                            view.setInputOptions(valuesSorted, defaultVals);
                            
                        } catch (InterruptedException | ExecutionException e) {
                            logger.error("Error in LogMultiAttrib - setLogUi", e);
                        }
                    }
                    
                });
            }
            
        }
    }
}
