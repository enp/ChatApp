package ru.itx.enp.chat;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/socket/{user}")
public class WebSocketEndpoint {
	private static Logger logger = LoggerFactory.getLogger(WebSocketEndpoint.class);
	private static Map<String,Session> connections = Collections.synchronizedMap(new HashMap<>());
	private static Nitrite db = Nitrite.builder().compressed().filePath("chat.db").openOrCreate("chat", "chatpwd");
	private static NitriteCollection collection = db.getCollection("chat");

	@OnOpen
	public void onOpen(Session session, @PathParam("user") String user) throws IOException {
		logger.info("Open session: {} from user {}", session.getId(), user);
		connections.put(user, session);
		int size = (int) collection.size();
		Cursor cursor = size > 3 ? collection.find(FindOptions.limit(size-3, size)) : collection.find();
		for (Document document : cursor) {
			logger.info("Load document: {} in session {} from user {}", document, session.getId(), user);
			session.getBasicRemote().sendText(document.get("text", String.class));
		}
	}

	@OnMessage
	public void onMessage(Session session, @PathParam("user") String from, String message) throws IOException {
		logger.info("Receive message: {} in session {} from user {}", message, session.getId(), from);
		Calendar now = Calendar.getInstance();
		String text = String.format("%tF %tT - %s: %s", now, now, from, message);
		collection.insert(Document.createDocument("text", text));
		for (String user : connections.keySet()) {
			logger.info("Send message {} to session {} from user {}", message, connections.get(user).getId(), user);
			connections.get(user).getBasicRemote().sendText(text);
		}
	}

	@OnClose
	public void onClose(Session session, @PathParam("user") String user) {
		logger.info("Close session: {} from user {}", session.getId(), user);
		connections.remove(user);
	}
}
