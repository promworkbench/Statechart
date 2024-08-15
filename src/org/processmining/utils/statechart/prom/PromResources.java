package org.processmining.utils.statechart.prom;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.utils.ProvidedObjectHelper;

public class PromResources {

    public static <T> void createObject(PluginContext context, T object,
            Class<? super T> type, String name, boolean favorite, boolean show) {
//        ProvidedObjectHelper.publish(context, name, object, type, false);
//        if (favorite) {
//            ProvidedObjectHelper.setFavorite(context, object);
//        }
        ProvidedObjectHelper.publish(context, name, object, type, false);
        if (favorite) {
            ProvidedObjectHelper.setFavorite(context, object);
        }
        if (show) {
            ProvidedObjectHelper.raise(context, object);
        }
    }
}
