package org.processmining.utils.statechart.svg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGLoaderHelper;

/**
 * Facade wrapping common functionality in SVGUniverse 
 * @author mleemans
 *
 */
public class SVGCollection {

    private final SVGUniverseFixed universe;
    
    public SVGCollection() {
        universe = new SVGUniverseFixed();
    }
    
    public SVGDiagram createDiagram(String name) {
        URI uri = universe.getStreamBuiltURI(name);
        return createDiagram(uri);
    }
    
    public SVGDiagram createDiagram(URI uri) {
        return universe.createDiagram(uri);
    }

    public SVGLoaderHelper createLoaderHelper(SVGDiagram diagram) {
        return new SVGLoaderHelper(diagram.getXMLBase(), universe, diagram);
    }
    
    public SVGDiagram loadSVG(ISVGReference ref) {
        return loadSVG(ref.getInputStream(), ref.getName());
    }

    public SVGDiagram loadSVG(InputStream stream, String name) {
        URI uri;
        try {
            uri = universe.loadSVG(stream, name);
        } catch (IOException e) {
            throw new SVGUtilException("Cannot load SVG from input stream", e);
        }
        return getDiagram(uri);
    }

    public void unloadSVG(String name) {
        unloadSVG(universe.getStreamBuiltURI(name));
    }
    
    public void unloadSVG(URI uri) {
        universe.removeDocument(uri);
    }
    
    public SVGDiagram getDiagram(ISVGReference ref) {
        return getDiagram(ref.getName());
    }
    
    public SVGDiagram getDiagram(URI uri) {
        SVGDiagram diagram = universe.getDiagram(uri);
        if (diagram == null) {
            throw new SVGUtilException("the svg given is not valid: " + uri);
        }
        return diagram;
    }

    public SVGDiagram getDiagram(String name) {
        return getDiagram(name2uri(name));
    }

    public URI name2uri(String name) {
        return universe.getStreamBuiltURI(name);
    }

    public URI ref2uri(ISVGReference icon) {
        return name2uri(icon.getName());
    }
}
