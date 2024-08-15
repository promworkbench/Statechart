package org.processmining.ui.statechart.workbench.log;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JPanel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;

public enum LogPreprocessors {
    // Special preprocessors overview view/tab
    Overview("Overview", null, null),
    
    // actual preprocessors, in the order they should be shown
    NestedCalls("Nested Calls", LogNestedCallsController.class, LogNestedCallsView.getDescriptionPanel()), 
    StructNames("Structured Names", LogStructNamesController.class, LogStructNamesView.getDescriptionPanel()), 
    PatternNames("Pattern Names", LogPatternNamesController.class, LogPatternNamesView.getDescriptionPanel()), 
    MultiAttribs("Multi Attributes", LogMultiAttribController.class, LogMultiAttribView.getDescriptionPanel()), 
    Classifier("Single Classifier", LogClassifierController.class, LogClassifierView.getDescriptionPanel()), 
    Existing("Existing List Labels", LogExistingController.class, LogExistingView.getDescriptionPanel());

    private final String label; 
    private final Class<? extends LogPreprocessController<?, ?>> fncMvcController;
    private final JPanel fncDescriptionPanel;
    
    private LogPreprocessors(String label,
            Class<? extends LogPreprocessController<?, ?>> fncMvcController, 
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

    public LogPreprocessController<?, ?> newController(PluginContext context, WorkbenchModel model) {
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
