package org.processmining.ui.statechart.workbench;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.text.View;

import sun.swing.SwingUtilities2;

import com.jidesoft.plaf.basic.BasicJideTabbedPaneUI;

public class WorkbenchMainTabUi extends BasicJideTabbedPaneUI {

    private static final int Offset = 5;
    private static final int TextOffset = 25;
    
    @Override
    protected void installDefaults() {
        super.installDefaults();
        _tabInsets = new Insets(7, 25, 7, 25);
        _selectedTabPadInsets = new Insets(0, 0, 0, 0);
    }

    @Override
    protected void layoutLabel(int tabPlacement,
                               FontMetrics metrics, int tabIndex,
                               String title, Icon icon,
                               Rectangle tabRect, Rectangle iconRect,
                               Rectangle textRect, boolean isSelected ) {
        textRect.x = textRect.y = iconRect.x = iconRect.y = 0;

        View v = getTextViewForTab(tabIndex);
        if (v != null) {
            _tabPane.putClientProperty("html", v);
        }

        SwingUtilities.layoutCompoundLabel(_tabPane,
                                           metrics, title, icon,
                                           SwingUtilities.CENTER,
                                           SwingUtilities.LEFT,
                                           SwingUtilities.CENTER,
                                           SwingUtilities.TRAILING,
                                           tabRect,
                                           iconRect,
                                           textRect,
                                           _textIconGap);

        _tabPane.putClientProperty("html", null);

        int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
        int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
        iconRect.x += xNudge + TextOffset;
        iconRect.y += yNudge;
        textRect.x += xNudge + TextOffset;
        textRect.y += yNudge;
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        String title = getCurrentDisplayTitleAt(_tabPane, tabIndex);
        Insets tabInsets = getTabInsets(tabPlacement, tabIndex);
        
        return metrics.stringWidth(title) + TextOffset 
            + Offset * 2
            + tabInsets.left + tabInsets.right + 3 + getTabGap();
    }
    
    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, FontMetrics metrics) {
        Insets tabInsets = getTabInsets(tabPlacement, tabIndex);
        
        return metrics.getHeight()
            + Offset * 2
            + tabInsets.top + tabInsets.bottom + 2;
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font,
            FontMetrics metrics, int tabIndex, String title,
            Rectangle textRect, boolean isSelected) {
        Font f = font;
        Color fg = Color.BLACK;
        if (isSelected) {
            f = f.deriveFont(Font.BOLD);
            fg = WorkbenchColors.Active;
        }
        g.setFont(f);
        int mnemIndex = _tabPane.getDisplayedMnemonicIndexAt(tabIndex);
        
        if (_tabPane.isEnabled() && _tabPane.isEnabledAt(tabIndex)) {
            g.setColor(fg);
            SwingUtilities2.drawStringUnderlineCharAt(_tabPane, g,
                         title, mnemIndex,
                         textRect.x, textRect.y + metrics.getAscent());

        } else { // tab disabled
            g.setColor(_tabPane.getBackgroundAt(tabIndex).brighter());
            SwingUtilities2.drawStringUnderlineCharAt(_tabPane, g,
                         title, mnemIndex,
                         textRect.x, textRect.y + metrics.getAscent());
            g.setColor(_tabPane.getBackgroundAt(tabIndex).darker());
            SwingUtilities2.drawStringUnderlineCharAt(_tabPane, g,
                         title, mnemIndex,
                         textRect.x - 1, textRect.y + metrics.getAscent() - 1);

        }
        
        g.setFont(font);
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
            int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g;
        
        Polygon p = _tabChevron(x, y, w, h);
        if (isSelected) {
            g2.setColor(WorkbenchColors.Active);
        } else {
            if (_tabPane.isEnabledAt(tabIndex)) {
                g2.setColor(WorkbenchColors.Inactive);
            } else {
                g2.setColor(WorkbenchColors.Disbled);
            }
        }
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawPolygon(p);
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement,
            int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        Polygon p = _tabChevron(x, y, w, h);
        g.setColor(WorkbenchColors.Back);
        g.fillPolygon(p);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        int width = _tabPane.getWidth();
        int height = _tabPane.getHeight();
        Insets insets = _tabPane.getInsets();

        int x = insets.left;
        int y = insets.top;

        g.setColor(WorkbenchColors.BackPane);
        g.fillRect(x, y, width, height);
        
        super.paint(g, c);
    }
    
    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        
    }
    
    private Polygon _tabChevron(int x, int y, int w, int h) {
        
        w -= 2 * Offset;
        h -= 2 * Offset;

        int xOffset = (int) (h / 2.0 * Math.tan(Math.PI / 4.0));

        Polygon p = new Polygon();
        p.addPoint(Offset + x, Offset + y);
        p.addPoint(Offset + x + w - xOffset, Offset + y);
        p.addPoint(Offset + x + w, Offset + y + h / 2);
        p.addPoint(Offset + x + w - xOffset, Offset + y + h);
        p.addPoint(Offset + x, Offset + y + h);
        p.addPoint(Offset + x + xOffset, Offset + y + h / 2);
//      p.addPoint(Offset + x, Offset + y);
//      p.addPoint(Offset + x + w, Offset + y);
//      p.addPoint(Offset + x + w + xOffset, Offset + y + h / 2);
//      p.addPoint(Offset + x + w, Offset + y + h);
//      p.addPoint(Offset + x, Offset + y + h);
//      p.addPoint(Offset + x + xOffset, Offset + y + h / 2);

        return p;
    }
}
