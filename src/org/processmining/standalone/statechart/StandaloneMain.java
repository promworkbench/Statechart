package org.processmining.standalone.statechart;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.cli.CLI;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.ui.statechart.workbench.WorkbenchController;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.xes.statechart.XESImport;

public class StandaloneMain {
    
    private static String[] packageNames = new String[]{
        "LpSolve"//,
        //"RunnerUpPackages"
    };
    
    public static void main(String[] args) {
        try {
            // Test arguments
            if (args.length < 1) {
                System.out.println("No input file argument specified");
                return;
            }
            String inputFile = args[args.length - 1];
            
            // Load ProM
            Boot.VERBOSE = Level.NONE;
            PackageManager.getInstance().initialize(Level.ERROR);
            PackageManager.getInstance().findOrInstallPackages(packageNames);
            PrintStream err = System.err;
            System.setErr(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    // dont show
                }
            }));
            CLI.main(new String[] {});
            System.loadLibrary("lpsolve55");
            System.loadLibrary("lpsolve55j");
            System.setErr(err);
            PluginManagerImpl.getInstance();
            
            // Import log
            System.out.println("Importing log");
            XLog log = XESImport.readXLog(inputFile);
            
            // Launch app
            System.out.println("Launching workbench");
            FakePluginContext context = new FakePluginContext();
            WorkbenchController ctrl = new WorkbenchController(context);
            ctrl.initialize();
            ctrl.activate();
            ctrl.getModel().setArtifact(WorkbenchArtifacts.LogOriginal, log);
            JComponent content = ctrl.getView().getComponent();
            
            JFrame frame = new JFrame("Statechart Workbench");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(content, BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
