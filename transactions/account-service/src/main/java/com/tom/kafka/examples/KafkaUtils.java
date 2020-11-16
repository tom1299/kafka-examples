/**
 * 
 */
package com.tom.kafka.examples;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class KafkaUtils {
	
	private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	public static Serde<String> keySerde = Serdes.String();

	private KafkaUtils() {
		// Hide visibility
	}

	public static <T> byte[] serialize(String topic, T data) {
		try {
			return OBJECT_MAPPER.writeValueAsBytes(data);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T deserialize(String topic, byte[] data, Class<T> clazz) {
		try {
			return OBJECT_MAPPER.readValue(data, clazz);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Properties addBootstrapServer(Properties props) {
		String brokerHost = System.getenv("BROKER_HOST");
		if (brokerHost == null || brokerHost.isEmpty()) {
			brokerHost = "172.18.0.2";
		}
		String brokerPort = System.getenv("BROKER_PORT");
		if (brokerPort == null || brokerPort.isEmpty()) {
			brokerPort = "30195";
		}
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerHost + ":" + brokerPort);
		return props;
	}

	public static void addShutdownHook(final KafkaStreams streams, final CountDownLatch latch, final String name) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.info("Shutting down {}", name);
			streams.close();
			latch.countDown();
		}, "streams-shutdown-hook"));
	}

	public static <T> Serde<T> getSerde(Class<T> clazz) {
		return Serdes.serdeFrom((topic, type) -> KafkaUtils.serialize(topic, type),
				(topic, data) -> KafkaUtils.deserialize(topic, data, clazz));
	}
}
