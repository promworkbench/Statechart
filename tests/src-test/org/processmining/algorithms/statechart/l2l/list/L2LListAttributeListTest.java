package org.processmining.algorithms.statechart.l2l.list;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.l2l.L2LAttributeList;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.xes.statechart.XesCompareSame;

public class L2LListAttributeListTest {

    @Test
    public void testFlatClassifier() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
            {"A", "B"} 
        });
        XLog expected = LogCreateTestUtil.createLogList(new String[][][] {
            { {"A"}, {"B"} }
        });
        
        L2LAttributeList.Parameters params = new L2LAttributeList.Parameters();
        params.clsList.add(new XEventNameClassifier());
        L2LAttributeList transform = new L2LListAttributeList(params);
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
}
