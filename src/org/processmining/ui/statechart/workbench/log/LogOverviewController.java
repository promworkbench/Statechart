package org.processmining.ui.statechart.workbench.log;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ui.statechart.workbench.WorkbenchController;
import org.processmining.ui.statechart.workbench.WorkbenchController.ViewState;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.signals.Action0;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Signal0;
import org.processmining.utils.statechart.signals.Signal1;
import org.processmining.utils.statechart.ui.ctrlview.AbstractController;
import org.processmining.utils.statechart.ui.ctrlview.IView;

public class LogOverviewController extends
        AbstractController<LogOverviewController.View> {

    public final Signal0 SignalLogComputing = new Signal0();
    public final Signal1<WorkbenchController.ViewState> SignalLogReady = new Signal1<>();
    
    public final Signal1<LogPreprocessors> SignalSelectAction = new Signal1<>();
    
    public static abstract class View implements IView {
        public final Signal1<LogPreprocessors> SignalSelectAction = new Signal1<>();
        public final Signal0 SignalUseNormalPreset = new Signal0();
        public final Signal0 SignalUseSWPreset = new Signal0();
    }

    protected final WorkbenchModel model;

    public LogOverviewController(PluginContext context, WorkbenchModel model) {
        super(context);
        this.model = model;
    }

    @Override
    public void initialize() {
        view = new LogOverviewView();
    }

    @Override
    public void activate() {
        regrec.register(view.SignalSelectAction, new Action1<LogPreprocessors>() {
            @Override
            public void call(LogPreprocessors t) {
                SignalSelectAction.dispatch(t);
            }
        });
        
        regrec.register(view.SignalUseNormalPreset, new Action0() {
            @Override
            public void call() {
                SignalLogComputing.dispatch();

                runBackground(new Runnable() {
                    @Override
                    public void run() {
                        model.setNormalLogPresets();
                        
                        // Finish
                        runUi(new Runnable() {
                            @Override
                            public void run() {
                                SignalLogReady.dispatch(ViewState.Discovery);
                            }
                        });
                    }
                });
            }
        });
        
        regrec.register(view.SignalUseSWPreset, new Action0() {
            @Override
            public void call() {
                SignalLogComputing.dispatch();

                runBackground(new Runnable() {
                    @Override
                    public void run() {
                        model.setSWLogPresets();
                        
                        // Finish
                        runUi(new Runnable() {
                            @Override
                            public void run() {
                                SignalLogReady.dispatch(ViewState.Discovery);
                            }
                        });
                    }
                });
            }
        });
    }
}
