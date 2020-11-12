package com.tom.kafka.examples.account.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.metrics.stats.Count;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.*;

public class SimpleConsumerReadingUncommitted {

    public static void main(String[] args) throws InterruptedException {
        Properties props = createDefaultProperties();
        KafkaConsumer<String, String> readUncommittedConsumer =
                createConsumer(props, "read_uncommitted", "account-events");

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Runnable runnableTask = () -> {
            while (true) {
                ConsumerRecords<String, String> records = readUncommittedConsumer.poll(Duration.ofSeconds(100));
                for (ConsumerRecord<String, String> record : records)
                    System.out.println(record.offset() + ": " + record.value());
            }
        };

        executorService.submit(runnableTask);
        CountDownLatch latch = new CountDownLatch(2);
        latch.await();
    }

    private static KafkaConsumer<String, String> createConsumer(Properties props, String isolationLevel, String topic) {
        Properties uncommittedProperties = (Properties) props.clone();
        uncommittedProperties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, isolationLevel);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(uncommittedProperties);
        consumer.subscribe(Arrays.asList(topic));
        return consumer;
    }

    private static Properties createDefaultProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "172.18.0.2:31147");
        props.put("group.id", "consumer-tutorial");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("enable.auto.commit", false);
        return props;
    }
}
