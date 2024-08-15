package org.processmining.recipes.statechart.m2m.ui;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.algorithms.statechart.layout.Direction;
import org.processmining.algorithms.statechart.layout.ProcessGraphLayout;
import org.processmining.algorithms.statechart.m2m.ui.Statechart2Svg;
import org.processmining.algorithms.statechart.m2m.ui.layout.StatechartLayout;
import org.processmining.algorithms.statechart.m2m.ui.layout.StatechartLayoutNode;
import org.processmining.algorithms.statechart.m2m.ui.style.StatechartStyle;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.labeling.ActivityLabeler;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.recipes.statechart.AbstractRecipe;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

public class Statechart2SVGRecipe extends
    AbstractRecipe<Pair<IEPTree, Statechart>, SVGDiagram, Statechart2SVGRecipe.Parameters> {

    public static Direction map(GraphDirection t) {
        switch (t) {
        case bottomTop:
            return Direction.BottomTop;
        case leftRight:
            return Direction.LeftRight;
        case rightLeft:
            return Direction.RightLeft;
        case topDown:
        default:
            return Direction.TopDown;
        }
    }
    
    public static class Parameters {
        public Direction layoutDir;
        public Set<String> selectedNodes = Collections.emptySet();

        public boolean recursionBackArrow;
        
        public ActivityLabeler activityLabeler;
        
        public Parameters() {
            layoutDir = Direction.TopDown;
            recursionBackArrow = true;
            activityLabeler = ActivityLabeler.Classifier;
        }
    }

    public Statechart2SVGRecipe() {
        super(new Parameters());
    }


    @Override
    protected SVGDiagram execute(Pair<IEPTree, Statechart> input) {
        Parameters params = getParameters();
        IActivityLabeler labeler = params.activityLabeler.getLabeler();

        StatechartStyle style = new StatechartStyle(input.getRight(), params.layoutDir, labeler);
        
        StatechartLayout layoutEngine = new StatechartLayout(style);
        ProcessGraphLayout<StatechartLayoutNode> layout = layoutEngine.calculate(input.getLeft(), input.getRight());

        try {
            Statechart2Svg modelTransform = new Statechart2Svg(style);
            SVGDiagram svg = modelTransform.transform(input.getRight(), layout);
            return svg;
        } catch (SVGException e) {
            e.printStackTrace();
            return null;
        }
    }
}
