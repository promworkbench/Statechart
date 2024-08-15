package org.processmining.ui.statechart.workbench.cancel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.signals.Action0;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Signal0;
import org.processmining.utils.statechart.ui.ctrlview.AbstractContainerController;

public class CancelWorkbenchController
    extends AbstractContainerController<CancelPreprocessors, CancelWorkbenchController.View> {

    public final Signal0 SignalCancelReady = new Signal0();

    private WorkbenchModel model;

    @SuppressWarnings("rawtypes")
    private List<CancelPreprocessController> preprocessControllers = new ArrayList<>();

    private CancelOverviewController ctrlOverview;

    public static abstract class View extends
            AbstractContainerController.View<CancelPreprocessors> {

        public abstract void displayComputing();
        public abstract void displayReady();
    }

    public CancelWorkbenchController(PluginContext context, WorkbenchModel model) {
        super(context);
        this.model = model;
    }

    @Override
    public void initialize() {
        view = new CancelWorkbenchView();

        ctrlOverview = new CancelOverviewController(context, model);
        registerChildController(CancelPreprocessors.Overview, ctrlOverview);

        for (CancelPreprocessors preprocessor : CancelPreprocessors.values()) {
            if (preprocessor.hasController()) {
                CancelPreprocessController<?, ?> ctrl = preprocessor.newController(context, model);
                registerChildController(preprocessor, ctrl);
                preprocessControllers.add(ctrl);
            }
        }
        
        initializeChildren();
    }

    @Override
    public void activate() {
        super.activate();
        
        regrec.register(ctrlOverview.SignalSelectAction, new Action1<CancelPreprocessors>() {
            @Override
            public void call(CancelPreprocessors t) {
                updateViewState(t);
            }
        });

        Action0 lstLogComputing = new Action0() {
            @Override
            public void call() {
                view.displayComputing();
            }
        };
        Action0 lstLogReady = new Action0() {
            @Override
            public void call() {
                view.displayReady();
                SignalCancelReady.dispatch();
            }
        };
        for (@SuppressWarnings("rawtypes")
        CancelPreprocessController ctrl : preprocessControllers) {
            regrec.register(ctrl.SignalCancelComputing, lstLogComputing);
            regrec.register(ctrl.SignalCancelReady, lstLogReady);
        }

        if (getCurrentViewState() == null) {
            updateViewState(CancelPreprocessors.Overview);
        }
        updateViewStatesEnabled(CancelPreprocessors.values(),
                Arrays.asList(CancelPreprocessors.values()));
        view.displayReady();
    }
}
