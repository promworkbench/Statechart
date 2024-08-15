package org.processmining.utils.statechart.ui.ctrlview;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Signal1;

import com.google.common.base.Preconditions;

public class AbstractContainerController<S extends Enum<S>, V extends AbstractContainerController.View<S>>
        extends AbstractController<V> {

    public static abstract class View<S extends Enum<S>> implements IView {

        public final Signal1<S> SignalViewStateChanged = new Signal1<>();

        public abstract void registerViewStatePane(S state, JComponent component);

        public abstract void setViewStateEnabled(S state, boolean isEnabled);

        public abstract void showViewState(S newState);
    }

    private S currentState = null;
    private Map<S, IController> childControllers = new HashMap<>();

    public AbstractContainerController(PluginContext context) {
        super(context);
    }

    /**
     * Register child controller
     * 
     * @param state
     */
    public void registerChildController(S state, IController ctrl) {
        childControllers.put(state, ctrl);
    }

    /**
     * Get child controller
     * 
     * @param state
     */
    public IController getChildController(S state) {
        return childControllers.get(state);
    }

    /**
     * Call this after you registered all the child controllers
     */
    protected void initializeChildren() {
        for (S state : childControllers.keySet()) {
            IController child = childControllers.get(state);
            child.initialize();
            view.registerViewStatePane(state, child.getView().getComponent());
        }
    }

    @Override
    public void initialize() {
        initializeChildren();
    }

    @Override
    public void activate() {
        regrec.register(view.SignalViewStateChanged, new Action1<S>() {
            @Override
            public void call(final S newState) {
                runUi(new Runnable() {
                    @Override
                    public void run() {
                        updateViewState(newState);
                    }
                });
            }
        });
    }

    protected S getCurrentViewState() {
        return currentState;
    }
    
    protected void updateViewState(S newState) {
        Preconditions.checkArgument(childControllers.containsKey(newState));
        
        if (newState != currentState) {
            if (currentState != null) {
                // deactivate old controller
                childControllers.get(currentState).deactivate();
            }

            // use new controller
            currentState = newState;
            IController ctrl = childControllers.get(currentState);

            // update ui
            view.showViewState(currentState);
            ctrl.activate();
        }
    }
    
    protected void updateViewStatesEnabled(S[] allStates, Collection<S> enabledViews) {
        Preconditions.checkArgument(allStates.length > 0, "At least some states must be available");
        Preconditions.checkArgument(enabledViews.size() > 0, "At least some view must be enabled");
        
        // enabled appropriate options
        for (S state : allStates) {
            view.setViewStateEnabled(state, enabledViews.contains(state));
        }

        // determine new legal state
        S newState = currentState;
        if (newState == null) {
            newState = allStates[allStates.length - 1];
        }
        for (int i = allStates.length - 1; i >= 0; i--) {
            if (newState == allStates[i] && !enabledViews.contains(allStates[i])) {
                if (i > 0) {
                    newState = allStates[i - 1];
                } else {
                    newState = null;
                }
            }
        }
        if (newState == null) {
            newState = enabledViews.iterator().next();
        }
        updateViewState(newState);
    }
}
