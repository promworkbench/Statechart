package org.processmining.ui.statechart.gfx;

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

public enum GfxIcons {
    IconSearch("icon_search.png"),
    IconSoftware("icon_sw.png"),
    IconPdf("fig_icon_pdf.png"),
    
    IconOptPercent("icon_opt_percent.png"),
    IconOptStat("icon_opt_stat.png"),
    IconOptTime("icon_opt_time.png"),
    IconOptExclamation("icon_opt_exclamation.png"),
    IconOptError("icon_opt_error.png");

    private final String resource;

    private GfxIcons(String resource) {
        this.resource = resource;
    }

    public String getName() {
        return GfxIcons.class.getResource(resource).toString();
    }

    public InputStream getInputStream() {
        return GfxIcons.class.getResourceAsStream(resource);
    }

    public URL getResource() {
        return GfxIcons.class.getResource(resource);
    }

    public ImageIcon getImageIcon(String description) {
        URL imgURL = GfxIcons.class.getResource(resource);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + resource);
            return null;
        }
    }
}
