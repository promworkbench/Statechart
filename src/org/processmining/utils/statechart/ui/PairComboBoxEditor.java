package org.processmining.utils.statechart.ui;

import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.apache.commons.lang3.tuple.Pair;

public class PairComboBoxEditor extends BasicComboBoxEditor {

    private final boolean stringFromFirst;

    public PairComboBoxEditor() {
        this(true);
    }
    
    public PairComboBoxEditor(boolean stringFromFirst) {
        this.stringFromFirst = stringFromFirst;
    }
    
    @Override
    public void setItem(Object anObject) {
        if (anObject instanceof Pair<?, ?>) {
            if (stringFromFirst) {
                super.setItem(((Pair<?, ?>) anObject).getLeft());
            } else {
                super.setItem(((Pair<?, ?>) anObject).getRight());
            }
            
        } else {
            super.setItem(anObject);
        }
    }
    
}
