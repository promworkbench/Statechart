package org.processmining.models.statechart.decorate.staticmetric;

public class FreqMetric {
    private final int freqAbsolute;
    private final int freqCase;
    
    public FreqMetric(int freqAbsolute, int freqCase) {
        this.freqAbsolute = freqAbsolute;
        this.freqCase = freqCase;
    }
    
    public FreqMetric(FreqMetric original) {
        this(original.getFreqAbsolute(), original.getFreqCase());
    }

    public int getFreqAbsolute() {
        return freqAbsolute;
    }

    public int getFreqCase() {
        return freqCase;
    }


}
