package org.processmining.ui.statechart.workbench.model;

import java.util.Collections;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.RecipeProcess;
import org.processmining.recipes.statechart.cancel.CancelNestedHandleRecipe;
import org.processmining.recipes.statechart.l2l.L2LAttributeListRecipe;
import org.processmining.recipes.statechart.l2l.L2LNestedCallsRecipe;
import org.processmining.utils.statechart.signals.Signal2;

public class WorkbenchModel extends RecipeProcess {

    public final Signal2<WorkbenchModel, RecipeArtifact<?>> SignalDataChanged = new Signal2<>();

    public WorkbenchModel() {
    }
    
    @Override
    public <T> void setArtifact(RecipeArtifact<T> key, T artifact,
            SetArtifactMode mode) {
        super.setArtifact(key, artifact, mode);
        SignalDataChanged.dispatch(this, key);
    }
    
    public void unsetArtifact(RecipeArtifact<?> key) {
        super.unsetArtifact(key);
        //SignalDataChanged.dispatch(this, key);
    }

    public void setNormalLogPresets() {
        // Use Default event classifier Hierarchy Heuristics 
        L2LAttributeListRecipe hierarchyRecipe = new L2LAttributeListRecipe();
        hierarchyRecipe.getParameters().clsList = 
                Collections.<XEventClassifier>singletonList(new XEventNameClassifier());
        this.setRecipe(WorkbenchArtifacts.LogPre, hierarchyRecipe, true);
        this.computeArtifact(WorkbenchArtifacts.LogPre, true);
    }

    public void setSWLogPresets() {
        // Use Nested Calls Hierarchy Heuristics 
        L2LNestedCallsRecipe hierarchyRecipe = new L2LNestedCallsRecipe();
        this.setRecipe(WorkbenchArtifacts.LogPre, hierarchyRecipe, true);
        this.computeArtifact(WorkbenchArtifacts.LogPre, true);
        
        // Use Handle Exceptions Cancellation Heuristics
        CancelNestedHandleRecipe cancelRecipe = new CancelNestedHandleRecipe();
        this.setRecipe(WorkbenchArtifacts.CancelOracleInput, cancelRecipe, true);
        this.computeArtifact(WorkbenchArtifacts.CancelOracleInput, true);
        
    }
}
