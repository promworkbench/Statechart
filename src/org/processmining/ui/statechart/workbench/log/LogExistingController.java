package org.processmining.ui.statechart.workbench.log;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.recipes.statechart.l2l.L2LIdentityRecipe;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;

public class LogExistingController
        extends
        LogPreprocessController<LogExistingController.View, L2LIdentityRecipe> {

    public static abstract class View extends LogPreprocessController.View {

    }

    public LogExistingController(PluginContext context, WorkbenchModel model) {
        super(context, model, new L2LIdentityRecipe());
        setNormalLabelFunc(null);
    }

    @Override
    public void initialize() {
        view = new LogExistingView();
    }
}
