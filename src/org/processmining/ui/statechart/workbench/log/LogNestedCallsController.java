package org.processmining.ui.statechart.workbench.log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.l2l.L2LSplitCalls;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.RecipeProcess.GetArtifactMode;
import org.processmining.recipes.statechart.l2l.L2LNestedCallsRecipe;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.ui.statechart.workbench.model.WorkbenchLogUtil;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.generic.ListUtil;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Action2;
import org.processmining.utils.statechart.signals.Signal1;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class LogNestedCallsController
        extends
        LogPreprocessController<LogNestedCallsController.View, L2LNestedCallsRecipe> {

    private static final Logger logger = LogManager
            .getLogger(LogNestedCallsController.class.getName());

    public static abstract class View extends LogPreprocessController.View {
        public final Signal1<XEventClassifier> SignalInputLabelClassifier = new Signal1<>();
        public final Signal1<XEventClassifier> SignalInputStartEndClassifier = new Signal1<>();
        
        public final Signal1<String> SignalInputStartSymbol = new Signal1<>();
        public final Signal1<String> SignalInputEndSymbol = new Signal1<>();
        
        public final Signal1<Boolean> SignalInputUseHandle = new Signal1<>();
        public final Signal1<String> SignalInputHandleSymbol = new Signal1<>();
        
        public final Signal1<Boolean> SignalInputUseSplit = new Signal1<>();
        public final Signal1<Pattern> SignalInputSplitPattern = new Signal1<>();

        public abstract void setLabelClassifierOptions(
                List<XEventClassifier> options, XEventClassifier defaultValue);

        public abstract void setStartEndClassifierOptions(
                List<XEventClassifier> options, XEventClassifier defaultValue);

        public abstract void setStartSymbolOptions(List<String> options,
                String defaultValue);

        public abstract void setEndSymbolOptions(List<String> options,
                String defaultValue);

        public abstract void setUseHandle(boolean use);
        
        public abstract void setHandleSymbolOptions(List<String> options,
                String defaultValue);

        public abstract void setUseSplit(boolean use);
        
        public abstract void setSplitPatternOptions(
                List<Pair<Pattern, String>> options,
                Pair<Pattern, String> defaultValue);
    }

    private XLog currentLog;
    private XLog currentLogSplitted;

    private boolean lastUseHandle;
    private String lastHandleSymbol;

    public LogNestedCallsController(PluginContext context, WorkbenchModel model) {
        super(context, model, new L2LNestedCallsRecipe());
        setNormalLabelFunc(new Function<XEvent, String>() {
            @Override
            public String apply(XEvent event) {
                return preprocessRecipe.getParameters()
                        .paramsNestedCalls.clsLabel.getClassIdentity(event);
            }
        });
    }

    @Override
    public void initialize() {
        view = new LogNestedCallsView();
    }

    @Override
    public void activate() {
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
                        preprocessRecipe.getParameters().setClsLabel(t);
                        updateUiTransform();
                    }
                });
        regrec.register(view.SignalInputStartEndClassifier,
                new Action1<XEventClassifier>() {
                    @Override
                    public void call(XEventClassifier t) {
                        preprocessRecipe.getParameters().setClsSR(t);
                        _computeStartEndOptions(t);
                        updateUiTransform();
                    }
                });
        regrec.register(view.SignalInputStartSymbol, new Action1<String>() {
            @Override
            public void call(String t) {
                preprocessRecipe.getParameters().setStartSymbol(t);
                updateUiTransform();
            }
        });
        regrec.register(view.SignalInputEndSymbol, new Action1<String>() {
            @Override
            public void call(String t) {
                preprocessRecipe.getParameters().setReturnSymbol(t);
                updateUiTransform();
            }
        });

        regrec.register(view.SignalInputUseHandle, new Action1<Boolean>() {
            @Override
            public void call(Boolean t) {
                lastUseHandle = t;
                if (lastUseHandle) {
                    preprocessRecipe.getParameters().setHandleSymbol(Optional.of(lastHandleSymbol));
                } else {
                    preprocessRecipe.getParameters().setHandleSymbol(Optional.<String>absent());
                }
                updateUiTransform();
            }
        });
        regrec.register(view.SignalInputHandleSymbol, new Action1<String>() {
            @Override
            public void call(String t) {
                lastHandleSymbol = t;
                if (lastUseHandle) {
                    preprocessRecipe.getParameters().setHandleSymbol(Optional.of(lastHandleSymbol));
                } else {
                    preprocessRecipe.getParameters().setHandleSymbol(Optional.<String>absent());
                }
                updateUiTransform();
            }
        });

        regrec.register(view.SignalInputUseSplit, new Action1<Boolean>() {
            @Override
            public void call(Boolean t) {
                preprocessRecipe.getParameters().setDoSplitCalls(t);
                currentLogSplitted = null;
                updateUiTransform();
            }
        });
        regrec.register(view.SignalInputSplitPattern, new Action1<Pattern>() {
            @Override
            public void call(Pattern t) {
                preprocessRecipe.getParameters().setReTraceBaseName(t);
                currentLogSplitted = null;
                updateUiTransform();
            }
        });
        
        super.activate();
    }

    private void setLogUi(XLog log) {
        if (log != currentLog) {
            currentLog = log;
            currentLogSplitted = null;

            lastUseHandle = preprocessRecipe.getParameters().paramsNestedCalls.handleSymbol.isPresent();
            if (lastUseHandle) {
                lastHandleSymbol = preprocessRecipe.getParameters().paramsNestedCalls.handleSymbol.get();
            }
            
            List<XEventClassifier> classifiers = WorkbenchLogUtil
                    .getAvailableClassifiers(currentLog);

            if (currentLog == null) {
                view.setLabelClassifierOptions(classifiers, null);
                view.setStartEndClassifierOptions(classifiers, null);
                view.setStartSymbolOptions(new ArrayList<String>(), null);
                view.setEndSymbolOptions(new ArrayList<String>(), null);
                view.setHandleSymbolOptions(new ArrayList<String>(), null);
            } else {
                XEventClassifier defaultLabel = ListUtil.findEqual(classifiers,
                        preprocessRecipe.getParameters().paramsNestedCalls.clsLabel);
                view.setLabelClassifierOptions(classifiers, defaultLabel);

                XEventClassifier defaultSR = ListUtil.findEqual(classifiers,
                        preprocessRecipe.getParameters().paramsNestedCalls.clsSR);
                view.setStartEndClassifierOptions(classifiers, defaultSR);

                view.setStartSymbolOptions(new ArrayList<String>(),
                        preprocessRecipe.getParameters().paramsNestedCalls.startSymbol);
                view.setEndSymbolOptions(new ArrayList<String>(),
                        preprocessRecipe.getParameters().paramsNestedCalls.returnSymbol);
                view.setHandleSymbolOptions(new ArrayList<String>(), lastHandleSymbol);
                _computeStartEndOptions(defaultSR);

                view.setUseSplit(preprocessRecipe.getParameters().doSplitCalls);
                List<Pair<Pattern, String>> optsSplit = Arrays
                        .asList(L2LSplitCalls.Parameters.ReSplitDefaults);
                Pair<Pattern, String> defaultSplit = ListUtil.findEqualByKey(
                        optsSplit, preprocessRecipe.getParameters().paramsSplitCalls.reTraceBaseName);
                view.setSplitPatternOptions(optsSplit, defaultSplit);
            }
        }
    }

    private void _computeStartEndOptions(final XEventClassifier defaultSR) {
        runBackground(new SwingWorker<List<String>, Void>() {

            @Override
            protected List<String> doInBackground() throws Exception {
                XEventClasses optsSR = XLogInfoFactory.createLogInfo(
                        currentLog, defaultSR).getEventClasses();

                List<String> optSRstrings = new ArrayList<>();
                for (XEventClass opt : optsSR.getClasses()) {
                    optSRstrings.add(opt.toString());
                }
                Collections.sort(optSRstrings);

                return optSRstrings;
            }

            @Override
            protected void done() {
                try {
                    List<String> optSRstrings = get();

                    view.setStartSymbolOptions(optSRstrings,
                            preprocessRecipe.getParameters().paramsNestedCalls.startSymbol);
                    view.setEndSymbolOptions(optSRstrings,
                            preprocessRecipe.getParameters().paramsNestedCalls.returnSymbol);
                    
                    view.setUseHandle(lastUseHandle);
                    view.setHandleSymbolOptions(optSRstrings, lastHandleSymbol);

                } catch (InterruptedException | ExecutionException e) {
                    logger.error(
                            "Error in LogNestedCalls - computeStartEndOptions",
                            e);
                }
            }
        });
    }

    @Override
    protected void setUiInputLog(XLog inputLog) {
        if (currentLogSplitted == null && currentLog != null) {
            logger.debug("Resplit input log for UI");
            currentLogSplitted = preprocessRecipe.splitLog(currentLog);
        }
        super.setUiInputLog(currentLogSplitted);
    }

    @Override
    protected void updateUiTransform() {
        if (currentLogSplitted == null && currentLog != null) {
            setUiInputLog(currentLog);
        } else {
            super.updateUiTransform();
        }
    }
}
