package org.processmining.utils.statechart.svg;

import java.util.Map;
import java.util.Map.Entry;

import org.processmining.plugins.graphviz.dot.AbstractDotElement;

public class DotUtil {

    public static void setOptions(AbstractDotElement dotElement,
            Map<String, String> optionsMap) {
        if (optionsMap != null) {
            for (Entry<String, String> e : optionsMap.entrySet()) {
                dotElement.setOption(e.getKey(), e.getValue());
            }
        }
    }

}
