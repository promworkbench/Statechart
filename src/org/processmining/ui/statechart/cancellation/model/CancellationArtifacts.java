package org.processmining.ui.statechart.cancellation.model;

import org.deckfour.xes.model.XLog;
import org.processmining.algorithms.statechart.align.FitnessPrecision;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.RecipeArtifactD1;
import org.processmining.recipes.statechart.RecipeArtifactD2;

import com.kitfox.svg.SVGDiagram;

public class CancellationArtifacts {
    public static final RecipeArtifact<XLog> LogOriginal = new RecipeArtifact<>();
    public static final RecipeArtifactD1<XLog, XLog> LogPre = new RecipeArtifactD1<>(LogOriginal);

    public static final RecipeArtifactD1<IMLog, XLog> LogIM = new RecipeArtifactD1<>(LogPre);
//    public static final RecipeArtifactD1<IEPTree, IMLog> EPTree = new RecipeArtifactD1<>(LogIM);
    public static final RecipeArtifactD1<IEPTree, XLog> EPTree = new RecipeArtifactD1<>(LogPre);
    
    public static final RecipeArtifactD1<Statechart, IEPTree> SC = new RecipeArtifactD1<>(EPTree);
    public static final RecipeArtifactD1<SVGDiagram, Statechart> SCSVG = new RecipeArtifactD1<>(SC);

    public static final RecipeArtifactD1<PetrinetDecorated, IEPTree> PTnet = new RecipeArtifactD1<>(EPTree);
    public static final RecipeArtifactD1<SVGDiagram, PetrinetDecorated> PTnetSVG = new RecipeArtifactD1<>(PTnet); 
    
    public static final RecipeArtifactD2<FitnessPrecision, XLog, PetrinetDecorated> Metrics = new RecipeArtifactD2<>(LogOriginal, PTnet); 
    
}
