/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.tom.kafka.examples.services;

import com.tom.kafka.examples.KafkaUtils;
import com.tom.kafka.examples.model.AccountEvent;
import com.tom.kafka.examples.model.OrderEvent;
import com.tom.kafka.examples.model.StockEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.functors.FalsePredicate;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;

@Slf4j
public class OrderFinalizer extends AbstractKafkaApp {


    protected void addTopology(StreamsBuilder builder) {

        KStream<String, StockEvent> stockStream = builder.stream("processed-stock-transactions", Consumed.with(KafkaUtils.keySerde,
                KafkaUtils.getSerde(StockEvent.class)));

        KStream<String, AccountEvent> accountStream = builder.stream("processed-account-transactions", Consumed.with(KafkaUtils.keySerde,
                KafkaUtils.getSerde(AccountEvent.class)));

        KStream<String, OrderEvent> pendingOrders = builder.stream("pending-transactions", Consumed.with(KafkaUtils.keySerde,
                KafkaUtils.getSerde(OrderEvent.class)));

        KStream<String, OrderEvent> accountAndStockJoined = pendingOrders.join(accountStream,
                (orderEvent, accountEvent) -> {
                    log.info("Checking transaction {} with account status", orderEvent.getTransactionId());
                    Boolean accountStatus = accountEvent.getStatus() == AccountEvent.Status.FULFILLED;
                    log.info("Account status for transaction {} is {}",
                            accountEvent.getTransactionId(), accountStatus);
                    if (!accountStatus) {
                        orderEvent.setStatus(OrderEvent.Status.REJECTED);
                    }
                    return orderEvent;
                },
                JoinWindows.of(Duration.ofMinutes(5)),
                StreamJoined.with(
                        Serdes.String(), /* key */
                        KafkaUtils.getSerde(OrderEvent.class),   /* left value */
                        KafkaUtils.getSerde(AccountEvent.class))  /* right value */
        );

        KStream<String, OrderEvent> finaLOrder = accountAndStockJoined.join(stockStream,
                (orderEvent, stockEvent) -> {
                    log.info("Finalizing transaction {} with stock status", orderEvent.getTransactionId());
                    if (orderEvent.getStatus() == OrderEvent.Status.REJECTED) {
                        log.info("Order for transaction {} already rejected", orderEvent.getTransactionId());
                        return orderEvent;
                    }
                    Boolean stockStatus = stockEvent.getStatus() == StockEvent.Status.FULFILLED;
                    log.info("Stock status for transaction {} is {}",
                            stockEvent.getTransactionId(), stockStatus);
                    if (!stockStatus) {
                        orderEvent.setStatus(OrderEvent.Status.REJECTED);
                    }
                    log.info("Final status of transaction {} is {}", orderEvent.getTransactionId(), orderEvent.getStatus());
                    return orderEvent;
                },
                JoinWindows.of(Duration.ofMinutes(5)),
                StreamJoined.with(
                        Serdes.String(), /* key */
                        KafkaUtils.getSerde(OrderEvent.class),   /* left value */
                        KafkaUtils.getSerde(StockEvent.class))  /* right value */
        );

        finaLOrder.selectKey((key, orderEvent) -> orderEvent.getTransactionId()).to("finished-transactions", Produced.with(KafkaUtils.keySerde, KafkaUtils.getSerde(OrderEvent.class)));
    }

    public static void main(String[] args) {
        OrderFinalizer app = new OrderFinalizer();
        app.run();
    }
}
