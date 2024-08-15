package org.processmining.ui.statechart.workbench.discovery.vis.settings;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.processmining.ui.statechart.workbench.WorkbenchColors;
import org.processmining.ui.statechart.workbench.discovery.DiscoveryWorkbenchController;

public abstract class AbstractSettingsPanel implements IPopoutSettingsPanel {

    private static final int RootPadding = 5;
    
    protected DiscoveryWorkbenchController.View baseView;
    protected PopoutSettingsWidget widgetButton;
    protected JPanel root;
    
    public AbstractSettingsPanel(DiscoveryWorkbenchController.View baseView) {
        this.baseView = baseView;

        root = new JPanel();
        root.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WorkbenchColors.BackPane),
            new EmptyBorder(RootPadding, RootPadding, RootPadding, RootPadding)
        ));
        root.setLayout(new BorderLayout());
    }
    
    @Override
    public JComponent getRootComponent() {
        return root;
    }

    @Override
    public void setWidgetComponent(PopoutSettingsWidget widgetButton) {
        this.widgetButton = widgetButton;
        updateWidget();
    }

    protected abstract void updateWidget();

    @Override
    public int getWindowWidth() {
        return PopoutSettingsWidget.DefaultWindowWidth;
    }

    @Override
    public int getWindowHeight() {
        return PopoutSettingsWidget.DefaultWindowHeight;
    }
    

}
