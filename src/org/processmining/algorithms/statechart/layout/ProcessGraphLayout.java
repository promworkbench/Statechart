package org.processmining.algorithms.statechart.layout;

import gnu.trove.impl.unmodifiable.TUnmodifiableObjectDoubleMap;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public class ProcessGraphLayout<T> {

    private final ModelForPGLayout<T> model;
    private final PGNodeExtendProvider<T> nodeExtentProvider;
    private final PGLayoutConfiguration<T> configuration;

    private final Map<T, Rectangle2D.Double> bounds;
    private final TObjectDoubleMap<T> centerOffsets;
    private final Rectangle2D.Double rootBounds;
    
    public ProcessGraphLayout(ModelForPGLayout<T> model, 
            PGNodeExtendProvider<T> nodeExtentProvider,
            PGLayoutConfiguration<T> configuration, 
            boolean useIdentity) {
        
        this.model = model;
        this.nodeExtentProvider = nodeExtentProvider;
        this.configuration = configuration;
        
        if (useIdentity) {
            bounds = new IdentityHashMap<>();
        } else {
            bounds = new THashMap<>();
        }
        centerOffsets = new TObjectDoubleHashMap<>();
        
        rootBounds = calculateBounds(model.getRoot());
        calculatePositions(model.getRoot());
    }

    public ModelForPGLayout<T> getModel() {
        return model;
    }

    public PGNodeExtendProvider<T> getNodeExtendProvider() {
        return nodeExtentProvider;
    }

    public PGLayoutConfiguration<T> getConfiguration() {
        return configuration;
    }

    public Map<T, Rectangle2D.Double> getNodeBounds() {
        return Collections.unmodifiableMap(bounds);
    }

    public TObjectDoubleMap<T> getCenterOffsets() {
        return new TUnmodifiableObjectDoubleMap<T>(centerOffsets);
    }
    
    public Rectangle2D.Double getBounds() {
        return rootBounds;
    }

    private Rectangle2D.Double calculateBounds(T node) {
        Rectangle2D.Double bound = new Rectangle2D.Double();
        Padding padding = configuration.getPaddingNode(node);
        
        if (model.isLeaf(node)) {
            // Single leaf box
            bound.width = nodeExtentProvider.getWidth(node) + padding.getHorizontal();
            bound.height = nodeExtentProvider.getHeight(node) + padding.getVertical();
            centerOffsets.put(node, getLogicalHeight(bound) * 0.5);
        } else {
            // Container
            if (configuration.isLayoutOrtogonal(node)) {
                // Stack in place
                double extendUpDown = 0;
                double extendForward = 0;
                
                T prevChild = null;
                for (T child : _getIterable(node)) {
                    final Rectangle2D.Double addBound = calculateBounds(child);
                    double logicWidth = getLogicalWidth(addBound);
                    double logicHeight = getLogicalHeight(addBound);
                    
                    extendForward = Math.max(extendForward, logicWidth);
                    extendUpDown += logicHeight;
                    if (prevChild != null) {
                        extendUpDown += configuration.getGapBetweenNodes(prevChild, child);
                    }
                    
                    prevChild = child;
                }

                if (configuration.getDirection().isHorizontal()) {
                    bound.width = extendForward;
                    bound.height = extendUpDown;
                } else {
                    bound.height = extendForward;
                    bound.width = extendUpDown;
                }
                
                bound.width += padding.getHorizontal();
                bound.height += padding.getVertical();
                centerOffsets.put(node, extendUpDown * 0.5 + getLogicalTop(padding));
            } else {
                // From start to end
                double extendUp = 0;
                double extendDown = 0;
                double extendForward = 0;
                
                T prevChild = null;
                for (T child : _getIterable(node)) {
                    final Rectangle2D.Double addBound = calculateBounds(child);
                    double logicHeight = getLogicalHeight(addBound);
                    double centerOffset = centerOffsets.get(child);
                    
                    switch (configuration.getNodeCenterAlignment(child)) {
                    case CenterOnFirstChild:
                        T firstSubChild = model.getFirstChild(child);
                        if (firstSubChild != null) {
                            double subCenterOffset = centerOffsets.get(firstSubChild);
                            
                            double heightUp = subCenterOffset 
                                    + getLogicalHeight(configuration.getPaddingNode(child));
                            
                            extendUp = Math.max(extendUp, heightUp);
                            extendDown = Math.max(extendDown, logicHeight - heightUp);
                        }
                        break;
                    case CenterWhole:
                    default:
                        extendUp = Math.max(extendUp, centerOffset);
                        extendDown = Math.max(extendDown, logicHeight - centerOffset);
                        break;
                    }
                    
                    extendForward += getLogicalWidth(addBound);
                    if (prevChild != null) {
                        extendForward += configuration.getGapBetweenNodes(prevChild, child);
                    }
                    
                    prevChild = child;
                }
                
                if (configuration.getDirection().isHorizontal()) {
                    bound.width = extendForward;
                    bound.height = extendUp + extendDown;
                } else {
                    bound.height = extendForward;
                    bound.width = extendUp + extendDown;
                }

                bound.width += padding.getHorizontal();
                bound.height += padding.getVertical();
                double centerOffset = extendUp + getLogicalTop(padding);
                        
                double ownWidth = nodeExtentProvider.getWidth(node) + padding.getHorizontal();
                double ownHeight = nodeExtentProvider.getHeight(node) + padding.getVertical();
                double ownCenterOffset = getLogicalHeight(bound) * 0.5;
                
                bound.width = Math.max(bound.width, ownWidth);
                if (ownHeight > bound.height) {
                    bound.height = ownHeight;
                    centerOffset = ownCenterOffset;
                }
                
                centerOffsets.put(node, centerOffset);
            }
        }
        
        bounds.put(node, bound);
        return bound;
    }

    private Iterable<T> _getIterable(T node) {
        if (configuration.isLayoutReverse(node)) {
            return model.getChildrenReverse(node);
        } else {
            return model.getChildren(node);
        }
    }

    private void calculatePositions(T node) {
        if (!model.isLeaf(node)) {
            // Container
            final Rectangle2D.Double bound = bounds.get(node);
            double nodeCenterOffset = centerOffsets.get(node);
            Padding padding = configuration.getPaddingNode(node);
            
            if (configuration.isLayoutOrtogonal(node)) {
                // Stack in place
                double extendUpDown = getLogicalPosV(bound) + getLogicalTop(padding);
                double logicWidth = getLogicalWidth(bound);

                T prevChild = null;
                for (T child : _getIterable(node)) {
                    if (prevChild != null) {
                        extendUpDown += configuration.getGapBetweenNodes(prevChild, child);
                    }
                    
                    final Rectangle2D.Double childBound = bounds.get(child);
                    double childLogicWidth = getLogicalWidth(childBound);
                    double childLogicHeight = getLogicalHeight(childBound);
//                    double centerOffset = centerOffsets.get(child);
                    
                    double posForward = getLogicalPosH(bound);
                    double posUpDown = extendUpDown;
                    switch (configuration.getNodeForwardAlignment(child)) {
                    case Middle:
                        posForward += logicWidth * 0.5 - childLogicWidth * 0.5;
                        break;
                    case End:
                        posForward += logicWidth - getLogicalRight(padding);
                        break;
                    case Start:
                    default:
                        posForward += getLogicalLeft(padding);
                        break;
                    }

                    if (configuration.getDirection().isHorizontal()) {
                        childBound.x = posForward;
                        childBound.y = posUpDown;
                    } else {
                        childBound.y = posForward;
                        childBound.x = posUpDown;
                    }

                    extendUpDown += childLogicHeight;
                    
                    prevChild = child;
                }
            } else {
                // From start to end
                double extendForward = getLogicalPosH(bound) + getLogicalLeft(padding);

                T prevChild = null;
                for (T child : _getIterable(node)) {
                    if (prevChild != null) {
                        extendForward += configuration.getGapBetweenNodes(prevChild, child);
                    }
                    
                    final Rectangle2D.Double childBound = bounds.get(child);
                    double childLogicWidth = getLogicalWidth(childBound);
//                    double childLogicHeight = getLogicalHeight(childBound);
                    double centerOffset = centerOffsets.get(child);
                    
                    Padding childPadding = configuration.getPaddingNode(child);
                    
                    double posForward = extendForward;
                    double posUpDown = getLogicalPosV(bound) + nodeCenterOffset - centerOffset;

                    switch (configuration.getNodeCenterAlignment(child)) {
                    case CenterOnFirstChild:
                        T firstSubChild = model.getFirstChild(child);
                        if (firstSubChild != null) {
                            double subCenterOffset = centerOffsets.get(firstSubChild);
                            posUpDown = getLogicalPosV(bound) + nodeCenterOffset - subCenterOffset - getLogicalTop(childPadding);
                        } // else already calculated
                        
                        break;
                    case CenterWhole:
                    default:
                        // already calculated
                        break;
                    }
                    
                    
                    if (configuration.getDirection().isHorizontal()) {
                        childBound.x = posForward;
                        childBound.y = posUpDown;
                    } else {
                        childBound.y = posForward;
                        childBound.x = posUpDown;
                    }

                    extendForward += childLogicWidth;
                    
                    prevChild = child;
                }
            }
            
            // All children are positioned, update their subchildren
            for (T child : model.getChildren(node)) {
                calculatePositions(child);
            }
        }
    }

    private double getLogicalPosH(Rectangle2D.Double bound) {
        if (configuration.getDirection().isHorizontal()) {
            return bound.getX();
        } else {
            return bound.getY();
        }
    }

    private double getLogicalPosV(Rectangle2D.Double bound) {
        if (configuration.getDirection().isHorizontal()) {
            return bound.getY();
        } else {
            return bound.getX();
        }
    }

    private double getLogicalHeight(Rectangle2D.Double bound) {
        if (configuration.getDirection().isHorizontal()) {
            return bound.getHeight();
        } else {
            return bound.getWidth();
        }
    }
    
    private double getLogicalWidth(Rectangle2D.Double bound) {
        if (configuration.getDirection().isHorizontal()) {
            return bound.getWidth();
        } else {
            return bound.getHeight();
        }
    }
    
    private double getLogicalTop(Padding padding) {
        if (configuration.getDirection().isHorizontal()) {
            return padding.getTop();
        } else {
            return padding.getLeft();
        }
    }

    private double getLogicalRight(Padding padding) {
        if (configuration.getDirection().isHorizontal()) {
            return padding.getRight();
        } else {
            return padding.getBottom();
        }
    }

    private double getLogicalLeft(Padding padding) {
        if (configuration.getDirection().isHorizontal()) {
            return padding.getLeft();
        } else {
            return padding.getTop();
        }
    }

    private double getLogicalHeight(Padding padding) {
        if (configuration.getDirection().isHorizontal()) {
            return padding.getVertical();
        } else {
            return padding.getHorizontal();
        }
    }
}
