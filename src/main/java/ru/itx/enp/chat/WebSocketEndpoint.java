package ru.itx.enp.chat;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/socket")
public class WebSocketEndpoint {
	private Logger logger = LoggerFactory.getLogger(WebSocketEndpoint.class);
	private static Set<Session> connections = Collections.synchronizedSet(new HashSet<>());

	@OnOpen
	public void onOpen(Session session) {
		logger.info("Open session: {}", session.getId());
		connections.add(session);
	}

	@OnMessage
	public void onMessage(Session session, String message) throws IOException {
		logger.info("Receive message: {} in session {}", message, session.getId());
		for (Session connection : connections) {
			logger.info("Found session {} in connections", connection.getId());
			if (!connection.equals(session)) {
				logger.info("Send message {} to session {}", message, connection.getId());
				connection.getBasicRemote().sendText(message);
			}
		}
	}

	@OnClose
	public void onClose(Session session) {
		logger.info("Close session: {}", session.getId());
		connections.remove(session);
	}
}
