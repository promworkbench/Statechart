package org.processmining.recipes.statechart;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Recipe artifact key Veriant: two dependencies
 * 
 * @author mleemans
 *
 * @param <T>
 *            type of artifact
 * @param <F1>
 *            type of first input for recipe to compute artifact
 * @param <F2>
 *            type of second input for recipe to compute artifact
 */
public class RecipeArtifactD2<T, F1, F2> extends RecipeArtifact<T> {

    private RecipeArtifact<F1> dep1;
    private RecipeArtifact<F2> dep2;

    public RecipeArtifactD2(RecipeArtifact<F1> dep1, RecipeArtifact<F2> dep2) {
        this.dep1 = dep1;
        this.dep2 = dep2;
    }

    public Pair<RecipeArtifact<F1>, RecipeArtifact<F2>> getDependentArtifacts() {
        return Pair.of(dep1, dep2);
    }
}