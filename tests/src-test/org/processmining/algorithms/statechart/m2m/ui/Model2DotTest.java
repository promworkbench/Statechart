package org.processmining.algorithms.statechart.m2m.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.Test;
import org.processmining.algorithms.statechart.m2m.EPTree2StatechartStates;
import org.processmining.algorithms.statechart.m2m.ui.Statechart2Dot;
import org.processmining.models.statechart.decorate.ui.dot.NullDotDecorator;
import org.processmining.models.statechart.eptree.EPTreeCreateUtil;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.labeling.ActivityLabeler;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.ISCTransition;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.Dot2Image;

public class Model2DotTest {

    @Test
    public void testSimpleNestingSeq() {
        IEPTree input = EPTreeCreateUtil.create("->(A, \\/=B(x(->(X, R\\/=B), Y)), C)");

        renderToFile("testSimpleNestingSeq.png", input);
    }

    @Test
    public void testDualRecursionTwice() {
        IEPTree input = EPTreeCreateUtil.create("->(Pre, \\/=F(x(->(S1, \\/=G(x(->(S1, R\\/=F, S2), B)), S2), B)), Post)");

        renderToFile("testDualRecursionTwice.png", input);
    }

    @Test
    public void testActConcurrent() {
        IEPTree input = EPTreeCreateUtil.create("/\\(C, /\\(A, B))");
        
        renderToFile("testActConcurrent.png", input);
    }

    @Test
    public void testLoop() {
        IEPTree input = EPTreeCreateUtil
                .create("->(<->(->(A, B), C), D)");
        
        renderToFile("testLoop.png", input);
    }

    @Test
    public void testSimpleTrycatch() {
        IEPTree input = EPTreeCreateUtil
                .create("->(A, TC(->(B, C), ->(Error, E2)), D)");
        
        renderToFile("testSimpleTrycatch.png", input);
    }
    
    private Path getOutputDirectory() {
        return new File("tests/src-test/out/Model2DotTest/").getAbsoluteFile().toPath();
    }
    
    private void renderToFile(String filename, IEPTree input) {
        EPTree2StatechartStates convertSC = new EPTree2StatechartStates();
        Statechart modelSC = convertSC.transform(input);
        
        Statechart2Dot convertDot = new Statechart2Dot(
            new NullDotDecorator<ISCState, ISCTransition, Statechart>(), 
            GraphDirection.leftRight, 
            Collections.<String>emptySet(), 
            true, 
            ActivityLabeler.Classifier.getLabeler()
        );
        Dot dot = convertDot.transform(modelSC);
        
        Dot2Image.dot2image(
            dot, 
            getOutputDirectory().resolve(filename).toFile(), 
            Dot2Image.Type.png
        );
    }
}
