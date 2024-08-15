package org.processmining.algorithms.statechart.m2m.ui.layout;

import gnu.trove.map.hash.THashMap;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.Map;

import org.processmining.algorithms.statechart.layout.ModelForPGLayout;
import org.processmining.algorithms.statechart.layout.ProcessGraphLayout;
import org.processmining.models.statechart.decorate.tracing.TraceUniqueDecorator;
import org.processmining.models.statechart.eptree.IEPTree;
import org.processmining.models.statechart.eptree.IEPTreeNode;
import org.processmining.models.statechart.sc.ISCState;
import org.processmining.models.statechart.sc.Statechart;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class StatechartLayoutAdapter implements
        ModelForPGLayout<StatechartLayoutNode> {
    
    private StatechartLayoutNode root;
    
    public StatechartLayoutAdapter(IEPTree inputTree, Statechart inputStatechart) {
        Preconditions.checkNotNull(inputTree);
        Preconditions.checkNotNull(inputStatechart);
        
        @SuppressWarnings("unchecked")
        TraceUniqueDecorator<ISCState, IEPTreeNode> treeTracer = inputStatechart
                .getStateDecorations().getForType(TraceUniqueDecorator.class);
        Preconditions.checkNotNull(treeTracer);
        
        // create root of layout tree
        root = new StatechartLayoutNode(null, null, inputStatechart);
        Map<IEPTreeNode, StatechartLayoutNode> treeNodeLookup = new THashMap<>();
        
        // Build blank layout tree from process tree
        for (IEPTreeNode node : inputTree.iteratePreOrder()) {
            // fix parent layout node
            IEPTreeNode parent = node.getParent();
            StatechartLayoutNode layoutParent = root;
            if (parent != null) {
                layoutParent = treeNodeLookup.get(parent);
            }
            
            // add layout control node for specific types
            switch (node.getNodeType()) {
            case Choice:
            case AndComposite:
            case Loop:
            case SeqCancel:
            case LoopCancel:
                // add extra layout node
                StatechartLayoutNode layoutNode = new StatechartLayoutNode(layoutParent, null, null);
                layoutParent.addChild(layoutNode);
                layoutParent = layoutNode;
                break;
            default:
                // nop
                break;
            }
            
            // add empty layout node
            StatechartLayoutNode layoutNode 
                = new StatechartLayoutNode(layoutParent, node, null);
            layoutParent.addChild(layoutNode);
            treeNodeLookup.put(node, layoutNode);
        }
        
        // Visit all the statechart states, and add to layout tree
        for (ISCState state : inputStatechart.iteratePreOrder()) {
            if (state == inputStatechart) {
                continue;
            }
            if (inputStatechart.isInitialState(state)
                || inputStatechart.isEndState(state)) {
                StatechartLayoutNode newLayoutNode = new StatechartLayoutNode(root, null, state);
                if (inputStatechart.isInitialState(state)) {
                    root.getChildren().add(0, newLayoutNode);
                } else {
                    root.addChild(newLayoutNode);
                }
                continue;
            }
            
            IEPTreeNode node = treeTracer.getDecoration(state);
            Preconditions.checkNotNull(node);
            
            StatechartLayoutNode layoutNode = treeNodeLookup.get(node);
            Preconditions.checkNotNull(layoutNode);
            
            switch (state.getStateType()) {
            case ChoicePseudo:
            case JoinPseudo:
            case SplitPseudo:
            case PointPseudo:
                // add a new child to parent layout control node
                layoutNode = layoutNode.getParent();
                StatechartLayoutNode newLayoutNode = new StatechartLayoutNode(layoutNode, null, state);
                boolean addPostfix = true;
                if (state.getPostset().size() > 1) {
                    addPostfix = false;
                }
                if (isControlForLoop(node)) {
                        addPostfix = !addPostfix;
                }
                if (addPostfix) {
                    layoutNode.addChild(newLayoutNode);
                } else {
                    layoutNode.getChildren().add(0, newLayoutNode);
                }
                break;
            default:
                // add to node itself
                layoutNode.setState(state);
                break;
            }
        }
    }
    
    private boolean isControlForLoop(IEPTreeNode node) {
        switch (node.getNodeType()) {
        case Loop:
        case LoopCancel:
            return true;
        default:
            return false;
        }
    }
    
/*
    private StatechartLayoutNode root;
//    private Map<ISCState, StatechartLayoutNode> mapLayout;
    
    private final Map<String, IEPTreeNode> lookupTree;
    private final Map<String, ISCState> lookupSC;
    private final Map<String, StatechartLayoutNode> lookupLayout;
    
    public StatechartLayoutAdapter(IEPTree inputTree, Statechart inputStatechart) {
        // Build lookup maps
        lookupTree = new THashMap<>();
        for (IEPTreeNode node : inputTree.iteratePreOrder()) {
            lookupTree.put(node.getId(), node);
        }
        
        lookupSC = new THashMap<>();
        for (ISCState state : inputStatechart.iteratePreOrder()) {
            lookupSC.put(state.getId(), state);
        }
        
        lookupLayout = new THashMap<>();

        // create root of layout tree
        root = new StatechartLayoutNode(null, null, inputStatechart);
        lookupLayout.put(inputStatechart.getId(), root);
        
//        ISCRegion region = inputStatechart.getRegions().get(0);
        
//        root.addChild(new StatechartLayoutNode(root, null, 
//            region.getInitialState()));
        
        _process(inputTree, inputStatechart);
        
//        root.visitDepthFirstOrder(new Action1<StatechartLayoutNode>() {
//            @Override
//            public void call(StatechartLayoutNode t) {
//                // nop
//            }
//        }, new Action1<StatechartLayoutNode>() {
//            @Override
//            public void call(StatechartLayoutNode t) {
//                // sort children on transitions
//            }
//        });
        
//        root.addChild(new StatechartLayoutNode(root, null, 
//            region.getEndStates().iterator().next()));
    }

//    private void _process(IEPTree inputTree, Statechart inputStatechart) {
//        ISCRegion baseRegion = inputStatechart.getRegions().get(0);
//        
//        for (ISCState state : inputStatechart.iteratePreOrder()) {
//            if (!lookupLayout.containsKey(state.getId())) {
//                if (state == baseRegion.getInitialState()
//                    || baseRegion.getEndStates().contains(state)) {
//                    StatechartLayoutNode node = new StatechartLayoutNode(root, null, state);
//                    root.addChild(node);
//                    lookupLayout.put(state.getId(), node);
//                } else {
//                    _createSubtree(state);
//                }
//            }
//        }
//        
//    }
    
    private void _process(IEPTree inputTree, Statechart inputStatechart) {
        ISCRegion baseRegion = inputStatechart.getRegions().get(0);
        ISCState start = baseRegion.getInitialState();
        
        // Adaptive BSF walking over the Statechart in order
        Set<ISCState> visited = new THashSet<>();
        Deque<ISCState> todo = new ArrayDeque<>();
        visited.add(start);
        todo.add(start);

        Set<ISCState> lastToProcess = new THashSet<>();
        
        while (!todo.isEmpty()) {
            ISCState current = todo.remove();

            StatechartLayoutNode node;
            if (current == baseRegion.getInitialState()
                    || baseRegion.getEndStates().contains(current)) {
                // add start and ends to root of the layout tree
                node = new StatechartLayoutNode(root, null, current);
                root.addChild(node);
                lookupLayout.put(current.getId(), node);
            } else {
                // create part of layout tree
                node = _createSubtree(current);
                if (node == null) {
                    lastToProcess.add(current);
                    for (ISCState next : current.getPostset()) {
                        if (!visited.contains(next)) {
                            visited.add(next);
                            todo.add(next);
                        }
                    }
                }
            }
            
            // enqueue postset of node and parents
            while (node != null) {
                if (node.getState() != null) {
                    for (ISCState next : node.getState().getPostset()) {
                        if (!visited.contains(next)) {
                            visited.add(next);
                            todo.add(next);
                        }
                    }
                }
                node = node.getParent();
            }
        }
        
        for (ISCState current : lastToProcess) {
            // look for right parent via pre or post sets 
            StatechartLayoutNode node, parent;
            boolean addPostfix;
            if (current.getPostset().size() > 1) {
                parent = findControlNode(current.getPostset());
                node = new StatechartLayoutNode(parent, null, current);
                addPostfix = false;
            } else {
                parent = findControlNode(current.getPreset());
                node = new StatechartLayoutNode(parent, null, current);
                addPostfix = true;
            }
            
            if (isControlForLoop(parent)) {
                addPostfix = !addPostfix;
            }
            if (addPostfix) {
                parent.addChild(node);
            } else {
                parent.getChildren().add(0, node);
            }
            lookupLayout.put(current.getId(), node);
        }
    }

    private boolean isControlForLoop(StatechartLayoutNode node) {
        for (StatechartLayoutNode child : node.getChildren()) {
            if (child.getNode() != null) {
                switch (child.getNode().getNodeType()) {
                case Loop:
                case LoopCancel:
                    return true;
                default:
                    return false;
                }
            }
        }
        return false;
    }

    private StatechartLayoutNode _createSubtree(ISCState current) {
        // first up, try finding matching Tree node, and query for parent
        IEPTreeNode treeNode = lookupTree.get(current.getId());
        if (treeNode != null) {
            // recursively create path from root to this node
            return getLayout(treeNode);
        } else {
            // otherwise, delay
            return null;
        }
    }

    private StatechartLayoutNode getLayout(IEPTreeNode treeNode) {
        if (treeNode == null) {
            return root;
        }
        
        StatechartLayoutNode node = lookupLayout.get(treeNode.getId());
        if (node == null) {
            StatechartLayoutNode parent = getLayout(treeNode.getParent());
            if (controlNodeWithTaus(treeNode)) {
                // add 'sequence control' layout node for special taus
                node = new StatechartLayoutNode(parent, null, null);
                parent.addChild(node);
                parent = node;
            }
            node = new StatechartLayoutNode(parent, treeNode, lookupSC.get(treeNode.getId()));
            parent.addChild(node);
            lookupLayout.put(treeNode.getId(), node);
        }
        
        return node;
    }

    private boolean controlNodeWithTaus(IEPTreeNode treeNode) {
        switch (treeNode.getNodeType()) {
        case AndComposite:
        case Choice:
        case Loop:
        case LoopCancel:
        case SeqCancel:
            return true;
        default:
            return false;
        }
    }

    private StatechartLayoutNode findControlNode(Set<ISCState> stateset) {
        List<List<StatechartLayoutNode>> paths = new ArrayList<>();
        int minDepth = Integer.MAX_VALUE;
        
        boolean rootInvolved = false;
        for (ISCState state : stateset) {
            StatechartLayoutNode layoutSearchNode = lookupLayout.get(state.getId());
            IEPTreeNode treeNode = layoutSearchNode.getNode();
            while (treeNode == null && layoutSearchNode != null) {
                if (layoutSearchNode.getState() instanceof Statechart) {
                    rootInvolved = true;
                }
                layoutSearchNode = layoutSearchNode.getParent();
                if (layoutSearchNode != null) {
                    treeNode = layoutSearchNode.getNode();
                }
            }
            if (treeNode != null) {
                List<StatechartLayoutNode> path = new ArrayList<>();
                buildParentPath(treeNode, path);
                paths.add(path);
                minDepth = Math.min(minDepth, path.size());
            }
        }

        // search for Least Common Ancestor
        StatechartLayoutNode lastOk = root;
        boolean done = false;
        if (!paths.isEmpty()) {
            StatechartLayoutNode candidate = null;
            if (rootInvolved) {
                for (int j = 0; j < paths.size() && !done; j++) {
                    if (!paths.get(j).isEmpty()) {
                        candidate = paths.get(j).get(0); 
                        done = true;
                    }
                }
            } else {
                for (int i = 0; i < minDepth && !done; i++) {
                    candidate = paths.get(0).get(i);
                    for (int j = 1; j < paths.size() && !done; j++) {
                        if (candidate != paths.get(j).get(i)) {
                            // Found the point where paths diverge
                            //return lastOk;
                            done = true;
                        }
                    }
                    if (!done) {
                        lastOk = candidate;
                    }
                }
            }
        }
        
        // Explore at this point for a <null, null> node
        if (!_isLayoutControlNode(lastOk)) {
            // Try parent first (Split-Join case)
            if (_isLayoutControlNode(lastOk.getParent())) {
                lastOk = lastOk.getParent();
            } else {
                // BFS on children (Loop tau case)
                Set<StatechartLayoutNode> visited = new THashSet<>();
                Deque<StatechartLayoutNode> todo = new ArrayDeque<>();
                visited.add(lastOk);
                todo.add(lastOk);
                while (!todo.isEmpty()) {
                    StatechartLayoutNode current = todo.remove();
                    if (_isLayoutControlNode(current)) {
                        lastOk = current;
                        todo.clear();
                    } else {
                        for (StatechartLayoutNode next : current.getChildren()) {
                            if (!visited.contains(next)) {
                                visited.add(next);
                                todo.add(next);
                            }
                        }
                    }
                }
            }
        }
        
        return lastOk;
    }

//    private StatechartLayoutNode getLCA(ISCState current) {
//        List<List<StatechartLayoutNode>> paths = new ArrayList<>();
//        int minDepth = Integer.MAX_VALUE;
//        
//        Set<ISCState> states = new THashSet<>(current.getPreset().size() + current.getPostset().size());
//        states.addAll(current.getPreset());
//        states.addAll(current.getPostset());
//        
//        for (ISCState state : states) {
//            IEPTreeNode treeNode = lookupTree.get(state.getId());
//            if (treeNode != null) {
//                List<StatechartLayoutNode> path = new ArrayList<>();
//                buildParentPath(treeNode, path);
//                paths.add(path);
//                minDepth = Math.min(minDepth, path.size());
//            }
//        }
//
//        StatechartLayoutNode lastOk = root;
//        if (!paths.isEmpty()) {
//            StatechartLayoutNode candidate = null;
//            for (int i = 0; i < minDepth; i++) {
//                candidate = paths.get(0).get(i);
//                for (int j = 1; j < paths.size(); j++) {
//                    if (candidate != paths.get(j).get(i)) {
//                        return lastOk;
//                    }
//                }
//                lastOk = candidate;
//            }
//        }
//        
//        return lastOk;
//    }

    private boolean _isLayoutControlNode(StatechartLayoutNode node) {
        return node != null && node.getNode() == null 
                && node.getState() == null;
    }

    private void buildParentPath(IEPTreeNode treeNode,
            List<StatechartLayoutNode> path) {
        if (treeNode == null) {
            return;
        }
        
        buildParentPath(treeNode.getParent(), path);
        
        StatechartLayoutNode node = lookupLayout.get(treeNode.getId());
        if (node != null) {
            path.add(node);
        }
    }
    */
    @Override
    public StatechartLayoutNode getRoot() {
        return root;
    }

    @Override
    public Iterable<StatechartLayoutNode> getChildren(StatechartLayoutNode node) {
        return node.getChildren();
    }

    @Override
    public Iterable<StatechartLayoutNode> getChildrenReverse(StatechartLayoutNode node) {
        return Lists.reverse(node.getChildren());
    }

    @Override
    public StatechartLayoutNode getFirstChild(StatechartLayoutNode node) {
        return node.getChildren().get(0);
    }

    @Override
    public boolean isLeaf(StatechartLayoutNode node) {
        return node.getChildren().isEmpty();
    }
    
    public Map<ISCState, Rectangle2D.Double> filterLayout(
            ProcessGraphLayout<StatechartLayoutNode> processGraphLayout) {
        Map<StatechartLayoutNode, Double> allBounds = processGraphLayout.getNodeBounds();
        Map<ISCState, Rectangle2D.Double> bounds = new THashMap<>();
        
        for (StatechartLayoutNode key : allBounds.keySet()) {
            if (key.getState() != null) {
                bounds.put(key.getState(), allBounds.get(key));
            }
        }
        
        return bounds;
    }
}
