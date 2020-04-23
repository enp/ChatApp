package ru.itx.enp.chat;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/socket/{user}")
public class WebSocketEndpoint {
	private Logger logger = LoggerFactory.getLogger(WebSocketEndpoint.class);
	private static Map<String,Session> connections = Collections.synchronizedMap(new HashMap<>());

	@OnOpen
	public void onOpen(Session session, @PathParam("user") String user) {
		logger.info("Open session: {} from user {}", session.getId(), user);
		connections.put(user, session);
	}

	@OnMessage
	public void onMessage(Session session, @PathParam("user") String from, String message) throws IOException {
		logger.info("Receive message: {} in session {} from user {}", message, session.getId(), from);
		for (String user : connections.keySet()) {
			logger.info("Found session {} in connections", connections.get(user).getId());
			if (!connections.get(user).equals(session)) {
				logger.info("Send message {} to session {}", message, connections.get(user).getId());
				connections.get(user).getBasicRemote().sendText(String.format("%s: %s", from, message));
			}
		}
	}

	@OnClose
	public void onClose(Session session, @PathParam("user") String user) {
		logger.info("Close session: {} from user {}", session.getId(), user);
		connections.remove(user);
	}
}
