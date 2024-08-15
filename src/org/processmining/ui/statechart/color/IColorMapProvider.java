package org.processmining.ui.statechart.color;

public interface IColorMapProvider {

    public IColorMapProvider invert();
    
    public IColorMap create();

    public IColorMapProvider deriveWithValueChangepoints(double... changepoints);

    public IColorMapProvider deriveWithValueRanges(double min, double... changepoints);
}
