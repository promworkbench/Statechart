package org.processmining.algorithms.statechart.m2m.ui;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.processmining.models.statechart.decorate.tracing.EdgeSemanticTracedAdapter;
import org.processmining.models.statechart.decorate.ui.dot.IDotDecorator;
import org.processmining.models.statechart.decorate.ui.dot.SCFrequencyDecorator;
import org.processmining.models.statechart.labeling.ActivityLabeler;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.ISCTransition;
import org.processmining.models.statechart.sc.Statechart;
import org.processmining.plugins.graphviz.dot.Dot;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.kitfox.svg.SVGDiagram;

public class Statechart2DotSvg implements Function<Statechart, SVGDiagram> {

    public static final double EdgeMinStroke = 1.0;
    public static final double EdgeMaxStroke = 3.0;

    public static final Function<ISCTransition, Pair<Set<ISCState>, Set<ISCState>>> FncEdge2Node 
        = new EdgeSemanticTracedAdapter<ISCTransition, ISCState>();
    /*
        = new Function<ISCTransition, Pair<List<ISCState>, List<ISCState>>>() {
        
        @Override
        public Pair<List<ISCState>, List<ISCState>> apply(ISCTransition edge) {
            List<ISCState> from = new ArrayList<>();
            List<ISCState> to = new ArrayList<>();

            Set<ISCState> visited = new THashSet<>();
            
            Deque<ISCState> queue = new ArrayDeque<ISCState>();
            queue.add(edge.getFrom());
            while (!queue.isEmpty()) {
                ISCState source = queue.removeLast();
                if (!visited.contains(source)) {
                    visited.add(source);
                    if (source.getStateType() == SCStateType.PointPseudo) {
                        queue.addAll(source.getPreset());
                    } else {
                        from.add(source);
                    }
                }
            }
            
            visited.clear();
            queue.add(edge.getTo());
            while (!queue.isEmpty()) {
                ISCState target = queue.removeLast();
                if (!visited.contains(target)) {
                    visited.add(target);
                    if (target.getStateType().isPseudostate()) {
                        queue.addAll(target.getPostset());
                    } else {
                        to.add(target);
                    }
                }
            }
            
            if (!from.isEmpty() && !to.isEmpty()) {
                return Pair.of(from, to);
            } else {
                return null;
            }
            
//            return Pair.of(Arrays.asList(edge.getFrom()), Arrays.asList(edge.getTo()));
        }
    };
    */
    private static final Logger logger = LogManager
            .getLogger(Statechart2DotSvg.class.getName());

    public Statechart2DotSvg() {

    }

    @Override
    public SVGDiagram apply(Statechart input) {
        return transform(input, Dot.GraphDirection.topDown, true);
    }

    public SVGDiagram transform(Statechart input, Dot.GraphDirection layoutDir,
            boolean recursionBackArrow) {
        return transform(input, layoutDir, Collections.<String> emptySet(),
                recursionBackArrow, ActivityLabeler.Classifier.getLabeler(),
                new SCFrequencyDecorator());
    }

//    public SVGDiagram transform(Statechart input, Dot.GraphDirection layoutDir,
//            boolean recursionBackArrow, 
//            IActivityLabeler activityLabeler) {
//        return transform(input, layoutDir, Collections.<String> emptySet(),
//                recursionBackArrow, activityLabeler);
//    }

    public SVGDiagram transform(Statechart input, Dot.GraphDirection layoutDir,
            Set<String> selectedIds, boolean recursionBackArrow, 
            IActivityLabeler activityLabeler,
            IDotDecorator<ISCState, ISCTransition, Statechart> dotDecorator) {
        Preconditions.checkNotNull(input);
        Statechart2Dot sc2dot = new Statechart2Dot(
                dotDecorator,
                layoutDir, selectedIds,
                recursionBackArrow, 
                activityLabeler);
        Dot dot = sc2dot.transform(input);

        if (logger.isDebugEnabled()) {
            logger.debug("\tDOT : " + dot.toString());
        }

        StatechartDot2Svg dot2svg = new StatechartDot2Svg(layoutDir);
        dot2svg.setDotTransformator(sc2dot);
        SVGDiagram svg = dot2svg.transform(dot);

        return svg;
    }

}
