package org.processmining.utils.statechart.svg;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.xml.sax.InputSource;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

public class SVGUniverseFixed extends SVGUniverse {

    private static final long serialVersionUID = 3731107052833415443L;

    public SVGDiagram createDiagram(URI uri) {
        InputStream is = new ByteArrayInputStream("<svg></svg>".getBytes(StandardCharsets.UTF_8));
        loadSVG(uri, new InputSource(new BufferedInputStream(is)));
        return getDiagram(uri);
    }
}
