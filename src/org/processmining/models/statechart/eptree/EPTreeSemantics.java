package org.processmining.models.statechart.eptree;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.models.statechart.decorate.error.EPTreeErrorTriggerDecorator;

public class EPTreeSemantics {

    public static Set<IEPTreeNode> getNodePreset(IEPTreeNode subject) {
        Set<IEPTreeNode> result = new THashSet<>();
        Set<IEPTreeNode> covered = new THashSet<>();
        _deriveNodePreset(subject, covered, result);
        return result;
    }

    public static Set<IEPTreeNode> getNodeClosingSet(IEPTreeNode subject) {
        Set<IEPTreeNode> result = new THashSet<>();
        Set<IEPTreeNode> covered = new THashSet<>();
        _deriveClosingSet(subject, covered, result);
        return result;
    }

    private static void _deriveNodePreset(IEPTreeNode subject, Set<IEPTreeNode> covered, Set<IEPTreeNode> result) {
        // we investigate <node>, and came from <subject>
        IEPTreeNode node = subject.getParent();
        IEPTreeNode child = subject;
        
        while(node != null) {
            switch (node.getNodeType()) {
            case OrComposite:
                result.add(node);
                node = null; // don't investigate parent
                break;
            case Choice:
            case AndComposite:
            case AndInterleaved:
                // investigate parent of <node>, so <node> becomes the new child
                child = node;
                node = node.getParent();
                break;
            case Seq:
            {
                int index = node.getChildren().indexOf(child);
                if (index > 0) {
                    // get the closing language of the previous element in the Seq
                    _deriveClosingSet(node.getChildren().get(index - 1), covered, result);
                    node = null;
                } else {
                    // investigate parent of <node>, so <node> becomes the new child
                    child = node;
                    node = node.getParent();
                }
                break;
            }
            case Loop:
            {
                int index = node.getChildren().indexOf(child);
                if (index >= 0) {
                    if (index == 0) {
                        // get the closing language from the redo part ...
                        Iterator<IEPTreeNode> it = node.getChildren().iterator();
                        it.next(); // skip first
                        while (it.hasNext()) {
                            _deriveClosingSet(it.next(), covered, result);
                        }
                        // ... and investigate parent of <node>, so <node> becomes the new child
                        child = node;
                        node = node.getParent();
                    } else {
                        _deriveClosingSet(node.getChildren().get(0), covered, result);
                        node = null; // don't investigate parent
                    }
                } else {
                    throw new IllegalStateException("Child not in children list of parent");
                }
                break;
            }
            case SeqCancel:
            case LoopCancel:
            {
                int index = node.getChildren().indexOf(child);
                if (index >= 0) {
                    if (index == 0) {
                        if (node.getNodeType() == EPNodeType.LoopCancel) {
                            // get the closing language from the redo part ...
                            Iterator<IEPTreeNode> it = node.getChildren().iterator();
                            it.next(); // skip first
                            while (it.hasNext()) {
                                _deriveClosingSet(it.next(), covered, result);
                            }
                        }
                        // normal flow part, investigate parent of <node>, so <node> becomes the new child
                        child = node;
                        node = node.getParent();
                    } else {
                        // cancel catch part, investigate triggers
                        
                        // 1. collect part of triggers we're interested in
                        Set<IEPTreeNode> candidates = new THashSet<IEPTreeNode>();
                        Set<IEPTreeNode> coveredCandidates = new THashSet<IEPTreeNode>();
                        _deriveOpeningSet(subject, coveredCandidates, candidates);
                        
                        // 2. find corresponding triggers (prefix semantics, so investigate all)
                        EPTreeErrorTriggerDecorator decorator = node.getTree().getDecorations()
                                .getForType(EPTreeErrorTriggerDecorator.class);
                        if (decorator != null) {
                            for (IEPTreeNode cand : node.getChildren().get(0).iteratePreOrder()) {
                                if (cand.getNodeType() == EPNodeType.ErrorTrigger) {
                                    // check if trigger overlaps with any candidate
                                    Set<String> triggered = decorator.getDecoration(cand);
                                    for (IEPTreeNode candidateTrigger : candidates) {
                                        if (triggered.contains(candidateTrigger.getLabel())) {
                                            result.add(cand);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        
                        node = null; // don't investigate parent
                    }
                } else {
                    throw new IllegalStateException("Child not in children list of parent");
                }
            }
                break;
            case Action:
            case Silent:
            case Log:
            case ErrorTrigger:
            case Collapsed:
            case Recurrent:
                throw new IllegalStateException(
                        "Preset semantics didn't expect leaf as parent: " + node.getNodeType());
            default:
                throw new IllegalStateException(
                    "Preset semantics not defined for node type: " + node.getNodeType());
            }
        }
    }
    
    private static void _deriveClosingSet(IEPTreeNode subject, Set<IEPTreeNode> covered, Set<IEPTreeNode> result) {
        Deque<IEPTreeNode> horizon = new ArrayDeque<>();
        horizon.add(subject);
        
        while (!horizon.isEmpty()) {
            IEPTreeNode node = horizon.poll();
            if (!covered.contains(node)) {
                covered.add(node);
                switch (node.getNodeType()) {
                case Action:
                case ErrorTrigger:
                case Collapsed:
                case OrComposite:
                case Recurrent:
                    // add actual labeled node
                    result.add(node);
                    break;
                case Silent:
                case Log:
                    // silent step, so what's the preset of this node?
                    _deriveNodePreset(node, covered, result);
                    break;
                case AndComposite:
                case AndInterleaved:
                case Choice:
                case SeqCancel:
                    // any branch can end here
                    for (IEPTreeNode child : node.getChildren()) {
                        horizon.add(child);
                    }
                    break;
                case Seq:
                    List<IEPTreeNode> children = node.getChildren();
                    horizon.add(children.get(children.size() - 1));
                    break;
                case Loop:
                case LoopCancel:
                    horizon.add(node.getChildren().get(0));
                    break;
                default:
                    throw new IllegalStateException(
                        "Preset semantics not defined for node type: " + node.getNodeType());
                }
            }
        }

        /*
        // in case of error trigger (prefix semantics), lookup and add corresponding catches
        // + include error postset for entire subtree?
        EPTreeErrorTriggerDecorator decorator = subject.getTree().getDecorations()
                .getForType(EPTreeErrorTriggerDecorator.class);
        if (decorator != null) {
            // 1. collect candidate catches
            Set<IEPTreeNode> candidates = new THashSet<IEPTreeNode>();
            Set<IEPTreeNode> coveredCandidates = new THashSet<IEPTreeNode>();
    
            IEPTreeNode parent = subject.getParent();
            while (parent != null) {
                if (parent.getNodeType() == EPNodeType.SeqCancel
                    || parent.getNodeType() == EPNodeType.LoopCancel) {
                    Iterator<IEPTreeNode> it = parent.getChildren().iterator();
                    it.next(); // skip first
                    while (it.hasNext()) {
                        _deriveOpeningSet(it.next(), coveredCandidates, candidates);
                    }
                }
                parent = parent.getParent();
            }
            
            // 2. check which catch correspond with our subjectNode trigger
            for (IEPTreeNode subjectNode : subject.iteratePreOrder()) {
                if (subjectNode.getNodeType() == EPNodeType.ErrorTrigger) {
                    // cancel catch part, investigate triggers
                    Set<String> triggered = decorator.getDecoration(subjectNode);
                    for (IEPTreeNode candidateTrigger : candidates) {
                        if (triggered.contains(candidateTrigger.getLabel())) {
                            result.add(subjectNode);
                        }
                    }
                }
            }
        }
        */
    }

    public static Set<IEPTreeNode> getNodePostset(IEPTreeNode subject) {
        Set<IEPTreeNode> result = new THashSet<>();
        Set<IEPTreeNode> covered = new THashSet<>();
        _deriveNodePostset(subject, covered, result);
        return result;
    }

    public static Set<IEPTreeNode> getNodeOpeningSet(IEPTreeNode subject) {
        Set<IEPTreeNode> result = new THashSet<>();
        Set<IEPTreeNode> covered = new THashSet<>();
        _deriveOpeningSet(subject, covered, result);
        return result;
    }

    private static void _deriveNodePostset(IEPTreeNode subject, Set<IEPTreeNode> covered, Set<IEPTreeNode> result) {
        // we investigate <node>, and came from <subject>
        IEPTreeNode node = subject.getParent();
        IEPTreeNode child = subject;
        
        while(node != null) {
            switch (node.getNodeType()) {
            case OrComposite:
                result.add(node);
                node = null; // don't investigate parent
                break;
            case Choice:
            case AndComposite:
            case AndInterleaved:
            case SeqCancel:
                // investigate parent of <node>, so <node> becomes the new child
                child = node;
                node = node.getParent();
                break;
            case Seq:
            {
                int index = node.getChildren().indexOf(child);
                if (index < node.getChildren().size() - 1) {
                    // get the closing language of the next element in the Seq
                    _deriveOpeningSet(node.getChildren().get(index + 1), covered, result);
                    node = null;
                } else {
                    // investigate parent of <node>, so <node> becomes the new child
                    child = node;
                    node = node.getParent();
                }
                break;
            }
            case Loop:
            case LoopCancel:
            {
                int index = node.getChildren().indexOf(child);
                if (index >= 0) {
                    if (index == 0) {
                        if (node.getNodeType() == EPNodeType.Loop) {
                            // get the closing language from the redo part ...
                            Iterator<IEPTreeNode> it = node.getChildren().iterator();
                            it.next(); // skip first
                            while (it.hasNext()) {
                                _deriveOpeningSet(it.next(), covered, result);
                            }
                        }
                        // ... and investigate parent of <node>, so <node> becomes the new child
                        child = node;
                        node = node.getParent();
                    } else {
                        _deriveOpeningSet(node.getChildren().get(0), covered, result);
                        node = null; // don't investigate parent
                    }
                } else {
                    throw new IllegalStateException("Child not in children list of parent");
                }
                break;
            }
            case Action:
            case Silent:
            case Log:
            case ErrorTrigger:
            case Collapsed:
            case Recurrent:
                throw new IllegalStateException(
                        "Postset semantics didn't expect leaf as parent: " + node.getNodeType());
            default:
                throw new IllegalStateException(
                    "Postset semantics not defined for node type: " + node.getNodeType());
            }
        }

        // in case of error trigger (prefix semantics), lookup and add corresponding catches
        // + include error postset for entire subtree?
        EPTreeErrorTriggerDecorator decorator = subject.getTree().getDecorations()
                .getForType(EPTreeErrorTriggerDecorator.class);
        if (decorator != null) {
            // 1. collect candidate catches
            Set<IEPTreeNode> candidates = new THashSet<IEPTreeNode>();
            Set<IEPTreeNode> coveredCandidates = new THashSet<IEPTreeNode>();
    
            IEPTreeNode parent = subject.getParent();
            while (parent != null) {
                if (parent.getNodeType() == EPNodeType.SeqCancel
                    || parent.getNodeType() == EPNodeType.LoopCancel) {
                    Iterator<IEPTreeNode> it = parent.getChildren().iterator();
                    it.next(); // skip first
                    while (it.hasNext()) {
                        _deriveOpeningSet(it.next(), coveredCandidates, candidates);
                    }
                }
                parent = parent.getParent();
            }
            
            // 2. check which catch correspond with our subjectNode trigger
            for (IEPTreeNode subjectNode : subject.iteratePreOrder()) {
                if (subjectNode.getNodeType() == EPNodeType.ErrorTrigger) {
                    // cancel catch part, investigate triggers
                    Set<String> triggered = decorator.getDecoration(subjectNode);
                    for (IEPTreeNode candidateTrigger : candidates) {
                        if (triggered.contains(candidateTrigger.getLabel())) {
                            result.add(candidateTrigger);
                        }
                    }
                }
            }
        }
    }
    
    private static void _deriveOpeningSet(IEPTreeNode subject, Set<IEPTreeNode> covered, Set<IEPTreeNode> result) {
        Deque<IEPTreeNode> horizon = new ArrayDeque<>();
        horizon.add(subject);
        
        while (!horizon.isEmpty()) {
            IEPTreeNode node = horizon.poll();
            if (!covered.contains(node)) {
                covered.add(node);
                switch (node.getNodeType()) {
                case Action:
                case ErrorTrigger:
                case Collapsed:
                case OrComposite:
                case Recurrent:
                    // add actual labeled node
                    result.add(node);
                    break;
                case Silent:
                case Log:
                    // silent step, so what's the preset of this node?
                    _deriveNodePostset(node, covered, result);
                    break;
                case AndComposite:
                case AndInterleaved:
                case Choice:
                    // any branch can end here
                    for (IEPTreeNode child : node.getChildren()) {
                        horizon.add(child);
                    }
                    break;
                case Seq:
                case Loop:
                case SeqCancel:
                case LoopCancel:
                    horizon.add(node.getChildren().get(0));
                    break;
                default:
                    throw new IllegalStateException(
                        "Postset semantics not defined for node type: " + node.getNodeType());
                }
            }
        }
        
    }

}
