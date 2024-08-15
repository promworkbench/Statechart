package org.processmining.ui.statechart.workbench.cancel;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.statechart.log.HierarchyActivityInfo;
import org.processmining.recipes.statechart.AbstractRecipe;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.RecipeProcess.GetArtifactMode;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.signals.Action0;
import org.processmining.utils.statechart.signals.Action2;
import org.processmining.utils.statechart.signals.Signal0;
import org.processmining.utils.statechart.ui.ctrlview.AbstractController;
import org.processmining.utils.statechart.ui.ctrlview.IView;

public abstract class CancelPreprocessController<
    V extends CancelPreprocessController.View, 
    R extends AbstractRecipe<Pair<XLog, HierarchyActivityInfo>, Set<String>, ?>
    > extends AbstractController<V> {

    public final Signal0 SignalCancelComputing = new Signal0();
    public final Signal0 SignalCancelReady = new Signal0();
    
    public static abstract class View implements IView {
        public final Signal0 SignalStartAction = new Signal0();

        public abstract void setLogUi(XLog inputLog, Set<String> cancelOracle);
        
        public abstract void setLogUi(Set<String> cancelOracle);
    }
    
    protected final WorkbenchModel model;
    protected final R preprocessRecipe;

    public CancelPreprocessController(PluginContext context, WorkbenchModel model,
            R inPreprocessRecipe) {
        super(context);
        this.model = model;
        this.preprocessRecipe = inPreprocessRecipe;
    }
    
    @Override
    public void activate() {
        setUiInputLog(model.getArtifact(WorkbenchArtifacts.LogPre,
                GetArtifactMode.GetOnly));

        regrec.register(model.SignalDataChanged,
                new Action2<WorkbenchModel, RecipeArtifact<?>>() {
                    @Override
                    public void call(WorkbenchModel t, RecipeArtifact<?> u) {
                        if (u == WorkbenchArtifacts.LogPre) {
                            setUiInputLog(model.getArtifact(
                                    WorkbenchArtifacts.LogPre, GetArtifactMode.GetOnly));
                        }
                    }

                });

        regrec.register(view.SignalStartAction, new Action0() {
            @Override
            public void call() {
                SignalCancelComputing.dispatch();
                
                runBackground(new Runnable() {
                    @Override
                    public void run() {
                        model.setRecipe(WorkbenchArtifacts.CancelOracleInput, preprocessRecipe, true);
                        model.computeArtifact(WorkbenchArtifacts.CancelOracleInput, true);
                        
                        runUi(new Runnable() {
                            @Override
                            public void run() {
                                SignalCancelReady.dispatch();
                            }
                        });
                    }
                });
            }
        });
    }

    protected void setUiInputLog(XLog inputLog) {
        if (view != null) {
            view.setLogUi(inputLog, getCancelOracle());
        }
    }
    
    protected void updateUiTransform() {
        if (view != null) {
            view.setLogUi(getCancelOracle());
        }
    }

    protected Set<String> getCancelOracle() {
        return model.getArtifact(WorkbenchArtifacts.CancelOracleInput, 
                GetArtifactMode.GetOnly);
    }
}
