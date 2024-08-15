package org.processmining.recipes.statechart;

import static org.junit.Assert.*;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.processmining.recipes.statechart.RecipeProcess;
import org.processmining.recipes.statechart.RecipeProcess.GetArtifactMode;
import org.processmining.recipes.statechart.RecipeProcess.SetArtifactMode;

import com.google.common.base.Function;

public class RecipeProcessTest {

    private static class DAG {
        public static final RecipeArtifact<Integer> A = new RecipeArtifact<>();
        public static final RecipeArtifactD1<String, Integer> B = new RecipeArtifactD1<>(
                A);
        public static final RecipeArtifactD1<String, Integer> C = new RecipeArtifactD1<>(
                A);
        public static final RecipeArtifactD2<String, String, String> D = new RecipeArtifactD2<>(
                B, C);
    }

    private RecipeProcess _createProcess() {
        RecipeProcess p = new RecipeProcess();
        
        p.setRecipe(DAG.B, new Function<Integer, String>() {
            @Override
            public String apply(Integer input) {
                return input.toString();
            }
            
        });
        
        p.setRecipe(DAG.C, new Function<Integer, String>() {
            @Override
            public String apply(Integer input) {
                return Integer.toString(input + 1);
            }
            
        });
        
        p.setRecipe(DAG.D, new Function<Pair<String, String>, String>() {
            @Override
            public String apply(Pair<String, String> input) {
                return input.getLeft() + " - " + input.getRight();
            }
            
        });
        
        return p;
    }

    @Test
    public void testGetCalculateStep() {
        RecipeProcess p = _createProcess();
        p.setArtifact(DAG.A, new Integer(1));
        
        String B = p.getArtifact(DAG.B);
        assertEquals("1", B);

        String C = p.getArtifact(DAG.C);
        assertEquals("2", C);
    }
    
    @Test
    public void testGetCalculateChain() {
        RecipeProcess p = _createProcess();
        p.setArtifact(DAG.A, new Integer(1));

        String D = p.getArtifact(DAG.D);
        assertEquals("1 - 2", D);
    }
    
    @Test
    public void testGetStep() {
        RecipeProcess p = _createProcess();
        p.setArtifact(DAG.A, new Integer(1));
        
        String B = p.getArtifact(DAG.B, GetArtifactMode.GetOnly);
        assertEquals(null, B);

        String C = p.getArtifact(DAG.C, GetArtifactMode.GetOnly);
        assertEquals(null, C);
    }
    
    @Test
    public void testGetCalculateStepReset() {
        RecipeProcess p = _createProcess();
        p.setArtifact(DAG.A, new Integer(1));
        
        String B = p.getArtifact(DAG.B);
        assertEquals("1", B);

        String C = p.getArtifact(DAG.C);
        assertEquals("2", C);

        assertTrue(p.hasArtifact(DAG.B));
        assertTrue(p.hasArtifact(DAG.C));
        
        // reset
        p.setArtifact(DAG.A, new Integer(2));

        assertFalse(p.hasArtifact(DAG.B));
        assertFalse(p.hasArtifact(DAG.C));
        
        B = p.getArtifact(DAG.B);
        assertEquals("2", B);

        C = p.getArtifact(DAG.C);
        assertEquals("3", C);
    }

    @Test
    public void testGetStepReset() {
        RecipeProcess p = _createProcess();
        p.setArtifact(DAG.A, new Integer(1));
        
        String B = p.getArtifact(DAG.B);
        assertEquals("1", B);

        String C = p.getArtifact(DAG.C);
        assertEquals("2", C);

        assertTrue(p.hasArtifact(DAG.B));
        assertTrue(p.hasArtifact(DAG.C));
        
        // reset
        p.setArtifact(DAG.A, new Integer(2));

        assertFalse(p.hasArtifact(DAG.B));
        assertFalse(p.hasArtifact(DAG.C));
        
        B = p.getArtifact(DAG.B, GetArtifactMode.GetOnly);
        assertEquals(null, B);

        C = p.getArtifact(DAG.C, GetArtifactMode.GetOnly);
        assertEquals(null, C);
    }

    @Test
    public void testGetStepResetNoUnset() {
        RecipeProcess p = _createProcess();
        p.setArtifact(DAG.A, new Integer(1));
        
        String B = p.getArtifact(DAG.B);
        assertEquals("1", B);

        String C = p.getArtifact(DAG.C);
        assertEquals("2", C);

        assertTrue(p.hasArtifact(DAG.B));
        assertTrue(p.hasArtifact(DAG.C));
        
        // reset
        p.setArtifact(DAG.A, new Integer(2), SetArtifactMode.SetOnly);

        assertTrue(p.hasArtifact(DAG.B));
        assertTrue(p.hasArtifact(DAG.C));
        
        B = p.getArtifact(DAG.B);
        assertEquals("1", B);

        C = p.getArtifact(DAG.C);
        assertEquals("2", C);
    }

    @Test
    public void testGetCalculateChainReset() {
        RecipeProcess p = _createProcess();
        p.setArtifact(DAG.A, new Integer(1));

        String D = p.getArtifact(DAG.D);
        assertEquals("1 - 2", D);

        assertTrue(p.hasArtifact(DAG.B));
        assertTrue(p.hasArtifact(DAG.C));
        assertTrue(p.hasArtifact(DAG.D));
        
        // reset
        p.setArtifact(DAG.A, new Integer(2), SetArtifactMode.UnsetResults);

        assertFalse(p.hasArtifact(DAG.B));
        assertFalse(p.hasArtifact(DAG.C));
        assertFalse(p.hasArtifact(DAG.D));
        
        D = p.getArtifact(DAG.D);
        assertEquals("2 - 3", D);
    }

    @Test
    public void testGetChainReset() {
        RecipeProcess p = _createProcess();
        p.setArtifact(DAG.A, new Integer(1));

        String D = p.getArtifact(DAG.D);
        assertEquals("1 - 2", D);

        assertTrue(p.hasArtifact(DAG.B));
        assertTrue(p.hasArtifact(DAG.C));
        assertTrue(p.hasArtifact(DAG.D));
        
        // reset
        p.setArtifact(DAG.A, new Integer(2), SetArtifactMode.UnsetResults);

        assertFalse(p.hasArtifact(DAG.B));
        assertFalse(p.hasArtifact(DAG.C));
        assertFalse(p.hasArtifact(DAG.D));
        
        D = p.getArtifact(DAG.D, GetArtifactMode.GetOnly);
        assertEquals(null, D);
    }

    @Test
    public void testGetChainResetNoUnset() {
        RecipeProcess p = _createProcess();
        p.setArtifact(DAG.A, new Integer(1));

        String D = p.getArtifact(DAG.D);
        assertEquals("1 - 2", D);

        assertTrue(p.hasArtifact(DAG.B));
        assertTrue(p.hasArtifact(DAG.C));
        assertTrue(p.hasArtifact(DAG.D));
        
        // reset
        p.setArtifact(DAG.A, new Integer(2), SetArtifactMode.SetOnly);

        assertTrue(p.hasArtifact(DAG.B));
        assertTrue(p.hasArtifact(DAG.C));
        assertTrue(p.hasArtifact(DAG.D));
        
        D = p.getArtifact(DAG.D);
        assertEquals("1 - 2", D);
    }

    @Test
    public void testGetCalculateStepNewFnc() {
        RecipeProcess p = _createProcess();
        p.setArtifact(DAG.A, new Integer(1));
        
        String B = p.getArtifact(DAG.B);
        assertEquals("1", B);

        String C = p.getArtifact(DAG.C);
        assertEquals("2", C);

        assertTrue(p.hasArtifact(DAG.B));
        assertTrue(p.hasArtifact(DAG.C));
        
        // new fnc
        p.setRecipe(DAG.C, new Function<Integer, String>() {
            @Override
            public String apply(Integer input) {
                return Integer.toString(input + 10);
            }
            
        });

        assertTrue(p.hasArtifact(DAG.B));
        assertFalse(p.hasArtifact(DAG.C));
        
        B = p.getArtifact(DAG.B);
        assertEquals("1", B);

        C = p.getArtifact(DAG.C);
        assertEquals("11", C);
    }

    @Test
    public void testGetCalculateChainNewFnc() {
        RecipeProcess p = _createProcess();
        p.setArtifact(DAG.A, new Integer(1));
        
        String D = p.getArtifact(DAG.D);
        assertEquals("1 - 2", D);

        assertTrue(p.hasArtifact(DAG.B));
        assertTrue(p.hasArtifact(DAG.C));
        assertTrue(p.hasArtifact(DAG.D));
        
        // new fnc
        p.setRecipe(DAG.C, new Function<Integer, String>() {
            @Override
            public String apply(Integer input) {
                return Integer.toString(input + 10);
            }
            
        });

        assertTrue(p.hasArtifact(DAG.B));
        assertFalse(p.hasArtifact(DAG.C));
        assertFalse(p.hasArtifact(DAG.D));
        
        D = p.getArtifact(DAG.D);
        assertEquals("1 - 11", D);
    }
}
