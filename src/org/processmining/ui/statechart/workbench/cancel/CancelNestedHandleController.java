package org.processmining.ui.statechart.workbench.cancel;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.RecipeProcess.GetArtifactMode;
import org.processmining.recipes.statechart.cancel.CancelNestedHandleRecipe;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.signals.Action2;


public class CancelNestedHandleController
    extends CancelPreprocessController<CancelNestedHandleController.View, CancelNestedHandleRecipe> {

    private static final Logger logger = LogManager
            .getLogger(CancelPreprocessController.class.getName());
    
    public static abstract class View extends CancelPreprocessController.View {
        
    }

    private XLog currentLog;
    
    public CancelNestedHandleController(PluginContext context, WorkbenchModel model) {
        super(context, model, new CancelNestedHandleRecipe());
    }

    @Override
    public void initialize() {
        view = new CancelNestedHandleView();
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
    }
    
    private void setLogUi(XLog log) {
        if (log != currentLog) {
            currentLog = log;
            
            
            if (currentLog != null) {
                runBackground(new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        model.setRecipe(WorkbenchArtifacts.CancelOracleInput, preprocessRecipe, true);
                        model.computeArtifact(WorkbenchArtifacts.CancelOracleInput, true);
                        
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                            
                            updateUiTransform();
                            
                        } catch (InterruptedException | ExecutionException e) {
                            logger.error("Error in LogMultiAttrib - setLogUi", e);
                        }
                    }
                    
                });
            }
            
        }
    }
}
