package org.processmining.utils.statechart.prom;

import java.util.Collection;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.events.ConnectionObjectListener.ListenerList;

public class MockupConnectionManager implements ConnectionManager {

	public <T extends Connection> Collection<T> getConnections(Class<T> connectionType, PluginContext context,
			Object... objects) throws ConnectionCannotBeObtained {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Connection> T getFirstConnection(Class<T> connectionType, PluginContext context,
			Object... objects) throws ConnectionCannotBeObtained {
		// TODO Auto-generated method stub
		return null;
	}

	public Connection getConnection(ConnectionID id) throws ConnectionCannotBeObtained {
		// TODO Auto-generated method stub
		return null;
	}

	public ListenerList getConnectionListeners() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<ConnectionID> getConnectionIDs() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Connection> T addConnection(T connection) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setEnabled(boolean isEnabled) {
		// TODO Auto-generated method stub
		
	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}

}
