package org.processmining.models.statechart.eptree;

import java.util.ArrayList;
import java.util.List;

import org.processmining.models.statechart.labeling.IActivityLabeler;

public class EPTreeUtil {
    
    private EPTreeUtil() {
        
    }

    public static List<String> getHierarchyPath(IEPTreeNode node, IActivityLabeler dataActivityLabeler) {
        List<String> result = new ArrayList<String>();
        getHierarchyPath(node, dataActivityLabeler, result);
        return result;
    }

    public static void getHierarchyPath(IEPTreeNode node, IActivityLabeler dataActivityLabeler, List<String> result) {
        if (node.getParent() != null) {
            getHierarchyPath(node.getParent(), dataActivityLabeler, result);
        }
        EPNodeType nodeType = node.getNodeType();
        if (nodeType.isLabelled()) {
            result.add(dataActivityLabeler.getLabel(node));
        }
    }
}
