package org.processmining.utils.statechart.ui;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class ReadonlyTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 7261607072510410466L;

    /**
     *  Constructs a default <code>DefaultTableModel</code>
     *  which is a table of zero columns and zero rows.
     */
    public ReadonlyTableModel() {
        super();
    }

    /**
     *  Constructs a <code>DefaultTableModel</code> with
     *  <code>rowCount</code> and <code>columnCount</code> of
     *  <code>null</code> object values.
     *
     * @param rowCount           the number of rows the table holds
     * @param columnCount        the number of columns the table holds
     *
     * @see #setValueAt
     */
    public ReadonlyTableModel(int rowCount, int columnCount) {
        super(rowCount, columnCount);
    }

    /**
     *  Constructs a <code>DefaultTableModel</code> with as many columns
     *  as there are elements in <code>columnNames</code>
     *  and <code>rowCount</code> of <code>null</code>
     *  object values.  Each column's name will be taken from
     *  the <code>columnNames</code> vector.
     *
     * @param columnNames       <code>vector</code> containing the names
     *                          of the new columns; if this is
     *                          <code>null</code> then the model has no columns
     * @param rowCount           the number of rows the table holds
     * @see #setDataVector
     * @see #setValueAt
     */
    @SuppressWarnings("rawtypes")
    public ReadonlyTableModel(Vector columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    /**
     *  Constructs a <code>DefaultTableModel</code> with as many
     *  columns as there are elements in <code>columnNames</code>
     *  and <code>rowCount</code> of <code>null</code>
     *  object values.  Each column's name will be taken from
     *  the <code>columnNames</code> array.
     *
     * @param columnNames       <code>array</code> containing the names
     *                          of the new columns; if this is
     *                          <code>null</code> then the model has no columns
     * @param rowCount           the number of rows the table holds
     * @see #setDataVector
     * @see #setValueAt
     */
    public ReadonlyTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    /**
     *  Constructs a <code>DefaultTableModel</code> and initializes the table
     *  by passing <code>data</code> and <code>columnNames</code>
     *  to the <code>setDataVector</code> method.
     *
     * @param data              the data of the table, a <code>Vector</code>
     *                          of <code>Vector</code>s of <code>Object</code>
     *                          values
     * @param columnNames       <code>vector</code> containing the names
     *                          of the new columns
     * @see #getDataVector
     * @see #setDataVector
     */
    @SuppressWarnings("rawtypes")
    public ReadonlyTableModel(Vector data, Vector columnNames) {
        super(data, columnNames);
    }

    /**
     *  Constructs a <code>DefaultTableModel</code> and initializes the table
     *  by passing <code>data</code> and <code>columnNames</code>
     *  to the <code>setDataVector</code>
     *  method. The first index in the <code>Object[][]</code> array is
     *  the row index and the second is the column index.
     *
     * @param data              the data of the table
     * @param columnNames       the names of the columns
     * @see #getDataVector
     * @see #setDataVector
     */
    public ReadonlyTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
