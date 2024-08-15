package org.processmining.models.statechart.doc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.processmining.ui.statechart.gfx.GfxIcons;
import org.processmining.ui.statechart.workbench.WorkbenchColors;

public class DocHelpButton extends JButton {

    private static final long serialVersionUID = 6462605005236829042L;

    public DocHelpButton() {
        super("  Manual / Help");
        setIcon(GfxIcons.IconPdf.getImageIcon("pdf"));
        setBackground(WorkbenchColors.Back);
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Doc.openDoc();
            }
        });
    }
}
