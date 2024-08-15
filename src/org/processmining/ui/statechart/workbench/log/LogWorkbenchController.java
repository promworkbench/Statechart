package org.processmining.ui.statechart.workbench.log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ui.statechart.workbench.WorkbenchController;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.signals.Action0;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Signal1;
import org.processmining.utils.statechart.ui.ctrlview.AbstractContainerController;

public class LogWorkbenchController
        extends
        AbstractContainerController<LogPreprocessors, LogWorkbenchController.View> {

    public final Signal1<WorkbenchController.ViewState> SignalLogReady = new Signal1<>();

    private WorkbenchModel model;

    @SuppressWarnings("rawtypes")
    private List<LogPreprocessController> preprocessControllers = new ArrayList<>();

    private LogOverviewController ctrlOverview;

    public static abstract class View extends
            AbstractContainerController.View<LogPreprocessors> {

        public abstract void displayComputing();
        public abstract void displayReady();
    }

    public LogWorkbenchController(PluginContext context, WorkbenchModel model) {
        super(context);
        this.model = model;
    }

    @Override
    public void initialize() {
        view = new LogWorkbenchView();

        ctrlOverview = new LogOverviewController(context, model);
        registerChildController(LogPreprocessors.Overview, ctrlOverview);

        for (LogPreprocessors preprocessor : LogPreprocessors.values()) {
            if (preprocessor.hasController()) {
                LogPreprocessController<?, ?> ctrl = preprocessor
                        .newController(context, model);
                registerChildController(preprocessor, ctrl);
                preprocessControllers.add(ctrl);
            }
        }
        
        initializeChildren();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void activate() {
        super.activate();
        
        regrec.register(ctrlOverview.SignalSelectAction, new Action1<LogPreprocessors>() {
            @Override
            public void call(LogPreprocessors t) {
                updateViewState(t);
            }
        });

        Action0 lstLogComputing = new Action0() {
            @Override
            public void call() {
                view.displayComputing();
            }
        };
        Action1<WorkbenchController.ViewState> lstLogReady = new Action1<WorkbenchController.ViewState>() {
            @Override
            public void call(WorkbenchController.ViewState t) {
                view.displayReady();
                SignalLogReady.dispatch(t);
            }
        };
        
        regrec.register(ctrlOverview.SignalLogComputing, lstLogComputing);
        regrec.register(ctrlOverview.SignalLogReady, lstLogReady);
        for (@SuppressWarnings("rawtypes")
            LogPreprocessController ctrl : preprocessControllers) {
            regrec.register(ctrl.SignalLogComputing, lstLogComputing);
            regrec.register(ctrl.SignalLogReady, lstLogReady);
        }

        if (getCurrentViewState() == null) {
            updateViewState(LogPreprocessors.Overview);
        }
        updateViewStatesEnabled(LogPreprocessors.values(),
                Arrays.asList(LogPreprocessors.values()));
        view.displayReady();
    }
}
