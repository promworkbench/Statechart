package org.processmining.plugins.statechart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.l2l.L2LSplitCalls;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.statechart.l2l.L2LSplitCallsDialog;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.ui.statechart.workbench.model.WorkbenchLogUtil;
import org.processmining.utils.statechart.generic.ListUtil;

/**
 * 
 * @author mleemans
 *
 */

@Plugin(
        name = "Preprocess log - Hierarcy heuristics", 
        level = PluginLevel.NightlyBuild,
        returnLabels = { "Preprocessed XLog" }, 
        returnTypes = { XLog.class }, 
        parameterLabels = {"Log" }, 
        categories = { PluginCategory.Filtering },
        keywords = { "Split traces", "Nested Calls", "Preprocessing", "Log2Log", "L2L" },
        help = "Use hierarchy heuristic to preprocess the given log. ", 
        userAccessible = true
)
public class L2LPlugin {

    /**
     * Run Naive Discovery
     * 
     * @param context               The given plug-in context.
     * @param log                   The given event log.
     * @return The transformed event log.
     * @throws UserCancelledException 
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
            variantLabel = "Preprocess log - Split traces on calls",
                    help = "Use the Split Calls heuristic to split traces in the given log. "
                            + "Assumed is that the logs has some form of Nested Calls structure, "
                            + "i.e., an interval containment hierarchy. "
                            + "This is used to split traces such that, in the resulting event log,"
                            + "each trace corresponds to a high level interval in one of the original traces.",
            requiredParameterLabels = { 0 }
)
    public XLog launchL2LSplitCalls(UIPluginContext context, XLog input) throws UserCancelledException {

        List<XEventClassifier> classifiers = WorkbenchLogUtil
                .getAvailableClassifiers(input);
        
        XEventClassifier defaultLabel = ListUtil.findEqual(classifiers, new XEventNameClassifier());
        XEventClassifier defaultSR = ListUtil.findEqual(classifiers, new XEventLifeTransClassifier());

        XEventClasses optsSR = XLogInfoFactory.createLogInfo(input, defaultSR).getEventClasses();
        List<String> optSRstrings = new ArrayList<>();
        for (XEventClass opt : optsSR.getClasses()) {
            optSRstrings.add(opt.toString());
        }
        Collections.sort(optSRstrings);

        List<Pair<Pattern, String>> optsSplit = Arrays.asList(L2LSplitCalls.Parameters.ReSplitDefaults);
        Pair<Pattern, String> defaultSplit = optsSplit.get(0);
        
        L2LSplitCallsDialog dialog = new L2LSplitCallsDialog(
                classifiers, defaultLabel,
                classifiers, defaultSR,
                optSRstrings, XLifecycleExtension.StandardModel.START.getEncoding(),
                optSRstrings, XLifecycleExtension.StandardModel.COMPLETE.getEncoding(),
                optsSplit, defaultSplit);
        InteractionResult balancedAlignmentResult = context.showConfiguration(
                "Split Traces on Nested Calls -- Parameters", dialog);

        if (balancedAlignmentResult == InteractionResult.CANCEL) {
            context.getFutureResult(0).cancel(true);
            return null;
        } else {
            L2LSplitCalls transform = new L2LSplitCalls(dialog.getParameters());
            return transform.apply(input);
        }
    }
}
