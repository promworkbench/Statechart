package org.processmining.models.statechart.msd;

import com.google.common.base.Preconditions;

public class MSDFragment extends MSDNode implements IMSDFragment {

    private final FragmentType type;

    public MSDFragment(IMSDNode parent, String name, FragmentType type) {
        super(parent, name);
        Preconditions.checkNotNull(type);
        this.type = type;
    }

    @Override
    public FragmentType getFragmentType() {
        return type;
    }

    public MSDFragmentPart createPart(String name) {
        MSDFragmentPart part = new MSDFragmentPart(this, name);
        addChild(part);
        return part;
    }
}
