package ru.itx.enp.chat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.EncodeException;
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

@ServerEndpoint(value = "/socket/{user}", encoders = { MessageConverter.class }, decoders = { MessageConverter.class })
public class WebSocketEndpoint {
	private static Logger logger = LoggerFactory.getLogger(WebSocketEndpoint.class);
	private static Map<String,Session> connections = Collections.synchronizedMap(new HashMap<>());
	private static Nitrite db = Nitrite.builder().compressed().filePath("chat.db").openOrCreate("chat", "chatpwd");
	private static NitriteCollection collection = db.getCollection("chat");

	@OnOpen
	public void onOpen(Session session, @PathParam("user") String user) throws IOException {
		logger.info("Open session for user {}", user);
		connections.put(user, session);
		int size = (int) collection.size();
		Cursor cursor = size > 3 ? collection.find(FindOptions.limit(size-3, size)) : collection.find();
		for (Document message : cursor) {
			logger.info("Load message {} for user {}", message, user);
			session.getAsyncRemote().sendObject(message);
		}
		broadcast(connections.keySet());
	}

	@OnMessage
	public void onMessage(Session session, @PathParam("user") String user, String text) throws IOException, EncodeException {
		logger.info("Receive message {} from user {}", text, user);
		Document message = Document.createDocument("user", user)
			.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
			.put("text", text);
		collection.insert(message);
		broadcast(message);
	}

	private void broadcast(Object message) {
		for (String destination : connections.keySet()) {
			logger.info("Send message {} to user {}", message, destination);
			connections.get(destination).getAsyncRemote().sendObject(message);
		}
	}

	@OnClose
	public void onClose(Session session, @PathParam("user") String user) {
		logger.info("Close session for user {}", user);
		connections.remove(user);
		broadcast(connections.keySet());
	}
}
