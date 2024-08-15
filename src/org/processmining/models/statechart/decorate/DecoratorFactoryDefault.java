package org.processmining.models.statechart.decorate;

import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecoratorFactory;
import org.processmining.models.statechart.decorate.error.EPTreeErrorTriggerDecorator;
import org.processmining.models.statechart.decorate.staticmetric.EPTreeFreqMetricDecorator;
import org.processmining.models.statechart.decorate.swapp.EPTreeSwAppDecorator;
import org.processmining.models.statechart.eptree.IEPTreeNode;

public class DecoratorFactoryDefault implements IDecoratorFactory {

    public Decorations<IEPTreeNode> createEPTreeDecorations() {
        Decorations<IEPTreeNode> decs = new Decorations<IEPTreeNode>();
        decs.registerDecorator(new EPTreeFreqMetricDecorator());
        decs.registerDecorator(new EPTreeSwAppDecorator());
        decs.registerDecorator(new EPTreeErrorTriggerDecorator());
        return decs;
    }
}
