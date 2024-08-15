package org.processmining.ui.statechart.workbench.discovery.vis.model;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.algorithms.statechart.m2m.ui.UiDataConstants;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.labeling.IActivityLabeler;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.plugins.graphviz.visualisation.export.ExportDialog;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.graphviz.visualisation.export.ExporterEMF;
import org.processmining.plugins.graphviz.visualisation.export.ExporterEPS;
import org.processmining.plugins.graphviz.visualisation.export.ExporterPNG;
import org.processmining.plugins.graphviz.visualisation.export.ExporterSVG;
import org.processmining.recipes.statechart.align.AnalysisAlignMetricOverlayManager;
import org.processmining.ui.statechart.svg.SvgIcons;
import org.processmining.ui.statechart.workbench.discovery.DiscoveryWorkbenchController;
import org.processmining.ui.statechart.workbench.discovery.ISubview;
import org.processmining.utils.statechart.svg.SVGCollection;
import org.processmining.utils.statechart.svg.SVGUtil;

import com.kitfox.svg.Group;
import com.kitfox.svg.RenderableElement;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.xml.StyleAttribute;

public class SubviewSvgModel implements ISubview {

    private DiscoveryWorkbenchController.View baseView;
    private NavigableSVGPanel svgPanel;
    private InfoPopupIntegration infoPopupIntegration;
    private IEPTree dataTree;

    public SubviewSvgModel(DiscoveryWorkbenchController.View baseView,
            AnalysisAlignMetricOverlayManager overlayManager) {
        this.baseView = baseView;

        SVGCollection svgcol = new SVGCollection();
        SVGDiagram svg = svgcol.loadSVG(SvgIcons.Blank);
        svgPanel = new NavigableSVGPanel(svg);
        
        infoPopupIntegration = new InfoPopupIntegration(baseView, overlayManager, svgPanel);
        
        _registerListeners();
    }

    @Override
    public JComponent getRootComponent() {
        return svgPanel;
    }
    
    public void displayModel(SVGDiagram image, boolean resetView) {
        svgPanel.setImage(image, resetView);
    }
    
    public void setEPTree(IEPTree tree, IActivityLabeler activityLabeler) {
        this.dataTree = tree;
        infoPopupIntegration.setEPTree(tree, activityLabeler);
    }

    public void showSvgExportDialog() {
        new ExportDialog(svgPanel, Arrays.asList(new Exporter[] {
            new ExporterPNG(),
            new ExporterSVG(),
            new ExporterEPS(),
//            new ExporterPDF(), // TODO internal NoClassDef issue
            new ExporterEMF()
        }));
    }
    
    private void _registerListeners() {
        // SVG onclick actions
        // double click --> _mousePressedAtSvg() -- signal triggers:
        //      * SignalClickInspectNode
        //      * SignalClickCollapsibleNode
        // single click --> _mouseHoverOverSvg() -- info popup open / close
        svgPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                _mousePressedAtSvg(e.getPoint(), e.getClickCount() > 1);
            }

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    _mousePickInfoSvg(e.getPoint());
                }
            }
        });
    }

    protected void _mousePressedAtSvg(Point point, boolean doubleClick) {
        if (svgPanel.isInImage(point)) {
            Point2D pointImage = svgPanel.transformUser2Image(point);
            try {
                // get the elements at the clicked position
                @SuppressWarnings("unchecked")
                List<List<RenderableElement>> elements = svgPanel.getImage()
                        .pick(pointImage, true, null);

                boolean isOnIcon = false;
                boolean isCollapsible = false;
                String nodeId = null;

                StyleAttribute attr;
                for (List<RenderableElement> path : elements) {
                    for (RenderableElement element : path) {
                        attr = SVGUtil.getAttr(element, UiDataConstants.KeyDataCollapseUi);
                        if (attr != null && attr.getBooleanValue()) {
                            isOnIcon = true;
                        }

                        attr = SVGUtil.getAttr(element, UiDataConstants.KeyDataCollapsible);
                        if (attr != null && attr.getBooleanValue()) {
                            isCollapsible = true;
                        }

                        attr = SVGUtil.getAttr(element, UiDataConstants.KeyDataNodeId);
                        if (attr != null) {
                            nodeId = attr.getStringValue();
                        }
                    }
                }

                if (isOnIcon && isCollapsible && nodeId != null) {
                    baseView.SignalClickCollapsibleNode.dispatch(nodeId);
                } else if (nodeId != null && doubleClick) {
                    baseView.SignalClickInspectNode.dispatch(nodeId);
                }

            } catch (SVGException e1) {
                e1.printStackTrace();
            }
        }
    }

    protected void _mousePickInfoSvg(Point point) {
        if (svgPanel.isInImage(point) && dataTree != null) {
            Point2D pointImage = svgPanel.transformUser2Image(point);
            try {
                // get the elements at the clicked position
                @SuppressWarnings("unchecked")
                List<List<RenderableElement>> elements = svgPanel.getImage()
                        .pick(pointImage, true, null);

                boolean isOnIcon = false;
                boolean isCollapsible = false;
                String nodeId = null;
                RenderableElement nodeElement = null;
                
                StyleAttribute attr;
                for (List<RenderableElement> path : elements) {
                    for (RenderableElement element : path) {
                        attr = SVGUtil.getAttr(element, UiDataConstants.KeyDataCollapseUi);
                        if (attr != null && attr.getBooleanValue()) {
                            isOnIcon = true;
                        }

                        attr = SVGUtil.getAttr(element, UiDataConstants.KeyDataCollapsible);
                        if (attr != null && attr.getBooleanValue()) {
                            isCollapsible = true;
                        }
                        
                        attr = SVGUtil.getAttr(element, UiDataConstants.KeyDataNodeId);
                        if (attr != null) {
                            nodeId = attr.getStringValue();
                            // prefer group elements, due to better popup positioning
                            if (nodeElement == null 
                                || !(nodeElement instanceof Group)
                                || element instanceof Group) {
                                nodeElement = element;
                            }
                        }
                    }
                }

                if (nodeElement != null && nodeId != null
                        && !(isOnIcon && isCollapsible)) {
                    infoPopupIntegration.setInfoPopupData(nodeId, nodeElement);
                    infoPopupIntegration.openInfoPopup();
                    return;
                }

            } catch (SVGException e1) {
                e1.printStackTrace();
            }
        }
        infoPopupIntegration.hideInfoPopup();
    }
}
