package org.processmining.statechart.testutils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.processmining.contexts.cli.CLI;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.impl.PluginManagerImpl;

public class IntegrationTestUtil {

    private static PluginManager pluginManager = null;
    
    public static PluginManager initializeProMWithRequiredPackages(
            String... packageNames) throws Throwable {
        if (pluginManager == null) {
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
            pluginManager = PluginManagerImpl.getInstance();
        }
        PackageManager.getInstance().findOrInstallPackages(packageNames);
        return pluginManager;
    }

}
