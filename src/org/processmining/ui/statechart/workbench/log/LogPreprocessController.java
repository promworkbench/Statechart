package org.processmining.ui.statechart.workbench.log;

import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.RecipeProcess.GetArtifactMode;
import org.processmining.recipes.statechart.l2l.AbstractL2LRecipe;
import org.processmining.ui.statechart.workbench.WorkbenchController;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.ui.statechart.workbench.model.WorkbenchLogUtil;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Action2;
import org.processmining.utils.statechart.signals.Signal0;
import org.processmining.utils.statechart.signals.Signal1;
import org.processmining.utils.statechart.ui.ctrlview.AbstractController;
import org.processmining.utils.statechart.ui.ctrlview.IView;

import com.google.common.base.Function;

public abstract class LogPreprocessController<V extends LogPreprocessController.View, R extends AbstractL2LRecipe<?>>
        extends AbstractController<V> {

    public final Signal0 SignalLogComputing = new Signal0();
    public final Signal1<WorkbenchController.ViewState> SignalLogReady = new Signal1<>();
    
    public static abstract class View implements IView {
        public final Signal1<WorkbenchController.ViewState> SignalStartAction = new Signal1<>();

        public abstract void setLogUi(XLog inputLog, Function<XEvent, String> labelNormal, Function<XTrace, XTrace> transform);
        public abstract void setLogUi(Function<XEvent, String> labelNormal, Function<XTrace, XTrace> transform);
    }

    protected final WorkbenchModel model;
    protected final R preprocessRecipe;

    protected XEventClassifier normalClassifier = new XEventNameClassifier();
    
    protected final Function<XTrace,XTrace> preprocessTraceFunc;
    private Function<XEvent, String> labelNormal;

    public LogPreprocessController(PluginContext context, WorkbenchModel model,
            R inPreprocessRecipe) {
        super(context);
        this.model = model;
        this.preprocessRecipe = inPreprocessRecipe;
        
        setNormalLabelFunc(new Function<XEvent, String>() {
            @Override
            public String apply(XEvent event) {
                return normalClassifier.getClassIdentity(event);
            }
        });
        this.preprocessTraceFunc = new Function<XTrace, XTrace>() {
            @Override
            public XTrace apply(XTrace input) {
                return preprocessRecipe.apply(input);
            }
        };
    }
    
    protected void setNormalLabelFunc(Function<XEvent, String> labelNormal) {
        this.labelNormal = labelNormal;
    }

    @Override
    public void activate() {
        setUiInputLog(model.getArtifact(WorkbenchArtifacts.LogOriginal,
                GetArtifactMode.GetOnly));

        regrec.register(model.SignalDataChanged,
                new Action2<WorkbenchModel, RecipeArtifact<?>>() {
                    @Override
                    public void call(WorkbenchModel t, RecipeArtifact<?> u) {
                        if (u == WorkbenchArtifacts.LogOriginal) {
                            setUiInputLog(model.getArtifact(
                                    WorkbenchArtifacts.LogOriginal,
                                    GetArtifactMode.GetOnly));
                        }
                    }

                });

        regrec.register(view.SignalStartAction, new Action1<WorkbenchController.ViewState>() {
            @Override
            public void call(final WorkbenchController.ViewState t) {
                SignalLogComputing.dispatch();
                
                runBackground(new Runnable() {
                    @Override
                    public void run() {
                        model.setRecipe(WorkbenchArtifacts.LogPre, preprocessRecipe, true);
                        model.computeArtifact(WorkbenchArtifacts.LogPre, true);
                        
                        runUi(new Runnable() {
                            @Override
                            public void run() {
                                SignalLogReady.dispatch(t);
                            }
                        });
                    }
                });
            }
        });
    }

    protected void setUiInputLog(XLog inputLog) {
        List<XEventClassifier> classifiers = WorkbenchLogUtil
                .getAvailableClassifiers(inputLog);
        
        if (!classifiers.isEmpty()) {
            normalClassifier = classifiers.get(0);
        }
        
        if (view != null) {
            view.setLogUi(inputLog, labelNormal, preprocessTraceFunc);
        }
    }
    
    protected void updateUiTransform() {
        if (view != null) {
            view.setLogUi(labelNormal, preprocessTraceFunc);
        }
    }
}
