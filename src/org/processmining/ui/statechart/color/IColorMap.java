package org.processmining.ui.statechart.color;

import java.awt.Color;

public interface IColorMap {

    public void setValueChangepoints(double... changepoints);
    
    public void setValueRange(double minVal, double maxVal);
    
    public double getMin();
    
    public double getMax();
    
    public Color getColor(double value);

}
