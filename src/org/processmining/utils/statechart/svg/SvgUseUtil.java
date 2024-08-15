package org.processmining.utils.statechart.svg;

import java.awt.geom.Rectangle2D;

import com.google.common.base.Preconditions;
import com.kitfox.svg.Group;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

public class SvgUseUtil {

    /**
     * Types of anchors for positioning svg
     * 
     * @author mleemans
     *
     *         Encodes the offset factor matrices for easy calculations
     */
    public static enum Anchor {
        TopLeft(0.0f, 0.0f, 0.0f, 0.0f), 
        TopCenter(0.5f, -0.5f, 0.0f, 0.0f), 
        TopRight(1.0f, -1.0f, 0.0f, 0.0f),

        MiddleLeft(0.0f, 0.0f, 0.5f, -0.5f), 
        Center(0.5f, -0.5f, 0.5f, -0.5f), 
        MiddleRight(1.0f, -1.0f, 0.5f, -0.5f),

        BottomLeft(0.0f, 0.0f, 1.0f, -1.0f), 
        BottomCenter(0.5f, -0.5f, 1.0f, -1.0f), 
        BottomRight(1.0f, -1.0f, 1.0f, -1.0f);

        private float factorParentWidth;
        private float factorChildWidth;
        private float factorParentHeight;
        private float factorChildHeight;

        private Anchor(float factorParentWidth, float factorChildWidth,
                float factorParentHeight, float factorChildHeight) {
            this.factorParentWidth = factorParentWidth;
            this.factorChildWidth = factorChildWidth;
            this.factorParentHeight = factorParentHeight;
            this.factorChildHeight = factorChildHeight;
        }

        public float calcX(float parentPosX, float parentWidth,
                float childWidth, Margin margin) {
            float x = parentPosX + parentWidth * factorParentWidth + childWidth
                    * factorChildWidth;
            if (factorParentWidth == 0.0f) {
                x += margin.left;
            } else if (factorParentWidth == 1.0f) {
                x -= margin.right;
            }
            return x;
        }

        public float calcY(float parentPosY, float parentHeight,
                float childHeight, Margin margin) {
            float y = parentPosY + parentHeight * factorParentHeight
                    + childHeight * factorChildHeight;
            if (factorParentHeight == 0.0f) {
                y += margin.top;
            } else if (factorParentHeight == 1.0f) {
                y -= margin.bottom;
            }
            return y;
        }
    }

    /**
     * Margin for placing SVG's (4 offsets)
     * 
     * @author mleemans
     *
     */
    public static class Margin {
        public static final Margin Zero = new Margin(0.0f);

        public final float top, right, bottom, left;

        public Margin(float amount) {
            this(amount, amount, amount, amount);
        }

        public Margin(float vmargin, float hmargin) {
            this(vmargin, hmargin, vmargin, hmargin);
        }

        public Margin(double vmargin, double hmargin) {
            this((float) vmargin, (float) hmargin, (float) vmargin, (float) hmargin);
        }

        public Margin(float top, float right, float bottom, float left) {
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.left = left;
        }
    }

    /**
     * Add SVG use as child to target group, scaled to ratio
     * 
     * @param target
     *            add to this group
     * @param useSVG
     *            element to reference
     * @param ratio
     *            between 0.0 and 1.0 for 0% till 100% fill
     * @throws SVGException
     */
    public static UseFixed addSvg(Group target, SVGDiagram useSVG, Anchor anchor,
            float ratio, Margin margin) throws SVGException {
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(useSVG);
        Preconditions.checkNotNull(anchor);
        Preconditions.checkNotNull(margin);

        target.updateTime(0.0); // force BB calculations
        Rectangle2D bbTo = target.getBoundingBox();

        float targetWidth = (float) bbTo.getWidth() * ratio;
        float targetHeight = (float) bbTo.getHeight() * ratio;

        float scaleH = targetHeight / useSVG.getHeight();
        float scaleW = targetWidth / useSVG.getWidth();

        float scale = Math.min(scaleH, scaleW);

        float toWidth = (float) useSVG.getWidth() * scale;
        float toHeight = (float) useSVG.getHeight() * scale;

        return addSvg(target, useSVG, anchor, toWidth, toHeight, margin);
    }

    /**
     * Add SVG use as child to target group, with given size
     * 
     * @param target
     * @param useSVG
     * @param anchor
     * @param toWidth
     * @param toHeight
     * @throws SVGException
     */
    public static UseFixed addSvg(Group target, SVGDiagram useSVG, Anchor anchor,
            float toWidth, float toHeight, Margin margin) throws SVGException {
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(useSVG);
        Preconditions.checkNotNull(anchor);
        Preconditions.checkNotNull(margin);

        target.updateTime(0.0); // force BB calculations
        Rectangle2D bbTo = target.getBoundingBox();

        float x = anchor.calcX((float) bbTo.getMinX(), (float) bbTo.getWidth(),
                toWidth, margin);
        float y = anchor.calcY((float) bbTo.getMinY(),
                (float) bbTo.getHeight(), toHeight, margin);

        UseFixed use = new UseFixed();
        use.setRefSvg(useSVG);
        use.setPos(x, y);
        use.setSize(toWidth, toHeight);

        target.loaderAddChild(null, use);
        return use;
    }
}
