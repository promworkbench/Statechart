package org.processmining.ui.statechart.workbench.discovery.vis.settings;

import org.processmining.ui.statechart.workbench.discovery.ISubview;

public interface IPopoutSettingsPanel extends ISubview {

    public void setWidgetComponent(PopoutSettingsWidget widgetButton);

    public int getWindowWidth();

    public int getWindowHeight();
}
