package org.processmining.recipes.statechart.m2m;

import org.processmining.algorithms.statechart.discovery.im.cancellation.IQueryCancelError;
import org.processmining.algorithms.statechart.m2m.EPTree2Petrinet;
import org.processmining.algorithms.statechart.m2m.EPTree2Petrinet.CancelationMode;
import org.processmining.algorithms.statechart.m2m.EPTree2Petrinet.HierarchyMode;
import org.processmining.algorithms.statechart.m2m.EPTree2Petrinet.RecursionMode;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.ptnet.PetrinetDecorated;
import org.processmining.recipes.statechart.AbstractRecipe;

public class EPTree2PetrinetRecipe extends 
    AbstractRecipe<IEPTree, PetrinetDecorated, EPTree2PetrinetRecipe.Parameters> {

    public static class Parameters {
        public HierarchyMode hierarchyMode;
        public RecursionMode recursionMode;
        public CancelationMode cancelationMode;
        
        public IQueryCancelError queryCatchError;
        
        public Parameters() {
            hierarchyMode = HierarchyMode.Subprocess;
            recursionMode = RecursionMode.InhibitorArcs;
            cancelationMode = CancelationMode.CancelationSubnet;
        }
    }
    
    public EPTree2PetrinetRecipe() {
        super(new Parameters());
    }

    @Override
    protected PetrinetDecorated execute(IEPTree input) {
        final Parameters params = getParameters();
        EPTree2Petrinet transform = new EPTree2Petrinet(
                params.hierarchyMode, 
                params.recursionMode,
                params.cancelationMode);
        if (params.queryCatchError != null) {
            transform.setQueryCatchError(params.queryCatchError);
        }
        return transform.transform(input);
    }

}
