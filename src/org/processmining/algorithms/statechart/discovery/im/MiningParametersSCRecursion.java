package org.processmining.algorithms.statechart.discovery.im;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.algorithms.statechart.discovery.ContextPath;
import org.processmining.algorithms.statechart.discovery.im.basecase.FinderSCCompositeOrRecursion;
import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.models.statechart.im.log.IMLogHierarchy;
import org.processmining.processtree.Block;

public class MiningParametersSCRecursion extends MiningParametersSC {

    private final Map<ContextPath, IMLogHierarchy> discoveryLogs = new THashMap<>();
    private final Map<ContextPath, List<Block>> discoveryTargets = new THashMap<>();
    private final Set<ContextPath> discoveryHorizon = new THashSet<>();
    private ContextPath currentContext;

    public MiningParametersSCRecursion(boolean useLifecycle, boolean useCancelation, IQueryCancelError queryCatchError) {
        super(new FinderSCCompositeOrRecursion(), useLifecycle, useCancelation, queryCatchError);
        
        currentContext = new ContextPath();
    }
    
    /*
    public MiningParametersSCRecursion() {
        super();
        
        setBaseCaseFinders(new ArrayList<BaseCaseFinder>(Arrays.asList(
            new BaseCaseFinderIMiEmptyLog(),
            new BaseCaseFinderIMiEmptyTrace(),
            new FinderSCCompositeOrRecursion(),
            new BaseCaseFinderIMi(),
            new BaseCaseFinderIM()
        )));
        
        currentContext = new ContextPath();
    }
    //*/

    /**
     * Used in FinderSCCompositeOrRecursion() for adding new parts to the discovery horizon
     * @param contextLabel
     * @param sublog
     * @param newClassifierLevel
     * @param targetNode
     */
    public void recordForDiscovery(ContextPath contextPath, IMLogHierarchy sublog, Block targetNode) {
        boolean changed = false;
        IMLogHierarchy dlog = discoveryLogs.get(contextPath);
        if (dlog == null) {
            dlog = sublog;
            discoveryLogs.put(contextPath, dlog);
            changed = true;
        } else {
            changed = dlog.addLog(sublog);
        }
        // Was needed in old implementation, now target nodes are looked
        // up after all the discovery is done (horizon is empty)
        //if (changed || targetNode != null) {
        if (changed) {
            discoveryHorizon.add(contextPath);
        }
        
        if (targetNode != null) {
            List<Block> dtargets = discoveryTargets.get(contextPath);
            if (dtargets == null) {
                dtargets = new ArrayList<>();
                discoveryTargets.put(contextPath, dtargets);
            }
            dtargets.add(targetNode);
        }
    }
    
    /**
     * Get the sublog associated with the given context label
     * @param contextLabel
     * @return
     */
    public IMLogHierarchy getContextSublog(ContextPath contextPath) {
        return discoveryLogs.get(contextPath);
    }

    /**
     * Get the target tree blocks associated with the given context label
     * @param contextLabel
     * @return
     */
    public List<Block> getContextTargets(ContextPath contextPath) {
        List<Block> dtargets = discoveryTargets.get(contextPath);
        if (dtargets == null) {
            return Collections.emptyList();
        }
        return dtargets;
    }
    
    public Map<ContextPath, List<Block>> getAllTargets() {
        return discoveryTargets;
    }
    
    /**
     * Get and remove a context from the discovery horizon
     * Returns null if the horizon is empty (i.e., we're done)
     * @return
     */
    public ContextPath popContextFromHorizon() {
        if (!discoveryHorizon.isEmpty()) {
            ContextPath context = discoveryHorizon.iterator().next();
            discoveryHorizon.remove(context);
            return context;
        }
        return null;
    }

    public void setCurrentContext(ContextPath contextPath) {
        this.currentContext = contextPath;
    }
    
    public ContextPath getCurrentContext() {
        return currentContext;
    }
}
