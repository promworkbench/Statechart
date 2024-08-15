package org.processmining.utils.statechart.svg;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.Text;
import com.kitfox.svg.Title;
import com.kitfox.svg.Tspan;
import com.kitfox.svg.xml.StyleAttribute;

public class SVG2XML {

    public String toXML(SVGDiagram svg) {
        StringWriter writer = new StringWriter();
        toXML(svg, writer);
        return writer.toString();
    }

    public void toXML(SVGDiagram svg, Writer stream) {
        try {
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            XMLStreamWriter xtw = xof.createXMLStreamWriter(stream);
            xtw.writeStartDocument("utf-8","1.0");
            
            _toXML(svg.getRoot(), xtw);
            
            xtw.writeEndDocument();
            xtw.flush();
            xtw.close();
        } catch (XMLStreamException e) {
            throw new SVGUtilException("Serialize svg to xml - stream error", e);
        } catch (SVGException e) {
            throw new SVGUtilException("Serialize svg to xml - svg error", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void _toXML(SVGElement elm, XMLStreamWriter xtw) throws XMLStreamException, SVGException {
        String tagName = elm.getTagName();
        
        xtw.writeStartElement(tagName);
        
        StyleAttribute attrib = new StyleAttribute();
        for (String key : (Set<String>) elm.getPresentationAttributes()) {
            elm.getPres(attrib.setName(key));
            String value = attrib.getStringValue();
            xtw.writeAttribute(key, value);
        }
        
        if (elm instanceof Title) {
            xtw.writeCharacters(((Title) elm).getText());
        } else if (elm instanceof Text) {
            @SuppressWarnings("rawtypes")
            List content = ((Text) elm).getContent();
            for (Object child : content) {
                if (child instanceof String) {
                    xtw.writeCharacters((String) child);
                }
                if (child instanceof Tspan) {
                    _toXML((Tspan) child, xtw);
                }
            }
        } else if (elm instanceof Tspan) {
            xtw.writeCharacters(((Tspan) elm).getText());
        }
        
        for (SVGElement child : (List<SVGElement>) elm.getChildren(null)) {
            _toXML(child, xtw);
        }
        
        xtw.writeEndElement();
    }
}
