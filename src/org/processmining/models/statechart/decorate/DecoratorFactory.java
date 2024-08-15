package org.processmining.models.statechart.decorate;

public class DecoratorFactory {

    private static IDecoratorFactory defaultInst = new DecoratorFactoryDefault();
    
    public static IDecoratorFactory getDefaultInst() {
        return defaultInst;
    }

    public static void setDefaultInst(IDecoratorFactory inst) {
        defaultInst = inst;
    }
    
    private DecoratorFactory() {
        
    }
}
