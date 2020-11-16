/**
 * 
 */
package com.tom.kafka.examples.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.io.Serializable;

public class EntitySerializer<T> implements Serializer<T>, Serializable {
	
	private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public byte[] serialize(String topic, T data) {
		try {
			return OBJECT_MAPPER.writeValueAsBytes(data);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
