package org.processmining.models.statechart.decorate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;

public class Decorations<T> implements Cloneable, Iterable<IDecorator<T, ?>> {

    private List<IDecorator<T, ?>> decorators = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public <D> D getForType(Class<D> class1) {
        Preconditions.checkNotNull(class1);

        for (int i = 0; i < decorators.size(); i++) {
            IDecorator<T, ?> decorator = decorators.get(i);
            if (decorator.getClass().equals(class1)) {
                return (D) decorator;
            }
        }
        return null;
    }

    public void registerDecorator(IDecorator<T, ?> newDecorator) {
        // cleanup old decorator of same type
        Iterator<IDecorator<T, ?>> it = decorators.iterator();
        while (it.hasNext()) {
            IDecorator<T, ?> decorator = it.next();
            if (decorator.getClass().equals(newDecorator.getClass())) {
                it.remove();
            }
        }
        // add new decorator
        decorators.add(newDecorator);
    }

    public void removeDecorations(T target) {
        Preconditions.checkNotNull(target);
        for (int i = 0; i < decorators.size(); i++) {
            decorators.get(i).removeDecoration(target);
        }
    }
    

    public void copyDecorations(T target, T oldTarget) {
        copyDecorations(target, oldTarget, this);
    }
    
    public void copyDecorations(T target, T oldTarget,
            Decorations<T> oldDecorations) {
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(oldTarget);
        Preconditions.checkNotNull(oldDecorations);

        for (int i = 0; i < decorators.size(); i++) {
            decorators.get(i).copyDecoration(target, oldTarget, oldDecorations);
        }
    }

    public void deriveDecorations(T target, Object oldTarget,
            Decorations<?> oldDecorations) {
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(oldTarget);

        for (int i = 0; i < decorators.size(); i++) {
            decorators.get(i).deriveDecoration(target, oldTarget, oldDecorations);
        }
    }

    public Decorations<T> deepNewInstance() {
        Decorations<T> newInst = new Decorations<T>();
        for (int i = 0; i < decorators.size(); i++) {
            newInst.registerDecorator(decorators.get(i).newInstance());
        }
        return newInst;
    }

    public <T2> Decorations<T2> deriveDecorationInstance(Class<T2> type) {
        Decorations<T2> newInst = new Decorations<T2>();
        for (int i = 0; i < decorators.size(); i++) {
            IDecorator<T2, ?> newDec = decorators.get(i).deriveDecorationInstance(type);
            if (newDec != null) {
                newInst.registerDecorator(newDec);
            }
        }
        return newInst;
    }
    
    @Override
    public Decorations<T> clone() {
        Decorations<T> newInst = new Decorations<T>();
        for (int i = 0; i < decorators.size(); i++) {
            newInst.registerDecorator((AbstractDecorator<T, ?>) decorators.get(i).clone());
        }
        return newInst;
    }

    @Override
    public Iterator<IDecorator<T, ?>> iterator() {
        return decorators.iterator();
    }

}
