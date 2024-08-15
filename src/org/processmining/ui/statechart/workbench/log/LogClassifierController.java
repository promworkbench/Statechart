package org.processmining.ui.statechart.workbench.log;

import java.util.Collections;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.RecipeProcess.GetArtifactMode;
import org.processmining.recipes.statechart.l2l.L2LAttributeListRecipe;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.ui.statechart.workbench.model.WorkbenchLogUtil;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.generic.ListUtil;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Action2;
import org.processmining.utils.statechart.signals.Signal1;

import com.google.common.base.Function;

public class LogClassifierController
        extends
        LogPreprocessController<LogClassifierController.View, L2LAttributeListRecipe> {

    public static abstract class View extends LogPreprocessController.View {
        public final Signal1<XEventClassifier> SignalInputLabelClassifier = new Signal1<>();

        public abstract void setLabelClassifierOptions(
                List<XEventClassifier> options, XEventClassifier defaultValue);
    }

    private XLog currentLog;

    public LogClassifierController(PluginContext context, WorkbenchModel model) {
        super(context, model, new L2LAttributeListRecipe());
        setNormalLabelFunc(new Function<XEvent, String>() {
            @Override
            public String apply(XEvent event) {
                List<XEventClassifier> lstCls = preprocessRecipe.getParameters().clsList;
                if (lstCls.size() > 0) {
                    return lstCls.get(0).getClassIdentity(event);   
                } else {
                    return normalClassifier.getClassIdentity(event);
                }
            }
            
        });
    }

    @Override
    public void initialize() {
        view = new LogClassifierView();
    }

    @Override
    public void activate() {
        super.activate();

        setLogUi(model.getArtifact(WorkbenchArtifacts.LogOriginal,
                GetArtifactMode.GetOnly));

        regrec.register(model.SignalDataChanged,
                new Action2<WorkbenchModel, RecipeArtifact<?>>() {
                    @Override
                    public void call(WorkbenchModel t, RecipeArtifact<?> u) {
                        if (u == WorkbenchArtifacts.LogOriginal) {
                            setLogUi(model.getArtifact(
                                    WorkbenchArtifacts.LogOriginal,
                                    GetArtifactMode.GetOnly));
                        }
                    }

                });

        regrec.register(view.SignalInputLabelClassifier,
                new Action1<XEventClassifier>() {
                    @Override
                    public void call(XEventClassifier t) {
                        setClassifier(t);
                    }
                });
    }

    protected void setClassifier(XEventClassifier t) {
        preprocessRecipe.getParameters().clsList = Collections.singletonList(t);
        updateUiTransform();
    }

    private void setLogUi(XLog log) {
        if (log != currentLog) {
            currentLog = log;

            List<XEventClassifier> classifiers = WorkbenchLogUtil
                    .getAvailableClassifiers(currentLog);

            if (preprocessRecipe.getParameters().clsList.isEmpty()) {
                setClassifier(classifiers.get(0));
            }
            
            if (currentLog == null) {
                view.setLabelClassifierOptions(classifiers, null);
            } else {
                XEventClassifier defaultLabel = null;
                if (!preprocessRecipe.getParameters().clsList.isEmpty()) {
                    defaultLabel = ListUtil.findEqual(classifiers,
                        preprocessRecipe.getParameters().clsList.get(0));
                } else if (!classifiers.isEmpty()) {
                    defaultLabel = classifiers.get(0);
                }
                view.setLabelClassifierOptions(classifiers, defaultLabel);
            }
        }
    }

}
