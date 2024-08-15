package org.processmining.ui.statechart.svg;

import java.io.InputStream;

import org.processmining.utils.statechart.svg.ISVGReference;

public enum SvgIcons implements ISVGReference {
    IconClock("icon_clock.svg"),
    IconEnd("icon_end.svg"),
    IconError("icon_error.svg"),
    IconExclamation("icon_exclamation.svg"),
    IconStart("icon_start.svg"),
    IconPlus("icon_plus.svg"),
    IconMinus("icon_minus.svg"),
    IconRecurrent("icon_recurrent.svg"),
    
    Blank("blank.svg"),

    TreeOpTau("treeop_tau.svg"),
    TreeOpSeq("treeop_seq.svg"),
    TreeOpChoice("treeop_choice.svg"),
    TreeOpPar("treeop_par.svg"),
    TreeOpInterleaved("treeop_interleaved.svg"),
    TreeOpLoop("treeop_loop.svg"),
    TreeOpCompOr("treeop_compor.svg"),
    TreeOpRecurOr("treeop_recuror.svg"),
    TreeOpSeqCancel("treeop_seqcancel.svg"),
    TreeOpLoopCancel("treeop_loopcancel.svg"),
    TreeOpErrorTrigger("treeop_errortrigger.svg"),
    TreeOpLog("icon_exclamation.svg");
    
    private final String resource;

    private SvgIcons(String resource) {
        this.resource = resource;
    }
    
    @Override
    public String getName() {
        return SvgIcons.class.getResource(resource).toString();
    }
    
    @Override
    public InputStream getInputStream() {
        return SvgIcons.class.getResourceAsStream(resource);
    }
}