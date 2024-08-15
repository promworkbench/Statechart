package org.processmining.algorithms.statechart.l2l.list;

import org.deckfour.xes.model.XLog;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.algorithms.statechart.l2l.L2LStructuredActivity;
import org.processmining.algorithms.statechart.l2l.LogCreateTestUtil;
import org.processmining.xes.statechart.XesCompareSame;

public class L2LListStructuredActivityTest {

    @Test
    public void testPackageNormal1() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
                {"org.apache.maven.plugin.descriptor.PluginDescriptor.setName"} 
        });
        XLog expected = LogCreateTestUtil.createLogList(new String[][][] {
                { {"org", "apache", "maven", "plugin", "descriptor", 
                    "PluginDescriptor", "setName"} }
        });
        
        L2LListStructuredActivity transform = new L2LListStructuredActivity(new L2LStructuredActivity.Parameters());
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
    
    @Test
    public void testPackageMethodNormal1() {
        XLog input = LogCreateTestUtil.createLogFlat(new String[][] { 
                {"org.apache.maven.plugin.descriptor.PluginDescriptor."
                        + "setName(java.lang.String)"} 
        });
        XLog expected = LogCreateTestUtil.createLogList(new String[][][] {
                { {"org", "apache", "maven", "plugin", "descriptor", 
                    "PluginDescriptor", "setName(java.lang.String)"} }
        });
        
        L2LListStructuredActivity transform = new L2LListStructuredActivity(new L2LStructuredActivity.Parameters());
        XLog actual = transform.apply(input);
        
        Assert.assertTrue(XesCompareSame.same(expected, actual));
    }
}
