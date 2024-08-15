package org.processmining.algorithms.statechart.m2m.ui;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.statechart.decorate.tracing.IEdgeSemanticTraced;
import org.processmining.models.statechart.decorate.ui.dot.IDotDecorator;
import org.processmining.models.statechart.decorate.ui.dot.NullDotDecorator;
import org.processmining.models.statechart.decorate.ui.dot.PTnetMetricDecorator;
import org.processmining.models.statechart.labeling.ActivityLabeler;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.plugins.graphviz.dot.Dot;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.kitfox.svg.SVGDiagram;

public class Petrinet2DotSvg implements Function<PetrinetDecorated, SVGDiagram> {

    public static final double EdgeMinStroke = 1.0;
    public static final double EdgeMaxStroke = 3.0;

    public static final PTnetMetricDecorator.ModelAwareEdge2Node FncEdge2Node
        = new PTnetMetricDecorator.ModelAwareEdge2Node() {
            private PetrinetDecorated model;

            @Override
            public void visitModel(PetrinetDecorated model) {
                this.model = model;
            }
            
            @Override
            public Pair<Set<Transition>, Set<Transition>> apply(Arc arc) {
                IEdgeSemanticTraced<Transition> sem = model.getEdgeSemantics(arc);
                if (sem != null) {
                    return sem.getEdgeSemantics();
                }
                return null;
            }
    };
//        = new EdgeSemanticTracedAdapter<Arc, Transition>();
    /*
        = new Function<Arc, Pair<Set<Transition>, Set<Transition>>>() {
        @Override
        public Pair<Set<Transition>, Set<Transition>> apply(Arc arc) {
            Set<Transition> from = new THashSet<>();
            Set<Transition> to = new THashSet<>();

            PetrinetNode source = arc.getSource();
            if (source != null) {
                if (source instanceof Place) {
                    for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : source
                            .getGraph().getInEdges(source)) {
                        if (edge instanceof Arc) {
                            from.add((Transition) edge.getSource());
                        }
                    }
                } else {
                    from.add((Transition) source);
                }
            }

            PetrinetNode target = arc.getTarget();
            if (target != null) {
                if (target instanceof Place) {
                    for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : target
                            .getGraph().getOutEdges(target)) {
                        if (edge instanceof Arc) {
                            to.add((Transition) edge.getTarget());
                        }
                    }
                } else {
                    to.add((Transition) target);
                }
            }
            
            if (!from.isEmpty() && !to.isEmpty()) {
                return Pair.of(from, to);
            } else {
                return null;
            }
            
//            PetrinetNode target = arc.getTarget();
//            if (target != null && target instanceof Transition) {
//                return (Transition) target;
//            } else {
//                return null;
//            }
            
//            AbstractDirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> 
//                graph = place.getGraph();
//            Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> 
//                edges = graph.getOutEdges(place);
//            if (edges.isEmpty()) {
//                return null;
//            } else {
//                PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge = edges.iterator().next();
//                return (Transition) edge.getTarget();
//            }
        }
    };
    */
    private static final Logger logger = LogManager
            .getLogger(Petrinet2DotSvg.class.getName());

    public Petrinet2DotSvg() {

    }

    @Override
    public SVGDiagram apply(PetrinetDecorated input) {
        return transform(input, Dot.GraphDirection.topDown);
    }

    public SVGDiagram transform(PetrinetDecorated input, Dot.GraphDirection layoutDir) {
        return transform(input, layoutDir, Collections.<String> emptySet(),
                ActivityLabeler.Classifier.getLabeler(),
                new NullDotDecorator<Transition, Arc, PetrinetDecorated>());
    }

    public SVGDiagram transform(PetrinetDecorated input, Dot.GraphDirection layoutDir,
            Set<String> selectedIds,
            IActivityLabeler activityLabeler,
            IDotDecorator<Transition, Arc, PetrinetDecorated> dotDecorator) {
        Preconditions.checkNotNull(input);

        Petrinet2Dot ptnet2dot = new Petrinet2Dot(
            layoutDir, selectedIds, 
            activityLabeler, dotDecorator);
        Dot dot = ptnet2dot.transform(input);

        if (logger.isDebugEnabled()) {
            logger.debug("\tDOT : " + dot.toString());
        }

        PetrinetDot2Svg dot2svg = new PetrinetDot2Svg(layoutDir);
        dot2svg.setDotTransformator(ptnet2dot);
        SVGDiagram svg = dot2svg.transform(dot);

        return svg;
    }

}
