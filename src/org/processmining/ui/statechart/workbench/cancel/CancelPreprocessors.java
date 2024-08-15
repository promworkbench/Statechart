package org.processmining.ui.statechart.workbench.cancel;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JPanel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;

public enum CancelPreprocessors {
    // Special preprocessors overview view/tab
    Overview("Overview", null, null),
    
    // actual preprocessors, in the order they should be shown
    NestedHandle("Nested Calls Handle", CancelNestedHandleController.class, CancelNestedHandleView.getDescriptionPanel()),
//    Quality("Quality Optimization", CancelQualityController.class, CancelQualityView.getDescriptionPanel()),
    Manual("Manual", CancelManualController.class, CancelManualView.getDescriptionPanel())
    ;
    
    private final String label; 
    private final Class<? extends CancelPreprocessController<?, ?>> fncMvcController;
    private final JPanel fncDescriptionPanel;
    
    private CancelPreprocessors(String label,
            Class<? extends CancelPreprocessController<?, ?>> fncMvcController, 
                    JPanel fncDescriptionPanel) {
        this.label = label;
        this.fncMvcController = fncMvcController;
        this.fncDescriptionPanel = fncDescriptionPanel;
    }
    
    public String getLabel() {
        return label;
    }

    public boolean hasController() {
        return fncMvcController != null;
    }

    public CancelPreprocessController<?, ?> newController(PluginContext context, WorkbenchModel model) {
        try {
            return fncMvcController
                    .getDeclaredConstructor(PluginContext.class, WorkbenchModel.class)
                    .newInstance(context, model);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean hasDescriptionPanel() {
        return fncDescriptionPanel != null;
    }

    public JPanel newDescriptionPanel() {
        return fncDescriptionPanel;
    }
}
