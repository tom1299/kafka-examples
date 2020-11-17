package com.tom.kafka.examples.services;

import com.tom.kafka.examples.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Slf4j
public abstract class AbstractKafkaApp {

    private final CountDownLatch latch = new CountDownLatch(1);

    private final StreamsBuilder builder;

    public AbstractKafkaApp() {
        super();
        builder = new StreamsBuilder();
    }

    public void run() {
        Properties props = new Properties();
        KafkaUtils.addBootstrapServer(props);

        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        props.put("enable.auto.commit", "false");
        props.put("isolation.level", "read_committed");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, this.getClass().getSimpleName());

        addTopology(this.builder);
        final Topology topology = builder.build();
        log.info(topology.describe().toString());

        final KafkaStreams streams = new KafkaStreams(topology, props);

        KafkaUtils.addShutdownHook(streams, latch, this.getClass().getSimpleName());

        start(streams);
    }

    protected abstract void addTopology(StreamsBuilder builder);

    private void start(KafkaStreams streams) {
        try {
            streams.start();
            latch.await();
        }
        catch (Exception e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
