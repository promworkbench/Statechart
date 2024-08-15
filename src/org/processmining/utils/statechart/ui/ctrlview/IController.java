package org.processmining.utils.statechart.ui.ctrlview;

public interface IController {

    public IView getView();

    /**
     * Instantiate and prepare everything here
     */
    public void initialize();

    /**
     * Register all listeners and start background work here
     */
    public void activate();

    /**
     * Unregister all listeners and stop background work here
     */
    public void deactivate();
}
