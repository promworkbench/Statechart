package org.processmining.visualizers.statechart;

import javax.swing.JComponent;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.processmining.algorithms.statechart.m2m.EPTree2StatechartStates;
import org.processmining.algorithms.statechart.m2m.ProcessTree2EPTree;
import org.processmining.algorithms.statechart.m2m.ui.Statechart2DotSvg;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.processtree.ProcessTree;

import com.google.common.base.Preconditions;
import com.kitfox.svg.SVGDiagram;

public class ProcessTree2StatechartView {

    private static final Logger logger = LogManager
            .getLogger(ProcessTree2StatechartView.class.getName());
    
    @Plugin(
            name = "View ProcessTree as Statechart - States", 
            returnLabels = { "Visualization of ProcessTree as Statechart - States" }, 
            returnTypes = { JComponent.class }, 
            parameterLabels = { "Process Tree" }, 
            userAccessible = true
    )
    @Visualizer
    public JComponent visProcessTreeStates(PluginContext context, ProcessTree tree) throws Exception {
        Preconditions.checkNotNull(tree);

        if (logger.isDebugEnabled()) {
            logger.debug("-- ProcessTree2StatechartView --");
            
            logger.debug("\tProcessTree: " + tree.toString());
            
            logger.debug("Transform ProcessTree 2 EPTree");
        }
        ProcessTree2EPTree pt2eptree = new ProcessTree2EPTree();
        IEPTree eptree = pt2eptree.transform(tree);
        if (logger.isDebugEnabled()) {
            logger.debug("\tEPTree: " + eptree.toString());
    
            logger.debug("Transform EPTree 2 Statechart - States");
        }
        EPTree2StatechartStates eptree2sc = new EPTree2StatechartStates();
        Statechart sc = eptree2sc.transform(eptree);
        if (logger.isDebugEnabled()) {
            logger.debug("\tStatechart: " + sc.toString());
            
            logger.debug("Transform Statechart 2 SVG");
        }
        Statechart2DotSvg sc2svg = new Statechart2DotSvg();
        SVGDiagram svg = sc2svg.transform(sc, GraphDirection.leftRight, true);
        
        NavigableSVGPanel graphPanel = new NavigableSVGPanel(svg);
        graphPanel.setFocusable(true);
        
        return graphPanel;
    }
}