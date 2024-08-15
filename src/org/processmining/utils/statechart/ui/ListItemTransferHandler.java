package org.processmining.utils.statechart.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Drag'n'Drop Transfer Handler for JList with <T> items
 * 
 * @author mleemans
 *
 *         Transfer action: Moving items Can import items: Yes
 *
 *         Based on:
 *         http://stackoverflow.com/questions/16586562/reordering-jlist
 *         -with-drag-and-drop
 * @camickr already suggested above.
 *          http://docs.oracle.com/javase/tutorial/uiswing/dnd/dropmodedemo.html
 *
 * @param <T>
 */
public class ListItemTransferHandler<T> extends TransferHandler {

    private static final long serialVersionUID = -3314805102545633606L;

    private static final Logger logger = LogManager
            .getLogger(ListItemTransferHandler.class.getName());

    private final DataFlavor localObjectFlavor;
    private Object[] transferedObjects = null;

    public ListItemTransferHandler() {
        localObjectFlavor = new ActivationDataFlavor(Object[].class,
                DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        @SuppressWarnings("unchecked")
        JList<T> list = (JList<T>) c;
        indices = list.getSelectedIndices();
        transferedObjects = list.getSelectedValuesList().toArray();
        return new DataHandler(transferedObjects,
                localObjectFlavor.getMimeType());
    }

    @Override
    public boolean canImport(TransferSupport info) {
        if (!info.isDrop() || !info.isDataFlavorSupported(localObjectFlavor)) {
            return false;
        }
        return true;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE; // TransferHandler.COPY_OR_MOVE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(TransferSupport info) {
        if (!canImport(info)) {
            return false;
        }
        JList<T> target = (JList<T>) info.getComponent();
        JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
        DefaultListModel<T> listModel = (DefaultListModel<T>) target.getModel();
        int index = dl.getIndex();
        int max = listModel.getSize();
        if (index < 0 || index > max) {
            index = max;
        }
        addIndex = index;
        try {
            Object[] values = (Object[]) info.getTransferable()
                    .getTransferData(localObjectFlavor);
            addCount = values.length;
            for (int i = 0; i < values.length; i++) {
                int idx = index++;
                listModel.add(idx, (T) values[i]);
                target.addSelectionInterval(idx, idx);
            }
            return true;
        } catch (UnsupportedFlavorException ufe) {
            logger.error("DnD Error - importData ufe", ufe);
        } catch (IOException ioe) {
            logger.error("DnD Error - importData ioe", ioe);
        }
        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        cleanup(c, action == MOVE);
    }

    private void cleanup(JComponent c, boolean remove) {
        if (remove && indices != null) {
            @SuppressWarnings("unchecked")
            JList<T> source = (JList<T>) c;
            DefaultListModel<T> model = (DefaultListModel<T>) source.getModel();
            if (addCount > 0) {
                // http://java-swing-tips.googlecode.com/svn/trunk/DnDReorderList/src/java/example/MainPanel.java
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] >= addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            for (int i = indices.length - 1; i >= 0; i--) {
                model.remove(indices[i]);
            }
        }
        indices = null;
        addCount = 0;
        addIndex = -1;
    }

    private int[] indices = null;
    private int addIndex = -1; // Location where items were added
    private int addCount = 0; // Number of items added.
}