import random
import uuid
import json
import time

from kafka import KafkaProducer

bootstrap_servers = '172.18.0.2:31147'
topic_name = 'account-events'


producer = KafkaProducer(bootstrap_servers=bootstrap_servers)

for i in range(10):
    accountEventId = uuid.uuid4();
    transactionId = uuid.uuid4();
    accountNumber = str(random.choice(list(range(1000, 1003))))
    order = {
      "id": f"{accountEventId}",
      "transactionId": f"{transactionId}",
      "amount": random.choice(list(range(1, 100))),
      "userId": f"{accountNumber}",
      "status": "new",
      "accountNumber": f"{accountNumber}",
      "type": random.choice(["deposit", "withdraw"])
    }

    message = json.dumps(order);
    print(f'Sending {message}')

    future = producer.send(topic_name, f'{message}'.encode('utf-8'), accountNumber.encode("utf-8"))
    result = future.get(timeout=60)
    time.sleep(0.5)
