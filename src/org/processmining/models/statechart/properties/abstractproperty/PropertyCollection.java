package org.processmining.models.statechart.properties.abstractproperty;

import java.util.Collection;

import org.processmining.plugins.properties.processmodel.abstractproperty.HighLevelProperty;

@SuppressWarnings("serial")
public abstract class PropertyCollection<K extends Collection<?>> 
    extends HighLevelProperty<K> {

    @SuppressWarnings("unchecked")
    @Override
    public K clone(Object element) {
        return createInstance((K) element);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String marshall(Object value) {
        return "<valueEntry value=\"" + toString((K) value) + "\"/>\n";
    }

    @Override
    public Object unmarshall(String xml) {
        String content = xml.split("<valueEntry value=\"")[1].split("\"")[0];
        return createInstance(content.split(getStringSeperationSymbol()));
    }
    protected String toString(K value) {
        StringBuilder builder = new StringBuilder();
        String sep = "";
        for (Object entry : value) {
            builder.append(sep);
            builder.append(entry.toString());
            sep = getStringSeperationSymbol();
        }
        return builder.toString();
    }

    protected String getStringSeperationSymbol() {
        return ",";
    }

    protected abstract K createInstance(K element);
    
    protected abstract K createInstance(String[] split);

}
