package org.processmining.plugins.statechart;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.statechart.WorkbenchLauncher;

/**
 * 
 * @author mleemans
 *
 */

@Plugin(
        name = "Discover using the Statechart Workbench", 
        level = PluginLevel.NightlyBuild,
        returnLabels = { "Statechart Workbench" }, 
        returnTypes = { WorkbenchLauncher.class }, 
        parameterLabels = {"Log" }, 
        categories = { PluginCategory.Discovery },
        keywords = { "Statechart", "Hierarchy", "Hierarchical", "Cancelation" },
        help = "Use the Statechart Workbench to extract a hierarchy from the "
                + "given log, and mine a hierarchical model (with cancellation)", 
        userAccessible = true
)
public class WorkbenchLauncherPlugin {

    /**
     * Run Naive Discovery
     * 
     * @param context               The given plug-in context.
     * @param log                   The given event log.
     * @return The transformed event log.
     */
    @UITopiaVariant(
            affiliation = UITopiaVariant.EHV, 
            author = "M. Leemans", 
            email = "m.leemans@tue.nl",
            uiLabel = UITopiaVariant.USEVARIANT,
            uiHelp = UITopiaVariant.USEVARIANT,
            pack = "Statechart"
    )
    @PluginVariant(
            variantLabel = "Discover using the Statechart Workbench",
                    help = "Use the Statechart Workbench to extract a hierarchy from the "
                            + "given log, and mine a hierarchical model",  
            requiredParameterLabels = { 0 }
    )
    public WorkbenchLauncher launch(PluginContext context, XLog log)
    {
        return new WorkbenchLauncher(log);
    }
}
