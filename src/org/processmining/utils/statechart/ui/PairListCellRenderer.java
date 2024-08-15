package org.processmining.utils.statechart.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.tuple.Pair;

public class PairListCellRenderer<L, R> extends JPanel implements ListCellRenderer<Pair<L, R>> {
    private static final long serialVersionUID = -1022514192224573916L;
    
    private final JLabel leftLabel = new JLabel();
    private final JLabel rightLabel;

    public PairListCellRenderer(JComboBox<Pair<Pattern, String>> comboBox, int rightWidth) {
        super(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        leftLabel.setOpaque(false);
        leftLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        leftLabel.setFont(comboBox.getFont());
        
        final Dimension dim = new Dimension(rightWidth, 0);
        rightLabel = new JLabel() {
            private static final long serialVersionUID = 1182356752057981606L;

            @Override
            public Dimension getPreferredSize() {
                return dim;
            }
        };
        rightLabel.setOpaque(false);
        rightLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        rightLabel.setForeground(Color.GRAY);
        rightLabel.setHorizontalAlignment(SwingConstants.LEFT);

        this.add(leftLabel);
        this.add(rightLabel, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends Pair<L, R>> list, Pair<L, R> value, int index,
            boolean isSelected, boolean cellHasFocus) {
        leftLabel.setText(value.getLeft().toString());
        rightLabel.setText(value.getRight().toString());

        leftLabel.setFont(list.getFont());
        rightLabel.setFont(list.getFont());

        if (index < 0) {
            leftLabel.setForeground(list.getForeground());
            this.setOpaque(false);
        } else {
            leftLabel.setForeground(isSelected ? list.getSelectionForeground()
                    : list.getForeground());
            this.setBackground(isSelected ? list.getSelectionBackground()
                    : list.getBackground());
            this.setOpaque(true);
        }
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(0, d.height);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        this.setName("List.cellRenderer");
    }

}
