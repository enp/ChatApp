package ru.itx.enp.chat;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageConverter implements Encoder.TextStream<Object>, Decoder.TextStream<Object> {

	@Override
	public void init(EndpointConfig config) {}

	@Override
	public void destroy() {}

	@Override
	public void encode(Object object, Writer writer) throws EncodeException, IOException {
		new ObjectMapper().writeValue(writer, object);
	}

	@Override
	public Object decode(Reader reader) throws DecodeException, IOException {
		return new ObjectMapper().readValue(reader, new TypeReference<Object>(){});
	}

}
