package com.tom.kafka.examples.account.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.*;

public class SimpleConsumerReadingUncommitted {

    public static void main(String[] args) throws InterruptedException {
        Properties props = createDefaultProperties();
        KafkaConsumer<String, String> uncommittedConsumer =
                createConsumer(props, "read_uncommitted", "account-events");

        Runnable readUncommittedTask = () -> {
            while (true) {
                ConsumerRecords<String, String> records = uncommittedConsumer.poll(Duration.ofSeconds(100));
                for (ConsumerRecord<String, String> record : records)
                    System.out.println("Account-Events: " + record.offset() + ": " + record.value());
            }
        };

        KafkaConsumer<String, String> committedConsumer =
                createConsumer(props, "read_committed", "order-events");
        Runnable readCommittedTask = () -> {
            while (true) {
                ConsumerRecords<String, String> records = committedConsumer.poll(Duration.ofSeconds(100));
                for (ConsumerRecord<String, String> record : records)
                    System.out.println("Order-Events: " + record.offset() + ": " + record.value());
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(readUncommittedTask);
        executorService.submit(readCommittedTask);
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }

    private static KafkaConsumer<String, String> createConsumer(Properties props, String isolationLevel, String topic) {
        Properties effectiveProps = (Properties) props.clone();
        effectiveProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, isolationLevel);
        effectiveProps.put("group.id", topic + "-consumer-group");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(effectiveProps);
        consumer.subscribe(Arrays.asList(topic));
        return consumer;
    }

    private static Properties createDefaultProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "172.18.0.2:31147");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("enable.auto.commit", false);
        return props;
    }
}
