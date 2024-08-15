package org.processmining.ui.statechart.workbench.cancel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.processmining.ui.statechart.workbench.WorkbenchColors;
import org.processmining.ui.statechart.workbench.util.UiFactory;
import org.processmining.utils.statechart.ui.WrapLayout;

public class CancelOverviewView extends CancelOverviewController.View {

    private JPanel panelRoot;
    
    public CancelOverviewView() {
        panelRoot = new JPanel();
        panelRoot.setLayout(new BoxLayout(panelRoot, BoxLayout.Y_AXIS));
        panelRoot.setBorder(UiFactory.createPaddingBorder());
        panelRoot.add(UiFactory.leftJustify(UiFactory.createTitleLabel(
                "Cancellation Preprocessing -- Select isError oracle heuristic")), 
                BorderLayout.PAGE_START);
        
        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new WrapLayout());
        
        for (CancelPreprocessors preprocessor : CancelPreprocessors.values()) {
            if (preprocessor.hasDescriptionPanel()) {
                panelOptions.add(createOption(
                        preprocessor.newDescriptionPanel(),
                        preprocessor));
            }
        }

        panelOptions.setSize(panelOptions.getPreferredSize());
        
        JScrollPane scrollPane = new JScrollPane(panelOptions);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelRoot.add(scrollPane, BorderLayout.CENTER);
    }
    
    @Override
    public JComponent getComponent() {
        return panelRoot;
    }
    
    public JPanel createOption(JPanel content, final CancelPreprocessors viewstate) {
        JPanel pad = new JPanel();
        pad.setLayout(new BorderLayout());
        pad.add(content, BorderLayout.CENTER);
        pad.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        final Border borderActive = BorderFactory.createLineBorder(
                WorkbenchColors.Active, 2);
        final Border borderInactive = BorderFactory.createLineBorder(
                WorkbenchColors.Inactive, 2);

        final JPanel wrap = new JPanel();
        wrap.setLayout(new BorderLayout());
        wrap.add(pad, BorderLayout.CENTER);
        wrap.setBorder(borderInactive);
        wrap.setPreferredSize(new Dimension(750, 106));
        wrap.setMaximumSize(wrap.getPreferredSize());
        
        wrap.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                wrap.setBorder(borderActive);
            }
            
            public void mouseExited(MouseEvent e) {
                wrap.setBorder(borderInactive);
            }
            
            public void mousePressed(MouseEvent e) {
                SignalSelectAction.dispatch(viewstate);
            }
        });
        
        return wrap;
    }
}
