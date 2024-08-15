package org.processmining.algorithms.statechart.discovery.subtrace;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  DiscEPTreeSubtraceNaiveTest.class,
  DiscEPTreeSubtraceRecursionTest.class,
  DiscEPTreeSubtraceNaiveCancellationTest.class
})
public class DiscEPTreeSubtraceTestSuite {

}
