package org.processmining.models.statechart.decorate.tracing;

import org.processmining.models.statechart.decorate.AbstractDecorator;
import org.processmining.models.statechart.decorate.Decorations;
import org.processmining.models.statechart.decorate.IDecorator;

/**
 * Traces uniquely elements from FromType to ToType. 
 * Typically, the model in FromType is derived from the model in ToType.
 * I.e., trace-unique expresses a derived-from dependency.
 * @author mleemans
 *
 * @param <FromType>
 * @param <ToType>
 */
public class TraceUniqueDecorator<FromType, ToType> extends AbstractDecorator<FromType, ToType> {

    @Override
    public AbstractDecorator<FromType, ToType> newInstance() {
        return new TraceUniqueDecorator<>();
    }

    @Override
    public <T2> IDecorator<T2, ToType> deriveDecorationInstance(Class<T2> type) {
        return new TraceUniqueDecorator<T2, ToType>();
    }
    
    @Override
    public void copyDecoration(FromType target, FromType oldTarget,
            IDecorator<FromType, ?> oldDecorator) {
        try {
            Object oldUntyped = oldDecorator.getDecoration(oldTarget);
            @SuppressWarnings("unchecked")
            ToType old = (ToType) oldUntyped;
            if (old != null) {
                setDecoration(target, old);
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
            // nop
        }
    }

    @Override
    public void deriveDecoration(FromType target, Object oldTarget,
            Decorations<?> oldDecorations) {
        try {
            @SuppressWarnings("rawtypes")
            TraceUniqueDecorator oldDecorator = oldDecorations.getForType(TraceUniqueDecorator.class);
            if (oldDecorator != null) {
                @SuppressWarnings("unchecked")
                Object oldUntyped = oldDecorator.getDecoration(oldTarget);
                @SuppressWarnings("unchecked")
                ToType old = (ToType) oldUntyped;
                if (old != null) {
                    setDecoration(target, old);
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
            // nop
        }
    }

}
