package com.tom.kafka.examples.services;

import com.tom.kafka.examples.KafkaUtils;
import com.tom.kafka.examples.model.AccountEvent;
import com.tom.kafka.examples.model.OrderEvent;
import com.tom.kafka.examples.model.StockEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j
public class OrderSplitter extends AbstractKafkaApp {

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected  void addTopology(StreamsBuilder builder) {
        KStream<String, OrderEvent> orderStream = builder.stream("priced-orders", Consumed.with(KafkaUtils.keySerde,
                KafkaUtils.getSerde(OrderEvent.class)));

        KStream<String, Object> mappedStream = orderStream.flatMapValues(orderEvent -> {
            ArrayList<Object> objects = new ArrayList<>(2);
            try {
                AccountEvent accountEvent = createAccountEvent(orderEvent);
                StockEvent stockEvent = createStockEvent(orderEvent);

                objects.add(accountEvent);
                objects.add(stockEvent);
                objects.add(orderEvent);
            }
            catch (Exception e) {
                log.error("An error occurred while trying to create an object", e);
            }
            return objects;
        });

        KStream[] splitStream = mappedStream.branch(
            (key, value) -> value instanceof AccountEvent,
            (key, value) -> value instanceof StockEvent,
            (key, value) -> value instanceof OrderEvent
        );

        splitStream[0].selectKey((key, accountEvent) -> ((AccountEvent)accountEvent).getAccountNumber()).to("account-transactions", Produced.with(KafkaUtils.keySerde, KafkaUtils.getSerde(AccountEvent.class)));
        splitStream[1].selectKey((key, stockEvent) -> ((StockEvent)stockEvent).getStock()).to("stock-transactions", Produced.with(KafkaUtils.keySerde, KafkaUtils.getSerde(StockEvent.class)));
        splitStream[1].selectKey((key, orderEvent) -> ((OrderEvent)orderEvent).getTransactionId()).to("pending-transactions", Produced.with(KafkaUtils.keySerde, KafkaUtils.getSerde(OrderEvent.class)));
    }

    private StockEvent createStockEvent(OrderEvent orderEvent) {
        StockEvent stockEvent = StockEvent.builder().id(createUUID())
                .transactionId(orderEvent.getTransactionId()).amount(orderEvent.getAmount())
                .stock(orderEvent.getStock()).build();
        stockEvent.setType(orderEvent.getType() == OrderEvent.Type.BUY ? StockEvent.Type.REMOVAL : StockEvent.Type.ADDITION);
        return stockEvent;
    }

    private AccountEvent createAccountEvent(OrderEvent orderEvent) {
        AccountEvent accountEvent = AccountEvent.builder().id(createUUID())
                .transactionId(orderEvent.getTransactionId()).accountNumber(orderEvent.getAccountNumber())
                .amount(orderEvent.getAmount()).userId(orderEvent.getUserId()).amount(orderEvent.getPrice()).build();
        accountEvent.setType(orderEvent.getType() == OrderEvent.Type.BUY ? AccountEvent.Type.DEPOSIT : AccountEvent.Type.WITHDRAW);
        return accountEvent;
    }

    private String createUUID() {
        return UUID.randomUUID().toString();
    }

    public static void main(String[] args) {
        OrderSplitter app = new OrderSplitter();
        app.run();
    }
}
