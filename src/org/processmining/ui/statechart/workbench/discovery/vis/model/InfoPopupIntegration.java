package org.processmining.ui.statechart.workbench.discovery.vis.model;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.NoninvertibleTransformException;

import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlayManager;
import org.processmining.ui.statechart.workbench.discovery.DiscoveryWorkbenchController;
import org.processmining.utils.statechart.svg.SvgPanelUtil;
import org.processmining.utils.statechart.svg.SvgPanelUtil.Corners;

import com.kitfox.svg.RenderableElement;
import com.kitfox.svg.SVGException;

public class InfoPopupIntegration {

    @SuppressWarnings("unused")
    private DiscoveryWorkbenchController.View baseView;
    
    private NavigableSVGPanel svgPanel;
    private NodeInfoPopup nodeInfoPopup;

    private String nodeInfoHoverLastNodeId = null;
    private RenderableElement nodeInfoHoverLastNodeElement= null;
    private IEPTree dataTree;
    private IActivityLabeler dataActivityLabeler;
    
    public InfoPopupIntegration(DiscoveryWorkbenchController.View baseView,
            AnalysisAlignMetricOverlayManager overlayManager, 
            NavigableSVGPanel svgPanel) {
        this.baseView = baseView;
        this.svgPanel = svgPanel;

        nodeInfoPopup = new NodeInfoPopup(baseView, overlayManager);
        
        _registerListeners();
    }

    private void _registerListeners() {
        // click on screen, not SVG or info popup --> close info popup
        nodeInfoPopup.getPopupWindow().addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                // For popupWindow -> great
            }
            
            @Override
            public void windowLostFocus(WindowEvent e) {
                // For popupWindow -> close
                hideInfoPopup();
            }
        });
        // some events may invalidate the location of the info popup 
        // --> reposition info popup
        svgPanel.addHierarchyBoundsListener(new HierarchyBoundsListener() {
            @Override
            public void ancestorResized(HierarchyEvent e) {
                openInfoPopup();
            }
            
            @Override
            public void ancestorMoved(HierarchyEvent e) {
                openInfoPopup();
            }
        });
        svgPanel.addComponentListener(new ComponentListener() {
            
            @Override
            public void componentShown(ComponentEvent e) {
                openInfoPopup();
            }
            
            @Override
            public void componentResized(ComponentEvent e) {
                openInfoPopup();
            }
            
            @Override
            public void componentMoved(ComponentEvent e) {
                openInfoPopup();
            }
            
            @Override
            public void componentHidden(ComponentEvent e) {
                hideInfoPopup();
            }
        });

        // drag svg panel --> info popup close
        svgPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e){
                hideInfoPopup();
            }

        });
    }

    public void hideInfoPopup() {
        nodeInfoHoverLastNodeId = null;
        nodeInfoHoverLastNodeElement = null;
        nodeInfoPopup.hide();
    }

    public void setEPTree(IEPTree tree, IActivityLabeler activityLabeler) {
        this.dataTree = tree;
        this.dataActivityLabeler = activityLabeler;
    }
    
    public void setInfoPopupData(String nodeId, RenderableElement nodeElement) {
        nodeInfoHoverLastNodeId = nodeId;
        nodeInfoHoverLastNodeElement = nodeElement;
    }
    
    public void openInfoPopup() {
        String nodeId = nodeInfoHoverLastNodeId;
        RenderableElement nodeElement = nodeInfoHoverLastNodeElement;

        if (nodeElement != null && nodeId != null) {
            try {
                IEPTreeNode node = dataTree.getNodeById(nodeId);
                nodeInfoPopup.setData(dataTree, node, dataActivityLabeler);
        
                Point screenLoc = svgPanel.getLocationOnScreen();
                Dimension size = svgPanel.getSize();
                Corners userCorners = SvgPanelUtil.getUserSpaceCorners(svgPanel, nodeElement);
                
                // determine position
                double posX = 0, posY = 0;

                if ((0 <= userCorners.bottomRight.getX() + nodeInfoPopup.getWidth()
                        && userCorners.bottomRight.getX() + nodeInfoPopup.getWidth() <= size.getWidth())
                    || (0 <= userCorners.topLeft.getX() - nodeInfoPopup.getWidth()
                        && userCorners.topLeft.getX() - nodeInfoPopup.getWidth() <= size.getWidth())) {
                    // left / right
                    posX = userCorners.bottomRight.getX();
                    if (posX + nodeInfoPopup.getWidth() > size.getWidth()) {
                        posX = userCorners.topLeft.getX() - nodeInfoPopup.getWidth();
                    }
                    if (posX + nodeInfoPopup.getWidth() > size.getWidth()) {
                        posX = size.getWidth() - nodeInfoPopup.getWidth();
                    }
                    if (posX < 0) {
                        posX = 0;
                    }

                    posY = userCorners.topLeft.getY();
                    if (posY + nodeInfoPopup.getHeight() > size.getHeight()) {
                        posY = userCorners.bottomRight.getY() - nodeInfoPopup.getHeight();
                    }
                    if (posY + nodeInfoPopup.getHeight() > size.getHeight()) {
                        posY = size.getWidth() - nodeInfoPopup.getHeight();
                    }
                    if (posY < 0) {
                        posY = 0;
                    }
                } else {
                    // under / above
                    posX = userCorners.topLeft.getX();
                    if (posX + nodeInfoPopup.getWidth() > size.getWidth()) {
                        posX = userCorners.bottomRight.getX() - nodeInfoPopup.getWidth();
                    }
                    if (posX + nodeInfoPopup.getWidth() > size.getWidth()) {
                        posX = size.getWidth() - nodeInfoPopup.getWidth();
                    }
                    if (posX < 0) {
                        posX = 0;
                    }

                    posY = userCorners.bottomRight.getY();
                    if (posY + nodeInfoPopup.getHeight() > size.getHeight()) {
                        posY = userCorners.topLeft.getY() - nodeInfoPopup.getHeight();
                    }
                    if (posY + nodeInfoPopup.getHeight() > size.getHeight()) {
                        posY = size.getWidth() - nodeInfoPopup.getHeight();
                    }
                    if (posY < 0) {
                        posY = 0;
                    }
                }
                
                int viewX = (int) (screenLoc.getX() + posX);
                int viewY = (int) (screenLoc.getY() + posY);
        
                nodeInfoPopup.viewAtLocation(viewX, viewY);
            } catch (SVGException e1) {
                e1.printStackTrace();
            } catch (NoninvertibleTransformException e) {
                e.printStackTrace();
            }
        }
    }
}
