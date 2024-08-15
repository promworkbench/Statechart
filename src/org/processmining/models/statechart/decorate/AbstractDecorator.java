package org.processmining.models.statechart.decorate;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import com.google.common.base.Preconditions;

public abstract class AbstractDecorator<T, D extends Object> implements IDecorator<T, D> {

    protected final Map<T, D> decorations = new THashMap<>();

    @Override
    public boolean hasDecoration(T target) {
        Preconditions.checkNotNull(target);
        return decorations.containsKey(target);
    }

    @Override
    public D getDecoration(T target) {
        Preconditions.checkNotNull(target);
        return decorations.get(target);
    }

    @Override
    public void setDecoration(T target, D decoration) {
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(decoration);
        
        decorations.put(target, decoration);
    }

    @Override
    public void removeDecoration(T target) {
        decorations.remove(target);
    }

    @Override
    public abstract IDecorator<T, D> newInstance();

    @Override
    public abstract <T2> IDecorator<T2, D> deriveDecorationInstance(Class<T2> type);
    
    @Override
    public void copyDecoration(T target, T oldTarget, Decorations<?> oldDecorations) {
        @SuppressWarnings("unchecked")
        IDecorator<T, ?> oldDecorator = oldDecorations.getForType(this.getClass());

        if (oldDecorator != null) {
            this.copyDecoration(target, oldTarget, oldDecorator);
        } else {
            throw new IllegalStateException(
                    "Could not copy decoration, no matching decorator found in old");
        }
    }

    @Override
    public abstract void copyDecoration(T target, T oldTarget, IDecorator<T, ?> oldDecorator);
    
    @Override
    public abstract void deriveDecoration(T target, Object oldTarget, Decorations<?> oldDecorations);

    @Override
    public IDecorator<T, D> clone() {
        IDecorator<T, D> newInst = newInstance();
        for (T key : decorations.keySet()) {
            newInst.copyDecoration(key, key, this);
        }
        return newInst;
    }
    
    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        bld.append("Decorator '" + this.getClass().getCanonicalName() + "':\n");
        for (T key : decorations.keySet()) {
            D value = decorations.get(key);
            bld.append("\t" + key + " => " + value + " \n");
        }
        return bld.toString();
    }
}
