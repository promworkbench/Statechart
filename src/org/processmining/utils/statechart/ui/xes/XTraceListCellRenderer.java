package org.processmining.utils.statechart.ui.xes;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;
import org.processmining.utils.statechart.ui.LabelledListCellRenderer;

import com.google.common.base.Function;

public class XTraceListCellRenderer extends LabelledListCellRenderer<XTrace> {

    private static final long serialVersionUID = 7765981604962745712L;

    public static final int MaxInstNameLength = 30;

    private static final XConceptExtension concept = XConceptExtension.instance();
    
    public XTraceListCellRenderer() {
        this(MaxInstNameLength);
    }
    
    public XTraceListCellRenderer(final int maxInstNameLength) {
        super(new Function<XTrace, String>() {
            @Override
            public String apply(XTrace trace) {
                String name = concept.extractName(trace);
                if (name == null || name.length() == 0) {
                        return "<no name>";
                }
                if (name.length() > maxInstNameLength) {
                    name = name.substring(0, maxInstNameLength) + "\u2026";
                }
                return name;
            }
        });
    }
}
