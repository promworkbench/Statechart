package org.processmining.ui.statechart.workbench.discovery;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.Timer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.align.metric.IMetric;
import org.processmining.algorithms.statechart.align.metric.IResourceMetric;
import org.processmining.algorithms.statechart.align.metric.ITimeMetric;
import org.processmining.algorithms.statechart.align.metric.ResourceAttribute;
import org.processmining.algorithms.statechart.align.metric.time.Event2TimeAttribute;
import org.processmining.algorithms.statechart.align.metric.time.IEvent2Time;
import org.processmining.algorithms.statechart.align.metric.value.IMetricValueConvertor;
import org.processmining.algorithms.statechart.m2m.EPTree2Petrinet.CancelationMode;
import org.processmining.algorithms.statechart.m2m.EPTree2Petrinet.HierarchyMode;
import org.processmining.algorithms.statechart.m2m.EPTree2Petrinet.RecursionMode;
import org.processmining.algorithms.statechart.m2m.EPTreeFilter.Parameters;
import org.processmining.algorithms.statechart.m2m.TransformationException;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.statechart.align.XAlignedTreeLog;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;
import org.processmining.models.statechart.decorate.staticmetric.SCComplexityMetric;
import org.processmining.models.statechart.decorate.swapp.EPTreeSwAppDecorator;
import org.processmining.models.statechart.decorate.swapp.SwAppDecoration;
import org.processmining.models.statechart.decorate.ui.IValueDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.labeling.ActivityLabeler;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.models.statechart.log.HierarchyActivityInfo;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.protocols.statechart.saw.SawServer;
import org.processmining.protocols.statechart.saw.SawServerStatus;
import org.processmining.protocols.statechart.saw.api.data.Joinpoint;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.RecipeArtifactD1;
import org.processmining.recipes.statechart.RecipeProcess.GetArtifactMode;
import org.processmining.recipes.statechart.SwitchArtifactRecipe;
import org.processmining.recipes.statechart.align.AlignLogFilterRecipe;
import org.processmining.recipes.statechart.align.AlignLogRecipe;
import org.processmining.recipes.statechart.align.AlignTreePostprocessingRecipe;
import org.processmining.recipes.statechart.align.AlignTreeRecipe;
import org.processmining.recipes.statechart.align.AnalysisAlgorithm;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlay;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlayManager;
import org.processmining.recipes.statechart.align.Log2LogAlignRecipe;
import org.processmining.recipes.statechart.discovery.DiscoverEPTreeRecipe;
import org.processmining.recipes.statechart.discovery.Log2IMLogRecipe;
import org.processmining.recipes.statechart.discovery.LogFilterRecipe;
import org.processmining.recipes.statechart.m2m.EPTree2PetrinetRecipe;
import org.processmining.recipes.statechart.m2m.EPTree2SeqDiagramRecipe;
import org.processmining.recipes.statechart.m2m.EPTree2StatechartRecipe;
import org.processmining.recipes.statechart.m2m.EPTreePostprocessRecipe;
import org.processmining.recipes.statechart.m2m.StatechartPostprocessRecipe;
import org.processmining.recipes.statechart.m2m.ui.EPtree2SVGRecipe;
import org.processmining.recipes.statechart.m2m.ui.Petrinet2DotSVGRecipe;
import org.processmining.recipes.statechart.m2m.ui.SeqDiagram2SVGRecipe;
import org.processmining.recipes.statechart.m2m.ui.Statechart2DotSVGRecipe;
import org.processmining.recipes.statechart.m2m.ui.Statechart2SVGRecipe;
import org.processmining.recipes.statechart.metrics.SCMetricRecipe;
import org.processmining.ui.statechart.color.IColorMap;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;
import org.processmining.ui.statechart.workbench.model.WorkbenchModel;
import org.processmining.utils.statechart.prom.PromResources;
import org.processmining.utils.statechart.signals.Action0;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Action2;
import org.processmining.utils.statechart.signals.Action3;
import org.processmining.utils.statechart.signals.Signal0;
import org.processmining.utils.statechart.signals.Signal1;
import org.processmining.utils.statechart.signals.Signal2;
import org.processmining.utils.statechart.ui.ctrlview.AbstractController;
import org.processmining.utils.statechart.ui.ctrlview.IView;

import com.kitfox.svg.SVGDiagram;

public class DiscoveryWorkbenchController extends
        AbstractController<DiscoveryWorkbenchController.View> {

    private static final Logger logger = LogManager
            .getLogger(DiscoveryWorkbenchController.class.getName());
    private static final int UpdateDelayDefault = 250;

    private static final RecipeArtifact<?> visArtrifacts[] = new RecipeArtifact<?>[] {
        WorkbenchArtifacts.EPTreeSVG,
        WorkbenchArtifacts.StatechartDotSVG,
        WorkbenchArtifacts.StatechartSVG,
        WorkbenchArtifacts.PetrinetDotSVG,
        WorkbenchArtifacts.MSDSVG
    };
    
    public static abstract class View implements IView {
        public final Signal1<Dot.GraphDirection> SignalInputDirection = new Signal1<>();
        public final Signal1<Boolean> SignalInputUseEPTreeReduct = new Signal1<>();
        public final Signal1<Boolean> SignalInputUseSCReduct = new Signal1<>();
        public final Signal1<Boolean> SignalInputUseRecurseArrow = new Signal1<>();

        public final Signal1<Double> SignalInputActivityThreshold = new Signal1<>();
        public final Signal1<Double> SignalInputPathThreshold = new Signal1<>();
        public final Signal2<Double, Double> SignalInputDepthThreshold = new Signal2<>();

        public final Signal1<String> SignalClickCollapsibleNode = new Signal1<>();
        public final Signal1<Set<String>> SignalSelectedNodes = new Signal1<>();
        
        public final Signal1<DiscoverEPTreeRecipe.DiscoveryAlgorithm> SignalInputUseAlgorithm = new Signal1<>();

        public final Signal0 SignalExportLog = new Signal0();
//        public final Signal0 SignalExportTree = new Signal0();
        public final Signal0 SignalExportPTnet = new Signal0();
//        public final Signal0 SignalExportSC = new Signal0();
        
        public final Signal1<ActivityLabeler> SignalInputActivityLabeler = new Signal1<>();
        
        public final Signal1<String> SignalClickInspectNode = new Signal1<>();
        
        public final Signal1<ModelVisualization> SignalInputSelectedVis = new Signal1<>();

        public final Signal1<Boolean> SignalInputUseCancelation = new Signal1<>();
        public final Signal1<Set<String>> SignalInputErrors = new Signal1<>();
        
        public final Signal1<AnalysisAlgorithm>  SignalInputUseAlignAlg = new Signal1<>();
        public final Signal1<AnalysisAlignMetricOverlay>  SignalInputUseAlignMetric = new Signal1<>();
        
        public final Signal1<IEvent2Time> SignalInputEvent2Time = new Signal1<>();
        public final Signal1<StatMode> SignalInputAnalysisStatMode = new Signal1<>();
        public final Signal1<String> SignalInputResourceAttribute = new Signal1<>();
        public final Signal1<Boolean> SignalInputShowLogMoves = new Signal1<>();
        
        public abstract void setInputDirection(Dot.GraphDirection dir);

        public abstract void setLogExportEnabled(boolean enabled);
        
        public abstract void setUseEPTreeReduct(boolean use);

        public abstract void setUseSCReduct(boolean use);

        public abstract void setUseRecurseArrow(boolean use);

        public abstract void setActivityThreshold(double threshold);

        public abstract void setPathThreshold(double threshold);

        public abstract void setDepthThreshold(double min, double max);

        public abstract void setDiscoveryAlgorithm(DiscoverEPTreeRecipe.DiscoveryAlgorithm algorithm);
        
        public abstract void displayDiscovering();
        
        public abstract void displayAligning();
        
        public abstract void displayAlignmentFailed();
        
        public abstract void updateAligningStatus(int min, int max, int value);

        public abstract void displayModel(SVGDiagram image, boolean resetView);

        public abstract void displayModelNotSupported();
        
        public abstract void setStatechartMetric(SCComplexityMetric metric);
        
        public abstract void setEPTree(IEPTree tree, IActivityLabeler activityLabeler);
        
        public abstract void setSelectedNodes(Set<String> selectedNodes);

        public abstract void setSawServerStatus(SawServerStatus status);
        
        public abstract void setMemoryUsage(long currentBytes, long maxBytes);
        
        public abstract void setStatus(String status);
        
        public abstract void setSawStatus(String status);
        
        public abstract void setActivityLabeler(ActivityLabeler activityLabeler);
        
        public abstract void setSelectedVis(ModelVisualization visOption);

        public abstract void setActivityInfo(HierarchyActivityInfo actInfo, IActivityLabeler labeler);
        
        public abstract void setInputUseCancelation(boolean value);
        
        public abstract void setInputErrors(Set<String> values);

        public abstract void setAlignmentAlgorithm(AnalysisAlgorithm algorithm);
        
        public abstract void setAlignmentMetric(AnalysisAlignMetricOverlay metric);
        
        public abstract void setEvent2TimeOptions(List<IEvent2Time> options);
        
        public abstract void setEvent2Time(IEvent2Time event2time);
        
        public abstract void setOverlayColorLegend(IColorMap nodeColorMap, 
                IMetricValueConvertor nodeValueConvertor,
                IColorMap edgeColorMap, IMetricValueConvertor edgeValueConvertor);

        public abstract void setAlignMetrics(XAlignedTreeLog alignedLog);

        public abstract void setAnalysisStatOptions(StatMode[] values);

        public abstract void setAnalysisStatMode(StatMode value);

        public abstract void setResourceAttributeOptions(List<String> attrOptions);

        public abstract void setResourceAttribute(String resourceAttribute);
        
        public abstract void setShowLogMoves(boolean use);

    }

    private WorkbenchModel model;

    // Tree Discovery
    private final Log2IMLogRecipe log2IMLogRecipe;
    private final LogFilterRecipe logFilterRecipe;
    private final DiscoverEPTreeRecipe discoverEPTreeRecipe;
    private final EPTreePostprocessRecipe eptreePostprocessRecipe;

    // Alignment
    private Log2LogAlignRecipe log2logalignRecipe;
    private AlignLogRecipe alignLogRecipe;
    private AlignTreeRecipe alignTreeRecipe;
    private AlignTreePostprocessingRecipe postAlignTreeRecipe;

    private SwitchArtifactRecipe<IEPTree> treeSwitchRecipe;
    
    // Tree Visualizations
    private final EPtree2SVGRecipe eptree2SVGRecipe;
    
    // Statechart Visualization
    private final EPTree2StatechartRecipe epTree2StatechartRecipe;
    private final StatechartPostprocessRecipe statechartPostprocessRecipe;
    private final Statechart2SVGRecipe statechart2SVGRecipe;
    private final Statechart2DotSVGRecipe statechart2DotSVGRecipe;

    // Petri net Visualization
    private final EPTree2PetrinetRecipe epTree2PetrinetRecipe;
    private final Petrinet2DotSVGRecipe petrinet2SVGRecipe;
    
    // Sequence Diagram Visualization
    private final EPTree2SeqDiagramRecipe epTree2SeqDiagramRecipe;
    private final SeqDiagram2SVGRecipe seqDiagram2SVGRecipe;

    private final Set<RecipeArtifact<?>> unsetQueue = new HashSet<>();
    private ModelVisualization selectedVis = ModelVisualization.StatechartDot;
    
    private final SawServer sawServer = SawServer.instance();
    private Timer updateTimer;
    
    private AnalysisAlgorithm selectedAlignAlg = AnalysisAlgorithm.Approx;
    private AnalysisAlignMetricOverlayManager overlayManager;
    private AnalysisAlignMetricOverlay selectedAlignMetric;
    private StatMode selectedStatMode = StatMode.Mean;
    
    private List<IValueDecorator> visDecorators;
    private EPTree2PetrinetRecipe epTree2PetrinetBasicRecipe;
    private Petrinet2DotSVGRecipe petrinetBasic2SVGRecipe;
    private AlignLogFilterRecipe alignLogFilterRecipe;
    
    public DiscoveryWorkbenchController(PluginContext context,
            WorkbenchModel model) {
        super(context);
        this.model = model;

        // Tree Discovery
        log2IMLogRecipe = new Log2IMLogRecipe();
        logFilterRecipe = new LogFilterRecipe();
        discoverEPTreeRecipe = new DiscoverEPTreeRecipe();
        eptreePostprocessRecipe = new EPTreePostprocessRecipe();
        
        // Alignment
        log2logalignRecipe = new Log2LogAlignRecipe();
        alignLogRecipe = new AlignLogRecipe();
        alignLogFilterRecipe = new AlignLogFilterRecipe();
        alignTreeRecipe = new AlignTreeRecipe();
        
        overlayManager = new AnalysisAlignMetricOverlayManager();
        postAlignTreeRecipe = new AlignTreePostprocessingRecipe();
        postAlignTreeRecipe.getParameters().overlayManager = overlayManager;

        treeSwitchRecipe = new SwitchArtifactRecipe<IEPTree>();
        treeSwitchRecipe.setUseFirst();
        selectedAlignAlg = AnalysisAlgorithm.Approx;
        selectedAlignMetric = overlayManager.getApproxMetric();
        
        // Tree Visualizations
        eptree2SVGRecipe = new EPtree2SVGRecipe();
        
        // Statechart Visualization
        epTree2StatechartRecipe = new EPTree2StatechartRecipe();
        statechartPostprocessRecipe = new StatechartPostprocessRecipe();
        statechart2SVGRecipe = new Statechart2SVGRecipe();
        statechart2DotSVGRecipe = new Statechart2DotSVGRecipe();

        // Petri net Visualization
        epTree2PetrinetRecipe = new EPTree2PetrinetRecipe();
        petrinet2SVGRecipe = new Petrinet2DotSVGRecipe();
        
        epTree2PetrinetBasicRecipe = new EPTree2PetrinetRecipe();
        {
            EPTree2PetrinetRecipe.Parameters params = epTree2PetrinetBasicRecipe.getParameters();
            params.hierarchyMode = HierarchyMode.LifecycleHierarchy;
            params.recursionMode = RecursionMode.IgnoreConstraint;
            params.cancelationMode = CancelationMode.MimicResetArcs;
        }
        petrinetBasic2SVGRecipe = new Petrinet2DotSVGRecipe();

        // Sequence Diagram Visualization
        epTree2SeqDiagramRecipe = new EPTree2SeqDiagramRecipe();
        seqDiagram2SVGRecipe = new SeqDiagram2SVGRecipe();
        
        // Connect recipes
        // Tree Discovery
        model.setRecipe(WorkbenchArtifacts.LogIM, log2IMLogRecipe);
        model.setRecipe(WorkbenchArtifacts.LogIMPre, logFilterRecipe);
        model.setRecipe(WorkbenchArtifacts.EPTree, discoverEPTreeRecipe);
        model.setRecipe(WorkbenchArtifacts.EPTreePost, eptreePostprocessRecipe);

        // Alignment
        model.setRecipe(WorkbenchArtifacts.LogAlign, log2logalignRecipe);
        model.setRecipe(WorkbenchArtifacts.AlignedLog, alignLogRecipe);
        model.setRecipe(WorkbenchArtifacts.AlignedLogPost, alignLogFilterRecipe);
        model.setRecipe(WorkbenchArtifacts.AlignedTree, alignTreeRecipe);
        model.setRecipe(WorkbenchArtifacts.AlignedTreePost, postAlignTreeRecipe);

        model.setRecipe(WorkbenchArtifacts.Tree2VisSwitch, treeSwitchRecipe);
        
        // Tree Visualizations
        model.setRecipe(WorkbenchArtifacts.EPTreeSVG, eptree2SVGRecipe);
        
        // Statechart Visualization
        model.setRecipe(WorkbenchArtifacts.Statechart, epTree2StatechartRecipe);
        model.setRecipe(WorkbenchArtifacts.StatechartPost, statechartPostprocessRecipe);
        model.setRecipe(WorkbenchArtifacts.StatechartSVG, statechart2SVGRecipe);
        model.setRecipe(WorkbenchArtifacts.StatechartDotSVG, statechart2DotSVGRecipe);
        model.setRecipe(WorkbenchArtifacts.StatechartMetric, new SCMetricRecipe());

        // Petri net Visualization
        model.setRecipe(WorkbenchArtifacts.Petrinet, epTree2PetrinetRecipe);
        model.setRecipe(WorkbenchArtifacts.PetrinetDotSVG, petrinet2SVGRecipe);
        
        model.setRecipe(WorkbenchArtifacts.PetrinetBasic, epTree2PetrinetBasicRecipe);
        model.setRecipe(WorkbenchArtifacts.PetrinetBasicDotSVG, petrinetBasic2SVGRecipe);
        
        model.setRecipe(WorkbenchArtifacts.PetrinetExport, epTree2PetrinetBasicRecipe);

        // Sequence Diagram Visualization
        model.setRecipe(WorkbenchArtifacts.MSD, epTree2SeqDiagramRecipe);
        model.setRecipe(WorkbenchArtifacts.MSDSVG, seqDiagram2SVGRecipe);
    }

    @Override
    public void initialize() {
        view = new DiscoveryWorkbenchView(overlayManager);

        Parameters depthFilterParams = eptreePostprocessRecipe.getParameters().filter;
        
        view.setActivityThreshold(logFilterRecipe.getParameters().threshold);
        view.setPathThreshold(discoverEPTreeRecipe.getParameters().getPathThreshold());
        view.setDepthThreshold(depthFilterParams.getDepthMin(), depthFilterParams.getDepthMax());
        view.setDiscoveryAlgorithm(discoverEPTreeRecipe.getParameters().getAlgorithm());
        view.setSelectedVis(selectedVis);
        view.setInputDirection(statechart2DotSVGRecipe.getParameters().graphDir);
        view.setUseEPTreeReduct(eptreePostprocessRecipe.getParameters().reduce);
        view.setUseSCReduct(statechartPostprocessRecipe.getParameters().reduce);
        view.setUseRecurseArrow(statechart2DotSVGRecipe.getParameters().recursionBackArrow);
        view.setActivityLabeler(statechart2DotSVGRecipe.getParameters().activityLabeler);

        updateVisDecorator();
        view.setAlignmentAlgorithm(selectedAlignAlg);
        view.setAlignmentMetric(selectedAlignMetric);
        view.setAnalysisStatOptions(StatMode.values());
        view.setAnalysisStatMode(selectedStatMode);
        view.setShowLogMoves(postAlignTreeRecipe.getParameters().expandLogMoves);

        Set<String> cancelOracle = model.getArtifact(WorkbenchArtifacts.CancelOracleInput, GetArtifactMode.GetOnly);
        if (cancelOracle != null) {
            discoverEPTreeRecipe.getParameters().setErrorClasses(cancelOracle);
        }
        view.setInputUseCancelation(discoverEPTreeRecipe.getParameters().getUseCancelation());
        view.setInputErrors(discoverEPTreeRecipe.getParameters().getErrorClasses());
        
        view.setSawServerStatus(sawServer.getStatus());
        sawServer.SignalStatus.register(new Action1<SawServerStatus>() {
            @Override
            public void call(final SawServerStatus t) {
                runUi(new Runnable() {
                    public void run() {
                        view.setSawServerStatus(t);
                    }
                });
            }
        }); // TODO when to cleanup?
//        view.setSawServerStatus(false);
        sawServer.start();
//        view.setSawServerStatus(sawServer.isRunning());
        
        view.setStatus("Initialized");
        view.setSawStatus("");
    }

    @Override
    public void activate() {
//        Function<Pair<IEPTree,IEPTree>,IEPTree> r = model.getRecipe(WorkbenchArtifacts.Tree2VisSwitch);
//        ((SwitchArtifactRecipe<IEPTree>) r).setUseFirst();

        regrec.register(alignLogRecipe.ProgressUpdate, new Action3<Integer, Integer, Integer>() {
            @Override
            public void call(final Integer min, final Integer max, final Integer value) {
                runUi(new Runnable() {
                    @Override
                    public void run() {
                        view.updateAligningStatus(min, max, value);
                    }
                });
            }
        });
        
        regrec.register(model.SignalDataChanged,
                new Action2<WorkbenchModel, RecipeArtifact<?>>() {
                    @Override
                    public void call(WorkbenchModel t, RecipeArtifact<?> u) {
                        if (u == WorkbenchArtifacts.LogPre) {
                            updateAnalysisInput();
                            updateDiagram(true);
                        }
                        if (u == WorkbenchArtifacts.CancelOracleInput) {
                            Set<String> cancelOracle = model.getArtifact(WorkbenchArtifacts.CancelOracleInput, GetArtifactMode.GetOnly);
                            discoverEPTreeRecipe.getParameters().setErrorClasses(cancelOracle);
                            
                            updateCancelOracle(true);
                            updateDiagram(true);
                        }
                    }
                });

        regrec.register(view.SignalInputActivityThreshold,
                new Action1<Double>() {
                    @Override
                    public void call(Double t) {
                        synchronized (unsetQueue) {
                            logFilterRecipe.getParameters().threshold = t;
                            unsetQueue.add(WorkbenchArtifacts.LogIMPre);
                        }
                        updateDiagram(true);
                    }
                });

        regrec.register(view.SignalInputPathThreshold, new Action1<Double>() {
            @Override
            public void call(Double t) {
                synchronized (unsetQueue) {
                    discoverEPTreeRecipe.getParameters().setPathThreshold(t);
                    unsetQueue.add(WorkbenchArtifacts.EPTree);
                }
                updateDiagram(true);
            }
        });

        regrec.register(view.SignalInputDepthThreshold,
                new Action2<Double, Double>() {
                    @Override
                    public void call(Double t, Double u) {
                        synchronized (unsetQueue) {
                            eptreePostprocessRecipe.getParameters().filter
                                    .setDepthFilter(t, u);
                            unsetQueue.add(WorkbenchArtifacts.EPTreePost);
                        }
                        updateDiagram(true);
                    }
                });

        regrec.register(view.SignalInputDirection,
                new Action1<Dot.GraphDirection>() {
                    @Override
                    public void call(Dot.GraphDirection t) {
                        synchronized (unsetQueue) {
                            eptree2SVGRecipe.getParameters().layoutDir = EPtree2SVGRecipe.map(t);
                            statechart2SVGRecipe.getParameters().layoutDir = Statechart2SVGRecipe.map(t);
                            statechart2DotSVGRecipe.getParameters().graphDir = t;
                            petrinet2SVGRecipe.getParameters().graphDir = t;
                            for (RecipeArtifact<?> vis : visArtrifacts) {
                                unsetQueue.add(vis);
                            }
                        }
                        updateDiagram(true);
                    }
                });
        
        regrec.register(view.SignalInputUseEPTreeReduct, new Action1<Boolean>() {
            @Override
            public void call(Boolean t) {
                synchronized (unsetQueue) {
                    eptreePostprocessRecipe.getParameters().reduce = t;
                    unsetQueue.add(WorkbenchArtifacts.EPTreePost);
                }
                updateDiagram(true);
            }
        });
        
        regrec.register(view.SignalInputUseSCReduct, new Action1<Boolean>() {
            @Override
            public void call(Boolean t) {
                synchronized (unsetQueue) {
                    statechartPostprocessRecipe.getParameters().reduce = t;
                    unsetQueue.add(WorkbenchArtifacts.StatechartPost);
                }
                updateDiagram(true);
            }
        });
        
        regrec.register(view.SignalInputUseRecurseArrow, new Action1<Boolean>() {
            @Override
            public void call(Boolean t) {
                synchronized (unsetQueue) {
                    statechart2DotSVGRecipe.getParameters().recursionBackArrow = t;
                    statechart2SVGRecipe.getParameters().recursionBackArrow = t;
                    unsetQueue.add(WorkbenchArtifacts.StatechartDotSVG);
                }
                updateDiagram(true);
            }
        });

        regrec.register(view.SignalClickCollapsibleNode, new Action1<String>() {
            @Override
            public void call(String t) {
                synchronized (unsetQueue) {
                    eptreePostprocessRecipe.getParameters().filter.invertNodeState(t);
                    unsetQueue.add(WorkbenchArtifacts.EPTreePost);
                }
                // TODO: possible add a refocus into view for the triggered node
                updateDiagram(false);
            }
        });

        regrec.register(view.SignalSelectedNodes, new Action1<Set<String>>() {
            @Override
            public void call(Set<String> t) {
                synchronized (unsetQueue) {
                    alignLogFilterRecipe.getParameters().selectedNodes = t;
                    unsetQueue.add(WorkbenchArtifacts.AlignedLogPost);
                    
                    statechart2DotSVGRecipe.getParameters().selectedNodes = t;
                    statechart2SVGRecipe.getParameters().selectedNodes = t;
                    petrinet2SVGRecipe.getParameters().selectedNodes = t;
                    petrinetBasic2SVGRecipe.getParameters().selectedNodes = t;
                    seqDiagram2SVGRecipe.getParameters().selectedNodes = t;
                    eptree2SVGRecipe.getParameters().selectedNodes = t;
                    for (RecipeArtifact<?> vis : visArtrifacts) {
                        unsetQueue.add(vis);
                    }
                }
                updateDiagram(false);
            }
        });

        regrec.register(view.SignalInputUseAlgorithm, new Action1<DiscoverEPTreeRecipe.DiscoveryAlgorithm>() {
            @Override
            public void call(DiscoverEPTreeRecipe.DiscoveryAlgorithm t) {
                synchronized (unsetQueue) {
                    discoverEPTreeRecipe.getParameters().setAlgorithm(t);
                    unsetQueue.add(WorkbenchArtifacts.EPTree);
                }
                updateDiagram(true);
            }
        });
        
        regrec.register(view.SignalInputActivityLabeler, new Action1<ActivityLabeler>() {
            @Override
            public void call(ActivityLabeler t) {
                synchronized (unsetQueue) {
                    eptree2SVGRecipe.getParameters().activityLabeler = t;
                    statechart2DotSVGRecipe.getParameters().activityLabeler = t;
                    statechart2SVGRecipe.getParameters().activityLabeler = t;
                    petrinet2SVGRecipe.getParameters().activityLabeler = t;

                    for (RecipeArtifact<?> vis : visArtrifacts) {
                        unsetQueue.add(vis);
                    }
                }
                updateDiagram(true);
            }
        });

        view.setLogExportEnabled(model.hasArtifact(WorkbenchArtifacts.LogPre));
        regrec.register(view.SignalExportLog, new Action0() {
            @Override
            public void call() {
                exportObject(WorkbenchArtifacts.LogAlign, XLog.class, "Statechart Model - Postprocessed Log");
            }
        });
//        regrec.register(view.SignalExportTree, new Action0() {
//            @Override
//            public void call() {
//                exportObject(WorkbenchArtifacts.EPTree, IEPTree.class, "Statechart Model - EP Tree");
//            }
//        });
        regrec.register(view.SignalExportPTnet, new Action0() {
            @Override
            public void call() {
                exportPetrinet(WorkbenchArtifacts.PetrinetExport, "Statechart Model - Petri net");
            }
        });
//        regrec.register(view.SignalExportSC, new Action0() {
//            @Override
//            public void call() {
//                exportObject(WorkbenchArtifacts.SCPost, Statechart.class, "Statechart Model - Statechart");
//            }
//        });
        
        regrec.register(view.SignalClickInspectNode, new Action1<String>() {
            @Override
            public void call(String t) {
                IEPTree tree = model.getArtifact(WorkbenchArtifacts.EPTree);
                IEPTreeNode node = tree.getNodeById(t);
                EPTreeSwAppDecorator d = tree.getDecorations().getForType(EPTreeSwAppDecorator.class);
                SwAppDecoration data = d.getDecoration(node);
                
                if (data != null) {
                    view.setSawStatus("Select joinpoint: " + data.getJoinpoint());
                    logger.info("Double click activity for: " + data.getFilename() + " @ " + data.getLinenr());
                    sawServer.getApi().selectJoinpoint(new Joinpoint(data));
                } else {
                    view.setSawStatus("Select joinpoint: <not found>");
                    logger.info("Double click activity for: No Data");
                }
            }
        });

        regrec.register(view.SignalInputSelectedVis, new Action1<ModelVisualization>() {
            @Override
            public void call(ModelVisualization t) {
//                synchronized (unsetQueue) {
//                    sc2SVGRecipe.getParameters().activityLabeler = t;
//                    petrinet2SVGRecipe.getParameters().activityLabeler = t;
//                    unsetQueue.add(WorkbenchArtifacts.SCSVG);
//                    unsetQueue.add(WorkbenchArtifacts.PTnetSVG);
//                }
                selectedVis = t;
                updateDiagram(true);
            }
        });

        regrec.register(view.SignalInputUseCancelation, new Action1<Boolean>() {
            @Override
            public void call(Boolean t) {
                synchronized (unsetQueue) {
                    discoverEPTreeRecipe.getParameters().setUseCancelation(t);
                    unsetQueue.add(WorkbenchArtifacts.EPTree);
                }
                updateCancelOracle(false);
                updateDiagram(true);
            }
        });
        
        regrec.register(view.SignalInputErrors, new Action1<Set<String>>() {
            @Override
            public void call(Set<String> t) {
                synchronized (unsetQueue) {
                    discoverEPTreeRecipe.getParameters().setErrorClasses(t);
                    view.setInputUseCancelation(discoverEPTreeRecipe.getParameters().getUseCancelation());
                    unsetQueue.add(WorkbenchArtifacts.EPTree);
                }
                updateCancelOracle(false);
                updateDiagram(true);
            }
        });
        
        regrec.register(view.SignalInputUseAlignAlg, new Action1<AnalysisAlgorithm>() {
            @Override
            public void call(AnalysisAlgorithm t) {
                selectedAlignAlg = t;
                updateVisDecorator();
                updateDiagram(true);
            }
        });
        regrec.register(view.SignalInputUseAlignMetric, new Action1<AnalysisAlignMetricOverlay>() {
            @Override
            public void call(AnalysisAlignMetricOverlay t) {
                selectedAlignMetric = t;
                if (selectedAlignMetric != overlayManager.getApproxMetric()) {
                    selectedAlignAlg = AnalysisAlgorithm.Align;
                }
                updateVisDecorator();
                updateDiagram(true);
            }
        });

        regrec.register(view.SignalInputEvent2Time, new Action1<IEvent2Time>() {
            @Override
            public void call(IEvent2Time t) {
                overlayManager.setEvent2Time(t);
                updateVisDecorator();
                updateDiagram(true);
            }
        });

        regrec.register(view.SignalInputAnalysisStatMode, new Action1<StatMode>() {
            @Override
            public void call(StatMode t) {
                selectedStatMode = t;
                updateVisDecorator();
                updateDiagram(true);
            }
        });
        
        regrec.register(view.SignalInputResourceAttribute, new Action1<String>() {
            @Override
            public void call(String t) {
                overlayManager.setResourceAttribute(t);
                updateVisDecorator();
                updateDiagram(true);
            }
        });
        regrec.register(view.SignalInputShowLogMoves, new Action1<Boolean>() {
            @Override
            public void call(Boolean t) {
                synchronized (unsetQueue) {
                    postAlignTreeRecipe.getParameters().expandLogMoves = t;
                    unsetQueue.add(WorkbenchArtifacts.AlignedTreePost);
                }
                updateDiagram(true);
            }
        });
        
        updateTimer = new Timer(UpdateDelayDefault, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Runtime runtime = Runtime.getRuntime();
                runUi(new Runnable() {
                    public void run() {
                        view.setMemoryUsage(runtime.totalMemory() - runtime.freeMemory(), runtime.maxMemory());
                    }
                });
            }
        });
        updateTimer.setInitialDelay(0);
        updateTimer.setRepeats(true);
        updateTimer.start();

        // initial update
        Set<String> cancelOracle = model.getArtifact(WorkbenchArtifacts.CancelOracleInput, GetArtifactMode.GetOnly);
        if (cancelOracle != null) {
            discoverEPTreeRecipe.getParameters().setErrorClasses(cancelOracle);
        }
        
        updateAnalysisInput();
        updateCancelOracle(true);
        updateDiagram(true);
    }

    protected void updateVisDecorator() {
        synchronized (unsetQueue) {
            if (selectedAlignAlg == AnalysisAlgorithm.Approx) {
                treeSwitchRecipe.setUseFirst();
            } else {
                treeSwitchRecipe.setUseSecond();
            }
            
            // Setup Decorators
            if (selectedAlignAlg == AnalysisAlgorithm.Approx) {
                selectedAlignMetric = overlayManager.getApproxMetric();
            }
            statechart2DotSVGRecipe.getParameters()
                .setupDecorator(selectedAlignAlg, selectedAlignMetric, selectedStatMode);
            seqDiagram2SVGRecipe.getParameters()
                .setupDecorator(selectedAlignAlg, selectedAlignMetric, selectedStatMode);
            petrinet2SVGRecipe.getParameters()
                .setupDecorator(selectedAlignAlg, selectedAlignMetric, selectedStatMode);
            petrinetBasic2SVGRecipe.getParameters()
                .setupDecorator(selectedAlignAlg, selectedAlignMetric, selectedStatMode);
            eptree2SVGRecipe.getParameters()
                .setupDecorator(selectedAlignAlg, selectedAlignMetric, selectedStatMode);

            // Register decorators
            visDecorators = new ArrayList<>();
            visDecorators.add(statechart2DotSVGRecipe.getParameters().dotDecorator);
            visDecorators.add(petrinet2SVGRecipe.getParameters().dotDecorator);
            visDecorators.add(petrinetBasic2SVGRecipe.getParameters().dotDecorator);
            visDecorators.add(seqDiagram2SVGRecipe.getParameters().svgDecorator);
            visDecorators.add(eptree2SVGRecipe.getParameters().svgDecorator);
            
            unsetQueue.add(WorkbenchArtifacts.Tree2VisSwitch);
            //unsetQueue.add(WorkbenchArtifacts.StatechartDotSVG);
        }
    }
    
    protected void updateAnalysisInput() {
        synchronized (unsetQueue) {
            XLog log = model.getArtifact(WorkbenchArtifacts.LogPre, GetArtifactMode.GetOnly);
            
            // prepare event 2 time
            List<IEvent2Time> e2tOptions = Event2TimeAttribute.extractValidAttributes(log, true);
            view.setEvent2TimeOptions(e2tOptions);
            
            IEvent2Time e2t = e2tOptions.get(0);
            if (selectedAlignMetric != null) {
                IMetric metric = selectedAlignMetric.getPrimaryMetric();
                if (metric instanceof ITimeMetric) {
                    e2t = ((ITimeMetric) metric).getEvent2Time();
                }
            }
            
            overlayManager.setEvent2Time(e2t);
            view.setEvent2Time(e2t);
            
            // prepare resource attribute
            List<String> attrOptions = ResourceAttribute.extractValidAttributes(log, true);
            view.setResourceAttributeOptions(attrOptions);

            String resAttr = attrOptions.get(0);
            if (selectedAlignMetric != null) {
                IMetric metric = selectedAlignMetric.getPrimaryMetric();
                if (metric instanceof IResourceMetric) {
                    resAttr = ((IResourceMetric) metric).getResourceAttribute();
                }
            }
            
            overlayManager.setResourceAttribute(resAttr);
            view.setResourceAttribute(resAttr);
            
            updateVisDecorator();
        }
    }

    protected void updateCancelOracle(boolean updateView) {
        synchronized (unsetQueue) {
            DiscoverEPTreeRecipe.Parameters params = discoverEPTreeRecipe.getParameters();

            // update params
            boolean useCancelation = params.getUseCancelation();
            if (useCancelation) {
                epTree2PetrinetRecipe.getParameters().queryCatchError = params.getQueryCatchError();
                epTree2PetrinetBasicRecipe.getParameters().queryCatchError = params.getQueryCatchError();
                alignLogRecipe.getParameters().queryCatchError = params.getQueryCatchError();
            } else {
                epTree2PetrinetRecipe.getParameters().queryCatchError = null;
                epTree2PetrinetBasicRecipe.getParameters().queryCatchError = null;
                alignLogRecipe.getParameters().queryCatchError = null;
            }
            
            if (updateView) {
                view.setInputUseCancelation(params.getUseCancelation());
                view.setInputErrors(params.getErrorClasses());
            }

            unsetQueue.add(WorkbenchArtifacts.EPTree);
        }
    }

    /**
     * Unregister all listeners and start background work here
     */
    public void deactivate() {
        super.deactivate();
        updateTimer.stop();
    }


    public <T> void exportPetrinet(
            final RecipeArtifactD1<PetrinetDecorated, T> petrinetRecipe, final String name) {
        scheduleBackground(new ControllerWorker<Void, Void>() {
            @Override
            public void prepareUi() {
                // TODO?
            }

            @Override
            protected Void doInBackground() throws Exception {
                logger.info("Start exporting - " + name);
                
                // create ProM object, computing artifact if needed
                PetrinetDecorated ptmodel = model.getArtifact(petrinetRecipe);
                Petrinet ptnet = ptmodel.getNet();
                context.addConnection(new InitialMarkingConnection(ptnet, ptmodel.getInitialMarking()));
                for (Marking m : ptmodel.getFinalMarkings()) {
                    context.addConnection(new FinalMarkingConnection(ptnet, m));   
                }
                PromResources.createObject(context, ptnet, Petrinet.class, name, true, true);

                logger.info("Done exporting - " + name);
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    // get() for checking if results where computed correctly
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error in exporting", e);
                }
            }
        });
    }
    
    public <T> void exportObject(
            final RecipeArtifact<T> recipe, 
            final Class<? super T> type, final String name) {
        scheduleBackground(new ControllerWorker<Void, Void>() {
            @Override
            public void prepareUi() {
                // TODO?
            }

            @Override
            protected Void doInBackground() throws Exception {
                logger.info("Start exporting - " + name);
                
                // create ProM object, computing artifact if needed
                PromResources.createObject(context, 
                    model.getArtifact(recipe), type, name, true, true);

                logger.info("Done exporting - " + name);
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    // get() for checking if results where computed correctly
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error in exporting", e);
                }
            }
        });
    }

    public void updateDiagram(final boolean resetView) {

        scheduleBackground(new ControllerWorker<Void, Void>() {
            long durDisc, durTotal;
            
            @Override
            public void prepareUi() {
                view.setStatus("Discovering...");
                view.displayDiscovering();
                
                view.setAlignmentAlgorithm(selectedAlignAlg);
                view.setAlignmentMetric(selectedAlignMetric);
                
            }

            @Override
            protected Void doInBackground() throws Exception {
                synchronized (model) {
                	// unset artifacts if requested, causes recomputation
                    synchronized (unsetQueue) {
                        for (RecipeArtifact<?> key : unsetQueue) {
                            model.unsetArtifact(key);
                        }
                        unsetQueue.clear();
                    }
                    
                    // compute discovery artifacts if needed
                    long start = System.currentTimeMillis();
                    model.getArtifact(WorkbenchArtifacts.EPTree);
                    long mid = System.currentTimeMillis();

                    // compute alignment artifacts if needed
                    if (selectedAlignAlg == AnalysisAlgorithm.Align) {
                        runUi(new Runnable() {
                            @Override
                            public void run() {
                                view.setStatus("Aligning...");
                                view.displayAligning();
                            }
                        
                        });
                    }
                    
                    // compute final artifacts if needed
                    model.getArtifact(selectedVis.getVisArtifact());
                    model.getArtifact(WorkbenchArtifacts.StatechartMetric);
                    model.getArtifact(WorkbenchArtifacts.EPTreePost);
                    
                    if (model.hasArtifact(WorkbenchArtifacts.LogPre)) {
                        model.getArtifact(WorkbenchArtifacts.JoinpointStats);
                        model.getArtifact(WorkbenchArtifacts.ActivityInfo);
                    }
                    long stop = System.currentTimeMillis();
                    durDisc = (mid - start);
                    durTotal = (stop - start);
                    
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    // get() for checking if results where computed correctly
                    get();
                    
                    // set log dependent things
                    if (model.hasArtifact(WorkbenchArtifacts.LogPre)) {
                        // set saw artifacts
                        sawServer.getApi().setJoinpointStats(model.getArtifact(WorkbenchArtifacts.JoinpointStats));
                    }

                    // pass artifacts to view
                    view.setStatus("Discovery completed in " + durDisc + " ms (total: " + durTotal + " ms)");
                    view.displayModel(model.getArtifact(selectedVis.getVisArtifact()), resetView);
                    view.setEPTree(model.getArtifact(WorkbenchArtifacts.EPTreePost), 
                            statechart2DotSVGRecipe.getParameters().activityLabeler.getLabeler());
                    
                    view.setActivityInfo(model.getArtifact(WorkbenchArtifacts.ActivityInfo), 
                            statechart2DotSVGRecipe.getParameters().activityLabeler.getLabeler());
                    view.setSelectedNodes(statechart2DotSVGRecipe.getParameters().selectedNodes);

                    view.setStatechartMetric(model.getArtifact(WorkbenchArtifacts.StatechartMetric));
                    if (model.hasArtifact(WorkbenchArtifacts.AlignedLog)) {
                        view.setAlignMetrics(model.getArtifact(WorkbenchArtifacts.AlignedLog));
                    }
                    
                    IValueDecorator decorator = getAppliedDecorator();
                    if (decorator != null) {
                        view.setOverlayColorLegend(
                            decorator.getNodeColorMap(),
                            decorator.getNodeValueConvertor(),
                            decorator.getEdgeColorMap(),
                            decorator.getEdgeValueConvertor()
                        );
                    } else {
                        view.setOverlayColorLegend(null, null, null, null);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    if (e.getCause() instanceof TransformationException) {
                        view.displayModelNotSupported();
                    }
                    
                    if (selectedAlignAlg == AnalysisAlgorithm.Align
                        && model.hasArtifact(WorkbenchArtifacts.EPTree)
                        && !model.hasArtifact(WorkbenchArtifacts.AlignedTreePost)) {
                        // Alignment failed, fallback to approximations
                        view.setStatus("Error in Alignment");
                        logger.error("Error in Alignment - updateDiagram", e);
                        view.displayAlignmentFailed();
                        // apply recovery strategy
                        selectedAlignAlg = AnalysisAlgorithm.Approx;
                        updateVisDecorator();
                        updateDiagram(true);
                    } else if (model.hasArtifact(WorkbenchArtifacts.EPTree)) {
                        // Postprocessing failed
                        view.setStatus("Error in Postprocessing");
                        logger.error("Error in Postprocessing - updateDiagram", e);
                    } else {
                        // Discovery failed
                        view.setStatus("Error in Discovering");
                        logger.error("Error in Discovery - updateDiagram", e);
                    }
                }
            }
        });

    }

    protected IValueDecorator getAppliedDecorator() {
        if (visDecorators == null) {
            return null;
        }
        
        for (IValueDecorator decorator : visDecorators) {
            if (decorator.isApplied()) {
                return decorator;
            }
        }
        
        return null;
    }
}
