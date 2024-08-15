package org.processmining.ui.statechart.workbench.discovery.vis.settings;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.processmining.ui.statechart.workbench.WorkbenchColors;
import org.processmining.ui.statechart.workbench.util.UiFactory;

public class PopoutSettingsWidget extends JButton { //extends JPanel {

    private static final long serialVersionUID = 4694298571025804891L;

    // window size
    public static final int DefaultWindowWidth = 350;
    public static final int DefaultWindowHeight = 400;

    // widget size
    public static final int WidgetWidth = 230;
    public static final int WidgetHeight = 80;
    
    // widget handler
    private class Handler implements ActionListener, WindowFocusListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!popupWindow.isVisible() && !popupUnfocusTimer.isRunning()) {
                openPopup();
            } else {
                hidePopup();
            }
        }

        @Override
        public void windowGainedFocus(WindowEvent e) {
            // For popupWindow -> great
        }

        @Override
        public void windowLostFocus(WindowEvent e) {
            // For popupWindow -> close
            popupUnfocusTimer.stop();
            popupUnfocusTimer.start();
            hidePopup();
        }
        
    }
    private Handler handler = new Handler();
    
    private JDialog popupWindow;
    private int windowWidth, windowHeight;
    private boolean openPopup;
    
    private Timer popupUnfocusTimer;

    public PopoutSettingsWidget(IPopoutSettingsPanel settingsPanel) {
        super("< Placeholder >");
        
        popupUnfocusTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popupUnfocusTimer.stop();
            }
        });
        
        // Window
        windowWidth = settingsPanel.getWindowWidth();
        windowHeight = settingsPanel.getWindowHeight();
        
        popupWindow = new JDialog();
        popupWindow.setUndecorated(true);
        popupWindow.setAlwaysOnTop(true);
        popupWindow.setLayout(new BorderLayout());
        popupWindow.add(settingsPanel.getRootComponent(), BorderLayout.CENTER);
        popupWindow.pack();
        popupWindow.setBounds(0, 0, windowWidth, windowHeight);

        // Widget (this)
        setHorizontalAlignment(SwingConstants.LEFT);
        UiFactory.forceSize(this, getWidgetWidth(), getWidgetHeight());
        
        setBackground(WorkbenchColors.Back);
        settingsPanel.setWidgetComponent(this);
        _registerListeners();
    }

    protected int getWidgetWidth() {
        return WidgetWidth;
    }
    
    protected int getWidgetHeight() {
        return WidgetHeight;
    }

    public void hidePopup() {
        popupWindow.setVisible(false);
    }

    public void openPopup() {
        openPopup = true;
        viewPopup();
    }
    
    public void viewPopup() {
        if (openPopup || popupWindow.isVisible()) {
            openPopup = false;
            
            JRootPane root = this.getRootPane();
            double yp = root.getLocationOnScreen().getY() + root.getHeight();
            
            Point screenLoc = this.getLocationOnScreen();
            int viewX = (int) (screenLoc.getX() - windowWidth);
            int viewY = (int) Math.min(screenLoc.getY(), yp - windowHeight);
            viewAtLocation(viewX, viewY);
        }
    }
    
    protected void viewAtLocation(int x, int y) {
        popupWindow.setLocation(x, y);
        popupWindow.setVisible(true);
    }
    
    private void _registerListeners() {
        this.addActionListener(handler);
        popupWindow.addWindowFocusListener(handler);

        // some events may invalidate the location of the popup 
        // --> reposition popup
        this.addHierarchyBoundsListener(new HierarchyBoundsListener() {
            @Override
            public void ancestorResized(HierarchyEvent e) {
                viewPopup();
            }
            
            @Override
            public void ancestorMoved(HierarchyEvent e) {
                viewPopup();
            }
        });
        this.addComponentListener(new ComponentListener() {
            
            @Override
            public void componentShown(ComponentEvent e) {
                viewPopup();
            }
            
            @Override
            public void componentResized(ComponentEvent e) {
                viewPopup();
            }
            
            @Override
            public void componentMoved(ComponentEvent e) {
                viewPopup();
            }
            
            @Override
            public void componentHidden(ComponentEvent e) {
                hidePopup();
            }
        });
    }
}
