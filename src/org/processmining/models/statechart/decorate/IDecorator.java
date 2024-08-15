package org.processmining.models.statechart.decorate;

public interface IDecorator<T, D extends Object> extends Cloneable {
    
    public boolean hasDecoration(T target);
    
    public D getDecoration(T target);
    
    public void setDecoration(T target, D decoration);
    
    public void removeDecoration(T target);

    public IDecorator<T, D> newInstance();

    public <T2> IDecorator<T2, D> deriveDecorationInstance(Class<T2> type);
    
    public void copyDecoration(T target, T oldTarget, Decorations<?> oldDecorations);

    public void copyDecoration(T target, T oldTarget, IDecorator<T, ?> oldDecorator);
    
    public void deriveDecoration(T target, Object oldTarget, Decorations<?> oldDecorations);

    public IDecorator<T, D> clone();
    
}
