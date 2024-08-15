package org.processmining.visualizers.statechart;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.statechart.WorkbenchLauncher;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.recipes.statechart.RecipeProcess.SetArtifactMode;
import org.processmining.ui.statechart.workbench.WorkbenchController;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;

public class WorkbenchView {

//    private static final Logger logger = LogManager
//            .getLogger(WorkbenchView.class.getName());
    
//    private boolean enableUiTracer;
//    private Properties uiTracerIni;
    
    public WorkbenchView() {
//        uiTracerIni = new Properties();
//        try {
//            FileInputStream is = new FileInputStream("UiTracer.ini");
//            uiTracerIni.load(is);
//            is.close();
//        } catch (FileNotFoundException e) {
//            // UiTracer.ini file not found.
//            logger.info("UiTracer.ini file not found", e);
//        } catch (IOException e) {
//            // Error while reading UiTracer.ini file
//            logger.warn("Error while reading UiTracer.ini file", e);
//        }
//        
//        enableUiTracer = uiTracerIni.containsKey("Enabled") 
//            && uiTracerIni.getProperty("Enabled", "false").toLowerCase().equals("true");
    }

    @Plugin(
            name = "Statechart Workbench", 
            returnLabels = { "Statechart Workbench - XLog" }, 
            returnTypes = { JComponent.class }, 
            parameterLabels = { "Event Log" }, 
            userAccessible = true
    )
    @Visualizer
    public JComponent visualize(PluginContext context, XLog log) {
        WorkbenchController ctrl = new WorkbenchController(context);
        ctrl.initialize();
        ctrl.activate();
        ctrl.getModel().setArtifact(WorkbenchArtifacts.LogOriginal, log);
//        _installUiTracer(ctrl);
        return ctrl.getView().getComponent();
    }

    @Plugin(
            name = "Statechart Workbench", 
            returnLabels = { "Statechart Workbench - XLog" }, 
            returnTypes = { JComponent.class }, 
            parameterLabels = { "Event Log (WorkbenchLauncher wrapper)" }, 
            userAccessible = true
    )
    @Visualizer
    public JComponent visualize(PluginContext context, WorkbenchLauncher launcher) {
        XLog log = launcher.xLog.get();
        if (log == null) {
            return new JLabel("XLog is no longere in memory, try launching again");
        }
        
        WorkbenchController ctrl = new WorkbenchController(context);
        ctrl.initialize();
        ctrl.activate();
        ctrl.getModel().setArtifact(WorkbenchArtifacts.LogOriginal, log);
//        _installUiTracer(ctrl);
        return ctrl.getView().getComponent();
    }
    @Plugin(
            name = "Statechart Workbench", 
            returnLabels = { "Statechart Workbench - Process Statechart Tree" }, 
            returnTypes = { JComponent.class }, 
            parameterLabels = { "Process Tree" }, 
            userAccessible = true
    )
    @Visualizer
    public JComponent visualize(PluginContext context, IEPTree eptree) {
        WorkbenchController ctrl = new WorkbenchController(context);
        ctrl.initialize();
        ctrl.activate();
        ctrl.getModel().setArtifact(WorkbenchArtifacts.EPTree, eptree, 
                SetArtifactMode.UnsetResults);
        return ctrl.getView().getComponent();
    }

    @Plugin(
            name = "Statechart Workbench", 
            returnLabels = { "Statechart Workbench - Statechart" }, 
            returnTypes = { JComponent.class }, 
            parameterLabels = { "Process Tree" }, 
            userAccessible = true
    )
    @Visualizer
    public JComponent visualize(PluginContext context, Statechart statechart) {
        WorkbenchController ctrl = new WorkbenchController(context);
        ctrl.initialize();
        ctrl.activate();
        ctrl.getModel().setArtifact(WorkbenchArtifacts.Statechart, statechart, 
                SetArtifactMode.UnsetResults);
        return ctrl.getView().getComponent();
    }

//    private void _installUiTracer(WorkbenchController ctrl) {
//        if (enableUiTracer) {
//            try {
//                File logTarget = new File(uiTracerIni.getProperty("TargetLogFile", 
//                        "uiTracer-" + System.currentTimeMillis() + ".xes"));
//                final UserUiTracer tracer = new UserUiTracer(logTarget);
//                tracer.connectListeners(ctrl);
//                Runtime.getRuntime().addShutdownHook(new Thread() {
//                    @Override
//                    public void run() {
//                        tracer.finish();
//                    }
//                });
//            } catch(IOException e) {
//                logger.warn("Failed to initialize UserUiTracer", e);
//            }
//        }
//    }
}
