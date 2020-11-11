package com.tom.kafka.examples.account.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Arrays;
import java.util.Properties;

public class SimpleConsumerReadingUncommitted {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "172.18.0.2:31147");
        props.put("group.id", "consumer-tutorial");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("enable.auto.commit", false);
//        props.put("enable.partition.eof", true);
//        props.put("auto.offset.reset", "earliest");
        props.put("isolation.level", "read_uncommitted");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("account-events"));

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);
                for (ConsumerRecord<String, String> record : records)
                    System.out.println(record.offset() + ": " + record.value());
            }
        } finally {
            consumer.close();
        }
    }
}
