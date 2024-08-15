package org.processmining.ui.statechart.workbench.common;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.ui.statechart.workbench.util.UiFactory;
import org.processmining.xes.statechart.classification.XEventListLabelClassifier;
import org.processmining.xes.statechart.extension.XListLabelExtension;
import org.processmining.xes.statechart.extension.XSubtraceExtension;

import com.google.common.base.Function;

public class LogHTracePanel extends JPanel {

    private static final long serialVersionUID = 7511411906347805850L;

    private static final String NoTraceText = "(No instance selected)";
    
    public static class Configuration {
        public int offsetX = 10;
        public int offsetY = 10;
        
        public Font textFont = new Font("Sans Serif", Font.PLAIN, 12);

        public int chevronWidth = 60;
        public int chevronHeight = 20;
        public int chevronInset = 5;
        public int chevronTextPad = 2;

        public Stroke chevronStroke = new BasicStroke(2.0f);
        public Color chevronStrokeColor = Color.GRAY;
        public Color chevronStrokeActiveColor = Color.RED;
        public Color chevronFillColor = Color.WHITE;
        public Color chevronTextColor = Color.BLACK;
        
        public int infoBoxWidth = 10 * chevronWidth;
        public int infoBoxHeight = 3 * chevronHeight;
        public int infoBoxOffset = 2;
        public Color infoBoxFillColor = new Color(0, 0, 0, (int)(255 * 0.8f));
        public Color infoBoxTextColor = Color.WHITE;
        public int infoBoxTextPadding = 2;
        
        public int chevronZActive = 10;
        public int chevronZDefault = 1;
        
        public Color getChevronFillColor(String text, boolean active) {
            return chevronFillColor;
        }
        
        public Color getChevronStrokeColor(String text, boolean active) {
            if (active) {
                return chevronStrokeActiveColor;
            } else {
                return chevronStrokeColor;
            }
        }
        
        public Color getChevronTextColor(String text, boolean active) {
            return chevronTextColor;
        }
        
        public int getZLayer(String text, boolean active) {
            if (active) {
                return chevronZActive;
            } else {
                return chevronZDefault;
            }
        }
    }
    private final Configuration config;
    
    private XEventListLabelClassifier listCls = new XEventListLabelClassifier();
    private XConceptExtension conceptExt = XConceptExtension.instance();
    private XSubtraceExtension subtraceExt = XSubtraceExtension.instance();
    private XEventClassifier lifecycleCls = new XEventLifeTransClassifier();

    private Dimension renderDim;
    
    private XTrace trace;
    private Function<XEvent, String> labelFnc;
    private Function<XEvent, String> instFnc = new Function<XEvent, String>() {
        @Override
        public String apply(XEvent paramXEvent) {
            return lifecycleCls.getClassIdentity(paramXEvent);
        }
    };
    
//    private int mouseX, mouseY; 
    private int activeIndex, activeLevel;

    private static class ChevronData {
        public final int x;
        public final int y;
        public final int width;
        public final String label;
        public final boolean active;
        public final XEvent event;
        public final String inst;
        
        public ChevronData(int x, int y, int width, String label, 
                boolean active, XEvent event, String inst) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.label = label;
            this.active = active;
            this.event = event;
            this.inst = inst;
        }
    }
    private TIntObjectMap<List<ChevronData>> chevrons = new TIntObjectHashMap<>();

    private ChevronData activeChevron;
    
    public LogHTracePanel() {
        this(null);
    }
    
    public LogHTracePanel(Configuration config) {
        if (config == null) {
            config = new Configuration();
        }
        this.config = config;
        
        _processMouseHover(-1, -1);
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                _processMouseHover(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                _processMouseHover(e.getX(), e.getY());
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                _processMouseHover(-1, -1);
            }
        });
    }

    protected void _processMouseHover(int x, int y) {
//        popupChevronX = x;
//        popupChevronY = y;
        if (x >= config.offsetX && y >= config.offsetY) {
            activeIndex = (x - config.offsetX) / config.chevronWidth;
            activeLevel = (y - config.offsetY) / config.chevronHeight;
        } else {
            activeIndex = -1;
            activeLevel = -1;
        }
        repaint();
    }
    
    public void viewNoTrace() {
        trace = null;
        renderDim = _calcDimension();
        UiFactory.forceSize(this, renderDim);
        revalidate();
        repaint();
    }

    public void viewTrace(XTrace trace, Function<XEvent, String> labelFnc) {
        this.labelFnc = labelFnc;
        this.trace = trace;
        renderDim = _calcDimension();
        UiFactory.forceSize(this, renderDim);
        repaint();
    }
    
    @SuppressWarnings("deprecation")
    private Dimension _calcDimension() {
        int width = 2 * config.offsetX;
        int height = 2 * config.offsetY;

        if (trace == null) {
            FontMetrics fontMetrics = Toolkit.getDefaultToolkit()
                    .getFontMetrics(config.textFont);
            width += fontMetrics.stringWidth(NoTraceText);
            height += fontMetrics.getHeight();
        } else {
            int maxN = 1;
            int widthN = trace.size();
            if (labelFnc == null) {
                if (XListLabelExtension.isListLabelTrace(trace)) {
                    for (XEvent event : trace) {
                        List<String> labelList = listCls.getClassIdentityList(event);
                        int n = labelList.size();
                        maxN = Math.max(maxN, n);
                    }
                } else {
                    widthN = 0;
                    List<Iterator<XEvent>> iterators = new ArrayList<>();
                    iterators.add(trace.iterator());
                    while(!iterators.isEmpty()) {
                        maxN = Math.max(maxN, iterators.size());
                        Iterator<XEvent> it = iterators.remove(iterators.size() - 1);
                        boolean useStack = false;
                        while (it.hasNext() && !useStack) {
                            XEvent event = it.next();
                            XTrace subtrace = subtraceExt.extractSubtrace(event);
                            if (subtrace != null) {
                                iterators.add(it);
                                iterators.add(subtrace.iterator());
                                useStack = true;
                            } else {
                                widthN++;
                            }
                        }
                    }
                }
            }
            width += widthN * config.chevronWidth;
            height += maxN * config.chevronHeight;
        }
        
        return new Dimension(
            Math.max(width, config.infoBoxWidth + 2 * config.offsetX), 
            Math.max(height, config.chevronHeight + 2 * config.infoBoxHeight + 2 * config.offsetY)
        );
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(config.textFont);
        chevrons.clear();
        activeChevron = null;
        
        if (trace == null) {
            String text = NoTraceText;
            int height = g2d.getFontMetrics().getHeight();
            g.drawString(text, config.offsetX, config.offsetY + height);
        } else {
            if (labelFnc != null) {
                _prepTraceClassify(g2d);
            } else {
                if (XListLabelExtension.isListLabelTrace(trace)) {
                    _prepTraceList(g2d);                    
                } else {
                    _prepTraceSubtrace(g2d);            
                }
            }
            _paintChevrons(g2d);
            if (activeChevron != null) {
                _paintInfoBox(g2d);
            }
        }
        
    }

    private void _paintChevrons(Graphics2D g2d) {
        int keys[] = chevrons.keys(); 
        Arrays.sort(keys);
        for (int i = 0; i < keys.length; i++) {
            int z = keys[i];
            for (ChevronData chevron : chevrons.get(z)) {
                _paintChevron(g2d, chevron.x, chevron.y, chevron.width, chevron.label, chevron.active);
            }
        }
    }

    private void _prepTraceClassify(Graphics2D g2d) {
        int currentX = config.offsetX;
        int currentY = config.offsetY;
        
        int index = 0;
        for (XEvent event : trace) {
            String label = labelFnc.apply(event);
            String inst = instFnc.apply(event);
            int width = config.chevronWidth;
            boolean active = (activeIndex == index && activeLevel == 0);
            _makeChevron(g2d, currentX, currentY, width, label, active, event, inst);
            
            currentX += width;
            index++;
        }
    }

    private void _prepTraceList(Graphics2D g2d) {
        int currentX = config.offsetX;
        int currentY = config.offsetY;

        TIntList startIndexList = new TIntArrayList();
        TIntList startXList = new TIntArrayList();
        List<XEvent> startEventList = new ArrayList<>();

        List<String> lastLabelList = new ArrayList<String>();
        List<String> lastInstList = new ArrayList<String>();
        int index = 0;
        for (XEvent event : trace) {
            List<String> labelList = listCls.getClassIdentityList(event);
            List<String> instList = listCls.getInstanceList(event);
            int n = labelList.size();
            int nLast = lastLabelList.size();
            
            // finish old
            for (int i = 0; i < nLast; i++) {
                if (labelList.size() <= i
                    || !lastLabelList.get(i).equals(labelList.get(i))
                    || instList.size() <= i
                    || !lastInstList.get(i).equals(instList.get(i))) {
                    String label = lastLabelList.get(i);
                    String inst = instList.get(i);
                    int startIndex = startIndexList.get(i);
                    int oldCurrentX = startXList.get(i);
                    int oldCurrentY = currentY + i * config.chevronHeight;
                    int width = currentX - oldCurrentX;
                    boolean active = (startIndex <= activeIndex && activeIndex <= (index - 1) && activeLevel == i);
                    _makeChevron(g2d, oldCurrentX, oldCurrentY, width, label, active, startEventList.get(i), inst);
                }
            }

            // start new
            for (int i = 0; i < n; i++) {
                if (lastLabelList.size() <= i
                    || !lastLabelList.get(i).equals(labelList.get(i))
                    || lastInstList.size() <= i
                    || !lastInstList.get(i).equals(instList.get(i))) {
                    if (i < startIndexList.size()) {
                        startIndexList.set(i, index);
                        startXList.set(i, currentX);
                        startEventList.set(i, event);
                    } else {
                        startIndexList.add(index);
                        startXList.add(currentX);
                        startEventList.add(event);
                    }
                }
            }
            
            lastLabelList = labelList;
            lastInstList = instList;
            currentX += config.chevronWidth;
            index++;
        }
        
        // finish last chevrons
        for (int i = 0; i < lastLabelList.size(); i++) {
            String label = lastLabelList.get(i);
            String inst = lastInstList.get(i);
            int startIndex = startIndexList.get(i);
            int oldCurrentX = startXList.get(i);
            int oldCurrentY = currentY + i * config.chevronHeight;
            int width = currentX - oldCurrentX;
            boolean active = (startIndex <= activeIndex && activeIndex <= (index - 1) && activeLevel == i);
            _makeChevron(g2d, oldCurrentX, oldCurrentY, width, label, active, startEventList.get(i), inst);
        }
    }

    private void _prepTraceSubtrace(Graphics2D g2d) {
        int currentX = config.offsetX;
        
        List<Iterator<XEvent>> iterators = new ArrayList<>();
        List<XEvent> intervals = new ArrayList<>();
        TIntList iStart = new TIntArrayList();
        
        iterators.add(trace.iterator());
        intervals.add(null);
        iStart.add(0);
        
        int index = 0;
        while(!iterators.isEmpty()) {
            Iterator<XEvent> it = iterators.remove(iterators.size() - 1);
            XEvent oldEvent = intervals.remove(intervals.size() - 1);
            int currentLevel = iterators.size();
            int currentY = config.offsetY + currentLevel * config.chevronHeight;
            int oldIndex = iStart.removeAt(iStart.size() - 1);
            int oldCurrentX = config.offsetX + oldIndex * config.chevronWidth;
            
            if (oldEvent != null) {
                // we processed the subtraces of oldEvent, now close oldEvent itself
                String label = conceptExt.extractName(oldEvent);
                String inst = lifecycleCls.getClassIdentity(oldEvent);
                int width = currentX - oldCurrentX;
                boolean active = (oldIndex <= activeIndex && activeIndex <= (index - 1) && activeLevel == currentLevel);
                _makeChevron(g2d, oldCurrentX, currentY, width, label, active, oldEvent, inst);
            }
            
            boolean useStack = false;
            while (it.hasNext() && !useStack) {
                XEvent event = it.next();
                XTrace subtrace = subtraceExt.extractSubtrace(event);
                if (subtrace != null) {
                    // store current progress for later continuation
                    iterators.add(it);
                    intervals.add(event);
                    iStart.add(index);
                    
                    // store new progress for subtrace investigation
                    iterators.add(subtrace.iterator());
                    intervals.add(null);
                    iStart.add(index);
                    useStack = true;
                } else {
                    // a leaf node, process event now
                    String label = conceptExt.extractName(event);
                    String inst = lifecycleCls.getClassIdentity(event);
                    int width = config.chevronWidth;
                    boolean active = (activeIndex == Math.floor((double)currentX / (double)config.chevronWidth) 
                            && activeLevel == Math.floor((double)currentY / (double)config.chevronHeight) );
                    _makeChevron(g2d, currentX, currentY, width, label, active, event, inst);
                    currentX += width;
                    index++;
                }
            }
        }
    }

    private void _makeChevron(Graphics2D g2d, int currentX, int currentY,
            int width, String label, boolean active, XEvent event, String instance) {
        int z = config.getZLayer(label, active);
        List<ChevronData> layer = chevrons.get(z);
        if (layer == null) {
            layer = new ArrayList<>();
            chevrons.put(z, layer);
        }
        ChevronData chevron = new ChevronData(currentX, currentY, width, label, active, event, instance);
        layer.add(chevron);
        
        if (active) {
            activeChevron = chevron;
        }
    }
    private void _paintInfoBox(Graphics2D g2d) {
        int x = activeIndex * config.chevronWidth + config.offsetX;
        int y = activeLevel * config.chevronHeight + config.offsetY + config.chevronHeight + config.infoBoxOffset;
        if (x + config.infoBoxWidth >= renderDim.width) {
            x = (activeChevron.x + activeChevron.width) - config.infoBoxWidth;
        }
        if (x <= config.offsetX) {
            x = config.offsetX;
        }
        if (y + config.infoBoxHeight >= renderDim.height) {
            y = activeChevron.y - config.infoBoxHeight - config.infoBoxOffset;
        }
        
        g2d.setColor(config.infoBoxFillColor);
        g2d.fillRect(x, y, config.infoBoxWidth, config.infoBoxHeight);

        FontMetrics fm = g2d.getFontMetrics();
        int textHeight = fm.getHeight();
        int labelWidth = fm.stringWidth("Lifecycle: "); 
        int valueWidth = config.infoBoxWidth - 2 * config.infoBoxTextPadding - labelWidth;
        
        String[][] info = new String[][] {
            { "Label: ", activeChevron.label },
            { "Concept: ", "" },
            { "", "" }
        };
        
        if (labelFnc == null) {
            info[2][0] = "Instance: ";
            info[1][1] = activeChevron.label;
            info[2][1] = activeChevron.inst;
        } else {
            info[1][1] = conceptExt.extractName(activeChevron.event);
            info[2][0] = "Lifecycle: ";
            info[2][1] = lifecycleCls.getClassIdentity(activeChevron.event);
        }
        
        g2d.setColor(config.infoBoxTextColor);
        for (int i = 0; i < info.length; i++) {
            g2d.drawString(info[i][0], 
                x + config.infoBoxTextPadding, 
                y + config.infoBoxTextPadding + (i + 1) * textHeight);
            g2d.drawString(_shortName(g2d, info[i][1], valueWidth), 
                x + config.infoBoxTextPadding + labelWidth, 
                y + config.infoBoxTextPadding + (i + 1) * textHeight);
        }
    }

    private void _paintChevron(Graphics2D g2d, int x, int y, int width, String text, boolean active) {
        Polygon p = new Polygon(new int[] {
            x,
            x + width,
            x + width + config.chevronInset,
            x + width,
            x,
            x + config.chevronInset
        }, new int[] {
            y,
            y,
            y + config.chevronHeight / 2,
            y + config.chevronHeight,
            y + config.chevronHeight,
            y + config.chevronHeight / 2
        }, 6);
        
        Color fillColor = config.getChevronFillColor(text, active);
        Color strokeColor = config.getChevronStrokeColor(text, active);
        Color textColor = config.getChevronTextColor(text, active);
        
        g2d.setColor(fillColor);
        g2d.fillPolygon(p);
        
        g2d.setStroke(config.chevronStroke);
        g2d.setColor(strokeColor);
        g2d.drawPolygon(p);

        text = _shortName(g2d, text, width - config.chevronTextPad * 2 - config.chevronInset * 2);
        int textHeight = g2d.getFontMetrics().getHeight();
        g2d.setColor(textColor);
        g2d.drawString(text, 
            x + config.chevronInset + config.chevronTextPad, 
            y + textHeight);
    }

    private String _shortName(Graphics2D g2d, String text, int width) {
        if (text == null || text.length() < 1) {
            return "";
        }
        
        FontMetrics fm = g2d.getFontMetrics();
        
        int currSize = 0;
        String ellipses = "\u2026";
        int ellipsesSize = fm.stringWidth(ellipses);

        /*
        int i;
        for (i = 0; i < text.length(); i++) {
            int add = fm.charWidth(text.charAt(i));
            if (currSize + add >= width) {
                break;
            } else {
                currSize += add;
            }
        }
        if (i < text.length()) {
            while (i >= 0 && currSize + ellipsesSize >= width) {
                currSize -= fm.charWidth(text.charAt(i));
                i--;
            }
            return text.substring(0, i) + ellipses;
        } else {
            return text;
        }
        */
        int i;
        for (i = text.length() - 1; i >= 0; i--) {
            int add = fm.charWidth(text.charAt(i));
            if (currSize + add >= width) {
                break;
            } else {
                currSize += add;
            }
        }
        if (i > 0) {
            while (i < text.length() && currSize + ellipsesSize >= width) {
                currSize -= fm.charWidth(text.charAt(i));
                i++;
            }
            return ellipses + text.substring(i, text.length());
        } else {
            return text;
        }
    }
}
