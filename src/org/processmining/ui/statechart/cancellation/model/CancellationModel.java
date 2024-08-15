package org.processmining.ui.statechart.cancellation.model;

import org.processmining.recipes.statechart.RecipeArtifact;
import org.processmining.recipes.statechart.RecipeProcess;
import org.processmining.utils.statechart.signals.Signal2;

public class CancellationModel extends RecipeProcess {

    public final Signal2<CancellationModel, RecipeArtifact<?>> SignalDataChanged = new Signal2<>();

    public CancellationModel() {
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
}
