package org.processmining.recipes.statechart.m2m.ui;

import java.util.Collections;
import java.util.Set;

import org.abego.treelayout.Configuration;
import org.abego.treelayout.Configuration.Location;
import org.abego.treelayout.TreeLayout;
import org.processmining.algorithms.statechart.m2m.ui.EPTree2Svg;
import org.processmining.algorithms.statechart.m2m.ui.decorate.AlignMetricUiDecorator;
import org.processmining.algorithms.statechart.m2m.ui.decorate.IUiDecorator;
import org.processmining.algorithms.statechart.m2m.ui.layout.EPTreeLayout;
import org.processmining.algorithms.statechart.m2m.ui.style.EPTreeStyle;
import org.processmining.models.statechart.decorate.align.metric.MetricValueScale.StatMode;
import org.processmining.models.statechart.decorate.ui.svg.ISvgDecorator;
import org.processmining.models.statechart.decorate.ui.svg.NullSvgDecorator;
import org.processmining.models.statechart.decorate.ui.svg.SvgMetricDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.labeling.ActivityLabeler;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.recipes.statechart.AbstractRecipe;
import org.processmining.recipes.statechart.align.AnalysisAlgorithm;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlay;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

public class EPtree2SVGRecipe extends 
    AbstractRecipe<IEPTree, SVGDiagram, EPtree2SVGRecipe.Parameters> {

    public static Configuration.Location map(Dot.GraphDirection dir) {
        switch (dir) {
        case bottomTop:
            return Location.Bottom;
        case leftRight:
            return Location.Left;
        case rightLeft:
            return Location.Right;
        case topDown:
        default:
            return Location.Top;
        }
    }
    
    public static class Parameters {
        public Configuration.Location layoutDir;
        
        public ActivityLabeler activityLabeler;

        public Set<String> selectedNodes = Collections.emptySet();
        public ISvgDecorator<IEPTreeNode, IEPTreeNode, IEPTree> svgDecorator;
        
        public Parameters() {
            layoutDir = Configuration.Location.Top;
            activityLabeler = ActivityLabeler.Classifier;
            
            svgDecorator = new NullSvgDecorator<IEPTreeNode, IEPTreeNode, IEPTree>();
        }

        public void setupDecorator(
                AnalysisAlgorithm selectedAlignAlg,
                AnalysisAlignMetricOverlay selectedAlignMetric, StatMode statMode) {
            if (selectedAlignAlg == AnalysisAlgorithm.Approx) {
                svgDecorator = new NullSvgDecorator<IEPTreeNode, IEPTreeNode, IEPTree>();
            } else {
                svgDecorator = createAlignMetricUiDecorator(selectedAlignMetric, statMode);
            }
        }

        public static ISvgDecorator<IEPTreeNode, IEPTreeNode, IEPTree> createAlignMetricUiDecorator(
                AnalysisAlignMetricOverlay selectedAlignMetric, StatMode statMode) {
            IUiDecorator<IEPTreeNode, IEPTreeNode> uiDecorator = new AlignMetricUiDecorator<>(
                selectedAlignMetric.getNodeMetrics(),
                selectedAlignMetric.getNodeColorMapProvider(),
                selectedAlignMetric.getEdgeMetrics(),
                selectedAlignMetric.getEdgeColorMapProvider(),
                statMode,
                EPTree2Svg.EdgeMinStroke, EPTree2Svg.EdgeMaxStroke,
                EPTree2Svg.FncEdge2Node
            );
            SvgMetricDecorator<IEPTreeNode, IEPTreeNode, IEPTree> svgDec = new SvgMetricDecorator<>(uiDecorator);
            svgDec.setExtendLabel(false); // extended labels and a-priori TreeLayout don't mix well
            return svgDec;
        }
    }

    public EPtree2SVGRecipe() {
        super(new Parameters());
    }

    @Override
    protected SVGDiagram execute(IEPTree input) {
        Parameters params = getParameters();
        IActivityLabeler labeler = params.activityLabeler.getLabeler();
        
        EPTreeStyle style = new EPTreeStyle(params.layoutDir, labeler);
        
        EPTreeLayout layoutEngine = new EPTreeLayout(style);
        TreeLayout<IEPTreeNode> layout = layoutEngine.calculate(input);

        try {
            EPTree2Svg modelTransform = new EPTree2Svg(style);
            SVGDiagram svg = modelTransform.transform(input, layout, 
                    params.selectedNodes,
                    params.svgDecorator);
            return svg;
        } catch (SVGException e) {
            e.printStackTrace();
            return null;
        }
    }
}
