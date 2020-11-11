import random
import uuid
import json
import time

from confluent_kafka import Producer

bootstrap_servers = '172.18.0.2:31147'
topic_name = 'account-events'

producer = Producer({
    'bootstrap.servers': bootstrap_servers,
    'transactional.id': "sample_producer"
})

producer.init_transactions()

for i in range(10):
    accountEventId = uuid.uuid4();
    transactionId = uuid.uuid4();
    accountNumber = str(random.choice(list(range(1000, 1003))))
    amount = random.choice(list(range(1, 100)))
    order = {
      "id": f"{accountEventId}",
      "transactionId": f"{transactionId}",
      "amount": amount,
      "userId": f"{accountNumber}",
      "status": "new",
      "accountNumber": f"{accountNumber}",
      "type": random.choice(["deposit", "withdraw"])
    }

    message = json.dumps(order);
    print(f'Sending account event {message}')

    producer.begin_transaction()
    producer.produce(topic_name, f'{message}'.encode('utf-8'), accountNumber.encode("utf-8"))

    if (amount % 2) == 0:
        print(f'Aborting transaction for account event {accountEventId}')
        producer.abort_transaction()
    else:
        producer.commit_transaction()

    time.sleep(0.5)
