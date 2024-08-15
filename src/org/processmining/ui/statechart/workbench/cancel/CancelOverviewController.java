package org.processmining.ui.statechart.workbench.cancel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Signal1;
import org.processmining.utils.statechart.ui.ctrlview.AbstractController;
import org.processmining.utils.statechart.ui.ctrlview.IView;

public class CancelOverviewController extends
        AbstractController<CancelOverviewController.View> {

    public final Signal1<CancelPreprocessors> SignalSelectAction = new Signal1<>();

    public static abstract class View implements IView {
        public final Signal1<CancelPreprocessors> SignalSelectAction = new Signal1<>();
    }

    protected final WorkbenchModel model;

    public CancelOverviewController(PluginContext context, WorkbenchModel model) {
        super(context);
        this.model = model;
    }

    @Override
    public void initialize() {
        view = new CancelOverviewView();
    }

    @Override
    public void activate() {
        regrec.register(view.SignalSelectAction,
                new Action1<CancelPreprocessors>() {
                    @Override
                    public void call(CancelPreprocessors t) {
                        SignalSelectAction.dispatch(t);
                    }
                });
    }

}
