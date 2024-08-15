package org.processmining.utils.statechart.ui;

import javax.annotation.Nullable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.utils.statechart.signals.Action1;

import com.google.common.base.Predicate;

public class SearchFilterAdapter {
    
    public static void installChangeFilter(final JTextField searchTextbox,
        final Action1<Predicate<String>> updateFilter) {
        SwingUtils.addChangeListener(searchTextbox, new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final String text = searchTextbox.getText();
                final String[] patterns = text.split("\\s+");

                Predicate<String> filter = null;
                if (patterns.length > 0 && patterns[0].length() > 0) {
                    filter = new Predicate<String>() {
                        @Override
                        public boolean apply(@Nullable String input) {
                            for (int i = 0; i < patterns.length; i++) {
                                if (input.contains(patterns[i])) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    };
                }

                updateFilter.call(filter);
            }
        });
    }
}
