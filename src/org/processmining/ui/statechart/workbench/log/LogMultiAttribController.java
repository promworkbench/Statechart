package org.processmining.ui.statechart.workbench.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.RecipeProcess.GetArtifactMode;
import org.processmining.recipes.statechart.l2l.L2LAttributeListRecipe;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.ui.statechart.workbench.model.WorkbenchLogUtil;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Action2;
import org.processmining.utils.statechart.signals.Signal1;

import com.google.common.base.Function;

public class LogMultiAttribController
        extends
        LogPreprocessController<LogMultiAttribController.View, L2LAttributeListRecipe> {

    private static final Logger logger = LogManager
            .getLogger(LogMultiAttribController.class.getName());
    
    public static abstract class View extends LogPreprocessController.View {
        public abstract void setClassifierOptions(
                Collection<XEventClassifier> options);

        public abstract void setAttributeOptions(Collection<String> options);

        public final Signal1<List<Object>> SignalInputSelectedOptions = new Signal1<List<Object>>();
    }

    private XLog currentLog;

    private final XEventClassifier originalClassifier = new XEventNameClassifier();
    
    public LogMultiAttribController(PluginContext context, WorkbenchModel model) {
        super(context, model, new L2LAttributeListRecipe());
        setNormalLabelFunc(new Function<XEvent, String>() {
            @Override
            public String apply(XEvent event) {
                return originalClassifier.getClassIdentity(event);
            }
            
        });
    }

    @Override
    public void initialize() {
        view = new LogMultiAttribView();
    }

    @Override
    public void activate() {
        super.activate();

        setLogUi(model.getArtifact(WorkbenchArtifacts.LogOriginal, GetArtifactMode.GetOnly));

        regrec.register(model.SignalDataChanged, new Action2<WorkbenchModel, RecipeArtifact<?>>() {
            @Override
            public void call(WorkbenchModel t, RecipeArtifact<?> u) {
                if (u == WorkbenchArtifacts.LogOriginal) {
                setLogUi(model.getArtifact(WorkbenchArtifacts.LogOriginal, GetArtifactMode.GetOnly));
                }
            }

        });

        regrec.register(view.SignalInputSelectedOptions,
                new Action1<List<Object>>() {
                    @Override
                    public void call(List<Object> t) {
                        preprocessRecipe.getParameters().clsList = _convert2ClassifierList(t);
                        updateUiTransform();
                    }
                });
    }

    private void setLogUi(XLog log) {
        if (log != currentLog) {
            currentLog = log;

            List<XEventClassifier> classifiers = WorkbenchLogUtil
                    .getAvailableClassifiers(currentLog);
            view.setClassifierOptions(classifiers);
            
            if (currentLog == null) {
                view.setAttributeOptions(new ArrayList<String>());
            } else {
                runBackground(new SwingWorker<List<String>, Void>() {
                    @Override
                    protected List<String> doInBackground() throws Exception {
                        XLogInfo logInfo = XLogInfoFactory
                                .createLogInfo(currentLog);
                        
                        final List<String> attribs = new ArrayList<>(logInfo
                                .getEventAttributeInfo().getAttributeKeys());
                        Collections.sort(attribs);
                        
                        return attribs;
                    }

                    @Override
                    protected void done() {
                        try {
                            List<String> attribs = get();
                            view.setAttributeOptions(attribs);
                            
                        } catch (InterruptedException | ExecutionException e) {
                            logger.error("Error in LogMultiAttrib - setLogUi", e);
                        }
                    }
                });
            }
        }
    }

    protected List<XEventClassifier> _convert2ClassifierList(List<Object> t) {
        List<XEventClassifier> result = new ArrayList<XEventClassifier>();

        for (Object obj : t) {
            if (obj instanceof XEventClassifier) {
                result.add((XEventClassifier) obj);
            } else if (obj instanceof String) {
                String attr = (String) obj;
                result.add(new XEventAttributeClassifier(attr, attr));
            }
        }

        return result;
    }
}
