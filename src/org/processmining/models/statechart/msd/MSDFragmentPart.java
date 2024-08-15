package org.processmining.models.statechart.msd;


public class MSDFragmentPart extends MSDNode implements IMSDFragmentPart {

    public MSDFragmentPart(IMSDNode parent, String name) {
        super(parent, name);
    }

    public MSDMessage createMessage(String name, MessageType type, 
            IActivation source, IActivation target, String id) {
        MSDMessage message = new MSDMessage(this, name, type, source, target, id);
        addChild(message);
        return message;
    }
    
    public MSDFragment createFragment(String name, FragmentType type) {
        MSDFragment fragment = new MSDFragment(this, name, type);
        addChild(fragment);
        return fragment;
    }
}
