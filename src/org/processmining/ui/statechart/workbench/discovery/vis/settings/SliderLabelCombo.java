package org.processmining.ui.statechart.workbench.discovery.vis.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.utils.statechart.ui.PercentInputField;
import org.processmining.utils.statechart.ui.SwingUtils;

import com.jidesoft.swing.RangeSlider;

public class SliderLabelCombo extends JPanel {

    private static final long serialVersionUID = 6413109449420023197L;
    
    private final JSlider slider;
    private final RangeSlider rangeSlider;
    
    public final PercentInputField inputLow, inputHigh;
    
    public SliderLabelCombo(JSlider slider, String title) {
        if (slider instanceof RangeSlider) {
            this.slider = null;
            this.rangeSlider = (RangeSlider) slider;
        } else {
            this.slider = slider;
            this.rangeSlider = null;
        }
        setLayout(new BorderLayout());

        JLabel labelTitle = new JLabel(title, SwingConstants.CENTER);
        
        JPanel labelValue = new JPanel();
        labelValue.setLayout(new BoxLayout(labelValue, BoxLayout.X_AXIS));
        if (rangeSlider != null) {
            inputLow = new PercentInputField();
            inputHigh = new PercentInputField();
            _prepInputField(inputLow);
            _prepInputField(inputHigh);
            labelValue.add(inputLow);
//            labelValue.add(new JLabel(" - "));
            labelValue.add(inputHigh);
        } else {
            inputLow = new PercentInputField();
            inputHigh = null;
            _prepInputField(inputLow);
            labelValue.add(inputLow);
        }
        
        add(labelTitle, BorderLayout.NORTH);
        add(slider, BorderLayout.CENTER);
        add(labelValue, BorderLayout.SOUTH);
        
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                _updateLabel();
            }
        });
        _updateLabel();
    }

    protected void _updateLabel() {
        if (rangeSlider != null) {
            inputLow.setPercent((double) rangeSlider.getLowValue() / 100.0);
            inputHigh.setPercent((double) rangeSlider.getHighValue() / 100.0);
        } else {
            inputLow.setPercent((double) slider.getValue() / 100.0);
        }
    }
    
    private void _prepInputField(PercentInputField inputfield) {
        Dimension size = new Dimension(35, 20);
        inputfield.getSpinner().setSize(size);
        inputfield.getSpinner().setPreferredSize(size);
        inputfield.getSpinner().setMaximumSize(size);
        
        SwingUtils.addChangeListener(inputfield.getTextField(), new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (rangeSlider != null) {
                    rangeSlider.setLowValue((int) (inputLow.getPercent() * 100.0));
                    rangeSlider.setHighValue((int) (inputHigh.getPercent() * 100.0));
                } else {
                    slider.setValue((int) (inputLow.getPercent() * 100.0)); 
                }
            }
        });
    }

}
