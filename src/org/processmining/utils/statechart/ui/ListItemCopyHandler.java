package org.processmining.utils.statechart.ui;

import javax.swing.JComponent;

/**
 * Drag'n'Drop Transfer Handler for JList with <T> items
 * @author mleemans
 *
 * Transfer action: Copy items
 * Can import items: No
 *
 */
public class ListItemCopyHandler<T> extends ListItemTransferHandler<T> {

    private static final long serialVersionUID = -2832655492232981934L;

    @Override
    public int getSourceActions(JComponent c) {
        return COPY; // TransferHandler.COPY_OR_MOVE;
    }

    @Override
    public boolean canImport(TransferSupport info) {
        return false;
    }
}
