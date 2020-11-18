import random
import uuid
import json
import time

from confluent_kafka import Producer

bootstrap_servers = '172.18.0.2:30195'

producer = Producer({
    'bootstrap.servers': bootstrap_servers,
    'transactional.id': "sample_producer"
})

producer.init_transactions()


def create_account_event():
    account_number = get_account_number()
    amount = random.choice(list(range(1, 100)))
    account_event = {
        "id": f"{uuid.uuid4()}",
        "transactionId": f"{uuid.uuid4()}",
        "amount": amount,
        "userId": f"{account_number}",
        "status": "new",
        "accountNumber": f"{account_number}",
        "type": random.choice(["deposit", "withdraw"])
    }
    return account_event


def create_order_event():
    amount = 20
    account_number = get_account_number()
    account_event = {
        "id": f"{uuid.uuid4()}",
        "transactionId": f"{uuid.uuid4()}",
        "amount": amount,
        "userId": f"{account_number}",
        "status": "new",
        "accountNumber": f"{account_number}",
        "stock": random.choice(["StockA", "StockB"]),
        "type": random.choice(["buy", "sell"])
    }
    return account_event


def create_price_event():
    price = random.choice(list(range(1, 100)))
    price_event = {
        "id": f"{uuid.uuid4()}",
        "newPrice": price,
        "stock": random.choice(["StockA", "StockB"]),
    }
    return price_event


def get_account_number():
    return str(random.choice(list(range(1000, 1003))))


for i in range(1):
    event = create_order_event()

    message = json.dumps(event)
    print(f'Sending event {message}')

    producer.begin_transaction()
    producer.produce('incoming-orders', f'{message}'.encode('utf-8'), event['transactionId'].encode("utf-8"))
    producer.flush()

    producer.commit_transaction()

    time.sleep(0.5)
