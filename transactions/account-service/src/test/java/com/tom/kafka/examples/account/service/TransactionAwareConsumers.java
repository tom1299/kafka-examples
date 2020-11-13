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

public class TransactionAwareConsumers {

    public static void main(String[] args) throws InterruptedException {
        Properties props = createDefaultProperties();

        final KafkaConsumer<String, String> uncommittedConsumer =
                createConsumer(props, "read_uncommitted", "account-events");
        Runnable readUncommittedTask = () -> {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = uncommittedConsumer.poll(Duration.ofSeconds(100));
                    for (ConsumerRecord<String, String> record : records)
                        System.out.println("Account-Events: " + record.offset() + ": " + record.value());
                }
            }
            catch (Exception e) {
                uncommittedConsumer.close();
            }
        };

        final KafkaConsumer<String, String> committedConsumer =
                createConsumer(props, "read_committed", "order-events");
        Runnable readCommittedTask = () -> {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = committedConsumer.poll(Duration.ofSeconds(100));
                    for (ConsumerRecord<String, String> record : records)
                        System.out.println("Order-Events: " + record.offset() + ": " + record.value());
                }
            }
            catch (Exception e) {
                committedConsumer.close();
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future readUncommittedFuture = executorService.submit(readUncommittedTask);
        Future readCommittedFuture =  executorService.submit(readCommittedTask);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("Shutting down consumers");
            readUncommittedFuture.cancel(true);
            readCommittedFuture.cancel(true);
            latch.countDown();
        }));

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
        props.put("bootstrap.servers", "172.18.0.2:30195");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("enable.auto.commit", false);
        return props;
    }
}
