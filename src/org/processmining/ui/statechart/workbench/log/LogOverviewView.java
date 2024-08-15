package org.processmining.ui.statechart.workbench.log;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.processmining.ui.statechart.gfx.GfxFigs;
import org.processmining.ui.statechart.workbench.WorkbenchColors;
import org.processmining.ui.statechart.workbench.util.UiFactory;
import org.processmining.utils.statechart.ui.WrapLayout;

public class LogOverviewView extends LogOverviewController.View {

    private JPanel panelRoot;
//    private JButton btnSWPreset;
    
    public LogOverviewView() {
        
//        // express options
//        JPanel panelOptionsExpresss = new JPanel();
//        panelOptionsExpresss.setLayout(new WrapLayout());
//        
//        panelOptionsExpresss.add(createExpressOptNormal());
//        panelOptionsExpresss.add(createExpressOptSW());
//        
//        panelOptionsExpresss.setSize(new Dimension(300, 1));
//        
//        // preprocess options
//        JPanel panelOptionsPreprocessors = new JPanel();
//        panelOptionsPreprocessors.setLayout(new WrapLayout());
//        
//        for (LogPreprocessors preprocessor : LogPreprocessors.values()) {
//            if (preprocessor.hasDescriptionPanel()) {
//                panelOptionsPreprocessors.add(createOption(
//                        preprocessor.newDescriptionPanel(),
//                        preprocessor));
//            }
//        }
//        
//        panelOptionsPreprocessors.setSize(new Dimension(300, 1));
//        
//        // glue together
//        JPanel panelOptions = createTitlePlusContent(
//            createTitlePlusContent(
//                UiFactory.createLabelTopLeft("Express presets:"),
//                panelOptionsExpresss
//            ),
//            createTitlePlusContent(
//                UiFactory.createLabelTopLeft("Custom preprocess heuristics:"),
//                panelOptionsPreprocessors
//            )
//        );
//        
//        JScrollPane scrollPane = new JScrollPane(panelOptions);
//        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//        scrollPane.setBorder(BorderFactory.createEmptyBorder());
//        
//        panelRoot = createTitlePlusContent(
//            UiFactory.createTitleLabel(
//                "Log Preprocessing -- Select hierarchy construction heuristic"
//            ), scrollPane
//        );
        
        // ---
        
        panelRoot = new JPanel();
        panelRoot.setLayout(new BorderLayout());
        panelRoot.setBorder(UiFactory.createPaddingBorder());

        panelRoot.add(UiFactory.createTitleLabel(
            "Log Preprocessing - Select hierarchy and classifier"
            ), BorderLayout.PAGE_START);

        JPanel panelOptionsBase = new JPanel();
        panelOptionsBase.setLayout(new BorderLayout());

        JPanel panelOptionsPreset = new JPanel();
        panelOptionsPreset.setLayout(new BoxLayout(panelOptionsPreset, BoxLayout.X_AXIS));
        panelOptionsPreset.add(Box.createHorizontalGlue());
        panelOptionsPreset.add(createExpressOptNormal());
        panelOptionsPreset.add(Box.createHorizontalStrut(5));
        panelOptionsPreset.add(createExpressOptSW());
        panelOptionsPreset.add(Box.createHorizontalGlue());
        panelOptionsBase.add(panelOptionsPreset, BorderLayout.NORTH);
        
        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new WrapLayout());
        
        final JLabel label = new JLabel("<html><br><h3 style=\"margin: 2 0 2 0;\">Advanced - Customize hierarchy construction heuristic and classifier:</h3></html>");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel panelOptionsWrap = new JPanel();
        panelOptionsWrap.setLayout(new BorderLayout());
        panelOptionsWrap.add(label, BorderLayout.NORTH);
        panelOptionsWrap.add(panelOptions, BorderLayout.CENTER);
        
        panelOptionsBase.add(panelOptionsWrap, BorderLayout.CENTER);
        
        for (LogPreprocessors preprocessor : LogPreprocessors.values()) {
            if (preprocessor.hasDescriptionPanel()) {
                panelOptions.add(createOption(
                        preprocessor.newDescriptionPanel(),
                        preprocessor));
            }
        }

        panelOptions.setSize(new Dimension(300, 1));
        
        JScrollPane scrollPane = new JScrollPane(panelOptionsBase);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelRoot.add(scrollPane, BorderLayout.CENTER);
    }
    
//    private JPanel createTitlePlusContent(Component title, Component content) {
//        JPanel panel = new JPanel();
//        panel.setLayout(new BorderLayout());
//        panel.setBorder(BorderFactory.createEmptyBorder());
//        
//        panel.add(title, BorderLayout.NORTH);
//        panel.add(content, BorderLayout.CENTER);
//        
//        return panel;
//    }

    private Component createExpressOptSW() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        panel.add(new JLabel("<html><h2>Start Discovery - Software Log</h2></html>"), BorderLayout.NORTH);
        StringBuilder usecaseStr = new StringBuilder();
        usecaseStr.append("<html>");
        usecaseStr.append("<p>Use default settings for software logs</p>");
        usecaseStr.append("<p>(Nested Calls, Exception Cancellation)</p>");
        usecaseStr.append("</html>");
        panel.add(new JLabel(usecaseStr.toString()), BorderLayout.SOUTH);
        
        JPanel imgpanel = new JPanel();
        imgpanel.add(GfxFigs.FigPreset_Software.getImageLabel(""));
        panel.add(imgpanel, BorderLayout.CENTER);
        
        final JPanel wrap = createOptionWrap(panel);
        wrap.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                SignalUseSWPreset.dispatch();
            }
        });
        return wrap;
    }
    
    private Component createExpressOptNormal() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        panel.add(new JLabel("<html><h2>Start Discovery - Normal Log</h2></html>"), BorderLayout.NORTH);
        StringBuilder usecaseStr = new StringBuilder();
        usecaseStr.append("<html>");
        usecaseStr.append("<p>Use default settings for normal logs</p>");
        usecaseStr.append("<p>(Single Classifier,  No cancellation)</p>");
        usecaseStr.append("</html>");
        panel.add(new JLabel(usecaseStr.toString()), BorderLayout.SOUTH);
        
        JPanel imgpanel = new JPanel();
        imgpanel.add(GfxFigs.FigPreset_Normal.getImageLabel(""));
        panel.add(imgpanel, BorderLayout.CENTER);
        
        final JPanel wrap = createOptionWrap(panel);
        wrap.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                SignalUseNormalPreset.dispatch();
            }
        });
        return wrap;
    }

    @Override
    public JComponent getComponent() {
        return panelRoot;
    }
    
    public JPanel createOption(JPanel content, final LogPreprocessors viewstate) {
        final JPanel wrap = createOptionWrap(content);
        
        wrap.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                SignalSelectAction.dispatch(viewstate);
            }
        });
        
        return wrap;
    }
    
    public JPanel createOptionWrap(JPanel content) {
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
        wrap.setPreferredSize(new Dimension(315, 200));
        wrap.setMaximumSize(wrap.getPreferredSize());
        
        wrap.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                wrap.setBorder(borderActive);
            }
            
            public void mouseExited(MouseEvent e) {
                wrap.setBorder(borderInactive);
            }
        });
        
        return wrap;
    }

}
