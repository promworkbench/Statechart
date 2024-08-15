package org.processmining.recipes.statechart;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.models.statechart.eptree.IEPTree;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

/**
 * Specifies a Recipe Process: A DAG for managing and computing recipe
 * artifacts.
 * 
 * @author mleemans
 *
 *         The DAG, specifying dependencies between artifacts is created via the
 *         RecipeArtifactD1 and RecipeArtifactD2 types.
 * 
 *         Example DAG: artifacts A, B, C, D where B and C depends on A and D
 *         depends on B and C.
 * 
 *         Example specification: <code>
 *         class DAG {
 *              public static final RecipeArtifact<T1> A = new RecipeArtifact<>();
 *              public static final RecipeArtifactD1<T2, T1> B = new RecipeArtifactD1<>(A);
 *              public static final RecipeArtifactD1<T3, T1> C = new RecipeArtifactD1<>(A);
 *              public static final RecipeArtifactD2<T4, T2, T3> D = new RecipeArtifactD2<>(B, C);
 *         }
 *         </code>
 */
public class RecipeProcess {

    /**
     * Operation modi for setting artifacts
     * 
     * @author mleemans
     *
     */
    public static enum SetArtifactMode {
        SetOnly(false), UnsetResults(true);

        private boolean unsetResults;

        private SetArtifactMode(boolean unsetResults) {
            this.unsetResults = unsetResults;
        }

        public boolean unsetResults() {
            return unsetResults;
        }
    }

    /**
     * Operation modi for getting artifacts
     * 
     * @author mleemans
     *
     */
    public static enum GetArtifactMode {
        GetOnly(false, false), ComputeNoUnset(true, false), ComputeAndUnsetResults(true, true);

        private boolean computeIfAbsent;
        private boolean unsetResultsIfComputed;

        private GetArtifactMode(boolean computeIfAbsent, boolean unsetResultsIfComputed) {
            this.computeIfAbsent = computeIfAbsent;
            this.unsetResultsIfComputed = unsetResultsIfComputed;
        }
        
        public boolean computeIfAbsent() {
            return computeIfAbsent;
        }

        public boolean unsetResultsIfComputed() {
            return unsetResultsIfComputed;
        }
    }

    // Recipes for artifacts
    private final Map<RecipeArtifact<?>, Function<?, ?>> recipes;

    // Artifact results
    private final Map<RecipeArtifact<?>, Object> artifacts;

    public RecipeProcess() {
        recipes = new HashMap<>();
        artifacts = new HashMap<>();
    }

    /**
     * Set recipe with default behavior: unset artifacts
     * @param key
     * @param fnc
     */
    public <F, T> void setRecipe(RecipeArtifactD1<T, F> key,
            Function<F, T> fnc) {
        setRecipe(key, fnc, true);
    }
    
    /**
     * Set the recipe function for the given key Variant: key has one
     * dependency, the input for the recipe function
     * 
     * @param key
     * @param fnc
     */
    public <F, T> void setRecipe(RecipeArtifactD1<T, F> key,
            Function<F, T> fnc, boolean unsetArtifact) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(fnc);
        recipes.put(key, (Function<?, ?>) fnc);

        if (unsetArtifact) {
            unsetArtifact(key);
        }
    }

    /**
     * Set recipe with default behavior: unset artifacts
     * @param key
     * @param fnc
     */
    public <F1, F2, T> void setRecipe(RecipeArtifactD2<T, F1, F2> key,
            Function<Pair<F1, F2>, T> fnc) {
        setRecipe(key, fnc, true);
    }
    
    /**
     * Set the recipe function for the given key Variant: key has two
     * dependencies, the input pair for the recipe function
     * 
     * @param key
     * @param fnc
     */
    public <F1, F2, T> void setRecipe(RecipeArtifactD2<T, F1, F2> key,
            Function<Pair<F1, F2>, T> fnc, boolean unsetArtifact) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(fnc);
        recipes.put(key, (Function<?, ?>) fnc);

        if (unsetArtifact) {
            unsetArtifact(key);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getRecipe(
            RecipeArtifact<IEPTree> key) {
        return (T) recipes.get(key);
    }

    /**
     * Set with default mode - don't retrieve connected and unset results
     * @param key
     * @return
     */
    public <T> void setArtifact(RecipeArtifact<T> key, T artifact) {
        setArtifact(key, artifact, SetArtifactMode.UnsetResults);
    }
    
    /**
     * Set the result artifact for the given key.
     * 
     * If retrieveConnectedInputs is true, and a context was provided, then all
     * objects connected to this artifact (as per the setted recipes) are
     * retrieved and setted.
     * 
     * If unsetResults is true, then all the depending artifacts are unset. An
     * artifact is (indirectly) depending on this artifact key, if there is
     * (indirectly) a function that requires it as input.
     * 
     * @param key
     * @param artifact
     * @param retrieveConnectedInputs
     * @param unsetResults
     */
    public <T> void setArtifact(RecipeArtifact<T> key, T artifact,
            SetArtifactMode mode) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(artifact);
        Preconditions.checkNotNull(mode);

        if (mode.unsetResults()) {
            unsetArtifact(key);
        }

        artifacts.put(key, artifact);
    }

    /**
     * Unset the artifact and all depending artifacts for the given key. An
     * artifact is (indirectly) depending on this artifact key, if there is
     * (indirectly) a function that requires it as input.
     * 
     * @param key
     */
    @SuppressWarnings("rawtypes")
    public void unsetArtifact(RecipeArtifact<?> key) {
        Preconditions.checkNotNull(key);
        Object prevValue = artifacts.remove(key);

        if (prevValue != null) {
            for (RecipeArtifact<?> dep : recipes.keySet()) {
                if (dep instanceof RecipeArtifactD2) {
                    Pair dep2 = ((RecipeArtifactD2) dep)
                            .getDependentArtifacts();
                    if (dep2.getLeft().equals(key)
                            || dep2.getRight().equals(key)) {
                        unsetArtifact(dep);
                    }
                } else if (dep instanceof RecipeArtifactD1) {
                    if (((RecipeArtifactD1) dep).getDependentArtifact().equals(
                            key)) {
                        unsetArtifact(dep);
                    }
                }
            }
        }
    }

    public boolean hasArtifact(RecipeArtifact<?> key) {
        return artifacts.containsKey(key);
    }

    /**
     * Get with default mode - compute if necessary and unset results
     * @param key
     * @return
     */
    public <T> T getArtifact(RecipeArtifact<T> key) {
        return getArtifact(key, GetArtifactMode.ComputeAndUnsetResults);
    }
    
    /**
     * Get the artifact for the given key.
     * 
     * If the artifact is not available, but the is a recipe to compute it, and
     * computeIfAbsent is true, then the artifact and all intermediate artifacts
     * will be computed.
     * 
     * If the artifact is computed, and unsetResultsIfComputed is true, then all
     * the depending artifacts are unset. An artifact is (indirectly) depending
     * on this artifact key, if there is (indirectly) a function that requires
     * it as input.
     * 
     * @param key
     * @param computeIfAbsent
     * @param unsetResultsIfComputed
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getArtifact(RecipeArtifact<T> key, GetArtifactMode mode) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(mode);

        boolean computeIfAbsent = mode.computeIfAbsent();
        boolean unsetResultsIfComputed = mode.unsetResultsIfComputed();

        T artifact = (T) artifacts.get(key);

        // artifact not available and we want to compute it
        if (artifact == null && computeIfAbsent) {
            artifact = computeArtifact(key, unsetResultsIfComputed);
        }

        return artifact;
    }

    /**
     * (Re-)compute artifact for the given key.
     * 
     * @param key
     * @param unsetResults
     * @return
     */
    public <T> T computeArtifact(RecipeArtifact<T> key) {
    	return computeArtifact(key, true);
    }
    
    /**
     * (Re-)compute artifact for the given key.
     * 
     * @param key
     * @param unsetResults
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T computeArtifact(RecipeArtifact<T> key, boolean unsetResults) {
        Object input;
        Function<Object, Object> fnc = (Function<Object, Object>) recipes
                .get(key);
        if (fnc == null) {
            throw new IllegalStateException("No recipe for computing artifact");
        }

        GetArtifactMode getMode = GetArtifactMode.ComputeNoUnset;
        SetArtifactMode setMode = SetArtifactMode.SetOnly;
        if (unsetResults) {
            getMode = GetArtifactMode.ComputeAndUnsetResults;
            setMode = SetArtifactMode.UnsetResults;
        }

        // get input artifacts (compute if necessary)
        if (key instanceof RecipeArtifactD2) {
            Pair deps = ((RecipeArtifactD2) key).getDependentArtifacts();
            if (fnc instanceof SwitchArtifactRecipe) {
                // optimized calculation for switch recipe:
                // don't compute the input we won't use
                SwitchArtifactRecipe switchFnc = (SwitchArtifactRecipe) fnc;
                if (switchFnc.isUseFirst()) {
                    Object input1 = getArtifact((RecipeArtifact) deps.getLeft(),
                            getMode);
                    input = Pair.of(input1, null);
                } else {
                    Object input2 = getArtifact((RecipeArtifact) deps.getRight(),
                            getMode);
                    input = Pair.of(null, input2);
                }
            } else {
                // normal (non-switch) D2 artifact
                Object input1 = getArtifact((RecipeArtifact) deps.getLeft(),
                        getMode);
                Object input2 = getArtifact((RecipeArtifact) deps.getRight(),
                        getMode);
                input = Pair.of(input1, input2);
            }
            
        } else if (key instanceof RecipeArtifactD1) {
            input = getArtifact(
                    ((RecipeArtifactD1) key).getDependentArtifact(), getMode);
        } else {
            throw new IllegalStateException("Artifact cannot be computed");
        }

        // compute this artifact
        T artifact = (T) fnc.apply(input);
        setArtifact(key, artifact, setMode);

        return artifact;
    }
}
