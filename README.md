# kafka-examples
Diverse examples around Kafka

## Setup

### k8s & Strimzi
All the examples use k8s and the [Strimzi](https://strimzi.io/) Kafka operator. For a k8s cluster [kind](https://kind.sigs.k8s.io/) is used.

### Strimzi
Setup of Strimzi basically follows the [quick start guide](https://strimzi.io/quickstarts/) with only a little modification.\
This modification concerns the exposure of the Kafka Broker outside of the cluster mainly for testing purposes.\
To expose the broker an additional listener on a `NodePort` needs to be configured as described [here](https://strimzi.io/docs/operators/master/using.html#proc-accessing-kafka-using-nodeports-str).

Therefor instead of provisioning the `kafka-persistent-single.yaml` manifest directly from the strimzi repository, the files needs to downloaded for adding the additional listener:
```yaml
apiVersion: kafka.strimzi.io/v1beta1
kind: Kafka
metadata:
  name: my-cluster
spec:
  kafka:
    version: 2.5.0
    replicas: 1
    listeners:
      plain: {}
      tls: {}
      external:
        type: nodeport
        tls: false
...
```
Note the new listener of type `external`. Adding the listener results in a new service after applying:
```sh
$ kubectl get services -n kafka
NAME                                  TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)                      AGE
my-cluster-kafka-external-bootstrap   NodePort    10.101.173.116   <none>        9094:31147/TCP               20h
```
Note the service `my-cluster-kafka-external-bootstrap`.\
The port `31147` in this case is no accessible on any node ip. The following command can be used to get these ips:
```sh
kubectl get nodes -o jsonpath={.items[*].status.addresses[].address}
172.18.0.2 172.18.0.3
```

### Creating topics
Topics can be created (and deleted) using strimzi containers which run Kafka scripts inside the cluster. To create a topic the following command can be used:

```sh
kubectl -n kafka run kafka-producer -ti --image=strimzi/kafka:0.20.0-kafka-2.6.0 --rm=true --restart=Never -- bin/kafka-topics.sh --create --topic account-events --bootstrap-server my-cluster-kafka-bootstrap:9092
```

### Kafka Transactions
The folder [transactions](./transactions) contains some examples and experiments with Kafka transactions.\
One of the examples for python is derived from [this example](https://github.com/confluentinc/confluent-kafka-python/blob/master/examples/eos-transactions.py)

### Findings
In order to be able to read `uncommitted` messages as a consumer. The producer needs to call `flush` after sending the message:
```python
producer.begin_transaction()
producer.produce(topic_name, f'{message}'.encode('utf-8'), accountNumber.encode("utf-8"))
producer.flush()
...
producer.abort_transaction()
... or
producer.commit_transaction()
```
An example for transactional producers and consumers can be found [here (python based producer)](transactions/account-service-test/sample_producer.py) and [here (java based committed and uncommitted consumers)](transactions/account-service/src/test/java/com/tom/kafka/examples/account/service/TransactionAwareConsumers.java).\
The python producer writes messages to the topics `account-events` and `order-events` using a transaction. Randomly transactions are aborted.\
Since the consumer for account-events is configured with `read_uncommitted` all messages are received. On the other hand the consumer for order-events -configured with `read_committed`- only receives the committed messages.\
\
Output of proudcer:
```sh
Sending account event {"id": "a967d12a-6fd2-46d8-993b-c470ab7822cd"...
Sending account event {"id": "5e0c2187-f3af-40ab-b84c-39a5ffeff318"...
Aborting transaction for account event 5e0c2187-f3af-40ab-b84c-39a5ffeff318
Sending account event {"id": "fd5b304e-b7ce-4f88-8faf-a1bb02c809de"...
Sending account event {"id": "7645f66f-c595-43f1-acba-81d3c0677e86"...
Aborting transaction for account event 7645f66f-c595-43f1-acba-81d3c0677e86
Sending account event {"id": "e1eeab3f-ffd3-4ab7-96aa-0eb43276527e"...
Sending account event {"id": "aadc54b0-4cab-4d7a-8b8f-d420331346dc"...
Aborting transaction for account event aadc54b0-4cab-4d7a-8b8f-d420331346dc
Sending account event {"id": "993a5e84-1b5e-4503-9712-11222bd421fd"...
Aborting transaction for account event 993a5e84-1b5e-4503-9712-11222bd421fd
Sending account event {"id": "d6d14065-27c4-4d8b-b942-ec98e069b6c6"...
Aborting transaction for account event d6d14065-27c4-4d8b-b942-ec98e069b6c6
Sending account event {"id": "52c16e5c-a54e-492d-bc6f-919cdd44c4f0"...
Aborting transaction for account event 52c16e5c-a54e-492d-bc6f-919cdd44c4f0
Sending account event {"id": "179002c4-28a2-433a-97c1-addf39ea7a05"...
Aborting transaction for account event 179002c4-28a2-433a-97c1-addf39ea7a05
```
Of the 10 messages sent, 3 have been part of a committed transaction while 7 have been part of an aborted transaction.\
This is reflected by the output from the consumers:
```sh
Account-Events: 514: {"id": "a967d12a-6fd2-46d8-993b-c470ab7822cd"...
Order-Events: 0: {"id": "a967d12a-6fd2-46d8-993b-c470ab7822cd"...
Account-Events: 516: {"id": "5e0c2187-f3af-40ab-b84c-39a5ffeff318"...
Account-Events: 518: {"id": "fd5b304e-b7ce-4f88-8faf-a1bb02c809de"...
Order-Events: 4: {"id": "fd5b304e-b7ce-4f88-8faf-a1bb02c809de"...
Account-Events: 520: {"id": "7645f66f-c595-43f1-acba-81d3c0677e86"...
Account-Events: 522: {"id": "e1eeab3f-ffd3-4ab7-96aa-0eb43276527e"...
Order-Events: 8: {"id": "e1eeab3f-ffd3-4ab7-96aa-0eb43276527e"...
Account-Events: 524: {"id": "aadc54b0-4cab-4d7a-8b8f-d420331346dc"...
Account-Events: 526: {"id": "993a5e84-1b5e-4503-9712-11222bd421fd"...
Account-Events: 528: {"id": "d6d14065-27c4-4d8b-b942-ec98e069b6c6"...
Account-Events: 530: {"id": "52c16e5c-a54e-492d-bc6f-919cdd44c4f0"...
Account-Events: 532: {"id": "179002c4-28a2-433a-97c1-addf39ea7a05"...
```
3 messages have been received for order-events reflecting the fact that the consumer will only read committed messages. 10 messages have been received by the consumer for account-events configured to read also uncommitted messages.

## TODOs
* Ask a question in Kafka's mailing list why a call to `flush()` is needed in order for the `read_uncommitted` to work ?
