package org.processmining.models.statechart.msd;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.tracing.TraceUniqueDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;

import com.google.common.base.Preconditions;

public class SeqDiagram implements ISeqDiagram {

    private final Decorations<IActivation> decorations;
    
    private List<ILifeline> lifelines = new ArrayList<ILifeline>();
    
    private IMSDNode root;

    public SeqDiagram() {
        this(new Decorations<IActivation>());
    }
    
    public SeqDiagram(Decorations<IActivation> decorations) {
        this.decorations = decorations;
        
        @SuppressWarnings("unchecked")
        TraceUniqueDecorator<IActivation, IEPTreeNode> treeTracer 
            = decorations.getForType(TraceUniqueDecorator.class);
        if (treeTracer == null) {
            treeTracer = new TraceUniqueDecorator<IActivation, IEPTreeNode>();
            decorations.registerDecorator(treeTracer);
        }
    }
    
    public void addLifeline(ILifeline val) {
        lifelines.add(val);
    }
    
    @Override
    public List<ILifeline> getLifelines() {
        return lifelines;
    }
    
    public void sortLifelines() {
        Preconditions.checkNotNull(lifelines);
        Preconditions.checkNotNull(root);

        int counter = 1;
        
        Set<ILifeline> unvisited = new THashSet<>(lifelines);
        for (ILifeline inst : lifelines) {
            if (inst.getLifelineType() == LifelineType.Environment) {
                inst.setRank(counter);
                unvisited.remove(inst);
                counter++;
            }
        }
        
        Iterator<IMSDNode> it = root.iteratePreOrder().iterator();
        while (it.hasNext() && !unvisited.isEmpty()) {
            IMSDNode node = it.next();
            if (node instanceof IMSDMessage) {
                IActivation act = ((IMSDMessage) node).getTarget();
                ILifeline inst = act.getLifeline();
                if (unvisited.contains(inst)) {
                    inst.setRank(counter);
                    unvisited.remove(inst);
                    counter++;
                }
            }
        }
        
        Collections.sort(lifelines, new Comparator<ILifeline>() {
            @Override
            public int compare(ILifeline a, ILifeline b) {
                return Integer.compare(a.getRank(), b.getRank());
            }
            
        });
    }

    public void setRoot(IMSDNode val) {
        root = val;
    }
    
    @Override
    public IMSDNode getRoot() {
        return root;
    }

    @Override
    public Decorations<IActivation> getDecorations() {
        return decorations;
    }

}
