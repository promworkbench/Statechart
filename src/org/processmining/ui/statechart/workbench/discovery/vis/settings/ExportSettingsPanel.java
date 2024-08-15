package org.processmining.ui.statechart.workbench.discovery.vis.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.processmining.ui.statechart.workbench.discovery.DiscoveryWorkbenchController;
import org.processmining.ui.statechart.workbench.discovery.vis.model.SubviewSvgModel;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ExportSettingsPanel extends AbstractSettingsPanel {

    private static final int WindowHeight = 85;
    
    private JButton btnLogExport;

    public ExportSettingsPanel(DiscoveryWorkbenchController.View baseView,
            SubviewSvgModel subviewSvgModel) {
        super(baseView);
        
        root.add(_createExportControls(subviewSvgModel), BorderLayout.NORTH);
        
    }

    @Override
    public int getWindowHeight() {
        return WindowHeight;
    }
    
    private Component _createExportControls(final SubviewSvgModel subviewSvgModel) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2, 0, 0));
        
        SlickerFactory f = SlickerFactory.instance();
        btnLogExport = f.createButton("Processed Log");
        JButton btnImage = f.createButton("Image");
//        JButton btnTree = f.createButton("EP Tree");
        JButton btnPTnet = f.createButton("Petri net");
        //JButton btnSC = f.createButton("SC"); 
        // TODO fix dependency bug in WorkbenchArtifacts.EPTreePost
        // in DiscoveryWorkbenchController

        panel.add(btnLogExport);
        panel.add(btnImage);
//        panel.add(btnTree);
        panel.add(btnPTnet);
        //panel.add(btnSC);

        btnLogExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                widgetButton.hidePopup();
                baseView.SignalExportLog.dispatch();
            }
        });
        btnImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //SignalExportLog.dispatch();
                widgetButton.hidePopup();
                subviewSvgModel.showSvgExportDialog();
            }
        });
//        btnTree.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                baseView.SignalExportTree.dispatch();
//            }
//        });
        btnPTnet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                widgetButton.hidePopup();
                baseView.SignalExportPTnet.dispatch();
            }
        });
//        btnSC.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                SignalExportSC.dispatch();
//            }
//        });
        
        return PopoutSettingsPanelUiFactory.createTitleWrap(
                "Export as...", panel);
    }

    @Override
    protected void updateWidget() {
        widgetButton.setText(PopoutSettingsPanelUiFactory.constructWidgetText(
                "Export as", 
                "Image, Processed Log, ",
                "Petri net, EPTree, ..."
            ));
    }
    
    public void setLogExportEnabled(boolean enabled) {
        btnLogExport.setEnabled(enabled);
    }
    
}
