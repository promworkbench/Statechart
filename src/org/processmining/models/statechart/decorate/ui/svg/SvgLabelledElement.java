package org.processmining.models.statechart.decorate.ui.svg;

import com.kitfox.svg.SVGElement;
import com.kitfox.svg.Text;

public class SvgLabelledElement {

    public final SVGElement element;
    public final Text label;
    
    public SvgLabelledElement(SVGElement element, Text label) {
        this.element = element;
        this.label = label;
    }
}
