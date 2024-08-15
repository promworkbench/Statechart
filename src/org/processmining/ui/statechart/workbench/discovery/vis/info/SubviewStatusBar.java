package org.processmining.ui.statechart.workbench.discovery.vis.info;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.processmining.protocols.statechart.saw.SawServerStatus;
import org.processmining.ui.statechart.workbench.discovery.DiscoveryWorkbenchController;
import org.processmining.ui.statechart.workbench.discovery.ISubview;

public class SubviewStatusBar implements ISubview {

    @SuppressWarnings("unused")
    private DiscoveryWorkbenchController.View baseView;
    
    private JPanel wrap;
    
    private JLabel sawServerStatus;
    private JLabel backgroundStatus;
    private JLabel memoryStatus;
    private JLabel sawActionStatus;
    
    public SubviewStatusBar(DiscoveryWorkbenchController.View baseView) {
        this.baseView = baseView;
        
        wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.X_AXIS));
        _createInfoBar(wrap);
    }

    @Override
    public JComponent getRootComponent() {
        return wrap;
    }
    
    public void setSawServerStatus(SawServerStatus status) {
        sawServerStatus.setText(status.getMsg());
    }
    
    public void setMemoryUsage(long currentBytes, long maxBytes) {
        NumberFormat format = NumberFormat.getInstance();
        StringBuilder bld = new StringBuilder();
        bld.append(format.format(currentBytes / 1024.0 / 1024.0));
        bld.append(" MB / ");
        bld.append(format.format(maxBytes / 1024.0 / 1024.0));
        bld.append(" MB");
        memoryStatus.setText(bld.toString());
    }
    
    public void setStatus(String status) {
        backgroundStatus.setText(status);
    }
    
    public void setSawStatus(String status) {
        sawActionStatus.setText(status);
    }
    
    private void _createInfoBar(JPanel wrap) {

        {
            JPanel entry = _createInfoBarEntry(wrap, 200);
            
            entry.add(new JLabel("Memory: "));
            memoryStatus = new JLabel("<Unkown>");
            entry.add(memoryStatus);
        }
        {
            JPanel entry = _createInfoBarEntry(wrap, 200);
            
            entry.add(new JLabel("SAW Server status: "));
            sawServerStatus = new JLabel("<Unkown>");
            entry.add(sawServerStatus);
        }
        
        {
            JPanel entry = _createInfoBarEntry(wrap, 300);

            backgroundStatus = new JLabel("<Ready>");
            entry.add(backgroundStatus);
        }

        wrap.add(Box.createHorizontalGlue());
        
        {
            JPanel entry = _createInfoBarEntry(wrap, 300);

            sawActionStatus = new JLabel("<Ready>");
            entry.add(sawActionStatus);
        }
        
    }
    
    protected JPanel _createInfoBarEntry(JPanel wrap, int width) {
        JPanel entry = new JPanel();
        entry.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        entry.setLayout(new BoxLayout(entry, BoxLayout.X_AXIS));
        wrap.add(entry);
//        entry.setPreferredSize(new Dimension(width, 20));
        return entry;
    }
}
