package org.processmining.tests.statechart;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.processmining.algorithms.statechart.align.AlignTestSuite;
import org.processmining.algorithms.statechart.discovery.AccessorTest;
import org.processmining.algorithms.statechart.discovery.EPTreeCompareSameTest;
import org.processmining.algorithms.statechart.discovery.list.DiscEPTreeListTestSuite;
import org.processmining.algorithms.statechart.discovery.subtrace.DiscEPTreeSubtraceTestSuite;
import org.processmining.algorithms.statechart.l2l.L2LTestSuite;
import org.processmining.algorithms.statechart.m2m.reduct.eptree.ReductTreeTest;
import org.processmining.models.statechart.eptree.EPTreeSemanticsTest;
import org.processmining.recipes.statechart.RecipeProcessTest;
import org.processmining.ui.statechart.workbench.integration.WorkbenchAlignIntegrationTest;
import org.processmining.utils.statechart.generic.SetUtilTest;
import org.processmining.utils.statechart.petrinet.PetrinetMarkingIteratorTest;
import org.processmining.utils.statechart.petrinet.PetrinetPlaceIteratorTest;
import org.processmining.utils.statechart.software.JoinpointStructureTest;
import org.processmining.utils.statechart.tree.TreeIterationTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    // Utils
    ReductTreeTest.class,
    RecipeProcessTest.class,
    TreeIterationTest.class,
    EPTreeCompareSameTest.class,
    AccessorTest.class,
    JoinpointStructureTest.class,
    SetUtilTest.class,
    EPTreeSemanticsTest.class,
    // Model utils
    PetrinetMarkingIteratorTest.class,
    PetrinetPlaceIteratorTest.class,
    // Log 2 Hierarchy
    L2LTestSuite.class,
    // Discovery
    DiscEPTreeListTestSuite.class,
    DiscEPTreeSubtraceTestSuite.class,
    // Align
    AlignTestSuite.class,
    // Integration
    WorkbenchAlignIntegrationTest.class
})
public class TestSuite {

}
