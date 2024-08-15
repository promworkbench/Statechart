package org.processmining.models.statechart.decorate.swapp;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.models.statechart.im.log.IMLogHierarchyListImpl;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.xes.statechart.XUtil;
import org.processmining.xes.statechart.classification.XEventListLabelElementClassifier;
import org.processmining.xes.statechart.extension.XListLabelExtension;
import org.processmining.xes.statechart.xport.XApplocExtension;

import com.google.common.base.Optional;

public class SwAppDecoration {

    private static final XListLabelExtension extListLabel = XListLabelExtension.instance();
    
    private String joinpoint;
    private String filename;
    private int linenr;
    
    public static Optional<SwAppDecoration> deriveInstance(IMLogHierarchy hlog, IMTrace trace, XEvent event) {
        if (hlog instanceof IMLogHierarchyListImpl) {
            IMLogHierarchyListImpl hlogList = (IMLogHierarchyListImpl) hlog;

            XEventListLabelElementClassifier c = hlogList.resolveClassifier(trace);
            int level = c.getLevel();
            
            Optional<XAttributeMap> dataMap = extListLabel.getDataMap(event, level);
            if (dataMap.isPresent()) {
                return _init(dataMap.get());
            } else {
                return Optional.absent();
            }
        } else {
            return _init(event.getAttributes());
        }
    }

    private static Optional<SwAppDecoration> _init(XAttributeMap map) {
        if (!XUtil.hasAttribs(map, XApplocExtension.KEY_JOINPOINT, 
                XApplocExtension.KEY_FILENAME, XApplocExtension.KEY_LINENR)) {
            return Optional.absent();
        }
        
        String joinpoint = XUtil.extractLiteral(map, XApplocExtension.KEY_JOINPOINT);
        String filename = XUtil.extractLiteral(map, XApplocExtension.KEY_FILENAME);
        int linenr = XUtil.extractInt(map, XApplocExtension.KEY_LINENR);
        
        if (joinpoint == null || filename == null || linenr == Integer.MIN_VALUE) {
            return Optional.absent();
        }
        
        return Optional.of(new SwAppDecoration(joinpoint, filename, linenr));
    }

    public SwAppDecoration(SwAppDecoration old) {
        joinpoint = old.getJoinpoint();
        filename = old.getFilename();
        linenr = old.getLinenr();
    }

    public SwAppDecoration() {
        joinpoint = "?";
        filename = "?";
        linenr = 0;
    }

    public SwAppDecoration(String joinpoint, String filename, int linenr) {
        this.joinpoint = joinpoint;
        this.filename = filename;
        this.linenr = linenr;
    }
    
    public String getJoinpoint() {
        return joinpoint;
    }

    public String getFilename() {
        return filename;
    }

    public int getLinenr() {
        return linenr;
    }

}
