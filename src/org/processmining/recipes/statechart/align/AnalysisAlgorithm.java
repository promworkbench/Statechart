package org.processmining.recipes.statechart.align;

public enum AnalysisAlgorithm {
    Approx("Approximations", "Metrics are guessed based on a fast approximation algorithms. "
            + "Warning: the numbers shown may not be reliable, but give an indication."), 
    Align("Alignments", "Metrics are based on a slower but accurate algorithm. "
            + "The numbers shown are reliable, but computation may take a while.");

    private String name, descriptionShort;

    private AnalysisAlgorithm(String name, String descriptionShort) {
        this.name = name;
        this.descriptionShort = descriptionShort;
    }

    public String getName() {
        return name;
    }
    public String getDescriptionShort() {
        return descriptionShort;
    }
}
