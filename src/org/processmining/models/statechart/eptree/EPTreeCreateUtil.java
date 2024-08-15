package org.processmining.models.statechart.eptree;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.lang3.StringUtils;

public class EPTreeCreateUtil {

    /**
     * From STring to tree instance
     * 
     * Example input:
     * <code>
     *      ->(A, \\/=B(->(X, \\/=B(Y))), C)\
     *      ->(A, \\/=B(->(x(tau, ->(X, R\\/=B)), x(tau, Y))), C)
     * </code>
     * 
     * @param input
     * @return
     */
    public static IEPTree create(String input) {
        Deque<IEPTreeNode> stack = new ArrayDeque<>();
        IEPTreeNode parent = null;
        IEPTreeNode root = null;
        
        IEPTree tree = new EPTree("");

        StringBuilder buffer = new StringBuilder();
        int i = 0;
        boolean inString = false;

        while (i < input.length()) {
            char c = input.charAt(i);
            i++;

            if (!inString) {
                if (c == '(') {
                    IEPTreeNode node = createNode(tree, parent, buffer.toString());
                    buffer.setLength(0);
                    if (parent != null) {
                        parent.addNode(node);
                    }
                    if (root == null) {
                        root = node;
                    }

                    stack.push(node);
                    parent = node;
                } else if (c == ')') {
                    // check needed: "))"  doesn't introduce an 'empty action' 
                    if (buffer.length() > 0) {
                        IEPTreeNode node = createNode(tree, parent, buffer.toString());
                        if (node != null && parent != null) {
                            parent.addNode(node);
                        }
                    }

                    buffer.setLength(0);
                    stack.pop();
                    parent = stack.peek();
                } else if (c == ',') {
                    // check needed: "),"  doesn't introduce an 'empty action' 
                    if (buffer.length() > 0) {
                        IEPTreeNode node = createNode(tree, parent, buffer.toString());
                        if (node != null && parent != null) {
                            parent.addNode(node);
                        }
                    }
                    buffer.setLength(0);
                } else {
                    buffer.append(c);
                    if (c == '\'' || c == '"') {
                        inString = true;
                    }
                }
            } else {
                buffer.append(c);
                if (c == '\'' || c == '"') {
                    inString = false;
                }
            }
        }

        if (buffer.length() > 0) {
            IEPTreeNode node = createNode(tree, parent, buffer.toString());
            buffer.setLength(0);
            if (node != null) {
                if (parent != null) {
                    parent.addNode(node);
                }
                if (root == null) {
                    root = node;
                }
            }
        }

        tree.setRoot(root);
        return tree;
    }

    private static IEPTreeNode createNode(IEPTree tree, IEPTreeNode parent, String buffer) {
        String symbol = buffer.trim();
        String name = "";

        EPNodeType type = null;
        if ((symbol.startsWith("\"") || symbol.startsWith("'"))
                && (symbol.endsWith("\"") || symbol.endsWith("'"))) {
            type = EPNodeType.Action;
            name = StringUtils.strip(symbol, "\"'");
        } else if (symbol.toLowerCase().equals("tau")) {
            type = EPNodeType.Silent;
            name = "tau";
        } else {
            if (symbol.contains("=")) {
                String[] parts = symbol.split("[=]", 2);
                symbol = parts[0];
                name = parts[1];
                
                if ((name.startsWith("\"") || name.startsWith("'"))
                        && (name.endsWith("\"") || name.endsWith("'"))) {
                    name = StringUtils.strip(name, "\"'");
                }
            }
            for (EPNodeType can : EPNodeType.values()) {
                if (symbol.equals(can.getSymbol()) || symbol.equals(can.name())) {
                    type = can;
                }
            }
        }
        
        if (type == null) {
            type = EPNodeType.Action;
            name = symbol;
        }

        return new EPTreeNode(tree, parent, type, name);
    }
}
