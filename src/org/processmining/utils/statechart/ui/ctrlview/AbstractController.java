package org.processmining.utils.statechart.ui.ctrlview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.utils.statechart.signals.SignalRegRecorder;

public abstract class AbstractController<V extends IView> implements
        IController {

    public static final int ScheduleDelayDefault = 200;

    /**
     * Worker class that has three steps: pre-ui, do-background, post-ui (i.e.,
     * done)
     * 
     * @author mleemans
     *
     * @param <T>
     * @param <W>
     */
    public static abstract class ControllerWorker<T, W> extends
            SwingWorker<T, W> {

        /**
         * Run on the ui thread before the background task starts
         */
        public abstract void prepareUi();
    }

    protected final PluginContext context;
    protected final SignalRegRecorder regrec = new SignalRegRecorder();
    protected V view;

    private final Timer scheduleTimer;
    private ActionListener recentScheduleLst;

    public AbstractController(PluginContext context) {
        this.context = context;

        scheduleTimer = new Timer(ScheduleDelayDefault, null);
        scheduleTimer.setRepeats(false);
        recentScheduleLst = null;
    }

    /**
     * Run command in background thread
     * 
     * @param command
     */
    public void runBackground(final Runnable command) {
        runBackground(new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                command.run();
                return null;
            }

        });
    }

    /**
     * Run doInBackground() in background thread, and call done() in ui thread
     * 
     * @param command
     */
    public <T, W> void runBackground(final SwingWorker<T, W> worker) {
        worker.execute();
    }

    /**
     * Run prepareUi() in ui thread, then run doInBackground() in background
     * thread, and call done() in ui thread
     * 
     * @param command
     */
    public <T, W> void runBackground(final ControllerWorker<T, W> worker) {
        runUi(new Runnable() {
            @Override
            public void run() {
                worker.prepareUi();
                worker.execute();
            }
        });
    }

    /**
     * Set delay for scheduled background work
     * 
     * @param command
     */
    public void setScheduleDelay(int newDelay) {
        scheduleTimer.setInitialDelay(newDelay);
    }

    /**
     * Schedule background work, unscheduling any previous unprocessed
     * background work
     * 
     * Run command in background thread
     * 
     * @param command
     */
    public void scheduleBackground(final Runnable command) {
        scheduleBackground(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runBackground(command);
            }
        });
    }

    /**
     * Schedule background work, unscheduling any previous unprocessed
     * background work
     * 
     * Run doInBackground() in background thread, and call done() in ui thread
     * 
     * @param command
     */
    public <T, W> void scheduleBackground(final SwingWorker<T, W> worker) {
        scheduleBackground(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runBackground(worker);
            }
        });
    }

    /**
     * Schedule background work, unscheduling any previous unprocessed
     * background work
     * 
     * Run prepareUi() in ui thread, then run doInBackground() in background
     * thread, and call done() in ui thread
     * 
     * @param command
     */
    public <T, W> void scheduleBackground(final ControllerWorker<T, W> worker) {
        scheduleBackground(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runBackground(worker);
            }
        });
    }


    /**
     * Schedule background work, unscheduling any previous unprocessed
     * background work
     * 
     * @param command
     */
    public void scheduleBackground(ActionListener lst) {
        scheduleTimer.stop();
        if (recentScheduleLst != null) {
            scheduleTimer.removeActionListener(recentScheduleLst);
        }

        scheduleTimer.addActionListener(lst);
        recentScheduleLst = lst;
        scheduleTimer.start();
    }

    /**
     * Run command in ui thread
     * @param command
     */
    public void runUi(Runnable command) {
        SwingUtilities.invokeLater(command);
    }

    /**
     * Get view object
     */
    public V getView() {
        return view;
    }

    /**
     * Instantiate and prepare everything here
     */
    public abstract void initialize();

    /**
     * Register all listeners and stop background work here
     */
    public abstract void activate();

    /**
     * Unregister all listeners and start background work here
     */
    public void deactivate() {
        regrec.unregisterAll();
    }
}
