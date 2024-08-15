package org.processmining.utils.statechart.svg;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;

import com.kitfox.svg.RenderableElement;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;

public class SvgPanelUtil {

    public static class Corners {
        public final Point2D topLeft;
        public final Point2D bottomRight;
        
        public Corners(Point2D topLeft, Point2D bottomRight) {
            this.topLeft = topLeft;
            this.bottomRight = bottomRight;
        }
    }
    
    private SvgPanelUtil() {
    }

    public static Corners getUserSpaceCorners(NavigableSVGPanel svgPanel, RenderableElement elm) throws NoninvertibleTransformException, SVGException {
        // Calculate position top right
        Rectangle2D elBB = elm.getBoundingBox();
        Point2D.Double elPosAdjTL = new Point2D.Double(elBB.getX(), elBB.getY());
        adjustForTransformations(elPosAdjTL, elm);
        
        // Calculate position bottom right
        Point2D.Double elPosAdjBR = new Point2D.Double(
            elBB.getX() + elBB.getWidth(), 
            elBB.getY() + elBB.getHeight()
        );
        adjustForTransformations(elPosAdjBR, elm);
        
        // adjust for view XForm
        AffineTransform viewXForm = svgPanel.getImage().getRoot().getViewXform();
        viewXForm.transform(elPosAdjTL, elPosAdjTL);
        viewXForm.transform(elPosAdjBR, elPosAdjBR);
        
        // Transform to user space
        Point2D userLocAdjTL = svgPanel.transformImage2User(elPosAdjTL);
        Point2D userLocAdjBR = svgPanel.transformImage2User(elPosAdjBR);

        return new Corners(userLocAdjTL, userLocAdjBR);
    }

    private static void adjustForTransformations(Point2D.Double pos, SVGElement elm) {
        while (elm != null) {
            if (elm instanceof RenderableElement) {
                AffineTransform pXForm = ((RenderableElement) elm).getXForm();
                if (pXForm != null) {
                    pXForm.transform(pos, pos);
                }
            }
            elm = elm.getParent();
        }
    }
}
