package org.processmining.ui.statechart.gfx;

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public enum GfxFigs {
    FigLog2Log_ExistingListLabel("fig_log2log_existing-red.png"),
    FigLog2Log_MultiAttribs("fig_log2log_attribs-red.png"),
    FigLog2Log_NestedCalls("fig_log2log_nested-red.png"),
    FigLog2Log_StructredNames("fig_log2log_structured-red.png"),
    FigLog2Log_PatternNames("fig_log2log_pattern-red.png"),
    FigLog2Log_Classifier("fig_log2log_classifier-red.png"),
    
    FigPreset_Software("fig_preset_sw.png"),
    FigPreset_Normal("fig_preset_normal.png"),

    FigCancel_Manual("fig_cancel_manual-red.png"),
    FigCancel_NestedCalls("fig_cancel_nested-red.png"),
    FigCancel_Quality("fig_cancel_quality-red.png");

    private final String resource;

    private GfxFigs(String resource) {
        this.resource = resource;
    }

    public String getName() {
        return GfxFigs.class.getResource(resource).toString();
    }

    public InputStream getInputStream() {
        return GfxFigs.class.getResourceAsStream(resource);
    }

    public URL getResource() {
        return GfxFigs.class.getResource(resource);
    }

    public ImageIcon getImageIcon(String description) {
        URL imgURL = GfxFigs.class.getResource(resource);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + resource);
            return null;
        }
    }
    
    public JLabel getImageLabel(String description) {
        return new JLabel(getImageIcon(description));
    }
}
