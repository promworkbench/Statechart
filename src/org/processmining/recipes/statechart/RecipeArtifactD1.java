package org.processmining.recipes.statechart;

/**
 * 
 * Recipe artifact key Veriant: one dependency
 * 
 * @author mleemans
 *
 * @param <T>
 *            type of artifact
 * @param <F>
 *            type of input for recipe to compute artifact
 */
public class RecipeArtifactD1<T, F> extends RecipeArtifact<T> {

    private RecipeArtifact<F> dependency;

    public RecipeArtifactD1(RecipeArtifact<F> dependency) {
        this.dependency = dependency;
    }

    public RecipeArtifact<F> getDependentArtifact() {
        return dependency;
    }
}