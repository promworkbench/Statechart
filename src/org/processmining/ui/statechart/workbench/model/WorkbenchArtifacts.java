package org.processmining.ui.statechart.workbench.model;

import java.util.Collection;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.models.statechart.align.XAlignedTreeLog;
import org.processmining.models.statechart.decorate.staticmetric.SCComplexityMetric;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.log.HierarchyActivityInfo;
import org.processmining.models.statechart.msd.ISeqDiagram;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.protocols.statechart.saw.api.data.JoinpointStat;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.RecipeArtifactD1;
import org.processmining.recipes.statechart.RecipeArtifactD2;

import com.kitfox.svg.SVGDiagram;

public class WorkbenchArtifacts {
    // Log input
    public static final RecipeArtifact<XLog> LogOriginal = new RecipeArtifact<>();
    public static final RecipeArtifactD1<XLog, XLog> LogPre = new RecipeArtifactD1<>(LogOriginal);

    // Log derivatives
    public static final RecipeArtifactD1<Collection<JoinpointStat>, XLog> JoinpointStats = new RecipeArtifactD1<>(LogPre);
    public static final RecipeArtifactD1<HierarchyActivityInfo, XLog> ActivityInfo = new RecipeArtifactD1<>(LogPre);
    
    // artifact cancellation oracle
    // for sw heuristics: promote exceptions to activities during hierarchies
    // use: discovery input
    public static final RecipeArtifactD2<Set<String>, XLog, HierarchyActivityInfo> CancelOracleInput = new RecipeArtifactD2<>(LogPre, ActivityInfo);
    
    // Tree Discovery
    public static final RecipeArtifactD1<IMLog, XLog> LogIM = new RecipeArtifactD1<>(LogPre);
    public static final RecipeArtifactD1<IMLog, IMLog> LogIMPre = new RecipeArtifactD1<>(LogIM);

    public static final RecipeArtifactD1<IEPTree, IMLog> EPTree = new RecipeArtifactD1<>(LogIMPre);

    // Alignment basics
    public static final RecipeArtifactD1<XLog, IMLog> LogAlign = new RecipeArtifactD1<>(LogIM);
    public static final RecipeArtifactD2<XAlignedTreeLog, XLog, IEPTree> AlignedLog = new RecipeArtifactD2<>(LogAlign, EPTree);//LogOriginal, EPTree);//LogAlign, EPTree);
    public static final RecipeArtifactD1<XAlignedTreeLog, XAlignedTreeLog> AlignedLogPost = new RecipeArtifactD1<>(AlignedLog);
    public static final RecipeArtifactD2<IEPTree, XAlignedTreeLog, IEPTree> AlignedTree = new RecipeArtifactD2<>(AlignedLogPost, EPTree);

    // Alignment post processing
    public static final RecipeArtifactD1<IEPTree, IEPTree> AlignedTreePost = new RecipeArtifactD1<>(AlignedTree);
    
    // Choose which tree to use for visualizations
    public static final RecipeArtifactD2<IEPTree, IEPTree, IEPTree> Tree2VisSwitch = new RecipeArtifactD2<>(EPTree, AlignedTreePost);
    public static final RecipeArtifactD1<IEPTree, IEPTree> EPTreePost = new RecipeArtifactD1<>(Tree2VisSwitch);
    
    // Tree Visualizations
    public static final RecipeArtifactD1<SVGDiagram, IEPTree> EPTreeSVG = new RecipeArtifactD1<>(EPTreePost);
    
    // Statechart Visualizations
    public static final RecipeArtifactD1<Statechart, IEPTree> Statechart = new RecipeArtifactD1<>(EPTreePost);
    public static final RecipeArtifactD1<Statechart, Statechart> StatechartPost = new RecipeArtifactD1<>(Statechart);
    public static final RecipeArtifactD1<SCComplexityMetric, Statechart> StatechartMetric = new RecipeArtifactD1<>(StatechartPost);

    public static final RecipeArtifactD1<SVGDiagram, Statechart> StatechartDotSVG = new RecipeArtifactD1<>(StatechartPost);
    public static final RecipeArtifactD2<SVGDiagram, IEPTree, Statechart> StatechartSVG = new RecipeArtifactD2<>(EPTreePost, StatechartPost);

    // Petri net Visualization
    public static final RecipeArtifactD1<PetrinetDecorated, IEPTree> Petrinet = new RecipeArtifactD1<>(EPTreePost);
    public static final RecipeArtifactD1<SVGDiagram, PetrinetDecorated> PetrinetDotSVG = new RecipeArtifactD1<>(Petrinet); 

    public static final RecipeArtifactD1<PetrinetDecorated, IEPTree> PetrinetBasic = new RecipeArtifactD1<>(EPTreePost);
    public static final RecipeArtifactD1<SVGDiagram, PetrinetDecorated> PetrinetBasicDotSVG = new RecipeArtifactD1<>(PetrinetBasic); 
    
    public static final RecipeArtifactD1<PetrinetDecorated, IEPTree> PetrinetExport = new RecipeArtifactD1<>(EPTree);
    
    // Sequence Diagram Visualization
    public static final RecipeArtifactD1<ISeqDiagram, IEPTree> MSD = new RecipeArtifactD1<>(EPTreePost);
    public static final RecipeArtifactD1<SVGDiagram, ISeqDiagram> MSDSVG = new RecipeArtifactD1<>(MSD);
    
    
}
