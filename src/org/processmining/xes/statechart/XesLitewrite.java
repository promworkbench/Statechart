package org.processmining.xes.statechart;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeTimestamp;

public class XesLitewrite implements Closeable, Flushable {

    public static Logger logger = LogManager.getLogger(XesLitewrite.class);

    private final BufferedWriter out;
    
    public static enum XesState {
	New,
	Header,
	DefineAttrLog,
	DefineAttrTrace,
	DefineAttrEvent,
	Trace,
	Event,
	Closed
    }
    private XesState state;
    
    private int indent = 0;

    public XesLitewrite(File target) throws IOException {
        String ext = FilenameUtils.getExtension(target.toString());
        if (ext.equals("gz")) {
            GZIPOutputStream gzip = new GZIPOutputStream(new FileOutputStream(target));
            out = new BufferedWriter(new OutputStreamWriter(gzip, "UTF-8"));
        } else {
            out = new BufferedWriter(new FileWriter(target));
        }
	state = XesState.New;
    }

    @Override
    public void flush() throws IOException {
	out.flush();
    }

    @Override
    public void close() throws IOException {
	out.close();
	state = XesState.Closed;
    }

    private void writeln(String string) {
	try {
	    for (int i = 0; i < indent; i++) {
		out.write("\t");
	    }
	    out.write(string);
	    out.write("\n");
	} catch (IOException e) {
	    e.printStackTrace();
	    logger.error("IOException, closing file...", e);
	    try {
		out.close();
	    } catch (IOException e1) {
		logger.error("IOException, error while closing file.", e);
	    }
	}
    }

    private void writeln(String template, Object... args) {
	writeln(String.format(template, args));
    }

    public void setHeader() {
	setHeader("Maikel Leemans - XesLitewrite Java");
    }
    
    public void setHeader(String creator) {
	checkArgument(state == XesState.New, "Expected state New, got state " + state.toString());
	
	writeln("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
	writeln("<!-- XES standard version: 1.0 -->");
	writeln("<log xes.version=\"1.0\" xes.features=\"nested-attributes\" openxes.version=\"1.0RC7\" xmlns=\"http://www.xes-standard.org/\" xes.creator=\"%s\">", creator);
	indent++;
	state = XesState.Header;
    }
    
    public void finish() {
	checkArgument(state == XesState.Header, "Expected state Header, got state " + state.toString());
	
	indent--;
	writeln("</log>");
	try {
	    close();
	} catch (IOException e) {
	    logger.error("IOException, error while closing file.", e);
	}
    }
    
    public void addExtension(String name, String prefix, String uri) {
	checkArgument(state == XesState.Header, "Expected state Header, got state " + state.toString());
	
	writeln("<extension name=\"%s\" prefix=\"%s\" uri=\"%s\" />", name, prefix, uri);
    }
    
    public void addExtension(XExtension ext) {
	addExtension(ext.getName(), ext.getPrefix(), ext.getUri().toString());
    }
    
    public void beginDefineAttrLog() {
	checkArgument(state == XesState.Header, "Expected state Header, got state " + state.toString());
	
	writeln("<global scope=\"log\">");
	indent++;
	state = XesState.DefineAttrLog;
    }
    
    public void endDefineAttrLog() {
	checkArgument(state == XesState.DefineAttrLog, "Expected state DefineAttrLog, got state " + state.toString());
	
	indent--;
	writeln("</global>");
	state = XesState.Header;
    }

    public void beginDefineAttrTrace() {
	checkArgument(state == XesState.Header, "Expected state Header, got state " + state.toString());
	
	writeln("<global scope=\"trace\">");
	indent++;
	state = XesState.DefineAttrTrace;
    }
    
    public void endDefineAttrTrace() {
	checkArgument(state == XesState.DefineAttrTrace, "Expected state DefineAttrTrace, got state " + state.toString());

	indent--;
	writeln("</global>");
	state = XesState.Header;
    }

    public void beginDefineAttrEvent() {
	checkArgument(state == XesState.Header, "Expected state Header, got state " + state.toString());
	
	writeln("<global scope=\"event\">");
	indent++;
	state = XesState.DefineAttrEvent;
    }
    
    public void endDefineAttrEvent() {
	checkArgument(state == XesState.DefineAttrEvent, "Expected state DefineAttrEvent, got state " + state.toString());

	indent--;
	writeln("</global>");
	state = XesState.Header;
    }

    public void beginTrace() {
	checkArgument(state == XesState.Header, "Expected state Header, got state " + state.toString());
	
	writeln("<trace>");
	indent++;
	state = XesState.Trace;
    }
    
    public void endTrace() {
	checkArgument(state == XesState.Trace, "Expected state Trace, got state " + state.toString());

	indent--;
	writeln("</trace>");
	state = XesState.Header;
    }

    public void beginEvent() {
	checkArgument(state == XesState.Trace, "Expected state Trace, got state " + state.toString());
	
	writeln("<event>");
	indent++;
	state = XesState.Event;
    }
    
    public void endEvent() {
	checkArgument(state == XesState.Event, "Expected state Event, got state " + state.toString());

	indent--;
	writeln("</event>");
	state = XesState.Trace;
    }

    public void addClassifier(String keys, String name) {
	checkArgument(state == XesState.Header, "Expected state Header, got state " + state.toString());
	
	writeln("<classifier keys=\"%s\" name=\"%s\" />", keys, name);
    }
    
    public void addClassifier(String[] keys, String name) {
	addClassifier(StringUtils.join(keys, " "), name);
    }

    public void addClassifier(XEventClassifier c) {
	addClassifier(c.getDefiningAttributeKeys(), c.name());
    }
    
    private String safeXML(String value) {
	value = value.replace("&", "&amp;");
	value = value.replace("<", "&lt;");
	value = value.replace(">", "&gt;");
	value = value.replace("\"", "&quot;");
	return value;
    }

    public void defineEventAttribs(XExtension extension) {
        for (XAttribute attrib : extension.getEventAttributes()) {
            defineAttr(attrib);
        }
    }
    
    public void defineAttr(XAttribute attrib) {
        if (attrib instanceof XAttributeTimestamp) {
            defineAttrTimestamp(attrib.getKey());
        } else if (attrib instanceof XAttributeDiscrete) {
            defineAttrDiscrete(attrib.getKey());
        } else if (attrib instanceof XAttributeContinuous) {
            defineAttrContinuous(attrib.getKey());
        } else if (attrib instanceof XAttributeBoolean) {
            defineAttrBoolean(attrib.getKey());
        } else {
            defineAttrString(attrib.getKey());
        }
    }

    public void defineAttrString(String key) {
	attrString(key, "string");
    }
    
    public void defineAttrBoolean(String key) {
        attrBoolean(key, false);
    }

    public void defineAttrContinuous(String key) {
        attrContinuous(key, 0);
    }

    public void defineAttrDiscrete(String key) {
        attrDiscrete(key, 0);
    }

    public void defineAttrTimestamp(String key) {
        attrTimestamp(key, 0);
    }

    public void attrString(String key, String value) {
	checkArgument(state != XesState.New && state != XesState.Closed, 
	        "Expected state not New or Closed, got state " + state.toString());
	
	writeln("<string key=\"%s\" value=\"%s\" />", key, safeXML(value));
    }

    public void attrBoolean(String key, boolean value) {
        checkArgument(state != XesState.New && state != XesState.Closed, 
                "Expected state not New or Closed, got state " + state.toString());
        
        writeln("<boolean key=\"%s\" value=\"%s\" />", key, safeXML(Boolean.toString(value)));
    }

    public void attrContinuous(String key, double value) {
        checkArgument(state != XesState.New && state != XesState.Closed, 
                "Expected state not New or Closed, got state " + state.toString());
        
        writeln("<float key=\"%s\" value=\"%s\" />", key, safeXML(Double.toString(value)));
    }

    public void attrDiscrete(String key, long value) {
        checkArgument(state != XesState.New && state != XesState.Closed, 
                "Expected state not New or Closed, got state " + state.toString());
        
        writeln("<int key=\"%s\" value=\"%s\" />", key, safeXML(Long.toString(value)));
    }
    
    public void attrTimestamp(String key, long valueMilis) {
        checkArgument(state != XesState.New && state != XesState.Closed, 
                "Expected state not New or Closed, got state " + state.toString());
        
	Date date = new Date(valueMilis);
	String value = XAttributeTimestamp.FORMATTER.format(date);

	writeln("<date key=\"%s\" value=\"%s\" />", key, safeXML(value));
    }
}
