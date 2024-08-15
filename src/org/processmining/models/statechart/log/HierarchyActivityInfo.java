package org.processmining.models.statechart.log;

import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class HierarchyActivityInfo {

    private final Multiset<String> activities = HashMultiset.<String>create();
//    private final Map<String, TIntSet> actLevels = new THashMap<>();
//    private final Map<String, Set<String>> actInstances = new THashMap<>();
    
    public void addActivity(String activity) {//, int level, String instance) {
        activities.add(activity);
        
//        TIntSet levels = actLevels.get(activity);
//        if (levels == null) {
//            levels = new TIntHashSet();
//            actLevels.put(activity, levels);
//        }
//        levels.add(level);
        
//        Set<String> instances = actInstances.get(activity);
//        if (instances == null) {
//            instances = new THashSet<>();
//            actInstances.put(activity, instances);
//        }
//        instances.add(instance);
    }
    
    public Set<String> getActivities() {
        return activities.elementSet();
    }
    
//    public TIntSet getLevels(String activity) {
//        return actLevels.get(activity);
//    }
    
//    public Set<String> getInstances(String activity) {
//        return actInstances.get(activity);
//    }
    
    public int getFrequency(String activity) {
        return activities.count(activity);
    }
    
    
}
