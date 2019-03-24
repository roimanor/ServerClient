package bgu.spl171.net.api.bidi;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl171.net.srv.ConnectionHandler;

public class ConnectionsImpl<T> implements Connections<T> {
	private ConcurrentHashMap<Integer, ConnectionHandler<T>> map = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, String> mapByName = new ConcurrentHashMap<>();

	@Override
	public boolean send(int connectionId, T msg) {

		boolean sent = false;
		if (map.get(connectionId) != null)
			sent = true;
		map.get(connectionId).send(msg);
		return sent;
	}

	@Override
	public void broadcast(T msg) {
		for (Integer key : map.keySet()) {
			send(key, msg);
		}
	}

	@Override
	public void disconnect(int connectionId) {
		map.remove(connectionId);
		mapByName.remove(connectionId);
	}

	public void connect(int key, ConnectionHandler<T> cHandler) {
		map.put(key, cHandler);
	}

	public void connectByName(int key, String name) {
		mapByName.put(key, name);
	}

	public boolean isLogged(String name) {
		return mapByName.containsValue(name);
	}

}
