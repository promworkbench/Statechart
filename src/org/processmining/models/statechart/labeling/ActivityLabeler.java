package org.processmining.models.statechart.labeling;

public enum ActivityLabeler {
    Classifier("Classifier", new ClassifierActivityLabeler()),
    ClassMethodName("Software - Class+Method", new ClassMethodNameActivityLabeler()),
    MethodName("Software - Method", new MethodNameActivityLabeler()),
    ClassName("Software - Class", new ClassNameActivityLabeler()),
    AbbrPackageClassMethodName("Software - Pkg+Class+Method", new AbbrPackageClassMethodNameActivityLabeler());

    private final String name;
    private final IActivityLabeler labeler;

    private ActivityLabeler(String name, IActivityLabeler labeler) {
        this.name = name;
        this.labeler = labeler;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }
    
    public IActivityLabeler getLabeler() {
        return labeler;
    }
}
