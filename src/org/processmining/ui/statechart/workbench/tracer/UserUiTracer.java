package org.processmining.ui.statechart.workbench.tracer;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;
import org.processmining.models.statechart.labeling.ActivityLabeler;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.align.AnalysisAlgorithm;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlay;
import org.processmining.recipes.statechart.discovery.DiscoverEPTreeRecipe.DiscoveryAlgorithm;
import org.processmining.ui.statechart.workbench.WorkbenchController;
import org.processmining.ui.statechart.workbench.WorkbenchController.ViewState;
import org.processmining.ui.statechart.workbench.cancel.CancelPreprocessors;
import org.processmining.ui.statechart.workbench.cancel.CancelWorkbenchController;
import org.processmining.ui.statechart.workbench.discovery.DiscoveryWorkbenchController;
import org.processmining.ui.statechart.workbench.discovery.ModelVisualization;
import org.processmining.ui.statechart.workbench.log.LogPreprocessors;
import org.processmining.ui.statechart.workbench.log.LogWorkbenchController;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.signals.Action0;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Action2;
import org.processmining.utils.statechart.signals.SignalRegRecorder;
import org.processmining.xes.statechart.XesLitewrite;

public class UserUiTracer {
    
    private XesLitewrite xes;
    private final SignalRegRecorder regrec = new SignalRegRecorder();
    
    private XConceptExtension xconcept = XConceptExtension.instance();
    private XTimeExtension xtime = XTimeExtension.instance();
    private XOrganizationalExtension xorg = XOrganizationalExtension.instance();

    public UserUiTracer(File target) throws IOException {
        xes = new XesLitewrite(target);
        xes.setHeader();
        
        xes.addExtension(xconcept);
        xes.addExtension(xtime);
        xes.addExtension(xorg);
        
        xes.addClassifier(new XEventNameClassifier());
        xes.addClassifier(new XEventAttributeClassifier("Group", XOrganizationalExtension.KEY_GROUP));
        xes.addClassifier(new XEventAndClassifier(
            new XEventAttributeClassifier("Group", XOrganizationalExtension.KEY_GROUP),
            new XEventAttributeClassifier("Resource", XOrganizationalExtension.KEY_RESOURCE)
        ));
        xes.addClassifier(new XEventAndClassifier(
            new XEventAttributeClassifier("Group", XOrganizationalExtension.KEY_GROUP),
            new XEventAttributeClassifier("Resource", XOrganizationalExtension.KEY_RESOURCE),
            new XEventAttributeClassifier("Role", XOrganizationalExtension.KEY_ROLE)
        ));

        xes.beginDefineAttrEvent();
        xes.defineEventAttribs(xconcept);
        xes.defineEventAttribs(xtime);
        xes.defineEventAttribs(xorg);
        xes.endDefineAttrEvent();
        
        xes.beginTrace();
    }
    
    public void connectListeners(WorkbenchController ctrl) {
        disconnect();
        
        connectWorkbenchMain(ctrl);
        connectWorkbenchModel(ctrl.getModel());
        
        connectWorkbenchLog(ctrl.getLogWorkbenchController());
        connectWorkbenchCancel(ctrl.getCancelWorkbenchController());
        connectWorkbenchDiscovery(ctrl.getDiscoveryWorkbenchController());
        
    }

    public void disconnect() {
        regrec.unregisterAll();
    }

    private void connectWorkbenchMain(WorkbenchController ctrl) {
        WorkbenchController.View view = ctrl.getView();
        
        regrec.register(view.SignalViewStateChanged, new Action1<WorkbenchController.ViewState>() {
            @Override public void call(ViewState t) { recordEvent("Main", "ViewStateChanged", t.getLabel()); }
        });
        
    }

    private void connectWorkbenchModel(WorkbenchModel model) {
        regrec.register(model.SignalDataChanged, new Action2<WorkbenchModel, RecipeArtifact<?>>() {
            @Override 
            public void call(WorkbenchModel t, RecipeArtifact<?> u) {
                if (u == WorkbenchArtifacts.LogPre) {
                    recordEvent("Model", "DataChanged", "LogPre");
                } else if (u == WorkbenchArtifacts.CancelOracleInput) {
                    recordEvent("Model", "DataChanged", "CancelOracleInput");
                } else if (u == WorkbenchArtifacts.LogIM) {
                    recordEvent("Model", "DataChanged", "LogIM");
                } else if (u == WorkbenchArtifacts.EPTree) {
                    recordEvent("Model", "DataChanged", "EPTree");
                } else if (u == WorkbenchArtifacts.LogAlign) {
                    recordEvent("Model", "DataChanged", "LogAlign");
                } else if (u == WorkbenchArtifacts.AlignedLog) {
                    recordEvent("Model", "DataChanged", "AlignedLog");
                } else if (u == WorkbenchArtifacts.EPTreePost) {
                    recordEvent("Model", "DataChanged", "EPTreePost");
                }
            }
        });
    }

    private void connectWorkbenchLog(LogWorkbenchController ctrl) {
        LogWorkbenchController.View view = ctrl.getView();
        
        regrec.register(ctrl.SignalLogReady, new Action1<WorkbenchController.ViewState>() {
            @Override public void call(ViewState t) { recordEvent("Log", "LogReady", t.getLabel()); }
        });
        regrec.register(view.SignalViewStateChanged, new Action1<LogPreprocessors>() {
            @Override public void call(LogPreprocessors t) { recordEvent("Log", "ViewStateChanged", t.getLabel()); }
        });
    }

    private void connectWorkbenchCancel(CancelWorkbenchController ctrl) {
        CancelWorkbenchController.View view = ctrl.getView();
        
        regrec.register(ctrl.SignalCancelReady, new Action0() {
            @Override public void call() { recordEvent("Cancel", "CancelReady"); }
        });
        regrec.register(view.SignalViewStateChanged, new Action1<CancelPreprocessors>() {
            @Override public void call(CancelPreprocessors t) { recordEvent("Cancel", "ViewStateChanged", t.getLabel()); }
        });
    }
    
    private void connectWorkbenchDiscovery(DiscoveryWorkbenchController ctrl) {
        DiscoveryWorkbenchController.View view = ctrl.getView();

        regrec.register(view.SignalClickCollapsibleNode, new Action1<String>() {
            @Override public void call(String t) { recordEventKeyValue("Discovery", "ClickCollapsibleNode", "node-id", t); }
        });
        regrec.register(view.SignalClickInspectNode, new Action1<String>() {
            @Override public void call(String t) { recordEventKeyValue("Discovery", "ClickInspectNode", "node-id", t); }
        });
        regrec.register(view.SignalExportLog, new Action0() {
            @Override public void call() { recordEvent("Discovery", "ExportLog"); }
        });
        regrec.register(view.SignalExportPTnet, new Action0() {
            @Override public void call() { recordEvent("Discovery", "ExportPTnet"); }
        });
        regrec.register(view.SignalInputActivityLabeler, new Action1<ActivityLabeler>() {
            @Override public void call(ActivityLabeler t) { recordEventKeyValue("Discovery", "InputActivityLabeler", "activity-labeler", t.getName()); }
        });
        regrec.register(view.SignalInputActivityThreshold, new Action1<Double>() {
            @Override public void call(Double t) { recordEventKeyValue("Discovery", "InputActivityThreshold", "activity-labeler", t); }
        });
        regrec.register(view.SignalInputAnalysisStatMode, new Action1<StatMode>() {
            @Override public void call(StatMode t) { recordEventKeyValue("Discovery", "InputAnalysisStatMode", "stat-mode", t.name()); }
        });
        regrec.register(view.SignalInputDepthThreshold, new Action2<Double, Double>() {
            @Override public void call(Double t, Double u) { recordEventKeyValue("Discovery", "InputDepthThreshold", 
                    "depth-threshold-max", u, "depth-threshold-min", t); }
        });
        regrec.register(view.SignalInputDirection, new Action1<Dot.GraphDirection>() {
            @Override public void call(Dot.GraphDirection t) { recordEventKeyValue("Discovery", "InputDirection", "graph-direction", t.name()); }
        });
        regrec.register(view.SignalInputErrors, new Action1<Set<String>>() {
            @Override public void call(Set<String> t) { recordEventKeyValue("Discovery", "InputErrors", "error-set", t); }
        });
        regrec.register(view.SignalInputEvent2Time, new Action1<IEvent2Time>() {
            @Override public void call(IEvent2Time t) { recordEventKeyValue("Discovery", "InputEvent2Time", "event2time", t.getName()); }
        });
        regrec.register(view.SignalInputPathThreshold, new Action1<Double>() {
            @Override public void call(Double t) { recordEventKeyValue("Discovery", "InputPathThreshold", "path-threshold", t); }
        });
        regrec.register(view.SignalInputResourceAttribute, new Action1<String>() {
            @Override public void call(String t) { recordEventKeyValue("Discovery", "InputResourceAttribute", "resource-attribute", t); }
        });
        regrec.register(view.SignalInputSelectedVis, new Action1<ModelVisualization>() {
            @Override public void call(ModelVisualization t) { recordEventKeyValue("Discovery", "InputSelectedVis", "model-visualization", t.getName()); }
        });
        regrec.register(view.SignalInputUseAlgorithm, new Action1<DiscoveryAlgorithm>() {
            @Override public void call(DiscoveryAlgorithm t) { recordEventKeyValue("Discovery", "InputUseAlgorithm", "discovery-algorithm", t.getName()); }
        });
        regrec.register(view.SignalInputUseAlignAlg, new Action1<AnalysisAlgorithm>() {
            @Override public void call(AnalysisAlgorithm t) { recordEventKeyValue("Discovery", "InputUseAlignAlg", "analysis-algorithm", t.getName()); }
        });
        regrec.register(view.SignalInputUseAlignMetric, new Action1<AnalysisAlignMetricOverlay>() {
            @Override public void call(AnalysisAlignMetricOverlay t) { recordEventKeyValue("Discovery", "InputUseAlignMetric", "analysis-metric", t.getName()); }
        });
        regrec.register(view.SignalInputUseCancelation, new Action1<Boolean>() {
            @Override public void call(Boolean t) { recordEventKeyValue("Discovery", "InputUseCancelation", "use-cancelation", t); }
        });
        regrec.register(view.SignalInputUseEPTreeReduct, new Action1<Boolean>() {
            @Override public void call(Boolean t) { recordEventKeyValue("Discovery", "InputUseEPTreeReduct", "use-eptree-reduct", t); }
        });
        regrec.register(view.SignalInputUseRecurseArrow, new Action1<Boolean>() {
            @Override public void call(Boolean t) { recordEventKeyValue("Discovery", "InputUseRecurseArrow", "use-recurse-arrow", t); }
        });
        regrec.register(view.SignalInputUseSCReduct, new Action1<Boolean>() {
            @Override public void call(Boolean t) { recordEventKeyValue("Discovery", "InputUseSCReduct", "use-sc-reduct", t); }
        });
        regrec.register(view.SignalSelectedNodes, new Action1<Set<String>>() {
            @Override public void call(Set<String> t) { recordEventKeyValue("Discovery", "SelectedNodes", "node-id-set", t); }
        });
    }

    protected void recordEvent(String source, String signal) {
        recordEvent(source, signal, null);
    }
    
    protected void recordEvent(String source, String signal, String detail) {
        long time = System.currentTimeMillis();
        
        StringBuilder bld = new StringBuilder();
        bld.append(source);
        bld.append(" ");
        bld.append(signal);
        if (detail != null) {
            bld.append(": ");
            bld.append(detail);
        }
        
        xes.beginEvent();
        xes.attrString(XConceptExtension.KEY_NAME, bld.toString());
        xes.attrTimestamp(XTimeExtension.KEY_TIMESTAMP, time);
        xes.attrString(XOrganizationalExtension.KEY_GROUP, source);
        xes.attrString(XOrganizationalExtension.KEY_RESOURCE, signal);
        if (detail != null) {
            xes.attrString(XOrganizationalExtension.KEY_ROLE, detail);
        } else {
            xes.attrString(XOrganizationalExtension.KEY_ROLE, "-");
        }
        xes.endEvent();
    }

    private void recordEventKeyValueBasis(String source, String signal) {
        long time = System.currentTimeMillis();
        
        StringBuilder bld = new StringBuilder();
        bld.append(source);
        bld.append(" ");
        bld.append(signal);
        
        xes.beginEvent();
        xes.attrString(XConceptExtension.KEY_NAME, bld.toString());
        xes.attrTimestamp(XTimeExtension.KEY_TIMESTAMP, time);
        xes.attrString(XOrganizationalExtension.KEY_GROUP, source);
        xes.attrString(XOrganizationalExtension.KEY_RESOURCE, signal);
        xes.attrString(XOrganizationalExtension.KEY_ROLE, "-");
    }

    protected void recordEventKeyValue(String source, String signal, String key, String value) {
        recordEventKeyValueBasis(source, signal);
        
        xes.attrString("data:key", key);
        xes.attrString("data:value", value);
        xes.attrString("data:" + key, value);
        
        xes.endEvent();
    }

    protected void recordEventKeyValue(String source, String signal, String key, Set<String> values) {
        recordEventKeyValueBasis(source, signal);
        
        xes.attrString("data:key", key);
        xes.attrString("data:value", values.toString());
        xes.attrString("data:" + key, values.toString());
        
        xes.endEvent();
    }

    protected void recordEventKeyValue(String source, String signal, String key, double value) {
        recordEventKeyValueBasis(source, signal);
        
        xes.attrString("data:key", key);
        xes.attrContinuous("data:value", value);
        xes.attrContinuous("data:" + key, value);
        
        xes.endEvent();
    }

    protected void recordEventKeyValue(String source, String signal,
            String key1, double value1, String key2, double value2) {
        recordEventKeyValueBasis(source, signal);
        
        xes.attrString("data:key", key1);
        xes.attrContinuous("data:value", value1);
        xes.attrContinuous("data:" + key1, value1);
        xes.attrContinuous("data:" + key2, value2);
        
        xes.endEvent();
    }

    protected void recordEventKeyValue(String source, String signal, String key, boolean value) {
        recordEventKeyValueBasis(source, signal);
        
        xes.attrString("data:key", key);
        xes.attrBoolean("data:value", value);
        xes.attrBoolean("data:" + key, value);
        
        xes.endEvent();
    }
    
    public void finish() {
        disconnect();
        xes.endTrace();
        xes.finish();
    }
}
