package org.processmining.models.statechart.decorate.align;

import gnu.trove.map.hash.THashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.processmining.models.statechart.align.ExecInterval;
import org.processmining.utils.statechart.generic.UnionFind;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

public class IntervalClusters {

    private List<List<ExecInterval>> clusters;
    
    public IntervalClusters(Iterable<ExecInterval> it) {
        UnionFind<ExecInterval> unionFind = new UnionFind<>();
        
        THashMap<XAlignmentMove, ExecInterval> complete2interval = new THashMap<>();
        THashMap<Object, ExecInterval> cause2interval = new THashMap<>();
        THashMap<XAlignmentMove, ExecInterval> start2interval = new THashMap<>();
        
        // merge all intervals in clusters using union-find
        for (ExecInterval ival : it) {
            Object cause = ival.getCause();
            if (cause == null) {
                cause = ival.getStart();
            }
            XAlignmentMove start = ival.getStart();
            XAlignmentMove complete = ival.getComplete();
            
            // union overlapping sets
            unionFind.add(ival);
            
            // connect this ival to existing end
            if (complete2interval.containsKey(cause)) {
                unionFind.union(ival, complete2interval.get(cause));
            } else if (cause != start && complete2interval.containsKey(start)) {
                unionFind.union(ival, complete2interval.get(start));
            }
            // connect this ival to same trigger / cause
            if (cause2interval.containsKey(cause)) {
                unionFind.union(ival, cause2interval.get(cause));
            }
            // connect other ival cause to this ival end
            if (cause2interval.containsKey(complete)) {
                unionFind.union(ival, cause2interval.get(complete));
            }
            if (start2interval.containsKey(complete)) {
                unionFind.union(ival, start2interval.get(complete));
            }
            
            // add self
            complete2interval.put(complete, ival);
            cause2interval.put(cause, ival);
            start2interval.put(start, ival);
        }
        
        // extract clusters
        clusters = new ArrayList<>();
        for (Collection<ExecInterval> cluster : unionFind.getComponents()) {
            clusters.add(new ArrayList<>(cluster));
        }
    }
    
    /*
    public IntervalClusters(IEPTreeNode node, Iterable<ExecInterval> it) {
        clusters = new ArrayList<>();
        
        // create initial batch of clusters
        for (ExecInterval interval : it) {
            List<ExecInterval> target = null;
            for (List<ExecInterval> cluster : clusters) {
                if (_isConnected(cluster.get(cluster.size() - 1), interval)) {
                    target = cluster;
                    break;
                }
            }
            
            if (target == null) {
                target = new ArrayList<>();
                clusters.add(target);
            }
            target.add(interval);
        }
        
        // merge clusters
        int i = 0;
        Iterator<List<ExecInterval>> itClusters = clusters.iterator();
        while(itClusters.hasNext()) {
            List<ExecInterval> cluster = itClusters.next();
            List<ExecInterval> mergeWidth = null;
            for (List<ExecInterval> cluster2 : clusters) {
                if (cluster != cluster2) {
                    if (_isConnected(cluster2, cluster.get(0))
                        || _isSameTrigger(cluster2.get(0), cluster.get(0))) {
                        mergeWidth = cluster2;
                        break;
                    }
                }
            }
            
            if (mergeWidth != null) {
                mergeWidth.addAll(cluster);
                clusters.remove(i);
                itClusters = clusters.iterator();
                i = 0;
            } else {
                i++;
            }
        }
        
        i++;
        
//        int i = 0;
//        Iterator<List<ExecInterval>> itClusters = clusters.iterator();
//        while(itClusters.hasNext()) {
//            List<ExecInterval> cluster = itClusters.next();
//            List<ExecInterval> mergeWidth = null;
//            for (int j = 0; j < i && mergeWidth == null; j++) {
//                List<ExecInterval> cluster2 = clusters.get(j);
//                // merge clusters on matching end-start (fragmented clusters)
//                if (_isConnected(cluster2, cluster.get(0))) {
//                    mergeWidth = cluster2;
//                }
//                // merge clusters on matching enabled symbol (concurrency)
//                else if (_isSameTrigger(cluster2.get(0), cluster.get(0))) {
//                    mergeWidth = cluster2;
//                }
//            }
//            
//            if (mergeWidth != null) {
//                mergeWidth.addAll(cluster);
//                itClusters.remove();
//            } else {
//                i++;   
//            }
//        }
//        
//        i ++;
        // Split loops based on start activities of source node
//        Set<IEPTreeNode> starts = new THashSet<>(node.getStartNodes());
        // TODO find split point using starts
    }

    private boolean _isConnected(List<ExecInterval> cluster2, ExecInterval next) {
        for (int i = cluster2.size() - 1; i >= 0; i--) {
            if (_isConnected(cluster2.get(i), next)) {
                return true;
            }
        }
        return false;
    }
    private boolean _isConnected(ExecInterval prev,
            ExecInterval next) {
        XAlignmentMove pE = prev.getComplete();
        if (pE == null) {
            pE = prev.getStart();
        }

        Object nE = next.getCause();
        if (nE == null) {
            nE = next.getStart();
        }
        
        return (pE != null && nE != null && pE == nE);
    }
    
    private boolean _isSameTrigger(ExecInterval execInterval,
            ExecInterval execInterval2) {
        Object eo1 = execInterval.getCause();
        Object eo2 = execInterval2.getCause();
        
        return (eo1 != null && eo2 != null && eo1 == eo2);
    }
*/
    public List<List<ExecInterval>> getClusters() {
        return clusters;
    }

    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        bld.append("[");
        String split1 = "";
        for (List<ExecInterval> cluster : clusters) {
            bld.append("{");
            String split2 = "";
            for (ExecInterval interval : cluster) {
                bld.append(interval);
                bld.append(split2);
                split2 = ", ";
            }
            bld.append("}");
            bld.append(split1);   
            split1 = ", \n";
        }
        
        bld.append("]");
        return bld.toString();
    }
}
