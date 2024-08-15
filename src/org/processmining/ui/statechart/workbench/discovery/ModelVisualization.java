package org.processmining.ui.statechart.workbench.discovery;

import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.ui.statechart.workbench.model.WorkbenchArtifacts;

import com.kitfox.svg.SVGDiagram;

public enum ModelVisualization {
    StatechartDot("Statechart", "Statechart, using DOT layout", WorkbenchArtifacts.StatechartDotSVG),
//    Statechart("Statechart (Exp.)", WorkbenchArtifacts.StatechartSVG),
    SeqDiagram("Sequence Diagram", "UML Message Sequence Diagram", WorkbenchArtifacts.MSDSVG),
    Petrinet("Petri net", "Petri net with cancelation regions and subprocesses", WorkbenchArtifacts.PetrinetDotSVG),
    PetrinetBasic("Basic Petri net", "Basic Place-Transition net, with separate transitions for activty start and complete", WorkbenchArtifacts.PetrinetBasicDotSVG),
    ExProcessTree("Process Tree", "Extended Process Tree", WorkbenchArtifacts.EPTreeSVG);
    
    private final String name, descriptionShort;
    private final RecipeArtifact<SVGDiagram> visArtifact;

    private ModelVisualization(String name, String descriptionShort, RecipeArtifact<SVGDiagram> visArtifact) {
        this.name = name;
        this.descriptionShort = descriptionShort;
        this.visArtifact = visArtifact;
    }

    public String getName() {
        return name;
    }
    
    public String getDescriptionShort() {
        return descriptionShort;
    }
    
    public String toString() {
        return getName();
    }

    public RecipeArtifact<SVGDiagram> getVisArtifact() {
        return visArtifact;
    }
}
