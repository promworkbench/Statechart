package org.processmining.ui.statechart.workbench;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;

import sun.swing.SwingUtilities2;

public class WorkbenchSideTabUi extends BasicTabbedPaneUI {

    private static final int Offset = 5;
    private static final int Width = 3;
    private static final int TextOffset = 13;
    
    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabInsets = new Insets(7, 25, 7, 25);
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
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
            tabPane.putClientProperty("html", v);
        }

        SwingUtilities.layoutCompoundLabel(tabPane,
                                           metrics, title, icon,
                                           SwingUtilities.CENTER,
                                           SwingUtilities.LEFT,
                                           SwingUtilities.CENTER,
                                           SwingUtilities.TRAILING,
                                           tabRect,
                                           iconRect,
                                           textRect,
                                           textIconGap);

        tabPane.putClientProperty("html", null);

        int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
        int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
        iconRect.x += xNudge + TextOffset;
        iconRect.y += yNudge;
        textRect.x += xNudge + TextOffset;
        textRect.y += yNudge;
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
        int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);
        
        if (tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex)) {
            g.setColor(fg);
            SwingUtilities2.drawStringUnderlineCharAt(tabPane, g,
                         title, mnemIndex,
                         textRect.x, textRect.y + metrics.getAscent());

        } else { // tab disabled
            g.setColor(tabPane.getBackgroundAt(tabIndex).brighter());
            SwingUtilities2.drawStringUnderlineCharAt(tabPane, g,
                         title, mnemIndex,
                         textRect.x, textRect.y + metrics.getAscent());
            g.setColor(tabPane.getBackgroundAt(tabIndex).darker());
            SwingUtilities2.drawStringUnderlineCharAt(tabPane, g,
                         title, mnemIndex,
                         textRect.x - 1, textRect.y + metrics.getAscent() - 1);

        }
        
        g.setFont(font);
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
            int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g;
        
        Polygon p = _tabBar(x, y, w, h);
        if (isSelected) {
            g2.setColor(WorkbenchColors.Active);
            g2.fillPolygon(p);
//        } else {
//            if (tabPane.isEnabledAt(tabIndex)) {
//                g2.setColor(WorkbenchColors.Inactive);
//            } else {
//                g2.setColor(WorkbenchColors.Disbled);
//            }
        }
        
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement,
            int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        
    }
    
    private Polygon _tabBar(int x, int y, int w, int h) {
        
        w -= 2 * Offset;
        h -= 2 * Offset;

        Polygon p = new Polygon();
        p.addPoint(Offset + x, Offset + y);
        p.addPoint(Offset + x + Width, Offset + y);
        p.addPoint(Offset + x + Width, Offset + y + h);
        p.addPoint(Offset + x, Offset + y + h);

        return p;
    }
}
