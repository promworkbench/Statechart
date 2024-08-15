package org.processmining.models.statechart.decorate.error;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.statechart.decorate.AbstractDecorator;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecorator;
import org.processmining.models.statechart.decorate.log.SubtraceDecorator;
import org.processmining.models.statechart.im.log.IMTraceReclass;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;

import com.google.common.base.Optional;

public class IMErrorTriggerDecorator 
    extends AbstractDecorator<XEvent, Set<String>> 
    implements SubtraceDecorator {

    public static Optional<IMErrorTriggerDecorator> getDecorator(IMTrace trace) {
        return getDecorator(trace, false);
    }

    public static Optional<IMErrorTriggerDecorator> getDecorator(IMTrace trace,
            boolean constructIfAbsent) {
        Decorations<XEvent> decorations = null;
        if (trace instanceof IMTraceReclass<?>) {
            decorations = ((IMTraceReclass<?>) trace).getEventDecorations();
        } else {
            throw new IllegalStateException("Expected IMTraceReclass");
        }
        
        IMErrorTriggerDecorator decorateErrorTrigger = decorations.getForType(IMErrorTriggerDecorator.class);
        if (constructIfAbsent && decorateErrorTrigger == null) {
            decorateErrorTrigger = new IMErrorTriggerDecorator();
            decorations.registerDecorator(decorateErrorTrigger);
        }
        
        return Optional.fromNullable(decorateErrorTrigger);
    }
    
    public static boolean hasAnyErrorTrigger(
            Optional<IMErrorTriggerDecorator> optDecorator, 
            XEvent event) {
        if (optDecorator.isPresent()) {
            IMErrorTriggerDecorator decorator = optDecorator.get();
            return (
                   decorator.hasDecoration(event)
                && !decorator.getDecoration(event).isEmpty()
            );
        } else {
            return false;
        }
    }
    
    @Override
    public AbstractDecorator<XEvent, Set<String>> newInstance() {
        return new IMErrorTriggerDecorator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T2> IDecorator<T2, Set<String>> deriveDecorationInstance(Class<T2> type) {
        if (type.equals(XEvent.class)) {
            return (IDecorator<T2, Set<String>>) new IMErrorTriggerDecorator();
        }
        return null;
    }

    public void addError(XEvent event, XEventClass c) {
        addError(event, c.toString());
    }
    public void addError(XEvent event, String c) {
        Set<String> errorClasses = decorations.get(event);
        if (errorClasses == null) {
            errorClasses = new THashSet<String>();
            decorations.put(event, errorClasses);
        }
        errorClasses.add(c);
    }

    @Override
    public void copyDecoration(XEvent target, XEvent oldTarget,
            IDecorator<XEvent, ?> oldDecorator) {
        try {
            @SuppressWarnings("unchecked")
            Set<String> old = (Set<String>) oldDecorator.getDecoration(oldTarget);
            if (old != null) {
                setDecoration(target, new THashSet<>(old));
            }
        } catch (ClassCastException e) {
            // NOP
        }
    }

    @Override
    public void deriveDecoration(XEvent target, Object oldTarget,
            Decorations<?> oldDecorations) {
        throw new IllegalArgumentException("Cannot derive from "
                + oldTarget.getClass());
    }

    @Override
    public void deriveForSubtrace(XTrace target, XEvent[] oldTargets,
            IDecorator<XEvent, ?> oldDecorator) {
        if (oldTargets.length < 2 || oldTargets[1] == null) {
            throw new IllegalArgumentException("Expected a complete event as old target");
        }
        @SuppressWarnings("unchecked")
        Set<String> old = (Set<String>) oldDecorator.getDecoration(oldTargets[1]);
        if (old != null) {
            // we got an error annotation on the complete event,
            // annotate it for the last event in the target
            setDecoration(target.get(target.size() - 1), new THashSet<String>(old));
        }
    }
}
