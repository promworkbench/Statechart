package org.processmining.ui.statechart.workbench.discovery.vis.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import org.processmining.ui.statechart.workbench.WorkbenchColors;
import org.processmining.utils.statechart.signals.Action1;

import com.google.common.base.Function;

public class PopoutSettingsPanelUiFactory {

    public static JPanel createTitleWrap(String title, Component content) {
        final JLabel label = new JLabel("<html><h4 style=\"margin: 2 0 2 0;\">" + title + "</h4></html>");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    public static String constructWidgetText(String title,
            String... lines) {
        StringBuilder bld = new StringBuilder();
        bld.append("<html>");
        
        bld.append("<h3 style=\"margin: 2 0 2 0;\">");
        bld.append(title);
        bld.append("</h3>");
        
        for (String line : lines) {
            bld.append("<p>");
            bld.append(line);
            bld.append("</p>");
        }
        
        bld.append("</html>");
        return bld.toString();
    }

    private static final int BtnPadding = 3;

    public static JToggleButton createMainToggleButton(String name) {
        return createMainToggleButton(name, null);
    }
    
    public static JToggleButton createMainToggleButton(String name, Icon icon) {
        JToggleButton btn = new JToggleButton(name, icon);
        btn.setBackground(WorkbenchColors.Back);
        btn.setBorder(BorderFactory.createCompoundBorder(
            btn.getBorder(),
            new EmptyBorder(BtnPadding, BtnPadding, BtnPadding, BtnPadding)
        ));
        return btn;
    }

    public static <T> JPanel constructTogglePanel(T[] values, final int cols, final Map<T, JToggleButton> targetMap, 
            Function<T, String> names, Function<T, String> tooltips, final Action1<T> selectedAction) {
        return constructTogglePanel(values, cols, targetMap, names, new Function<T, Icon>() {
            @Override
            public Icon apply(T paramF) {
                return null;
            }
        }, tooltips, selectedAction);
    }
    
    public static <T> JPanel constructTogglePanel(T[] values, final int cols, final Map<T, JToggleButton> targetMap, 
            Function<T, String> names, Function<T, Icon> icons, Function<T, String> tooltips, final Action1<T> selectedAction) {
        JPanel panel = new JPanel();
        final int rows = (int)Math.ceil((double)values.length / (double)cols);
        panel.setLayout(new GridLayout(rows, cols));

        ButtonGroup group = new ButtonGroup();
        ActionListener lst = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (T key : targetMap.keySet()) {
                    if (targetMap.get(key).isSelected()) {
                        selectedAction.call(key);
                    }
                }
            }
        };

        int i = 0;
        for (T val : values) {
            i++;
            JToggleButton btn = PopoutSettingsPanelUiFactory
                .createMainToggleButton(names.apply(val), icons.apply(val));
            String tooltip = tooltips.apply(val);
            if (tooltip != null) {
                btn.setToolTipText(tooltip);
            }
            targetMap.put(val, btn);
            group.add(btn);
            btn.addActionListener(lst);
            panel.add(btn);
        }
        while (i < rows * cols) {
            i++;
            panel.add(new JLabel());
        }
        
        return panel;
    }
}
