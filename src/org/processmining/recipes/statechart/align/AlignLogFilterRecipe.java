package org.processmining.recipes.statechart.align;

import java.util.Iterator;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.statechart.align.XAlignedTreeLog;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.recipes.statechart.AbstractRecipe;
import org.processmining.xesalignmentextension.XAlignmentExtension;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public class AlignLogFilterRecipe extends AbstractRecipe<XAlignedTreeLog, XAlignedTreeLog, AlignLogFilterRecipe.Parameters> {

    public static final boolean FilterDemo = false;
    
    public static class Parameters {
        public Set<String> selectedNodes;
    }

    public AlignLogFilterRecipe() {
        super(new Parameters());
    }

    @Override
    protected XAlignedTreeLog execute(XAlignedTreeLog input) {
        Parameters params = getParameters();
        XAlignmentExtension alignInst = XAlignmentExtension.instance();

        XAlignedTreeLog result = input;
        if (AlignLogFilterRecipe.FilterDemo) {
            // Prototpye filtering alg:
            if (params.selectedNodes != null && !params.selectedNodes.isEmpty()) {
                result = input.copy();
                Iterator<XTrace> itTraces = result.getLog().iterator();
                while (itTraces.hasNext()) {
                    XTrace trace = itTraces.next();
                    boolean match = false;
                    for (XEvent event : trace) {
                        XAlignmentMove move = alignInst.extendEvent(event);
                        IEPTreeNode node = result.getNode(move);
                        if (node != null && params.selectedNodes.contains(node.getId())) {
                            match = true;
                        }
                    }
                    if (!match) {
                        itTraces.remove();
                    }
                }
            }
        }
        
        return result;
    }
}
