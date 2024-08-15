package org.processmining.ui.statechart.workbench.log;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.l2l.list.L2LListPatternActivity;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.RecipeProcess.GetArtifactMode;
import org.processmining.recipes.statechart.l2l.L2LPatternActivityRecipe;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.ui.statechart.workbench.model.WorkbenchLogUtil;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.generic.ListUtil;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Action2;
import org.processmining.utils.statechart.signals.Signal1;

import com.google.common.base.Function;

public class LogPatternNamesController
        extends
        LogPreprocessController<LogPatternNamesController.View, L2LPatternActivityRecipe> {

    public static abstract class View extends LogPreprocessController.View {
        public final Signal1<XEventClassifier> SignalInputLabelClassifier = new Signal1<>();
        public final Signal1<Pattern> SignalInputPartsPattern = new Signal1<>();

        public abstract void setLabelClassifierOptions(
                List<XEventClassifier> options, XEventClassifier defaultValue);

        public abstract void setPartsPatternOptions(
                List<Pair<Pattern, String>> options,
                Pair<Pattern, String> defaultValue);
    }

    private XLog currentLog;

    public LogPatternNamesController(PluginContext context, WorkbenchModel model) {
        super(context, model, new L2LPatternActivityRecipe());
        setNormalLabelFunc(new Function<XEvent, String>() {
            @Override
            public String apply(XEvent arg0) {
                return preprocessRecipe.getParameters().clsLabel.getClassIdentity(arg0);
            }
            
        });
    }

    @Override
    public void initialize() {
        view = new LogPatternNamesView();
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
                        preprocessRecipe.getParameters().clsLabel = t;
                        updateUiTransform();
                    }
                });
        regrec.register(view.SignalInputPartsPattern, new Action1<Pattern>() {
            @Override
            public void call(Pattern t) {
                preprocessRecipe.getParameters().reParts = t;
                updateUiTransform();
            }
        });
    }

    private void setLogUi(XLog log) {
        if (log != currentLog) {
            currentLog = log;

            List<XEventClassifier> classifiers = WorkbenchLogUtil
                    .getAvailableClassifiers(currentLog);

            if (currentLog == null) {
                view.setLabelClassifierOptions(classifiers, null);
            } else {
                XEventClassifier defaultLabel = ListUtil.findEqual(classifiers,
                        preprocessRecipe.getParameters().clsLabel);
                view.setLabelClassifierOptions(classifiers, defaultLabel);
            }

            List<Pair<Pattern, String>> optsSelect = Arrays
                    .asList(L2LListPatternActivity.Parameters.RePartsDefaults);
            Pair<Pattern, String> defaultSelect = ListUtil.findEqualByKey(
                    optsSelect, preprocessRecipe.getParameters().reParts);
            view.setPartsPatternOptions(optsSelect, defaultSelect);
        }
    }
}
