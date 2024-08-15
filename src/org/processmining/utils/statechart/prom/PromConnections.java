package org.processmining.utils.statechart.prom;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;

public class PromConnections {

    public static <T extends Connection> T getConnectedOrNull(PluginContext context,
            Class<T> connectionType, Object... objects) {
        try {
            // TODO use safeGetFirstConnection() Widgets / Felix?
            return context.getConnectionManager().getFirstConnection(
                    connectionType, context, objects);
        } catch (ConnectionCannotBeObtained exc) {
            return null;
        }
    }
}
