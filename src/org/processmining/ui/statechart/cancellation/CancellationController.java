package org.processmining.ui.statechart.cancellation;

import gnu.trove.set.hash.THashSet;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingWorker;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.algorithms.statechart.align.FitnessPrecision;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.discovery.DiscoverCancellationRecipe;
import org.processmining.recipes.statechart.l2l.L2LAttributeListRecipe;
import org.processmining.recipes.statechart.m2m.EPTree2PetrinetRecipe;
import org.processmining.recipes.statechart.m2m.EPTree2StatechartRecipe;
import org.processmining.recipes.statechart.m2m.ui.Petrinet2DotSVGRecipe;
import org.processmining.recipes.statechart.m2m.ui.Statechart2DotSVGRecipe;
import org.processmining.recipes.statechart.metrics.FitnessPrecisionRecipe;
import org.processmining.ui.statechart.cancellation.model.CancellationArtifacts;
import org.processmining.ui.statechart.cancellation.model.CancellationModel;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Signal1;
import org.processmining.utils.statechart.ui.ctrlview.AbstractController;
import org.processmining.utils.statechart.ui.ctrlview.IView;

import com.kitfox.svg.SVGDiagram;

public class CancellationController extends
    AbstractController<CancellationController.View> {

    private static final Logger logger = LogManager
            .getLogger(CancellationController.class.getName());

    public static abstract class View implements IView {
        public final Signal1<Dot.GraphDirection> SignalInputDirection = new Signal1<>();
//        public final Signal1<Double> SignalInputActivityThreshold = new Signal1<>();
        public final Signal1<Double> SignalInputPathThreshold = new Signal1<>();
        
        public final Signal1<Set<String>> SignalInputErrors = new Signal1<>();

        public abstract void setInputDirection(Dot.GraphDirection dir);

//        public abstract void setActivityThreshold(double threshold);

        public abstract void setPathThreshold(double threshold);

        public abstract void setInputErrors(Set<String> values);

        public abstract void setErrorOptions(Set<String> values);

        public abstract void displayDiscovering();

        public abstract void displayModel(SVGDiagram imageSC, SVGDiagram imagePTnet, boolean resetView);
        
        public abstract void setMetrics(FitnessPrecision metrics);

    }

    private CancellationModel model;
    private AtomicBoolean isUpdating = new AtomicBoolean();

    private final L2LAttributeListRecipe logPreprocessRecipe;
    private final DiscoverCancellationRecipe discoverEPTreeRecipe;
    
    private final EPTree2StatechartRecipe epTree2SCRecipe;
    private final Statechart2DotSVGRecipe sc2SvgRecipe;
    
    private final EPTree2PetrinetRecipe epTree2PTnetRecipe;
    private final Petrinet2DotSVGRecipe ptnet2SvgRecipe;
    
    private final FitnessPrecisionRecipe metricsRecipe;
    
    private final Set<RecipeArtifact<?>> unsetQueue = new HashSet<>();
    private SwingWorker<Void, Void> taskAlignments;
//    private Log2IMLogRecipe log2IMLogRecipe;

    public CancellationController(PluginContext context) {
        super(context);
        this.model = new CancellationModel();
//*
        XEventNameClassifier classifier = new XEventNameClassifier();
        
        logPreprocessRecipe = new L2LAttributeListRecipe();
        logPreprocessRecipe.getParameters().clsList.add(classifier);
        
        discoverEPTreeRecipe = new DiscoverCancellationRecipe();
        discoverEPTreeRecipe.getParameters().setPathThreshold(1.0);
        
        epTree2SCRecipe = new EPTree2StatechartRecipe();
        sc2SvgRecipe = new Statechart2DotSVGRecipe();
        sc2SvgRecipe.getParameters().graphDir = GraphDirection.leftRight;
        
        epTree2PTnetRecipe = new EPTree2PetrinetRecipe();
//        epTree2PTnetRecipe.getParameters().hierarchyMode = HierarchyMode.Subprocess;
        ptnet2SvgRecipe = new Petrinet2DotSVGRecipe();
        ptnet2SvgRecipe.getParameters().graphDir = GraphDirection.leftRight;
        
        metricsRecipe = new FitnessPrecisionRecipe();
/*/
        XEventNameClassifier classifier = new XEventNameClassifier();
        
        logPreprocessRecipe = new L2LNestedCallsRecipe();
//        logPreprocessRecipe.getParameters().clsList.add(classifier);

        log2IMLogRecipe = new Log2IMLogRecipe();
        discoverEPTreeRecipe = new DiscoverEPTreeRecipe();
        discoverEPTreeRecipe.getParameters().setPathThreshold(1.0);
        
        epTree2SCRecipe = new EPTree2StatechartRecipe();
        sc2SvgRecipe = new Statechart2DotSVGRecipe();
        sc2SvgRecipe.getParameters().graphDir = GraphDirection.leftRight;
        
        epTree2PTnetRecipe = new EPTree2PetrinetRecipe();
        epTree2PTnetRecipe.getParameters().setLifecycleHierarchySupport(true);
        ptnet2SvgRecipe = new Petrinet2DotSVGRecipe();
        ptnet2SvgRecipe.getParameters().graphDir = GraphDirection.leftRight;
        
        metricsRecipe = new FitnessPrecisionRecipe();
        metricsRecipe.getParameters().classifier = new XEventAndClassifier(
                new XEventNameClassifier(),
                new XEventLifeTransClassifier()
        );
//*/
        
        model.setRecipe(CancellationArtifacts.LogPre, logPreprocessRecipe);
//        model.setRecipe(CancellationArtifacts.LogIM,  log2IMLogRecipe);
        model.setRecipe(CancellationArtifacts.EPTree, discoverEPTreeRecipe);
        
        model.setRecipe(CancellationArtifacts.SC, epTree2SCRecipe);
        model.setRecipe(CancellationArtifacts.SCSVG, sc2SvgRecipe);
        
        model.setRecipe(CancellationArtifacts.PTnet, epTree2PTnetRecipe);
        model.setRecipe(CancellationArtifacts.PTnetSVG, ptnet2SvgRecipe);
        
        model.setRecipe(CancellationArtifacts.Metrics, metricsRecipe);
    }

    public CancellationModel getModel() {
        return model;
    }

    @Override
    public void initialize() {
        view = new CancellationView();

//        view.setActivityThreshold(logFilterRecipe.getParameters().threshold);
        view.setPathThreshold(discoverEPTreeRecipe.getParameters().getPathThreshold());
        view.setInputDirection(sc2SvgRecipe.getParameters().graphDir);

        Set<String> classes = new THashSet<String>();
        XEventNameClassifier classifier = new XEventNameClassifier();
        XLogInfo info = XLogInfoFactory.createLogInfo(model.getArtifact(CancellationArtifacts.LogOriginal), classifier);
        for (XEventClass ec : info.getEventClasses().getClasses()) {
            classes.add(ec.getId());
        }
        view.setErrorOptions(classes);
        view.setInputErrors(discoverEPTreeRecipe.getParameters().getErrorClasses());
    }

    @Override
    public void activate() {
//        regrec.register(model.SignalDataChanged,
//                new Action2<TrycatchModel, RecipeArtifact<?>>() {
//                    @Override
//                    public void call(TrycatchModel t, RecipeArtifact<?> u) {
//                        if (u == TrycatchArtifacts.LogOriginal) {
//                            updateDiagram(true);
//                        }
//                    }
//                });
        
        regrec.register(view.SignalInputPathThreshold, new Action1<Double>() {
            @Override
            public void call(Double t) {
                synchronized (unsetQueue) {
                    discoverEPTreeRecipe.getParameters().setPathThreshold(t);
                    unsetQueue.add(CancellationArtifacts.EPTree);
                }
                updateDiagram(true);
            }
        });

        regrec.register(view.SignalInputDirection,
                new Action1<Dot.GraphDirection>() {
                    @Override
                    public void call(Dot.GraphDirection t) {
                        synchronized (unsetQueue) {
                            sc2SvgRecipe.getParameters().graphDir = t;
                            ptnet2SvgRecipe.getParameters().graphDir = t;
                            unsetQueue.add(CancellationArtifacts.SCSVG);
                            unsetQueue.add(CancellationArtifacts.PTnetSVG);
                        }
                        updateDiagram(true);
                    }
                });
        
        regrec.register(view.SignalInputErrors,
                new Action1<Set<String>>() {
                    @Override
                    public void call(Set<String> t) {
                        synchronized (unsetQueue) {
                            discoverEPTreeRecipe.getParameters().setErrorClasses(t);
                            epTree2PTnetRecipe.getParameters().queryCatchError =
//                                    discoverEPTreeRecipe.getParameters().getQueryCatchError();
                                    discoverEPTreeRecipe.getParameters().queryCatchError;
                            unsetQueue.add(CancellationArtifacts.EPTree);
                        }
                        updateDiagram(true);
                    }
                });
        
        // initial update
        updateDiagram(true);
    }

    public void updateDiagram(final boolean resetView) {

        if (isUpdating.get()) {
            return;
        }
        
        if (taskAlignments != null) {
            try {
                if (!taskAlignments.isDone()) {
                    taskAlignments.cancel(true);
                }
            } catch(CancellationException e) {
                logger.warn("CancellationException");
            }
            taskAlignments = null;
        }
        
        taskAlignments = new SwingWorker<Void, Void>() {
//            long durDisc, durTotal;
            
//            @Override
//            public void prepareUi() {
////                view.setStatus("Discovering...");
////                view.displayDiscovering();
//            }

            @Override
            protected Void doInBackground() throws Exception {
                synchronized (model) {
                        // unset artifacts if requested, causes recompuation
//                    synchronized (unsetQueue) {
//                        for (RecipeArtifact<?> key : unsetQueue) {
//                            model.unsetArtifact(key);
//                        }
//                        unsetQueue.clear();
//                    }
                    
                    // compute artifacts if needed
//                    long start = System.currentTimeMillis();
//                    model.getArtifact(TrycatchArtifacts.EPTree);
//                    long mid = System.currentTimeMillis();
//                    model.getArtifact(TrycatchArtifacts.SCSVG);
//                    model.getArtifact(TrycatchArtifacts.PTnetSVG);
                    model.getArtifact(CancellationArtifacts.Metrics);
//                    model.getArtifact(WorkbenchArtifacts.EPTreePost);
                    
//                    if (model.hasArtifact(WorkbenchArtifacts.LogPre)) {
//                        model.getArtifact(WorkbenchArtifacts.JoinpointStats);
//                    }
//                    long stop = System.currentTimeMillis();
//                    durDisc = (mid - start);
//                    durTotal = (stop - start);
                    
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    // get() for checking if results where computed correctly
                    get();
                    
                    // set saw artifacts
//                    if (model.hasArtifact(WorkbenchArtifacts.LogPre)) {
//                        sawServer.getApi().setJoinpointStats(model.getArtifact(WorkbenchArtifacts.JoinpointStats));
//                    }
                    
                    // pass artifacts to view
//                    view.setStatus("Discovery completed in " + durDisc + " ms (total: " + durTotal + " ms)");
//                    view.displayModel(
//                            model.getArtifact(TrycatchArtifacts.SCSVG), 
//                            model.getArtifact(TrycatchArtifacts.PTnetSVG), 
//                            resetView);
                    view.setMetrics(
                            model.getArtifact(CancellationArtifacts.Metrics)
                            );
//                    view.setStatechartMetric(model.getArtifact(WorkbenchArtifacts.SCMetric));
//                    view.setEPTree(model.getArtifact(WorkbenchArtifacts.EPTreePost), 
//                            sc2SvgRecipe.getParameters().activityLabeler.getLabeler());
//                    view.setSelectedNodes(sc2SvgRecipe.getParameters().selectedNodes);
//                    isUpdating.set(false);
                } catch (InterruptedException | ExecutionException e) {
//                    view.setStatus("Error in Discovering");
                    logger.error("Error in Discovery - updateDiagram", e);
                }
            }
        };

        
        final ControllerWorker<Void, Void> step1 = new ControllerWorker<Void, Void>() {
//            long durDisc, durTotal;
            
            @Override
            public void prepareUi() {
//                view.setStatus("Discovering...");
                view.displayDiscovering();
                view.setMetrics(null);
            }

            @Override
            protected Void doInBackground() throws Exception {
                synchronized (model) {
                        // unset artifacts if requested, causes recompuation
                    synchronized (unsetQueue) {
                        for (RecipeArtifact<?> key : unsetQueue) {
                            model.unsetArtifact(key);
                        }
                        unsetQueue.clear();
                    }
                    
                    // compute artifacts if needed
//                    long start = System.currentTimeMillis();
                    model.getArtifact(CancellationArtifacts.EPTree);
//                    long mid = System.currentTimeMillis();
                    model.getArtifact(CancellationArtifacts.SCSVG);
                    model.getArtifact(CancellationArtifacts.PTnetSVG);
//                    model.getArtifact(TrycatchArtifacts.Metrics);
//                    model.getArtifact(WorkbenchArtifacts.EPTreePost);
                    
//                    if (model.hasArtifact(WorkbenchArtifacts.LogPre)) {
//                        model.getArtifact(WorkbenchArtifacts.JoinpointStats);
//                    }
//                    long stop = System.currentTimeMillis();
//                    durDisc = (mid - start);
//                    durTotal = (stop - start);

                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    // get() for checking if results where computed correctly
                    get();
                    
                    // set saw artifacts
//                    if (model.hasArtifact(WorkbenchArtifacts.LogPre)) {
//                        sawServer.getApi().setJoinpointStats(model.getArtifact(WorkbenchArtifacts.JoinpointStats));
//                    }
                    
                    // pass artifacts to view
//                    view.setStatus("Discovery completed in " + durDisc + " ms (total: " + durTotal + " ms)");
                    view.displayModel(
                            model.getArtifact(CancellationArtifacts.SCSVG), 
                            model.getArtifact(CancellationArtifacts.PTnetSVG), 
                            resetView);
//                    view.setMetrics(
//                            model.getArtifact(TrycatchArtifacts.Metrics)
//                            );
//                    view.setStatechartMetric(model.getArtifact(WorkbenchArtifacts.SCMetric));
//                    view.setEPTree(model.getArtifact(WorkbenchArtifacts.EPTreePost), 
//                            sc2SvgRecipe.getParameters().activityLabeler.getLabeler());
//                    view.setSelectedNodes(sc2SvgRecipe.getParameters().selectedNodes);
//                    scheduleBackground(step2);
//                    step2.
                    scheduleBackground(taskAlignments);
                    //taskAlignments.execute();
                    
                    isUpdating.set(false);
                } catch (InterruptedException | ExecutionException e) {
//                    view.setStatus("Error in Discovering");
                    logger.error("Error in Discovery - updateDiagram", e);
                }
            }
        };
        
        scheduleBackground(step1);

    }
}
