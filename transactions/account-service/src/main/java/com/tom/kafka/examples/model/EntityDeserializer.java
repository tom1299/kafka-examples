package com.tom.kafka.examples.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

public class EntityDeserializer<T> implements Deserializer<T> {
	
	private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private Class objectClass;

	public EntityDeserializer(Class<T> objectClass) {
		this.objectClass = objectClass;
	}

	@Override
	public T deserialize(String topic, byte[] data) {
		try {
			return (T) OBJECT_MAPPER.readValue(data, objectClass);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
