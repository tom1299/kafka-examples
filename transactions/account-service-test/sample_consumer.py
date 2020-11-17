from confluent_kafka import Consumer

bootstrap_servers = '172.18.0.2:30195'

c = Consumer({
    'bootstrap.servers': bootstrap_servers,
    'group.id': 'account-events-consumers',
    'auto.offset.reset': 'earliest'
})

c.subscribe(['processed-account-transactions'])

while True:
    msg = c.poll(1.0)

    if msg is None:
        continue
    if msg.error():
        print("Consumer error: {}".format(msg.error()))
        continue

    print(f"Received message: {msg.value().decode('utf-8')}, from topic {msg.topic()}")

c.close()